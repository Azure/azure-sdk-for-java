// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.implementation.util;

import com.azure.storage.blob.implementation.models.BlobListArrowParseException;
import org.apache.arrow.memory.BufferAllocator;
import org.apache.arrow.memory.RootAllocator;
import org.apache.arrow.vector.FieldVector;
import org.apache.arrow.vector.Float8Vector;
import org.apache.arrow.vector.TimeStampMilliVector;
import org.apache.arrow.vector.VarCharVector;
import org.apache.arrow.vector.VectorSchemaRoot;
import org.apache.arrow.vector.complex.MapVector;
import org.apache.arrow.vector.complex.impl.UnionMapWriter;
import org.apache.arrow.vector.dictionary.Dictionary;
import org.apache.arrow.vector.dictionary.DictionaryEncoder;
import org.apache.arrow.vector.dictionary.DictionaryProvider;
import org.apache.arrow.vector.ipc.ArrowStreamWriter;
import org.apache.arrow.vector.types.pojo.DictionaryEncoding;
import org.apache.arrow.vector.types.pojo.Field;
import org.apache.arrow.vector.util.Text;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Parity tests for Arrow IPC content the ListBlobs reader intentionally rejects or treats as an error. Where possible
 * the payloads are produced with the official {@code arrow-vector} writer (test scope) so the vendored reader's
 * rejection paths are validated against genuine Arrow output rather than hand-crafted bytes. The remaining cases cover
 * malformed/edge inputs that the writer cannot produce (null and empty streams).
 * <p>
 * Fidelity parity lives in {@link BlobListArrowFlatbufConstantsTest}.
 */
public class BlobListArrowStreamReaderRejectionTests {

    @Test
    public void rejectsNullStream() {
        BlobListArrowParseException ex = assertThrows(BlobListArrowParseException.class,
            () -> ArrowBlobListDeserializer.deserialize(null));
        assertTrue(ex.getMessage().contains("input stream is null"), "Unexpected message: " + ex.getMessage());
    }

    @Test
    public void rejectsStreamWithNoSchema() {
        assertRejected(new byte[0], "stream contained no schema");
    }

    @Test
    public void rejectsDictionaryEncodedStreams() throws Exception {
        byte[] payload;
        try (BufferAllocator allocator = new RootAllocator()) {
            payload = buildDictionaryEncodedPayload(allocator);
        }
        assertRejected(payload, "dictionary-encoded streams are not supported");
    }

    @Test
    public void rejectsUnsupportedColumnType() throws Exception {
        byte[] payload;
        try (BufferAllocator allocator = new RootAllocator()) {
            Float8Vector score = new Float8Vector("Score", allocator);
            score.allocateNew();
            score.setSafe(0, 1.5);
            score.setValueCount(1);
            payload = writeBatch(allocator, Collections.singletonList(score), 1);
        }
        assertRejected(payload, "unsupported Arrow type 'FloatingPoint'");
    }

    @Test
    public void rejectsUnsupportedTimestampUnit() throws Exception {
        byte[] payload;
        try (BufferAllocator allocator = new RootAllocator()) {
            TimeStampMilliVector creationTime = new TimeStampMilliVector("Creation-Time", allocator);
            creationTime.allocateNew();
            creationTime.setSafe(0, 1000L);
            creationTime.setValueCount(1);
            payload = writeBatch(allocator, Collections.singletonList(creationTime), 1);
        }
        assertRejected(payload, "unsupported timestamp unit 'MILLISECOND'");
    }

    @Test
    public void rejectsMapWithNonStringValues() throws Exception {
        byte[] payload;
        try (BufferAllocator allocator = new RootAllocator()) {
            MapVector metadata = MapVector.empty("Metadata", allocator, false);
            UnionMapWriter mapWriter = metadata.getWriter();
            mapWriter.setPosition(0);
            mapWriter.startMap();
            mapWriter.startEntry();
            mapWriter.key().varChar().writeVarChar(new Text("k1"));
            mapWriter.value().bigInt().writeBigInt(42L);
            mapWriter.endEntry();
            mapWriter.endMap();
            metadata.setValueCount(1);
            payload = writeBatch(allocator, Collections.singletonList(metadata), 1);
        }
        assertRejected(payload, "map entries must be string keys and values");
    }

    // region helpers

    private static void assertRejected(byte[] payload, String expectedMessageFragment) {
        InputStream stream = new ByteArrayInputStream(payload);
        BlobListArrowParseException ex
            = assertThrows(BlobListArrowParseException.class, () -> ArrowBlobListDeserializer.deserialize(stream));
        assertTrue(ex.getMessage().contains(expectedMessageFragment), "Unexpected message: " + ex.getMessage());
    }

    /**
     * Writes a single-batch Arrow IPC stream from the supplied vectors. The vectors are owned by (and closed with) the
     * returned {@link VectorSchemaRoot}.
     */
    private static byte[] writeBatch(BufferAllocator allocator, List<FieldVector> vectors, int rowCount)
        throws Exception {
        List<Field> fields = new ArrayList<>(vectors.size());
        for (FieldVector vector : vectors) {
            fields.add(vector.getField());
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (VectorSchemaRoot root = new VectorSchemaRoot(fields, vectors, rowCount);
            ArrowStreamWriter writer = new ArrowStreamWriter(root, null, out)) {
            writer.start();
            writer.writeBatch();
            writer.end();
        }
        return out.toByteArray();
    }

    /**
     * Builds an Arrow IPC stream whose single column is dictionary-encoded, which forces the official writer to emit a
     * {@code DictionaryBatch} message ahead of the record batch.
     */
    private static byte[] buildDictionaryEncodedPayload(BufferAllocator allocator) throws Exception {
        VarCharVector dictVector = new VarCharVector("Name-dict", allocator);
        VarCharVector name = new VarCharVector("Name", allocator);
        try {
            dictVector.allocateNew();
            dictVector.setSafe(0, "blob1".getBytes(StandardCharsets.UTF_8));
            dictVector.setSafe(1, "blob2".getBytes(StandardCharsets.UTF_8));
            dictVector.setValueCount(2);
            Dictionary dictionary = new Dictionary(dictVector, new DictionaryEncoding(1L, false, null));

            name.allocateNew();
            name.setSafe(0, "blob1".getBytes(StandardCharsets.UTF_8));
            name.setSafe(1, "blob2".getBytes(StandardCharsets.UTF_8));
            name.setValueCount(2);

            DictionaryProvider.MapDictionaryProvider provider = new DictionaryProvider.MapDictionaryProvider();
            provider.put(dictionary);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            FieldVector encoded = (FieldVector) DictionaryEncoder.encode(name, dictionary);
            try (VectorSchemaRoot root = new VectorSchemaRoot(Collections.singletonList(encoded.getField()),
                Collections.singletonList(encoded), 2);
                ArrowStreamWriter writer = new ArrowStreamWriter(root, provider, out)) {
                writer.start();
                writer.writeBatch();
                writer.end();
            }
            return out.toByteArray();
        } finally {
            name.close();
            dictVector.close();
        }
    }

    //endregion
}

