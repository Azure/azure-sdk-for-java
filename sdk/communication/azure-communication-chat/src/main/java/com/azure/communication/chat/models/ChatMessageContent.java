// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.chat.models;

import com.azure.communication.chat.implementation.models.ChatParticipant;
import com.azure.core.annotation.Fluent;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/** The ChatMessageContent model. */
@Fluent
public final class ChatMessageContent {
    /*
     * Chat message content for type "text" or "html" messages.
     */
    @JsonProperty(value = "message")
    private String message;

    /*
     * Chat message content for type "topicUpdated" messages.
     */
    @JsonProperty(value = "topic")
    private String topic;

    /*
     * Chat message content for type "participantAdded" or "participantRemoved"
     * messages.
     */
    @JsonProperty(value = "participants")
    private List<ChatParticipant> participants;

    /*
     * Chat message content for type "participantAdded" or "participantRemoved"
     * messages.
     */
    @JsonProperty(value = "initiator")
    private String initiator;

    /**
     * Get the message property: Chat message content for type "text" or "html" messages.
     *
     * @return the message value.
     */
    public String getMessage() {
        return this.message;
    }

    /**
     * Set the message property: Chat message content for type "text" or "html" messages.
     *
     * @param message the message value to set.
     * @return the ChatMessageContent object itself.
     */
    public ChatMessageContent setMessage(String message) {
        this.message = message;
        return this;
    }

    /**
     * Get the topic property: Chat message content for type "topicUpdated" messages.
     *
     * @return the topic value.
     */
    public String getTopic() {
        return this.topic;
    }

    /**
     * Set the topic property: Chat message content for type "topicUpdated" messages.
     *
     * @param topic the topic value to set.
     * @return the ChatMessageContent object itself.
     */
    public ChatMessageContent setTopic(String topic) {
        this.topic = topic;
        return this;
    }

    /**
     * Get the participants property: Chat message content for type "participantAdded" or "participantRemoved" messages.
     *
     * @return the participants value.
     */
    public List<ChatParticipant> getParticipants() {
        return this.participants;
    }

    /**
     * Set the participants property: Chat message content for type "participantAdded" or "participantRemoved" messages.
     *
     * @param participants the participants value to set.
     * @return the ChatMessageContent object itself.
     */
    public ChatMessageContent setParticipants(List<ChatParticipant> participants) {
        this.participants = participants;
        return this;
    }

    /**
     * Get the initiator property: Chat message content for type "participantAdded" or "participantRemoved" messages.
     *
     * @return the initiator value.
     */
    public String getInitiator() {
        return this.initiator;
    }

    /**
     * Set the initiator property: Chat message content for type "participantAdded" or "participantRemoved" messages.
     *
     * @param initiator the initiator value to set.
     * @return the ChatMessageContent object itself.
     */
    public ChatMessageContent setInitiator(String initiator) {
        this.initiator = initiator;
        return this;
    }
}
