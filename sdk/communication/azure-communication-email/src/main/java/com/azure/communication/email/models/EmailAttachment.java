// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.email.models;

import com.azure.core.util.BinaryData;

/** Attachment to the email. */
public final class EmailAttachment {
    /*
     * Name of the attachment
     */
    private final String name;

    /*
     * MIME type of the content being attached.
     */
    private final String contentType;

    /*
     * Base64 encoded contents of the attachment
     */
    private final BinaryData content;

    /**
     * Creates an instance of EmailAttachment class.
     *
     * @param name the name value to set.
     * @param contentType the contentType value to set.
     * @param content the content value to set.
     */
    public EmailAttachment(String name, String contentType, BinaryData content) {
        this.name = name;
        this.contentType = contentType;
        this.content = content;
    }

    /**
     * Get the name property: Name of the attachment.
     *
     * @return the name value.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Get the contentType property: MIME type of the content being attached.
     *
     * @return the contentType value.
     */
    public String getContentType() {
        return this.contentType;
    }

    /**
     * Get the content property: Contents of the attachment.
     *
     * @return the content value.
     */
    public BinaryData getContent() {
        return this.content;
    }
}
