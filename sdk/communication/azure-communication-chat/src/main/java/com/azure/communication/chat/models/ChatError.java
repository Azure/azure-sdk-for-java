// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.chat.models;

import com.azure.core.annotation.Fluent;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.io.IOException;
import java.util.List;

/**
 * The Chat Services error.
 */
@Fluent
public final class ChatError implements JsonSerializable<ChatError> {

    private final String code;

    private final String message;

    private final String target;

    private final List<ChatError> details;

    private final ChatError innerError;

    /**
     * Constructs a new ChatError
     * @param message The message of the original error
     * @param code The error code
     * @param target The target of the error
     * @param details Additional details
     * @param innerError The inner error
     */
    public ChatError(String message, String code, String target, List<ChatError> details, ChatError innerError) {
        this.message = message;
        this.code = code;
        this.target = target;
        this.details = details;
        this.innerError = innerError;
    }

    /**
     * Get the code property: The error code.
     *
     * @return the code value.
     */
    public String getCode() {
        return this.code;
    }

    /**
     * Get the message property: The error message.
     *
     * @return the message value.
     */
    public String getMessage() {
        return this.message;
    }

    /**
     * Get the target property: The error target.
     *
     * @return the target value.
     */
    public String getTarget() {
        return this.target;
    }

    /**
     * Get the details property: Further details about specific errors that led to this error.
     *
     * @return the details value.
     */
    public List<ChatError> getDetails() {
        return this.details;
    }

    /**
     * Get the innerError property: The inner error if any.
     *
     * @return the innerError value.
     */
    public ChatError getInnerError() {
        return this.innerError;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeStringField("code", code);
        jsonWriter.writeStringField("message", message);
        jsonWriter.writeStringField("target", target);
        jsonWriter.writeArrayField("details", details, (writer, error) -> error.toJson(writer));
        jsonWriter.writeJsonField("innerError", innerError);
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of ChatError from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of ChatError if the JsonReader was pointing to an instance of it, or null
     * if it was pointing to JSON null.
     * @throws IOException If an error occurs while reading the ChatError.
     */
    public static ChatError fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            String code = null;
            String message = null;
            String target = null;
            List<ChatError> details = null;
            ChatError innerError = null;
            while (jsonReader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();
                if ("code".equals(fieldName)) {
                    code = reader.getString();
                } else if ("message".equals(fieldName)) {
                    message = reader.getString();
                } else if ("target".equals(fieldName)) {
                    target = reader.getString();
                } else if ("details".equals(fieldName)) {
                    details = reader.readArray(ChatError::fromJson);
                } else if ("innerError".equals(fieldName)) {
                    innerError = ChatError.fromJson(reader);
                } else {
                    reader.skipChildren();
                }
            }
            return new ChatError(message, code, target, details, innerError);
        });
    }
}
