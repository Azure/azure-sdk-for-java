// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.implementation.util;

import com.azure.storage.blob.implementation.models.BlobItemInternal;
import com.azure.storage.blob.implementation.models.BlobListArrowParseException;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Golden-payload decode tests for the vendored {@link BlobListArrowStreamReader} / {@link ArrowBlobListDeserializer}.
 * <p>
 * The fixture ({@code arrow/representative.arrow.base64}) is a real Arrow IPC stream that was produced once by the
 * official {@code arrow-vector} writer (see {@code ArrowFixtureGenerator}) and committed as a frozen resource. This test
 * decodes those exact bytes with the vendored reader and pins every field the ListBlobs path relies on, so any
 * regression in the vendored FlatBuffer accessors surfaces here without requiring the Arrow library on the test
 * classpath. It replaces the former differential parity test that re-decoded a freshly written payload with Apache
 * Arrow's own accessors; the frozen fixture is the same shape but removes the runtime dependency.
 */
public class BlobListArrowGoldenDecodeTests {

    @Test
    public void decodesRepresentativePayload() throws IOException {
        ArrowBlobListDeserializer.ArrowListBlobsResult result
            = ArrowBlobListDeserializer.deserialize(openFixture("representative.arrow.base64"));

        // Schema metadata.
        assertEquals("nextPage", result.getNextMarker());
        assertEquals(Integer.valueOf(2), result.getNumberOfRecords());

        // Two rows: one blob, one prefix.
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
        // The prefix row had no metadata entries; the reader must not surface an (empty) map for it.
        assertNull(prefix.getMetadata());
        assertFalse(items.isEmpty());
    }

    @Test
    public void allColumnsFixtureDecodesWithoutRejection() throws IOException {
        // Every column the SDK claims to handle is present in this full-schema fixture and must decode without the
        // unknown-column guard firing (no false positives) and without a type-mismatch error.
        ArrowBlobListDeserializer.ArrowListBlobsResult result
            = ArrowBlobListDeserializer.deserialize(openFixture("allcolumns.arrow.base64"));

        List<BlobItemInternal> items = result.getBlobItems();
        assertEquals(1, items.size());
        BlobItemInternal blob = items.get(0);

        // Spot-check one field from each Arrow type category to prove full-schema decode.
        assertEquals("blob1", blob.getName().getContent());
        assertEquals(Boolean.FALSE, blob.isDeleted());
        assertEquals(7L, (long) blob.getProperties().getContentLength());
        assertEquals(1000L, blob.getProperties().getCreationTime().toEpochSecond());
        assertEquals("mv", blob.getMetadata().get("mk"));
    }

    @Test
    public void allColumnsFixtureSchemaMatchesKnownColumnSet() throws IOException {
        // Drift guard: the frozen full-schema fixture and the deserializer's known-column set must stay identical. If a
        // column is added to KNOWN_COLUMNS without regenerating the fixture (or vice versa), this fails loudly.
        Set<String> fixtureColumns;
        try (InputStream stream = openFixture("allcolumns.arrow.base64")) {
            BlobListArrowStreamReader.DecodedArrowStream decodedArrowStream = BlobListArrowStreamReader.read(stream);
            fixtureColumns = decodedArrowStream.batches().get(0).getColumnNames();
        }
        assertEquals(ArrowBlobListDeserializer.knownColumns(), fixtureColumns);
    }

    @Test
    public void rejectsUnknownColumn() throws IOException {
        // The service added a column a future SDK understands; this SDK must reject loudly and name the offender.
        try (InputStream stream = openFixture("reject-unknown-column.arrow.base64")) {
            BlobListArrowParseException ex
                = assertThrows(BlobListArrowParseException.class, () -> ArrowBlobListDeserializer.deserialize(stream));
            assertTrue(ex.getMessage().contains("FutureField"), "Unexpected message: " + ex.getMessage());
        }
    }

    private static InputStream openFixture(String name) throws IOException {
        byte[] base64;
        try (InputStream resource = classpathResource("/arrow/" + name)) {
            assertNotNull(resource, "missing test fixture: arrow/" + name);
            base64 = readAll(resource);
        }
        return new ByteArrayInputStream(Base64.getDecoder().decode(base64));
    }

    /**
     * Loads a resource from the test classpath. Calling {@code getResourceAsStream} on this test's class literal is
     * equivalent to going through its class loader directly (like
     * {@code Thread.currentThread().getContextClassLoader().getResourceAsStream(...)}); we use the class literal because
     * this is a static context with no {@code this} on which to call {@code getClass()}. The leading {@code /} makes the
     * path absolute from the classpath root.
     *
     * @param absolutePath the classpath-absolute resource path, beginning with {@code /}
     * @return the resource stream, or null if the resource is not found
     */
    private static InputStream classpathResource(String absolutePath) {
        return BlobListArrowGoldenDecodeTests.class.getResourceAsStream(absolutePath);
    }

    private static byte[] readAll(InputStream in) throws IOException {
        java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
        return out.toByteArray();
    }
}
