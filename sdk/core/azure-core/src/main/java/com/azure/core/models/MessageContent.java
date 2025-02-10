// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.util.BinaryData;

/**
 * <p>Represents a message with a specific content type and data.</p>
 *
 * <p>This class encapsulates a message that includes a content type and its corresponding data. The data is
 * represented as a {@link BinaryData} object, and the content type is a string.</p>
 *
 * <p>This class is useful when you want to work with a message that includes a specific type of content and its
 * corresponding data. For example, you can use it to represent a message with JSON data, XML data,
 * or plain text data.</p>
 *
 * @see BinaryData
 */
@Fluent
public class MessageContent {
    private BinaryData binaryData;
    private String contentType;

    /**
     * Creates a new instance of {@link MessageContent}.
     */
    public MessageContent() {
    }

    /**
     * Gets the message body.
     *
     * @return The message body.
     */
    public BinaryData getBodyAsBinaryData() {
        return binaryData;
    }

    /**
     * Sets the message body.
     *
     * @param binaryData The message body.
     * @return The updated {@link MessageContent} object.
     */
    public MessageContent setBodyAsBinaryData(BinaryData binaryData) {
        this.binaryData = binaryData;
        return this;
    }

    /**
     * Gets the content type.
     *
     * @return The content type.
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * Sets the content type.
     *
     * @param contentType The content type.
     * @return The updated {@link MessageContent} object.
     */
    public MessageContent setContentType(String contentType) {
        this.contentType = contentType;
        return this;
    }
}
