// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.implementation.util;

import com.azure.storage.blob.implementation.models.BlobListArrowParseException;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Parity tests for Arrow IPC content the ListBlobs reader intentionally rejects or treats as an error. The payloads
 * are committed golden fixtures (see {@code src/test/resources/arrow}) originally produced with the official
 * {@code arrow-vector} writer, so these tests validate the vendored reader's rejection paths against genuine Arrow
 * output with no Arrow dependency. The remaining cases cover malformed/edge inputs that the writer cannot produce
 * (null and empty streams).
 */
public class BlobListArrowStreamReaderRejectionTests {

    @Test
    public void rejectsNullStream() {
        BlobListArrowParseException ex
            = assertThrows(BlobListArrowParseException.class, () -> ArrowBlobListDeserializer.deserialize(null));
        assertTrue(ex.getMessage().contains("input stream is null"), "Unexpected message: " + ex.getMessage());
    }

    @Test
    public void rejectsStreamWithNoSchema() {
        InputStream stream = new ByteArrayInputStream(new byte[0]);
        BlobListArrowParseException ex
            = assertThrows(BlobListArrowParseException.class, () -> ArrowBlobListDeserializer.deserialize(stream));
        assertTrue(ex.getMessage().contains("stream contained no schema"), "Unexpected message: " + ex.getMessage());
    }

    @Test
    public void rejectsDictionaryEncodedStreams() throws IOException {
        assertRejected("reject-dictionary.arrow.base64", "dictionary-encoded streams are not supported");
    }

    @Test
    public void rejectsUnsupportedColumnType() throws IOException {
        assertRejected("reject-float.arrow.base64", "unsupported Arrow type 'FloatingPoint'");
    }

    @Test
    public void rejectsUnsupportedTimestampUnit() throws IOException {
        assertRejected("reject-timestamp-milli.arrow.base64", "unsupported timestamp unit 'MILLISECOND'");
    }

    @Test
    public void rejectsMapWithNonStringValues() throws IOException {
        assertRejected("reject-map-nonstring.arrow.base64", "map entries must be string keys and values");
    }

    // region helpers

    private static void assertRejected(String fixture, String expectedMessageFragment) throws IOException {
        try (InputStream stream = openFixture(fixture)) {
            BlobListArrowParseException ex
                = assertThrows(BlobListArrowParseException.class, () -> ArrowBlobListDeserializer.deserialize(stream));
            assertTrue(ex.getMessage().contains(expectedMessageFragment), "Unexpected message: " + ex.getMessage());
        }
    }

    private static InputStream openFixture(String name) throws IOException {
        byte[] base64;
        try (InputStream resource
            = BlobListArrowStreamReaderRejectionTests.class.getResourceAsStream("/arrow/" + name)) {
            assertNotNull(resource, "missing test fixture: arrow/" + name);
            base64 = readAll(resource);
        }
        return new ByteArrayInputStream(Base64.getDecoder().decode(base64));
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

    //endregion
}
