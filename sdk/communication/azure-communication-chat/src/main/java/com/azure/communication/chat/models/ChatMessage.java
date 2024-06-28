// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.chat.models;

import com.azure.communication.chat.implementation.converters.CommunicationIdentifierConverter;
import com.azure.communication.chat.implementation.models.CommunicationIdentifierModel;
import com.azure.communication.common.CommunicationIdentifier;
import com.azure.core.annotation.Fluent;
import com.azure.core.util.CoreUtils;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.Map;

/**
 * The ChatMessage model.
 */
@Fluent
public final class ChatMessage implements JsonSerializable<ChatMessage> {
    /**
     * The id of the chat message.
     */
    private String id;

    /**
     * Type of the chat message.
     *
     */
    private ChatMessageType type;

    /**
     * Version of the chat message.
     */
    private String version;

    /**
     * Content of the chat message.
     */
    private ChatMessageContent content;

    /**
     * The display name of the chat message sender. This property is used to
     * populate sender name for push notifications.
     */
    private String senderDisplayName;

    /**
     * The timestamp when the chat message arrived at the server. The timestamp
     * is in RFC3339 format: `yyyy-MM-ddTHH:mm:ssZ`.
     */
    private OffsetDateTime createdOn;

    /**
     * Identifies a participant in Azure Communication services. A participant
     * is, for example, a phone number or an Azure communication user. This
     * model must be interpreted as a union: Apart from rawId, at most one
     * further property may be set.
     */
    private CommunicationIdentifier sender;

    /**
     * The timestamp when the chat message was deleted. The timestamp is in
     * RFC3339 format: `yyyy-MM-ddTHH:mm:ssZ`.
     */
    private OffsetDateTime deletedOn;

    /**
     * The timestamp when the chat message was edited. The timestamp is in
     * RFC3339 format: `yyyy-MM-ddTHH:mm:ssZ`.
     */
    private OffsetDateTime editedOn;

    /**
     * Message metadata.
     */
    private Map<String, String> metadata;

    /**
     * Get the id property: The id of the chat message. This id is server generated.
     *
     * @return the id value.
     */
    public String getId() {
        return this.id;
    }

    /**
     * Set the id property: The id of the chat message.
     *
     * @param id the id to set.
     * @return the ChatMessage object itself.
     */
    public ChatMessage setId(String id) {
        this.id = id;
        return this;
    }

    /**
     * Get the type property: Type of the chat message.
     *
     * <p>Possible values: - Text - ThreadActivity/TopicUpdate - ThreadActivity/AddMember - ThreadActivity/DeleteMember.
     *
     * @return the type value.
     */
    public ChatMessageType getType() {
        return this.type;
    }

    /**
     * Set the type property: Type of the chat message.
     *
     * <p>Possible values: - Text - ThreadActivity/TopicUpdate - ThreadActivity/AddMember - ThreadActivity/DeleteMember.
     *
     * @param type the type value to set.
     * @return the ChatMessage object itself.
     */
    public ChatMessage setType(ChatMessageType type) {
        this.type = type;
        return this;
    }

    /**
     * Get the version property: Version of the chat message.
     *
     * @return the version value.
     */
    public String getVersion() {
        return this.version;
    }

    /**
     * Set the version property: Version of the chat message.
     *
     * @param version the version to set.
     * @return the ChatMessage object itself.
     */
    public ChatMessage setVersion(String version) {
        this.version = version;
        return this;
    }

    /**
     * Get the content property: Content of the chat message.
     *
     * @return the content value.
     */
    public ChatMessageContent getContent() {
        return this.content;
    }

    /**
     * Set the content property: Content of the chat message.
     *
     * @param content the content value to set.
     * @return the ChatMessage object itself.
     */
    public ChatMessage setContent(ChatMessageContent content) {
        this.content = content;
        return this;
    }

    /**
     * Get the senderDisplayName property: The display name of the chat message sender. This property is used to
     * populate sender name for push notifications.
     *
     * @return the senderDisplayName value.
     */
    public String getSenderDisplayName() {
        return this.senderDisplayName;
    }

    /**
     * Set the senderDisplayName property: The display name of the chat message sender. This property is used to
     * populate sender name for push notifications.
     *
     * @param senderDisplayName the senderDisplayName value to set.
     * @return the ChatMessage object itself.
     */
    public ChatMessage setSenderDisplayName(String senderDisplayName) {
        this.senderDisplayName = senderDisplayName;
        return this;
    }

    /**
     * Get the createdOn property: The timestamp when the chat message arrived at the server. The timestamp is in
     * RFC3339 format: `yyyy-MM-ddTHH:mm:ssZ`.
     *
     * @return the createdOn value.
     */
    public OffsetDateTime getCreatedOn() {
        return this.createdOn;
    }

    /**
     * Set the createdOn property: The timestamp when the chat message arrived at the server. The timestamp is in
     * RFC3339 format: `yyyy-MM-ddTHH:mm:ssZ`.
     *
     * @param createdOn the createdOn value to set.
     * @return the ChatMessage object itself.
     */
    public ChatMessage setCreatedOn(OffsetDateTime createdOn) {
        this.createdOn = createdOn;
        return this;
    }

