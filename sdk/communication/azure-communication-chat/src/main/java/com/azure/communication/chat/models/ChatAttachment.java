// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.chat.models;

import com.azure.core.annotation.Fluent;

/** An attachment in a chat message. */
@Fluent
public final class ChatAttachment {
    /*
     * Id of the attachment
     */
    private String id;

    /*
     * The type of attachment.
     */
    private AttachmentType attachmentType;

    /*
     * The file extension of the attachment, if available
     */
    private String extension;

    /*
     * The name of the attachment content.
     */
    private String name;

    /*
     * The URL where the attachment can be downloaded
     */
    private String url;

    /*
     * The URL where the preview of attachment can be downloaded
     */
    private String previewUrl;

    public ChatAttachment(String id, AttachmentType attachmentType) {
        this.id = id;
        this.attachmentType = attachmentType;
    }

    /**
     * Get the id property: Id of the attachment.
     *
     * @return the id value.
     */
    public String getId() {
        return this.id;
    }

    /**
     * Get the attachmentType property: The type of attachment.
     *
     * @return the attachmentType value.
     */
    public AttachmentType getAttachmentType() {
        return this.attachmentType;
    }

    /**
     * Get the extension property: The file extension of the attachment, if
     * available.
     *
     * @return the extension value.
     */
    public String getExtension() {
        return this.extension;
    }

    /**
     * Set the extension property: The file extension of the attachment, if
     * available.
     *
     * @param extension the extension value to set.
     * @return the ChatAttachment object itself.
     */
    public ChatAttachment setExtension(String extension) {
        this.extension = extension;
        return this;
    }

    /**
     * Get the name property: The name of the attachment content.
     *
     * @return the name value.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Set the name property: The name of the attachment content.
     *
     * @param name the name value to set.
     * @return the ChatAttachment object itself.
     */
    public ChatAttachment setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Get the url property: The URL where the attachment can be downloaded.
     *
     * @return the url value.
     */
    public String getUrl() {
        return this.url;
    }

    /**
     * Set the url property: The URL where the attachment can be downloaded.
     *
     * @param url the url value to set.
     * @return the ChatAttachment object itself.
     */
    public ChatAttachment setUrl(String url) {
        this.url = url;
        return this;
    }

    /**
     * Get the previewUrl property: The URL where the preview of attachment can be
     * downloaded.
     *
     * @return the previewUrl value.
     */
    public String getPreviewUrl() {
        return this.previewUrl;
    }

    /**
     * Set the previewUrl property: The URL where the preview of attachment can be
     * downloaded.
     *
     * @param previewUrl the previewUrl value to set.
     * @return the ChatAttachment object itself.
     */
    public ChatAttachment setPreviewUrl(String previewUrl) {
        this.previewUrl = previewUrl;
        return this;
    }
}
