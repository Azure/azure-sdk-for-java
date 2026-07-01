// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.implementation.util;

import com.azure.storage.blob.implementation.models.BlobListArrowParseException;
import com.azure.storage.blob.implementation.util.arrow.Buffer;
import com.azure.storage.blob.implementation.util.arrow.Endianness;
import com.azure.storage.blob.implementation.util.arrow.Field;
import com.azure.storage.blob.implementation.util.arrow.FieldNode;
import com.azure.storage.blob.implementation.util.arrow.Int;
import com.azure.storage.blob.implementation.util.arrow.KeyValue;
import com.azure.storage.blob.implementation.util.arrow.Message;
import com.azure.storage.blob.implementation.util.arrow.MessageHeader;
import com.azure.storage.blob.implementation.util.arrow.RecordBatch;
import com.azure.storage.blob.implementation.util.arrow.Schema;
import com.azure.storage.blob.implementation.util.arrow.TimeUnit;
import com.azure.storage.blob.implementation.util.arrow.Timestamp;
import com.azure.storage.blob.implementation.util.arrow.Type;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Minimal Apache Arrow IPC stream reader scoped to the needs of the ListBlobs Arrow response.
 * <p>
 * This reader intentionally supports only the subset of the Arrow IPC format emitted by the Storage ListBlobs
 * endpoint: a single schema message followed by zero or more uncompressed, little-endian record batches whose
 * columns are UTF-8 strings, booleans, integers, second-precision timestamps, and map&lt;string,string&gt;. Anything
 * outside that subset (dictionaries, compression, big-endian, unsupported types) fails fast with
 * {@link BlobListArrowParseException}.
 * <p>
 * It depends only on the {@code arrow-format} flatbuffer definitions for metadata decoding and reads record batch
 * bodies directly, so it does not require the {@code arrow-vector} runtime.
 */
final class BlobListArrowStreamReader {
    // This is hex representation of the maximum value of an unsigned 32-bit integer.
    private static final int CONTINUATION_MARKER = 0xFFFFFFFF;

    private BlobListArrowStreamReader() {
    }

    /**
     * The decoded contents of an Arrow IPC stream.
     */
    static final class DecodedArrowStream {
        private final Map<String, String> schemaMetadata;
        private final List<Batch> batches;

        DecodedArrowStream(Map<String, String> schemaMetadata, List<Batch> batches) {
            this.schemaMetadata = schemaMetadata;
            this.batches = batches;
        }

        Map<String, String> getSchemaMetadata() {
            return schemaMetadata;
        }

        List<Batch> getBatches() {
            return batches;
        }
    }

    /**
     * A single decoded record batch: a row count and columns addressable by field name.
     */
    static final class Batch {
        private final int rowCount;
        private final Map<String, Column> columns;

        Batch(int rowCount, Map<String, Column> columns) {
            this.rowCount = rowCount;
            this.columns = columns;
        }

        int getRowCount() {
            return rowCount;
        }

        Column getColumn(String name) {
            return columns.get(name);
        }

        Set<String> getColumnNames() {
            return Collections.unmodifiableSet(columns.keySet());
        }
    }

    /**
     * Reads and decodes an Arrow IPC stream.
     *
     * @param stream the Arrow IPC stream.
     * @return the decoded schema metadata and record batches.
     * @throws BlobListArrowParseException if the stream is malformed or uses an unsupported feature.
     */
    static DecodedArrowStream read(InputStream stream) {
        byte[] bytes;
        try {
            bytes = readAll(stream);
        } catch (IOException e) {
            throw new BlobListArrowParseException("ListBlobs Arrow parse failure: unable to read IPC stream.", e);
        }

        ByteBuffer steamBuffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);

        Map<String, String> schemaMetadata = null;
        List<ArrowField> fields = null;
        List<Batch> batches = new ArrayList<>();

