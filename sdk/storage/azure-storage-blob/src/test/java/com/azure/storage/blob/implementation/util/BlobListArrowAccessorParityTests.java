// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.implementation.util;

import com.azure.storage.blob.implementation.util.arrow.Buffer;
import com.azure.storage.blob.implementation.util.arrow.Field;
import com.azure.storage.blob.implementation.util.arrow.Int;
import com.azure.storage.blob.implementation.util.arrow.KeyValue;
import com.azure.storage.blob.implementation.util.arrow.Message;
import com.azure.storage.blob.implementation.util.arrow.MessageHeader;
import com.azure.storage.blob.implementation.util.arrow.RecordBatch;
import com.azure.storage.blob.implementation.util.arrow.Schema;
import com.azure.storage.blob.implementation.util.arrow.Timestamp;
import com.azure.storage.blob.implementation.util.arrow.Type;
import org.apache.arrow.memory.BufferAllocator;
import org.apache.arrow.memory.RootAllocator;
import org.apache.arrow.vector.BigIntVector;
import org.apache.arrow.vector.BitVector;
import org.apache.arrow.vector.FieldVector;
import org.apache.arrow.vector.SmallIntVector;
import org.apache.arrow.vector.TimeStampSecVector;
import org.apache.arrow.vector.UInt1Vector;
import org.apache.arrow.vector.UInt4Vector;
import org.apache.arrow.vector.VarCharVector;
import org.apache.arrow.vector.VectorSchemaRoot;
import org.apache.arrow.vector.complex.MapVector;
import org.apache.arrow.vector.complex.impl.UnionMapWriter;
import org.apache.arrow.vector.ipc.ArrowStreamWriter;
import org.apache.arrow.vector.util.Text;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Differential drift detector for the vendored Arrow FlatBuffer accessors.
 * <p>
 * This test builds a real Arrow IPC stream with the official {@code arrow-vector} writer, then decodes the same bytes
 * twice: once with Apache Arrow's own generated {@code org.apache.arrow.flatbuf.*} accessors (the upstream source of
 * truth, available at test scope via {@code arrow-vector} &rarr; {@code arrow-format}) and once with the vendored
 * accessors in {@code com.azure.storage.blob.implementation.util.arrow}. It asserts every accessor the ListBlobs reader
 * relies on returns identical results from both implementations.
 * <p>
 * <strong>Why this exists:</strong> the vendored accessors hardcode FlatBuffer vtable offsets and struct sizes taken
 * from a specific Arrow schema revision. When the Arrow test dependency is upgraded (for example to 19.x), re-running
 * this test re-validates the vendored copy against the new upstream: if Arrow renumbers a field, resizes a struct, or
 * changes an accessor's semantics in a way that affects how ListBlobs payloads are parsed, this test fails and points
 * at the exact accessor that diverged, so the vendored copy can be brought back in sync. A compatible upgrade that does
 * not touch these structures leaves the test passing (no false positives).
 */
public class BlobListArrowAccessorParityTests {

    private static final int CONTINUATION_MARKER = 0xFFFFFFFF;

    @Test
    public void vendoredAccessorsMatchArrowAccessors() throws Exception {
        byte[] stream;
        try (BufferAllocator allocator = new RootAllocator()) {
            stream = buildRepresentativePayload(allocator);
        }

        List<byte[]> messages = extractMessageMetadata(stream);
        boolean sawSchema = false;
        boolean sawRecordBatch = false;

        for (byte[] metadata : messages) {
            org.apache.arrow.flatbuf.Message arrowMessage
                = org.apache.arrow.flatbuf.Message.getRootAsMessage(littleEndian(metadata));
            Message vendoredMessage = Message.getRootAsMessage(littleEndian(metadata));

            assertEquals(arrowMessage.headerType(), vendoredMessage.headerType(), "headerType");
            assertEquals(arrowMessage.bodyLength(), vendoredMessage.bodyLength(), "bodyLength");

            if (vendoredMessage.headerType() == MessageHeader.SCHEMA) {
                sawSchema = true;
                org.apache.arrow.flatbuf.Schema arrowSchema
                    = (org.apache.arrow.flatbuf.Schema) arrowMessage.header(new org.apache.arrow.flatbuf.Schema());
                Schema vendoredSchema = (Schema) vendoredMessage.header(new Schema());
                assertNotNull(arrowSchema, "arrow schema header");
                assertNotNull(vendoredSchema, "vendored schema header");
                compareSchema(arrowSchema, vendoredSchema);
            } else if (vendoredMessage.headerType() == MessageHeader.RECORD_BATCH) {
                sawRecordBatch = true;
                org.apache.arrow.flatbuf.RecordBatch arrowBatch = (org.apache.arrow.flatbuf.RecordBatch) arrowMessage
                    .header(new org.apache.arrow.flatbuf.RecordBatch());
                RecordBatch vendoredBatch = (RecordBatch) vendoredMessage.header(new RecordBatch());
                assertNotNull(arrowBatch, "arrow record batch header");
                assertNotNull(vendoredBatch, "vendored record batch header");
                compareRecordBatch(arrowBatch, vendoredBatch);
            }
        }

        assertTrue(sawSchema, "expected a Schema message in the stream");
        assertTrue(sawRecordBatch, "expected a RecordBatch message in the stream");
    }

