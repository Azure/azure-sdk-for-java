// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.models;

import com.azure.core.v2.util.BinaryData;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Tests for {@link MessageContent}
 */
public class MessageContentTest {
    /**
     * Verify default parameters.
     */
    @Test
    public void initialize() {
        // Act
        final MessageContent message = new MessageContent();

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
        final MessageContent message = new MessageContent();

        // Act
        final MessageContent actual = message.setContentType(contentType).setBodyAsBinaryData(binaryData);

        // Assert
        assertEquals(binaryData, actual.getBodyAsBinaryData());
        assertEquals(contentType, actual.getContentType());
    }
}