        int pos = 0;
        int length = bytes.length;
        while (atLeastFourBytesRemaining(pos, length)) {
            int marker = steamBuffer.getInt(pos);
            pos += 4;

            int metadataLength;
            if (isModernStream(marker)) {
                if (pos + 4 > length) {
                    break;
                }
                metadataLength = steamBuffer.getInt(pos);
                pos += 4;
            } else {
                // Pre-0.15 streams used a bare length prefix without the continuation marker.
                metadataLength = marker;
            }

            if (metadataLength == 0) {
                // End-of-stream marker.
                break;
            }
            if (isMessageOutOfBounds(metadataLength, pos, length)) {
                throw new BlobListArrowParseException(
                    "ListBlobs Arrow parse failure: message metadata length is out of bounds.");
            }

            ByteBuffer messageBuffer
                = ByteBuffer.wrap(bytes, pos, metadataLength).slice().order(ByteOrder.LITTLE_ENDIAN);
            Message message = Message.getRootAsMessage(messageBuffer);
            pos += metadataLength;

            long bodyLength = message.bodyLength();
            if (isMessageOutOfBounds(bodyLength, pos, length)) {
                throw new BlobListArrowParseException(
                    "ListBlobs Arrow parse failure: message body length is out of bounds.");
            }
            int bodyStart = pos;
            pos += (int) bodyLength;

            byte headerType = message.headerType();
            if (headerType == MessageHeader.SCHEMA) {
                Schema schema = requireHeader((Schema) message.header(new Schema()), "schema");
                if (schema.endianness() != Endianness.LITTLE) {
                    throw new BlobListArrowParseException(
                        "ListBlobs Arrow parse failure: only little-endian streams are supported.");
                }
                schemaMetadata = readKeyValueMetadata(schema);
                fields = readFields(schema);
            } else if (headerType == MessageHeader.RECORD_BATCH) {
                if (fields == null) {
                    throw new BlobListArrowParseException(
                        "ListBlobs Arrow parse failure: record batch encountered before schema.");
                }
                RecordBatch recordBatch
                    = requireHeader((RecordBatch) message.header(new RecordBatch()), "record batch");
                if (recordBatch.compression() != null) {
                    throw new BlobListArrowParseException(
                        "ListBlobs Arrow parse failure: compressed record batches are not supported.");
                }
                batches.add(buildBatch(fields, recordBatch, steamBuffer, bodyStart));
            } else if (headerType == MessageHeader.DICTIONARY_BATCH) {
                throw new BlobListArrowParseException(
                    "ListBlobs Arrow parse failure: dictionary-encoded streams are not supported.");
            }
            // Other header types (NONE, and the commented-out Tensor/SparseTensor members of the Arrow MessageHeader
            // union) are not expected in a ListBlobs response and are ignored. See MessageHeader for why those two
            // constants are kept (commented out) and how to revive them if the service ever starts emitting them.
        }

        if (fields == null) {
            throw new BlobListArrowParseException("ListBlobs Arrow parse failure: stream contained no schema.");
        }

