// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.implementation.util;

import com.azure.storage.blob.implementation.models.BlobListArrowParseException;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ArrowBlobListDeserializerTests {
    @Test
    public void parseNullStreamFailsFast() {
        BlobListArrowParseException exception
            = assertThrows(BlobListArrowParseException.class, () -> ArrowBlobListDeserializer.deserialize(null));

        assertTrue(exception.getMessage().startsWith("ListBlobs Arrow parse failure:"));
    }

    @Test
    public void parseInvalidPayloadFailsFast() {
        ByteArrayInputStream invalidPayload
            = new ByteArrayInputStream("not-an-arrow-stream".getBytes(StandardCharsets.UTF_8));

        BlobListArrowParseException exception = assertThrows(BlobListArrowParseException.class,
            () -> ArrowBlobListDeserializer.deserialize(invalidPayload));

        assertTrue(exception.getMessage().startsWith("ListBlobs Arrow parse failure:"));
    }
}