    private static void compareSchema(org.apache.arrow.flatbuf.Schema arrow, Schema vendored) {
        assertEquals(arrow.endianness(), vendored.endianness(), "schema.endianness");
        assertEquals(arrow.fieldsLength(), vendored.fieldsLength(), "schema.fieldsLength");
        for (int i = 0; i < arrow.fieldsLength(); i++) {
            compareField(arrow.fields(i), vendored.fields(i));
        }
        assertEquals(arrow.customMetadataLength(), vendored.customMetadataLength(), "schema.customMetadataLength");
        for (int i = 0; i < arrow.customMetadataLength(); i++) {
            org.apache.arrow.flatbuf.KeyValue arrowKv = arrow.customMetadata(i);
            KeyValue vendoredKv = vendored.customMetadata(i);
            assertEquals(arrowKv.key(), vendoredKv.key(), "keyValue.key");
            assertEquals(arrowKv.value(), vendoredKv.value(), "keyValue.value");
        }
    }

    private static void compareField(org.apache.arrow.flatbuf.Field arrow, Field vendored) {
        assertEquals(arrow.name(), vendored.name(), "field.name");
        assertEquals(arrow.typeType(), vendored.typeType(), "field.typeType for " + vendored.name());
        assertEquals(arrow.childrenLength(), vendored.childrenLength(), "field.childrenLength for " + vendored.name());

        if (vendored.typeType() == Type.INT) {
            org.apache.arrow.flatbuf.Int arrowInt
                = (org.apache.arrow.flatbuf.Int) arrow.type(new org.apache.arrow.flatbuf.Int());
            Int vendoredInt = (Int) vendored.type(new Int());
            assertNotNull(arrowInt, "arrow Int type for " + vendored.name());
            assertNotNull(vendoredInt, "vendored Int type for " + vendored.name());
            assertEquals(arrowInt.bitWidth(), vendoredInt.bitWidth(), "int.bitWidth for " + vendored.name());
            assertEquals(arrowInt.isSigned(), vendoredInt.isSigned(), "int.isSigned for " + vendored.name());
        } else if (vendored.typeType() == Type.TIMESTAMP) {
            org.apache.arrow.flatbuf.Timestamp arrowTs
                = (org.apache.arrow.flatbuf.Timestamp) arrow.type(new org.apache.arrow.flatbuf.Timestamp());
            Timestamp vendoredTs = (Timestamp) vendored.type(new Timestamp());
            assertNotNull(arrowTs, "arrow Timestamp type for " + vendored.name());
            assertNotNull(vendoredTs, "vendored Timestamp type for " + vendored.name());
            assertEquals(arrowTs.unit(), vendoredTs.unit(), "timestamp.unit for " + vendored.name());
        }

        for (int i = 0; i < arrow.childrenLength(); i++) {
            compareField(arrow.children(i), vendored.children(i));
        }
    }

    private static void compareRecordBatch(org.apache.arrow.flatbuf.RecordBatch arrow, RecordBatch vendored) {
        assertEquals(arrow.length(), vendored.length(), "recordBatch.length");

        assertEquals(arrow.nodesLength(), vendored.nodesLength(), "recordBatch.nodesLength");
        for (int i = 0; i < arrow.nodesLength(); i++) {
            assertEquals(arrow.nodes(i).length(), vendored.nodes(i).length(), "fieldNode.length at " + i);
        }

        assertEquals(arrow.buffersLength(), vendored.buffersLength(), "recordBatch.buffersLength");
        for (int i = 0; i < arrow.buffersLength(); i++) {
            Buffer vendoredBuffer = vendored.buffers(i);
            assertEquals(arrow.buffers(i).offset(), vendoredBuffer.offset(), "buffer.offset at " + i);
            assertEquals(arrow.buffers(i).length(), vendoredBuffer.length(), "buffer.length at " + i);
        }

        assertEquals(arrow.compression() == null, vendored.compression() == null, "recordBatch.compression presence");
    }

    // region helpers

    private static ByteBuffer littleEndian(byte[] metadata) {
        return ByteBuffer.wrap(metadata).order(ByteOrder.LITTLE_ENDIAN);
    }

