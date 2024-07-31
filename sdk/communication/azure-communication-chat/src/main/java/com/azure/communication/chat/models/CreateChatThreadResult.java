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
 * The CreateChatThreadResult model.
 */
@Fluent
public final class CreateChatThreadResult implements JsonSerializable<CreateChatThreadResult> {
    /**
     * The thread property.
     */
    private ChatThreadProperties chatThreadProperties;

    /**
     * The participants that failed to be added to the chat thread.
     */
    private List<ChatError> invalidParticipants;

    /**
     * Constructs a new instance of CreateChatThreadResult
     * @param chatThreadProperties The chat thread that was created.
     * @param invalidParticipants List of errors that occurred when attempting to create the chat thread.
     */
    public CreateChatThreadResult(ChatThreadProperties chatThreadProperties, List<ChatError> invalidParticipants) {
        this.chatThreadProperties = chatThreadProperties;
        this.invalidParticipants = invalidParticipants;
    }

    /**
     * Get the chatThread property: The chatThread property.
     *
     * @return the chatThread value.
     */
    public ChatThreadProperties getChatThread() {
        return this.chatThreadProperties;
    }

    /**
     * Get the invalidParticipants property: The participants that failed to be added to the chat thread.
     * The 'target' property of each ChatError will reference the failed participant.
     *
     * @return the invalidParticipants value.
     */
    public List<ChatError> getInvalidParticipants() {
        return this.invalidParticipants;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeJsonField("chatThread", chatThreadProperties);
        // Not serializing 'invalidParticipants' as it is json read only.
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of AddChatParticipantsResult from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of AddChatParticipantsResult if the JsonReader was pointing to an instance of it, or null
     * if it was pointing to JSON null.
     * @throws IOException If an error occurs while reading the AddChatParticipantsResult.
     */
    public static CreateChatThreadResult fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            ChatThreadProperties chatThreadProperties = null;
            List<ChatError> invalidParticipants = null;
            while (jsonReader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();
                if ("chatThread".equals(fieldName)) {
                    chatThreadProperties = ChatThreadProperties.fromJson(reader);
                } else if ("invalidParticipants".equals(fieldName)) {
                    invalidParticipants = reader.readArray(ChatError::fromJson);
                } else {
                    reader.skipChildren();
                }
            }
            return new CreateChatThreadResult(chatThreadProperties, invalidParticipants);
        });
    }
}