    /**
     * Get the sender property: Identifies a participant in Azure Communication services. A
     * participant is, for example, a phone number or an Azure communication user. This model must be interpreted as a
     * union: Apart from rawId, at most one further property may be set.
     *
     * @return the sender value.
     */
    public CommunicationIdentifier getSender() {
        return this.sender;
    }

    /**
     * Set the sender property: Identifies a participant in Azure Communication services. A
     * participant is, for example, a phone number or an Azure communication user. This model must be interpreted as a
     * union: Apart from rawId, at most one further property may be set.
     *
     * @param sender the sender value to set.
     * @return the ChatMessage object itself.
     */
    public ChatMessage setSender(CommunicationIdentifier sender) {
        this.sender = sender;
        return this;
    }

    /**
     * Get the deletedOn property: The timestamp when the chat message was deleted. The timestamp is in RFC3339 format:
     * `yyyy-MM-ddTHH:mm:ssZ`.
     *
     * @return the deletedOn value.
     */
    public OffsetDateTime getDeletedOn() {
        return this.deletedOn;
    }

    /**
     * Set the deletedOn property: The timestamp when the chat message was deleted. The timestamp is in RFC3339 format:
     * `yyyy-MM-ddTHH:mm:ssZ`.
     *
     * @param deletedOn the deletedOn value to set.
     * @return the ChatMessage object itself.
     */
    public ChatMessage setDeletedOn(OffsetDateTime deletedOn) {
        this.deletedOn = deletedOn;
        return this;
    }

    /**
     * Get the editedOn property: The timestamp when the chat message was edited. The timestamp is in RFC3339 format:
     * `yyyy-MM-ddTHH:mm:ssZ`.
     *
     * @return the editedOn value.
     */
    public OffsetDateTime getEditedOn() {
        return this.editedOn;
    }

    /**
     * Set the editedOn property: The timestamp when the chat message was edited. The timestamp is in RFC3339 format:
     * `yyyy-MM-ddTHH:mm:ssZ`.
     *
     * @param editedOn the editedOn value to set.
     * @return the ChatMessage object itself.
     */
    public ChatMessage setEditedOn(OffsetDateTime editedOn) {
        this.editedOn = editedOn;
        return this;
    }

    /**
     * Get the metadata property: Message metadata.
     *
     * @return the metadata value.
     */
    public Map<String, String> getMetadata() {
        return this.metadata;
    }

    /**
     * Set the metadata property: Message metadata.
     *
     * @param metadata the metadata value to set.
     * @return the ChatMessage object itself.
     */
    public ChatMessage setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeStringField("id", id);
        jsonWriter.writeStringField("type", type != null ? type.toString() : null);
        jsonWriter.writeJsonField("content", content);
        jsonWriter.writeStringField("senderDisplayName", senderDisplayName);
        jsonWriter.writeStringField("createdOn", createdOn != null ? createdOn.toString() : null);
        // final CommunicationIdentifierModel identifier = CommunicationIdentifierConverter.convert(sender);
        // jsonWriter.writeJsonField("senderCommunicationIdentifier", identifier);
        jsonWriter.writeStringField("deletedOn", deletedOn != null ? deletedOn.toString() : null);
        jsonWriter.writeStringField("editedOn", editedOn != null ? editedOn.toString() : null);
        jsonWriter.writeMapField("metadata", metadata, JsonWriter::writeString);
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of SendChatMessageOptions from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of SendChatMessageOptions if the JsonReader was pointing to an instance of it, or null
     * if it was pointing to JSON null.
     * @throws IOException If an error occurs while reading the SendChatMessageOptions.
     */
    public static ChatMessage fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            final ChatMessage message = new ChatMessage();
            while (jsonReader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();
                if ("id".equals(fieldName)) {
                    message.setId(reader.getString());
                } else if ("type".equals(fieldName)) {
                    message.setType(ChatMessageType.fromString(reader.getString()));
                } else if ("version".equals(fieldName)) {
                    message.setVersion(reader.getString());
                } else if ("content".equals(fieldName)) {
                    message.setContent(ChatMessageContent.fromJson(reader));
                } else if ("senderDisplayName".equals(fieldName)) {
                    message.setSenderDisplayName(reader.getString());
                } else if ("createdOn".equals(fieldName)) {
                    final String value = reader.getString();
                    if (!CoreUtils.isNullOrEmpty(value)) {
                        message.setCreatedOn(OffsetDateTime.parse(value));
                    }
                } else if ("senderCommunicationIdentifier".equals(fieldName)) {
                    final CommunicationIdentifierModel identifier = reader.readObject(CommunicationIdentifierModel::fromJson);
                    message.setSender(CommunicationIdentifierConverter.convert(identifier));
                } else if ("deletedOn".equals(fieldName)) {
                    final String value = reader.getString();
                    if (!CoreUtils.isNullOrEmpty(value)) {
                        message.setDeletedOn(OffsetDateTime.parse(value));
                    }
                } else if ("editedOn".equals(fieldName)) {
                    final String value = reader.getString();
                    if (!CoreUtils.isNullOrEmpty(value)) {
                        message.setEditedOn(OffsetDateTime.parse(value));
                    }
                } else if ("metadata".equals(fieldName)) {
                    message.setMetadata(reader.readMap(JsonReader::getString));
                } else {
                    reader.skipChildren();
                }
            }
            return message;
        });
    }
}
