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
import java.util.HashMap;
import java.util.Map;

/**
 * The ChatThread model.
 */
@Fluent
public final class ChatThreadProperties implements JsonSerializable<ChatThreadProperties> {

    private String id;

    private String topic;

    private OffsetDateTime createdOn;

    private CommunicationIdentifier createdBy;

    // Property bag of chat thread metadata key-value pairs.
    private Map<String, String> metadata = new HashMap<>();

    // Thread retention policy.
    private ChatRetentionPolicy retentionPolicy;

    /**
     * Creates a new instance of {@link ChatThreadProperties}.
     */
    public ChatThreadProperties() {
    }

    /**
     * Gets the id property: Chat thread id.
     *
     * @return the id value.
     */
    public String getId() {
        return this.id;
    }

    /**
     * Sets the id property: Chat thread id.
     *
     * @param id the id value to set.
     * @return the ChatThreadProperties object itself.
     */
    public ChatThreadProperties setId(String id) {
        this.id = id;
        return this;
    }

    /**
     * Gets the topic property: Chat thread topic.
     *
     * @return the topic value.
     */
    public String getTopic() {
        return this.topic;
    }

    /**
     * Sets the topic property: Chat thread topic.
     *
     * @param topic the topic value to set.
     * @return the ChatThreadProperties object itself.
     */
    public ChatThreadProperties setTopic(String topic) {
        this.topic = topic;
        return this;
    }

    /**
     * Gets the createdOn property: The timestamp when the chat thread was created. The timestamp is in RFC3339 format:
     * `yyyy-MM-ddTHH:mm:ssZ`.
     *
     * @return the createdOn value.
     */
    public OffsetDateTime getCreatedOn() {
        return this.createdOn;
    }

    /**
     * Sets the createdOn property: The timestamp when the chat thread was created. The timestamp is in RFC3339 format:
     * `yyyy-MM-ddTHH:mm:ssZ`.
     *
     * @param createdOn the createdOn value to set.
     * @return the ChatThreadProperties object itself.
     */
    public ChatThreadProperties setCreatedOn(OffsetDateTime createdOn) {
        this.createdOn = createdOn;
        return this;
    }

    /**
     * Gets the createdBy property: Identifies a participant in Azure Communication services. A
     * participant is, for example, a phone number or an Azure communication user. This model must be interpreted as a
     * union: Apart from rawId, at most one further property may be set.
     *
     * @return the createdBy value.
     */
    public CommunicationIdentifier getCreatedBy() {
        return this.createdBy;
    }

    /**
     * Sets the createdBy property: Identifies a participant in Azure Communication services. A
     * participant is, for example, a phone number or an Azure communication user. This model must be interpreted as a
     * union: Apart from rawId, at most one further property may be set.
     *
     * @param createdBy the createdBy value to set.
     * @return the ChatThreadProperties object itself.
     */
    public ChatThreadProperties setCreatedBy(CommunicationIdentifier createdBy) {
        this.createdBy = createdBy;
        return this;
    }

    /**
     * Gets the metadata key-value pairs associated with the chat thread.
     *
     * @return the metadata map.
     */
    public Map<String, String> getMetadata() {
        return this.metadata;
    }

    /**
     * Sets the metadata key-value pairs associated with the chat thread.
     *
     * @param metadata the metadata map to set.
     * @return the ChatThreadProperties object itself.
     */
    public ChatThreadProperties setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
        return this;
    }

    /**
     * Gets the chat thread retention policy.
     *
     * @return the retentionPolicy.
     */
    public ChatRetentionPolicy getRetentionPolicy() {
        return this.retentionPolicy;
    }

    /**
     * Sets the chat thread retention policy.
     *
     * @param retentionPolicy the retention policy to set.
     * @return the ChatThreadProperties object itself.
     */
    public ChatThreadProperties setRetentionPolicy(ChatRetentionPolicy retentionPolicy) {
        this.retentionPolicy = retentionPolicy;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeStringField("id", id);
        jsonWriter.writeStringField("topic", topic);
        jsonWriter.writeStringField("createdOn", createdOn != null ? createdOn.toString() : null);
        final CommunicationIdentifierModel identifier = CommunicationIdentifierConverter.convert(createdBy);
        jsonWriter.writeJsonField("createdBy", identifier);

        // Write metadata as a JSON object of string fields
        jsonWriter.writeStartObject("metadata");
        for (Map.Entry<String, String> entry : metadata.entrySet()) {
            jsonWriter.writeStringField(entry.getKey(), entry.getValue());
        }
        jsonWriter.writeEndObject();

        if (retentionPolicy != null) {
            jsonWriter.writeJsonField("retentionPolicy", retentionPolicy);
        }
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of ChatThreadProperties from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of ChatThreadProperties if the JsonReader was pointing to an instance of it, or null
     * if it was pointing to JSON null.
     * @throws IOException If an error occurs while reading the ChatThreadProperties.
     */
    public static ChatThreadProperties fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            final ChatThreadProperties properties = new ChatThreadProperties();
            while (jsonReader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();
                if ("id".equals(fieldName)) {
                    properties.setId(reader.getString());
                } else if ("topic".equals(fieldName)) {
                    properties.setTopic(reader.getString());
                } else if ("createdOn".equals(fieldName)) {
                    final String value = reader.getString();
                    if (!CoreUtils.isNullOrEmpty(value)) {
                        properties.setCreatedOn(OffsetDateTime.parse(value));
                    }
                } else if ("createdBy".equals(fieldName)) {
                    final CommunicationIdentifierModel identifier
                        = reader.readObject(CommunicationIdentifierModel::fromJson);
                    properties.setCreatedBy(CommunicationIdentifierConverter.convert(identifier));
                } else if ("metadata".equals(fieldName)) {
                    Map<String, String> metadata = reader.readMap(reader1 -> reader1.getString());
                    properties.metadata = metadata;
                } else if ("retentionPolicy".equals(fieldName)) {
                    properties.retentionPolicy = ChatRetentionPolicy.fromJson(reader);
                } else {
                    reader.skipChildren();
                }
            }
            return properties;
        });
    }
}
