// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) TypeSpec Code Generator.
package com.azure.communication.messages.models.channels;

import com.azure.communication.messages.models.CommunicationMessagesChannel;
import com.azure.communication.messages.models.MessageTemplateItem;
import com.azure.communication.messages.models.MessageTemplateStatus;
import com.azure.core.annotation.Generated;
import com.azure.core.annotation.Immutable;
import com.azure.core.util.BinaryData;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import java.io.IOException;

/**
 * The WhatsApp-specific template response contract.
 */
@Immutable
public final class WhatsAppMessageTemplateItem extends MessageTemplateItem {

    /*
     * The type discriminator describing a template type.
     */
    @Generated
    private CommunicationMessagesChannel kind = CommunicationMessagesChannel.WHATS_APP;

    /*
     * WhatsApp platform's template content. This is the payload returned from WhatsApp API.
     */
    @Generated
    private BinaryData content;

    /**
     * Creates an instance of WhatsAppMessageTemplateItem class.
     *
     * @param language the language value to set.
     * @param status the status value to set.
     */
    @Generated
    private WhatsAppMessageTemplateItem(String language, MessageTemplateStatus status) {
        super(language, status);
    }

    /**
     * Get the kind property: The type discriminator describing a template type.
     *
     * @return the kind value.
     */
    @Generated
    @Override
    public CommunicationMessagesChannel getKind() {
        return this.kind;
    }

    /**
     * Get the content property: WhatsApp platform's template content. This is the payload returned from WhatsApp API.
     *
     * @return the content value.
     */
    @Generated
    public BinaryData getContent() {
        return this.content;
    }

    /**
     * {@inheritDoc}
     */
    @Generated
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeStringField("language", getLanguage());
        jsonWriter.writeStringField("status", getStatus() == null ? null : getStatus().toString());
        jsonWriter.writeStringField("kind", this.kind == null ? null : this.kind.toString());
        if (this.content != null) {
            jsonWriter.writeFieldName("content");
            this.content.writeTo(jsonWriter);
        }
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of WhatsAppMessageTemplateItem from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of WhatsAppMessageTemplateItem if the JsonReader was pointing to an instance of it, or null
     * if it was pointing to JSON null.
     * @throws IllegalStateException If the deserialized JSON object was missing any required properties.
     * @throws IOException If an error occurs while reading the WhatsAppMessageTemplateItem.
     */
    @Generated
    public static WhatsAppMessageTemplateItem fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            String name = null;
            String language = null;
            MessageTemplateStatus status = null;
            CommunicationMessagesChannel kind = CommunicationMessagesChannel.WHATS_APP;
            BinaryData content = null;
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();
                if ("name".equals(fieldName)) {
                    name = reader.getString();
                } else if ("language".equals(fieldName)) {
                    language = reader.getString();
                } else if ("status".equals(fieldName)) {
                    status = MessageTemplateStatus.fromString(reader.getString());
                } else if ("kind".equals(fieldName)) {
                    kind = CommunicationMessagesChannel.fromString(reader.getString());
                } else if ("content".equals(fieldName)) {
                    content = reader.getNullable(nonNullReader -> BinaryData.fromObject(nonNullReader.readUntyped()));
                } else {
                    reader.skipChildren();
                }
            }
            WhatsAppMessageTemplateItem deserializedWhatsAppMessageTemplateItem
                = new WhatsAppMessageTemplateItem(language, status);
            deserializedWhatsAppMessageTemplateItem.name = name;
            deserializedWhatsAppMessageTemplateItem.kind = kind;
            deserializedWhatsAppMessageTemplateItem.content = content;
            return deserializedWhatsAppMessageTemplateItem;
        });
    }

    /*
     * The template's name.
     */
    @Generated
    private String name;

    /**
     * Get the name property: The template's name.
     *
     * @return the name value.
     */
    @Generated
    @Override
    public String getName() {
        return this.name;
    }
}