    /**
     * Splits an Arrow IPC stream into the metadata FlatBuffer of each encapsulated message, mirroring the framing the
     * production reader performs (continuation marker + metadata length prefix, body skipped).
     */
    private static List<byte[]> extractMessageMetadata(byte[] stream) {
        List<byte[]> messages = new ArrayList<>();
        ByteBuffer buffer = ByteBuffer.wrap(stream).order(ByteOrder.LITTLE_ENDIAN);
        int pos = 0;
        int length = stream.length;
        while (pos + 4 <= length) {
            int marker = buffer.getInt(pos);
            pos += 4;

            int metadataLength;
            if (marker == CONTINUATION_MARKER) {
                if (pos + 4 > length) {
                    break;
                }
                metadataLength = buffer.getInt(pos);
                pos += 4;
            } else {
                metadataLength = marker;
            }

            if (metadataLength == 0) {
                break;
            }

            byte[] metadata = new byte[metadataLength];
            System.arraycopy(stream, pos, metadata, 0, metadataLength);
            messages.add(metadata);
            pos += metadataLength;

            long bodyLength = Message.getRootAsMessage(littleEndian(metadata)).bodyLength();
            pos += (int) bodyLength;
        }
        return messages;
    }

    /**
     * Builds an Arrow IPC stream with a representative ListBlobs schema (string; signed and unsigned integers of
     * several bit widths; boolean; second-precision timestamp; and a map&lt;string,string&gt; column) plus schema-level
     * metadata, so the differential comparison exercises every vendored accessor &mdash; including {@code Int.bitWidth}
     * / {@code Int.isSigned} across multiple widths and both signedness values, and the nested map &rarr; struct &rarr;
     * key/value fields.
     */
    private static byte[] buildRepresentativePayload(BufferAllocator allocator) throws Exception {
        VarCharVector name = new VarCharVector("Name", allocator);
        UInt1Vector uint8 = new UInt1Vector("U8", allocator);
        SmallIntVector int16 = new SmallIntVector("I16", allocator);
        UInt4Vector uint32 = new UInt4Vector("U32", allocator);
        BigIntVector contentLength = new BigIntVector("Content-Length", allocator);
        BitVector deleted = new BitVector("Deleted", allocator);
        TimeStampSecVector creationTime = new TimeStampSecVector("Creation-Time", allocator);
        MapVector metadata = MapVector.empty("Metadata", allocator, false);

        name.allocateNew();
        uint8.allocateNew();
        int16.allocateNew();
        uint32.allocateNew();
        contentLength.allocateNew();
        deleted.allocateNew();
        creationTime.allocateNew();

        name.setSafe(0, "blob1".getBytes(StandardCharsets.UTF_8));
        uint8.setSafe(0, 200);
        int16.setSafe(0, -5);
        uint32.setSafe(0, 42);
        contentLength.setSafe(0, 7L);
        deleted.setSafe(0, 0);
        creationTime.setSafe(0, 1000L);

        name.setValueCount(1);
        uint8.setValueCount(1);
        int16.setValueCount(1);
        uint32.setValueCount(1);
        contentLength.setValueCount(1);
        deleted.setValueCount(1);
        creationTime.setValueCount(1);

        UnionMapWriter mapWriter = metadata.getWriter();
        mapWriter.setPosition(0);
        mapWriter.startMap();
        mapWriter.startEntry();
        mapWriter.key().varChar().writeVarChar(new Text("k1"));
        mapWriter.value().varChar().writeVarChar(new Text("v1"));
        mapWriter.endEntry();
        mapWriter.endMap();
        metadata.setValueCount(1);

        List<FieldVector> vectors = new ArrayList<>();
        vectors.add(name);
        vectors.add(uint8);
        vectors.add(int16);
        vectors.add(uint32);
        vectors.add(contentLength);
        vectors.add(deleted);
        vectors.add(creationTime);
        vectors.add(metadata);

        List<org.apache.arrow.vector.types.pojo.Field> fields = new ArrayList<>();
        for (FieldVector vector : vectors) {
            fields.add(vector.getField());
        }

        Map<String, String> schemaMetadata = new LinkedHashMap<>();
        schemaMetadata.put("NextMarker", "nextPage");
        schemaMetadata.put("NumberOfRecords", "1");
        org.apache.arrow.vector.types.pojo.Schema schema
            = new org.apache.arrow.vector.types.pojo.Schema(fields, schemaMetadata);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (VectorSchemaRoot root = new VectorSchemaRoot(schema, vectors, 1);
            ArrowStreamWriter writer = new ArrowStreamWriter(root, null, out)) {
            writer.start();
            writer.writeBatch();
            writer.end();
        }
        return out.toByteArray();
    }

    //endregion
}


