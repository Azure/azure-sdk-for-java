// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.responses.models;

import com.azure.core.annotation.Generated;
import com.azure.core.annotation.Immutable;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import java.io.IOException;

/**
 * A response message item, representing a role and content.
 */
@Immutable
public class ResponsesMessage extends ResponsesItem {

    /*
     * The type property.
     */
    @Generated
    private ResponsesItemType type = ResponsesItemType.MESSAGE;

    /*
     * The role associated with the message.
     */
    @Generated
    private ResponsesMessageRole role;

    /*
     * The status property.
     */
    @Generated
    private ResponsesMessageStatus status;

    /**
     * Creates an instance of ResponsesMessage class.
     */
    @Generated
    public ResponsesMessage() {
    }

    /**
     * Get the type property: The type property.
     *
     * @return the type value.
     */
    @Generated
    @Override
    public ResponsesItemType getType() {
        return this.type;
    }

    /**
     * Get the role property: The role associated with the message.
     *
     * @return the role value.
     */
    @Generated
    public ResponsesMessageRole getRole() {
        return this.role;
    }

    /**
     * Get the status property: The status property.
     *
     * @return the status value.
     */
    @Generated
    public ResponsesMessageStatus getStatus() {
        return this.status;
    }

    /**
     * Set the status property: The status property.
     *
     * @param status the status value to set.
     * @return the ResponsesMessage object itself.
     */
    @Generated
    ResponsesMessage setStatus(ResponsesMessageStatus status) {
        this.status = status;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Generated
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeStringField("type", this.type == null ? null : this.type.toString());
        jsonWriter.writeStringField("role", this.role == null ? null : this.role.toString());
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of ResponsesMessage from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of ResponsesMessage if the JsonReader was pointing to an instance of it, or null if it was
     * pointing to JSON null.
     * @throws IOException If an error occurs while reading the ResponsesMessage.
     */
    @Generated
    public static ResponsesMessage fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            String discriminatorValue = null;
            try (JsonReader readerToUse = reader.bufferObject()) {
                // Prepare for reading
                readerToUse.nextToken();
                while (readerToUse.nextToken() != JsonToken.END_OBJECT) {
                    String fieldName = readerToUse.getFieldName();
                    readerToUse.nextToken();
                    if ("role".equals(fieldName)) {
                        discriminatorValue = readerToUse.getString();
                        break;
                    } else {
                        readerToUse.skipChildren();
                    }
                }
                // Use the discriminator value to determine which subtype should be deserialized.
                if ("user".equals(discriminatorValue)) {
                    return ResponsesUserMessage.fromJson(readerToUse.reset());
                } else if ("system".equals(discriminatorValue)) {
                    return ResponsesSystemMessage.fromJson(readerToUse.reset());
                } else if ("developer".equals(discriminatorValue)) {
                    return ResponsesDeveloperMessage.fromJson(readerToUse.reset());
                } else if ("assistant".equals(discriminatorValue)) {
                    return ResponsesAssistantMessage.fromJson(readerToUse.reset());
                } else {
                    return fromJsonKnownDiscriminator(readerToUse.reset());
                }
            }
        });
    }

    @Generated
    static ResponsesMessage fromJsonKnownDiscriminator(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            ResponsesMessage deserializedResponsesMessage = new ResponsesMessage();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();
                if ("id".equals(fieldName)) {
                    deserializedResponsesMessage.setId(reader.getString());
                } else if ("role".equals(fieldName)) {
                    deserializedResponsesMessage.role = ResponsesMessageRole.fromString(reader.getString());
                } else if ("status".equals(fieldName)) {
                    deserializedResponsesMessage.status = ResponsesMessageStatus.fromString(reader.getString());
                } else {
                    reader.skipChildren();
                }
            }
            return deserializedResponsesMessage;
        });
    }
}
