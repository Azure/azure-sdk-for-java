// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.chat.models;

import com.azure.core.annotation.Fluent;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.io.IOException;

/**
 * An attachment in a chat message.
 */
@Fluent
public final class ChatAttachment implements JsonSerializable<ChatAttachment> {
    /**
     * Id of the attachment
     */
    private final String id;

    /**
     * The type of attachment.
     */
    private final ChatAttachmentType attachmentType;

    /**
     * The name of the attachment content.
     */
    private String name;

    /**
     * The URL where the attachment can be downloaded
     */
    private String url;

    /**
     * The URL where the preview of attachment can be downloaded
     */
    private String previewUrl;

    /**
     * Create a new instance of ChatAttachment
     *
     * @param id the unique id of the chat attachment
     * @param attachmentType the type of attachment
     */
    public ChatAttachment(String id, ChatAttachmentType attachmentType) {
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
    public ChatAttachmentType getAttachmentType() {
        return this.attachmentType;
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

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeStringField("id", id);
        jsonWriter.writeStringField("attachmentType", attachmentType != null ? attachmentType.toString() : null);
        jsonWriter.writeStringField("name", name);
        jsonWriter.writeStringField("url", url);
        jsonWriter.writeStringField("previewUrl", previewUrl);
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of ChatAttachment from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of ChatAttachment if the JsonReader was pointing to an instance of it, or null
     * if it was pointing to JSON null.
     * @throws IOException If an error occurs while reading the ChatAttachment.
     */
    public static ChatAttachment fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            String id = null;
            ChatAttachmentType attachmentType = null;
            String name = null;
            String url = null;
            String previewUrl = null;
            while (jsonReader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();
                if ("id".equals(fieldName)) {
                    id = reader.getString();
                } else if ("attachmentType".equals(fieldName)) {
                    attachmentType = ChatAttachmentType.fromString(reader.getString());
                } else if ("name".equals(fieldName)) {
                    name = reader.getString();
                } else if ("url".equals(fieldName)) {
                    url = reader.getString();
                } else if ("previewUrl".equals(fieldName)) {
                    previewUrl = reader.getString();
                } else {
                    reader.skipChildren();
                }
            }
            return new ChatAttachment(id, attachmentType)
                .setName(name)
                .setUrl(url)
                .setPreviewUrl(previewUrl);
        });
    }
}
