// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.experimental.models;

import com.azure.core.util.BinaryData;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Tests for {@link MessageWithMetadata}
 */
public class MessageWithMetadataTest {
    /**
     * Verify default parameters.
     */
    @Test
    public void initialize() {
        // Act
        final MessageWithMetadata message = new MessageWithMetadata();

        // Assert
        assertNull(message.getBodyAsBinaryData(), "'body' should initially be null.");
        assertNull(message.getContentType(), "'contentType' should initially be null.");
    }

    /**
     * Verify properties can be set.
     */
    @Test
    public void settingProperties() {
        // Arrange
        final BinaryData binaryData = BinaryData.fromString("foo.bar.baz");
        final String contentType = "some-content";
        final MessageWithMetadata message = new MessageWithMetadata();

        // Act
        final MessageWithMetadata actual = message.setContentType(contentType)
            .setBodyAsBinaryData(binaryData);

        // Assert
        assertEquals(binaryData, actual.getBodyAsBinaryData());
        assertEquals(contentType, actual.getContentType());
    }
}
