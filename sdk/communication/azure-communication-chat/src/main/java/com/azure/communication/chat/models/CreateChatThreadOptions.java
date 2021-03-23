// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.chat.models;

import com.azure.core.annotation.Fluent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** The CreateChatThreadOptions model. */
@Fluent
public final class CreateChatThreadOptions {
    /*
     * The chat thread topic.
     */
    private final String topic;

    /*
     * Members to be added to the chat thread.
     */
    private List<ChatParticipant> participants = new ArrayList<>();

    private String idempotencyToken;

    /**
     * Get the topic property: The chat thread topic.
     *
     * @return the topic value.
     */
    public String getTopic() {
        return this.topic;
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
     * Adds another participant to the list of participants to create the chat thread with
     *
     * @param participant The participant to add
     * @return the CreateChatThreadOptions object itself
     */
    public CreateChatThreadOptions addParticipant(ChatParticipant participant) {
        this.participants.add(participant);
        return this;
    }

    /**
     * Get the idempotencyToken property
     *
     * @return the idempotencyToken.
     */
    public String getIdempotencyToken() {
        return this.idempotencyToken;
    }

    /**
     * Set the idempotencyToken property:
     * If specified, the client directs that the request is repeatable; that is, that the
     * client can make the request multiple times with the same idempotencyToken and get back an appropriate
     * response without the server executing the request multiple times. The value of the idempotencyToken
     * is an opaque string representing a client-generated, globally unique for all time, identifier for the
     * request. It is recommended to use version 4 (random) UUIDs.
     *
     * @param idempotencyToken the idempotencyToken.
     * @return the CreateChatThreadOptions object itself.
     */
    public CreateChatThreadOptions setIdempotencyToken(String idempotencyToken) {
        this.idempotencyToken = idempotencyToken;
        return this;
    }

    /**
     * Creates a new instance of CreateChatThreadOptions
     * @param topic the topic value to set.
     */
    public CreateChatThreadOptions(String topic) {
        this.topic = topic;
        this.idempotencyToken = UUID.randomUUID().toString();
    }
}
