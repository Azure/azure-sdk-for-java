// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.chat.models;

import com.azure.core.annotation.Fluent;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.UUID;

/** The CreateChatThreadOptions model. */
@Fluent
public final class CreateChatThreadOptions {
    /*
     * The chat thread topic.
     */
    @JsonProperty(value = "topic", required = true)
    private String topic;

    /*
     * Members to be added to the chat thread.
     */
    @JsonProperty(value = "participants", required = true)
    private List<ChatParticipant> participants;

    private String repeatabilityRequestId;

    /**
     * Get the topic property: The chat thread topic.
     *
     * @return the topic value.
     */
    public String getTopic() {
        return this.topic;
    }

    /**
     * Set the topic property: The chat thread topic.
     *
     * @param topic the topic value to set.
     * @return the CreateChatThreadOptions object itself.
     */
    public CreateChatThreadOptions setTopic(String topic) {
        this.topic = topic;
        return this;
    }

    /**
     * Get the participants property: Participants to be added to the chat thread.
     *
     * @return the participants value.
     */
    public List<ChatParticipant> getParticipants() {
        return this.participants;
    }

    /**
     * Set the participants property: Participants to be added to the chat thread.
     *
     * @param participants the participants value to set.
     * @return the CreateChatThreadOptions object itself.
     */
    public CreateChatThreadOptions setParticipants(List<ChatParticipant> participants) {
        this.participants = participants;
        return this;
    }

    /**
     * Get the repeatabilityRequestID property
     *
     * @return the repeatabilityRequestID.
     */
    public String getRepeatabilityRequestId() {
        return this.repeatabilityRequestId;
    }

    /**
     * Set the repeatabilityRequestID property: If specified, the client directs that the request is repeatable;
     * that is, that the client can make the request multiple times with the same Repeatability-Request-ID
     * and get back an appropriate response without the server executing the request multiple times.
     * The value of the Repeatability-Request-ID is an opaque string representing a client-generated,
     * globally unique for all time, identifier for the request.
     * It is recommended to use version 4 (random) UUIDs.
     *
     * @param repeatabilityRequestId the repeatabilityRequestID.
     * @return the CreateChatThreadOptions object itself.
     */
    public CreateChatThreadOptions setRepeatabilityRequestId(String repeatabilityRequestId) {
        this.repeatabilityRequestId = repeatabilityRequestId;
        return this;
    }

    /**
     * Creates a new instance of CreateChatThreadOptions
     */
    public CreateChatThreadOptions() {
        this.repeatabilityRequestId = UUID.randomUUID().toString();
    }
}
