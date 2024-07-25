// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.chat.models;

import com.azure.communication.chat.implementation.converters.CommunicationIdentifierConverter;
import com.azure.communication.chat.implementation.models.CommunicationIdentifierModel;
import com.azure.communication.common.CommunicationIdentifier;
import com.azure.core.annotation.Fluent;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.io.IOException;
import java.util.List;

/**
 * Content of a chat message.
 */
@Fluent
public final class ChatMessageContent implements JsonSerializable<ChatMessageContent> {

    private final String message;

    private final String topic;

    private final Iterable<ChatParticipant> participants;

    private Iterable<ChatAttachment> attachments;

    private final CommunicationIdentifier initiator;

    /**
     * Constructs a new ChatMessageContent
     *
     * @param message The message
     * @param topic The topic
     * @param participants The participants
     * @param initiator The initiator
     */
    public ChatMessageContent(
            String message, String topic, Iterable<ChatParticipant> participants, CommunicationIdentifier initiator) {

        this.message = message;
        this.topic = topic;
        this.participants = participants;
        this.initiator = initiator;
    }

    /**
     * Get the message property: Chat message content for messages of types text or
     * html.
     *
     * @return the message value.
     */
    public String getMessage() {
        return this.message;
    }

    /**
     * Get the topic property: Chat message content for messages of type
     * topicUpdated.
     *
     * @return the topic value.
     */
    public String getTopic() {
        return this.topic;
    }

    /**
     * Get the participants property: Chat message content for messages of types
     * participantAdded or participantRemoved.
     *
     * @return the participants value.
     */
    public Iterable<ChatParticipant> getParticipants() {
        return this.participants;
    }

    /**
     * Get the attachment property: Chat message content for messages of type
     * attachments
     *
     * @return the attachments value.
     */
    public Iterable<ChatAttachment> getAttachments() {
        return this.attachments;
    }

    /**
     * Set the attachments property: List of attachments for this message.
     *
     * @param attachments the attachments value to set.
     * @return the ChatMessageContent object itself.
     */
    public ChatMessageContent setAttachments(Iterable<ChatAttachment> attachments) {
        this.attachments = attachments;
        return this;
    }

    /**
     * Get the initiator property: Chat message content for messages of types
     * participantAdded or participantRemoved.
     *
     * @return the initiator value.
     */
    public CommunicationIdentifier getInitiator() {
        return this.initiator;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeStringField("message", message);
        jsonWriter.writeStringField("topic", topic);
        jsonWriter.writeArrayField("participants", participants, (writer, participant) -> participant.toJson(writer));
        jsonWriter.writeArrayField("attachments", attachments, (writer, attachment) -> attachment.toJson(writer));
        // final CommunicationIdentifierModel identifier = CommunicationIdentifierConverter.convert(initiator);
        // jsonWriter.writeJsonField("initiatorCommunicationIdentifier", identifier);
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of ChatMessageContent from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of ChatMessageContent if the JsonReader was pointing to an instance of it, or null
     * if it was pointing to JSON null.
     * @throws IOException If an error occurs while reading the ChatMessageContent.
     */
    public static ChatMessageContent fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            String message = null;
            String topic = null;
            List<ChatParticipant> participants = null;
            List<ChatAttachment> attachments = null;
            CommunicationIdentifier initiator = null;
            while (jsonReader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();
                if ("message".equals(fieldName)) {
                    message = reader.getString();
                } else if ("topic".equals(fieldName)) {
                    topic = reader.getString();
                } else if ("participants".equals(fieldName)) {
                    participants = reader.readArray(ChatParticipant::fromJson);
                } else if ("attachments".equals(fieldName)) {
                    attachments = reader.readArray(ChatAttachment::fromJson);
                } else if ("initiatorCommunicationIdentifier".equals(fieldName)) {
                    final CommunicationIdentifierModel identifier = reader.readObject(CommunicationIdentifierModel::fromJson);
                    initiator = CommunicationIdentifierConverter.convert(identifier);
                } else {
                    reader.skipChildren();
                }
            }
            return new ChatMessageContent(message, topic, participants, initiator)
                .setAttachments(attachments);
        });
    }
}