        Map<String, String> finalSchemaMetadata = schemaMetadata == null ? new HashMap<>() : schemaMetadata;
        return new DecodedArrowStream(finalSchemaMetadata, batches);
    }

    private static boolean isMessageOutOfBounds(long bodyLength, int pos, int length) {
        return bodyLength < 0 || pos + bodyLength > length;
    }

    private static boolean isModernStream(int marker) {
        return marker == CONTINUATION_MARKER;
    }

    private static boolean atLeastFourBytesRemaining(int pos, int length) {
        return pos + 4 <= length;
    }

    private static <T> T requireHeader(T header, String description) {
        if (header == null) {
            throw new BlobListArrowParseException(
                "ListBlobs Arrow parse failure: " + description + " message header is missing.");
        }
        return header;
    }

    private static Batch buildBatch(List<ArrowField> fields, RecordBatch recordBatch, ByteBuffer body, int bodyStart) {
        BatchCursor cursor = new BatchCursor(recordBatch, body, bodyStart);
        Map<String, Column> columns = new LinkedHashMap<>();
        for (ArrowField field : fields) {
            columns.put(field.name, buildColumn(field, cursor));
        }
        return new Batch((int) recordBatch.length(), columns);
    }

    private static Column buildColumn(ArrowField field, BatchCursor cursor) {
        FieldNode node = cursor.nextNode();
        int valueCount = (int) node.length();

        switch (field.typeType) {
            case Type.UTF8:
            case Type.BINARY: {
                BufferRegion validity = cursor.nextBuffer();
                BufferRegion offsets = cursor.nextBuffer();
                BufferRegion data = cursor.nextBuffer();
                return new StringColumn(valueCount, validity, offsets, data, cursor.body, cursor.bodyStart);
            }

            case Type.BOOL: {
                BufferRegion validity = cursor.nextBuffer();
                BufferRegion data = cursor.nextBuffer();
                return new BoolColumn(valueCount, validity, data, cursor.body, cursor.bodyStart);
            }

            case Type.INT: {
                BufferRegion validity = cursor.nextBuffer();
                BufferRegion data = cursor.nextBuffer();
                return new IntColumn(valueCount, validity, data, field.bitWidth, field.signed, cursor.body,
                    cursor.bodyStart);
            }

            case Type.TIMESTAMP: {
                BufferRegion validity = cursor.nextBuffer();
                BufferRegion data = cursor.nextBuffer();
                return new TimestampColumn(valueCount, validity, data, cursor.body, cursor.bodyStart);
            }

            case Type.MAP: {
                BufferRegion validity = cursor.nextBuffer();
                BufferRegion offsets = cursor.nextBuffer();
                // Map has a single Struct child ("entries") with key and value children.
                ArrowField entries = field.children.get(0);
                StructColumn struct = (StructColumn) buildColumn(entries, cursor);
                Column keyColumn = struct.children.get(0);
                Column valueColumn = struct.children.get(1);
                if (!(keyColumn instanceof StringColumn) || !(valueColumn instanceof StringColumn)) {
                    throw new BlobListArrowParseException("ListBlobs Arrow parse failure: field '" + field.name
                        + "' map entries must be string keys and values.");
                }
                return new MapColumn(valueCount, validity, offsets, (StringColumn) keyColumn,
                    (StringColumn) valueColumn, cursor.body, cursor.bodyStart);
            }

            case Type.STRUCT: {
                cursor.nextBuffer(); // struct validity buffer
                List<Column> children = new ArrayList<>(field.children.size());
                for (ArrowField child : field.children) {
                    children.add(buildColumn(child, cursor));
                }
                return new StructColumn(children);
            }

            default:
                throw new BlobListArrowParseException("ListBlobs Arrow parse failure: field '" + field.name
                    + "' has unsupported Arrow type '" + Type.name(field.typeType) + "'.");
        }
    }

    private static List<ArrowField> readFields(Schema schema) {
        int count = schema.fieldsLength();
        List<ArrowField> fields = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            fields.add(readField(schema.fields(i)));
        }
        return fields;
    }

    private static ArrowField readField(Field field) {
        ArrowField arrowField = new ArrowField();
        arrowField.name = field.name();
        arrowField.typeType = field.typeType();

        if (arrowField.typeType == Type.INT) {
            Int intType = (Int) field.type(new Int());
            if (intType != null) {
                arrowField.bitWidth = intType.bitWidth();
                arrowField.signed = intType.isSigned();
            }
        } else if (arrowField.typeType == Type.TIMESTAMP) {
            Timestamp timestamp = (Timestamp) field.type(new Timestamp());
            if (timestamp != null && timestamp.unit() != TimeUnit.SECOND) {
                throw new BlobListArrowParseException("ListBlobs Arrow parse failure: field '" + arrowField.name
                    + "' uses an unsupported timestamp unit '" + TimeUnit.name(timestamp.unit()) + "'.");
            }
        }

        int childCount = field.childrenLength();
        arrowField.children = new ArrayList<>(childCount);
        for (int i = 0; i < childCount; i++) {
            arrowField.children.add(readField(field.children(i)));
        }
        return arrowField;
    }

    private static Map<String, String> readKeyValueMetadata(Schema schema) {
        int count = schema.customMetadataLength();
        if (count == 0) {
            return new HashMap<>();
        }
        Map<String, String> metadata = new HashMap<>();
        for (int i = 0; i < count; i++) {
            KeyValue keyValue = schema.customMetadata(i);
            metadata.put(keyValue.key(), keyValue.value());
        }
        return metadata;
    }

    private static byte[] readAll(InputStream stream) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] chunk = new byte[8192];
        int read;
        while ((read = stream.read(chunk)) != -1) {
            buffer.write(chunk, 0, read);
        }
        return buffer.toByteArray();
    }

    /**
     * A parsed schema field with the minimal type information required for decoding.
     */
    private static final class ArrowField {
        private String name;
        private byte typeType;
        private int bitWidth;
        private boolean signed;
        private List<ArrowField> children = new ArrayList<>();
    }

    /**
     * Sequentially hands out the field nodes and buffers of a record batch in pre-order.
     */
    private static final class BatchCursor {
        private final RecordBatch recordBatch;
        private final ByteBuffer body;
        private final int bodyStart;
        private int nodeIndex;
        private int bufferIndex;

        BatchCursor(RecordBatch recordBatch, ByteBuffer body, int bodyStart) {
            this.recordBatch = recordBatch;
            this.body = body;
            this.bodyStart = bodyStart;
        }

        FieldNode nextNode() {
            if (nodeIndex >= recordBatch.nodesLength()) {
                throw new BlobListArrowParseException(
                    "ListBlobs Arrow parse failure: record batch is missing expected field nodes.");
            }
            return recordBatch.nodes(nodeIndex++);
        }

        BufferRegion nextBuffer() {
            if (bufferIndex >= recordBatch.buffersLength()) {
                throw new BlobListArrowParseException(
                    "ListBlobs Arrow parse failure: record batch is missing expected buffers.");
            }
            Buffer buffer = recordBatch.buffers(bufferIndex++);
            return new BufferRegion(buffer.offset(), buffer.length());
        }
    }

    /**
     * Offset and length of a single buffer within the record batch body.
     */
    private static final class BufferRegion {
        private final long offset;
        private final long length;

        BufferRegion(long offset, long length) {
            this.offset = offset;
            this.length = length;
        }
    }

    /**
     * Base class for decoded columns.
     */
    abstract static class Column {
        final int valueCount;
        final BufferRegion validity;
        final ByteBuffer body;
        final int bodyStart;

        Column(int valueCount, BufferRegion validity, ByteBuffer body, int bodyStart) {
            this.valueCount = valueCount;
            this.validity = validity;
            this.body = body;
            this.bodyStart = bodyStart;
        }

        boolean isNull(int index) {
            if (validity == null || validity.length == 0) {
                return false;
            }
            int bytePosition = bodyStart + (int) validity.offset + (index >> 3);
            int bit = (body.get(bytePosition) >> (index & 7)) & 1;
            return bit == 0;
        }
    }

    /**
     * UTF-8 string column (Arrow Utf8/Binary).
     */
    static final class StringColumn extends Column {
        private final BufferRegion offsets;
        private final BufferRegion data;

        StringColumn(int valueCount, BufferRegion validity, BufferRegion offsets, BufferRegion data, ByteBuffer body,
            int bodyStart) {
            super(valueCount, validity, body, bodyStart);
            this.offsets = offsets;
            this.data = data;
        }

        String get(int index) {
            int start = body.getInt(bodyStart + (int) offsets.offset + index * 4);
            int end = body.getInt(bodyStart + (int) offsets.offset + (index + 1) * 4);
            int dataStart = bodyStart + (int) data.offset + start;
            byte[] valueBytes = new byte[end - start];
            for (int i = 0; i < valueBytes.length; i++) {
                valueBytes[i] = body.get(dataStart + i);
            }
            return new String(valueBytes, StandardCharsets.UTF_8);
        }
    }

    /**
     * Boolean column stored as a bitmap (Arrow Bool).
     */
    static final class BoolColumn extends Column {
        private final BufferRegion data;

        BoolColumn(int valueCount, BufferRegion validity, BufferRegion data, ByteBuffer body, int bodyStart) {
            super(valueCount, validity, body, bodyStart);
            this.data = data;
        }

        boolean get(int index) {
            int bytePosition = bodyStart + (int) data.offset + (index >> 3);
            int bit = (body.get(bytePosition) >> (index & 7)) & 1;
            return bit == 1;
        }
    }

    /**
     * Integer column (Arrow Int) of width 8/16/32/64, signed or unsigned, returned as a long.
     */
    static final class IntColumn extends Column {
        private final BufferRegion data;
        private final int bitWidth;
        private final boolean signed;

        IntColumn(int valueCount, BufferRegion validity, BufferRegion data, int bitWidth, boolean signed,
            ByteBuffer body, int bodyStart) {
            super(valueCount, validity, body, bodyStart);
            this.data = data;
            this.bitWidth = bitWidth;
            this.signed = signed;
        }

        long get(int index) {
            int base = bodyStart + (int) data.offset;
            switch (bitWidth) {
                case 64:
                    return body.getLong(base + index * 8);

                case 32: {
                    int value = body.getInt(base + index * 4);
                    return signed ? value : (value & 0xFFFFFFFFL);
                }

                case 16: {
                    short value = body.getShort(base + index * 2);
                    return signed ? value : (value & 0xFFFF);
                }

                case 8: {
                    byte value = body.get(base + index);
                    return signed ? value : (value & 0xFF);
                }

                default:
                    throw new BlobListArrowParseException(
                        "ListBlobs Arrow parse failure: unsupported integer bit width '" + bitWidth + "'.");
            }
        }
    }

    /**
     * Second-precision timestamp column (Arrow Timestamp, SECOND unit).
     */
    static final class TimestampColumn extends Column {
        private final BufferRegion data;

        TimestampColumn(int valueCount, BufferRegion validity, BufferRegion data, ByteBuffer body, int bodyStart) {
            super(valueCount, validity, body, bodyStart);
            this.data = data;
        }

        long getEpochSeconds(int index) {
            return body.getLong(bodyStart + (int) data.offset + index * 8);
        }
    }

    /**
     * Struct column holding ordered child columns (used internally for map entries).
     */
    static final class StructColumn extends Column {
        private final List<Column> children;

        StructColumn(List<Column> children) {
            super(0, null, null, 0);
            this.children = children;
        }
    }

    /**
     * Map&lt;string,string&gt; column (Arrow Map of Struct&lt;key:utf8,value:utf8&gt;).
     */
    static final class MapColumn extends Column {
        private final BufferRegion offsets;
        private final StringColumn keys;
        private final StringColumn values;

        MapColumn(int valueCount, BufferRegion validity, BufferRegion offsets, StringColumn keys, StringColumn values,
            ByteBuffer body, int bodyStart) {
            super(valueCount, validity, body, bodyStart);
            this.offsets = offsets;
            this.keys = keys;
            this.values = values;
        }

        Map<String, String> get(int index) {
            int start = body.getInt(bodyStart + (int) offsets.offset + index * 4);
            int end = body.getInt(bodyStart + (int) offsets.offset + (index + 1) * 4);
            Map<String, String> map = new HashMap<>();
            for (int entry = start; entry < end; entry++) {
                map.put(keys.get(entry), values.get(entry));
            }
            return map.isEmpty() ? null : map;
        }
    }
}
