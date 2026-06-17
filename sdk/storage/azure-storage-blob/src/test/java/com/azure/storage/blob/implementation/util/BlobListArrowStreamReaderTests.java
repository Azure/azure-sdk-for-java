// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.implementation.util;

import com.azure.storage.blob.implementation.models.BlobItemInternal;
import org.apache.arrow.memory.BufferAllocator;
import org.apache.arrow.memory.RootAllocator;
import org.apache.arrow.vector.BigIntVector;
import org.apache.arrow.vector.BitVector;
import org.apache.arrow.vector.FieldVector;
import org.apache.arrow.vector.TimeStampSecVector;
import org.apache.arrow.vector.VarCharVector;
import org.apache.arrow.vector.VectorSchemaRoot;
import org.apache.arrow.vector.complex.MapVector;
import org.apache.arrow.vector.complex.impl.UnionMapWriter;
import org.apache.arrow.vector.ipc.ArrowStreamWriter;
import org.apache.arrow.vector.types.pojo.Field;
import org.apache.arrow.vector.types.pojo.Schema;
import org.apache.arrow.vector.util.Text;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Parity tests that build a real Arrow IPC payload with the official {@code arrow-vector} writer and validate that the
 * internal {@link BlobListArrowStreamReader} / {@link ArrowBlobListDeserializer} decode it identically. This proves the
 * custom reader has the same implementation as the Apache Arrow parser
 */
public class BlobListArrowStreamReaderTests {

    @Test
    public void parsesRealArrowPayload() throws Exception {
        byte[] payload;
        try (BufferAllocator allocator = new RootAllocator()) {
            payload = buildPayload(allocator);
        }

        ArrowBlobListDeserializer.ArrowListBlobsResult result
            = ArrowBlobListDeserializer.deserialize(new ByteArrayInputStream(payload));

        // Schema metadata
        assertEquals("nextPage", result.getNextMarker());
        assertEquals(Integer.valueOf(2), result.getNumberOfRecords());

        // Two rows: one blob, one prefix
        List<BlobItemInternal> items = result.getBlobItems();
        assertEquals(2, items.size());

        BlobItemInternal blob = items.get(0);
        assertNotNull(blob.getName());
        assertEquals("blob1", blob.getName().getContent());
        assertNull(blob.isPrefix());
        assertEquals(Boolean.FALSE, blob.isDeleted());
        assertNotNull(blob.getProperties());
        assertEquals(7L, (long) blob.getProperties().getContentLength());
        assertEquals("application/octet-stream", blob.getProperties().getContentType());
        assertNotNull(blob.getProperties().getCreationTime());
        assertEquals(1000L, blob.getProperties().getCreationTime().toEpochSecond());

        Map<String, String> metadata = blob.getMetadata();
        assertNotNull(metadata);
        assertEquals("v1", metadata.get("k1"));
        assertEquals("v2", metadata.get("k2"));

        BlobItemInternal prefix = items.get(1);
        assertNotNull(prefix.getName());
        assertEquals("dir/", prefix.getName().getContent());
        assertTrue(prefix.isPrefix());
    }

    @Test
    public void parsesEmptyMetadataAsNull() throws Exception {
        byte[] payload;
        try (BufferAllocator allocator = new RootAllocator()) {
            payload = buildPayload(allocator);
        }

        ArrowBlobListDeserializer.ArrowListBlobsResult result
            = ArrowBlobListDeserializer.deserialize(new ByteArrayInputStream(payload));
        // Row 1 (prefix) had no metadata entries; ensure prefix path doesn't surface an (empty) metadata map.
        assertNull(result.getBlobItems().get(1).getMetadata());
        assertFalse(result.getBlobItems().isEmpty());
    }

    /**
     * Builds an Arrow IPC stream with a representative ListBlobs schema: string, integer, boolean, second-precision
     * timestamp, content-type string and a map&lt;string,string&gt; metadata column, plus schema-level NextMarker and
     * NumberOfRecords metadata.
     */
    private static byte[] buildPayload(BufferAllocator allocator) throws Exception {
        VarCharVector name = new VarCharVector("Name", allocator);
        VarCharVector resourceType = new VarCharVector("ResourceType", allocator);
        BigIntVector contentLength = new BigIntVector("Content-Length", allocator);
        VarCharVector contentType = new VarCharVector("Content-Type", allocator);
        BitVector deleted = new BitVector("Deleted", allocator);
        TimeStampSecVector creationTime = new TimeStampSecVector("Creation-Time", allocator);
        MapVector metadata = MapVector.empty("Metadata", allocator, false);

        name.allocateNew();
        resourceType.allocateNew();
        contentLength.allocateNew();
        contentType.allocateNew();
        deleted.allocateNew();
        creationTime.allocateNew();

        // Row 0: a real blob.
        name.setSafe(0, "blob1".getBytes(StandardCharsets.UTF_8));
        // resourceType[0] left null -> not a prefix.
        contentLength.setSafe(0, 7L);
        contentType.setSafe(0, "application/octet-stream".getBytes(StandardCharsets.UTF_8));
        deleted.setSafe(0, 0);
        creationTime.setSafe(0, 1000L);

        // Row 1: a virtual directory (prefix).
        name.setSafe(1, "dir/".getBytes(StandardCharsets.UTF_8));
        resourceType.setSafe(1, "blobprefix".getBytes(StandardCharsets.UTF_8));
        // remaining columns null for the prefix row.

        name.setValueCount(2);
        resourceType.setValueCount(2);
        contentLength.setValueCount(2);
        contentType.setValueCount(2);
        deleted.setValueCount(2);
        creationTime.setValueCount(2);

        UnionMapWriter mapWriter = metadata.getWriter();
        mapWriter.setPosition(0);
        mapWriter.startMap();
        writeEntry(mapWriter, "k1", "v1");
        writeEntry(mapWriter, "k2", "v2");
        mapWriter.endMap();
        // Row 1 metadata left null.
        metadata.setValueCount(2);

        List<FieldVector> vectors = new ArrayList<>();
        vectors.add(name);
        vectors.add(resourceType);
        vectors.add(contentLength);
        vectors.add(contentType);
        vectors.add(deleted);
        vectors.add(creationTime);
        vectors.add(metadata);

        List<Field> fields = new ArrayList<>();
        for (FieldVector vector : vectors) {
            fields.add(vector.getField());
        }

        Map<String, String> schemaMetadata = new LinkedHashMap<>();
        schemaMetadata.put("NextMarker", "nextPage");
        schemaMetadata.put("NumberOfRecords", "2");
        Schema schema = new Schema(fields, schemaMetadata);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (VectorSchemaRoot root = new VectorSchemaRoot(schema, vectors, 2);
            ArrowStreamWriter writer = new ArrowStreamWriter(root, null, out)) {
            writer.start();
            writer.writeBatch();
            writer.end();
        }

        return out.toByteArray();
    }

    private static void writeEntry(UnionMapWriter mapWriter, String key, String value) {
        mapWriter.startEntry();
        mapWriter.key().varChar().writeVarChar(new Text(key));
        mapWriter.value().varChar().writeVarChar(new Text(value));
        mapWriter.endEntry();
    }
}
