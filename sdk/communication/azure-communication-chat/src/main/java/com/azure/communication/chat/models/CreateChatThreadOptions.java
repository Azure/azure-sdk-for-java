// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.chat.models;

import com.azure.core.annotation.Fluent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * The CreateChatThreadOptions model.
 */
@Fluent
public final class CreateChatThreadOptions {
    /**
     * The chat thread topic.
     */
    private final String topic;

    /**
     * Members to be added to the chat thread.
     */
    private List<ChatParticipant> participants = new ArrayList<>();

    /**
    * Property bag of chat thread metadata key - value pairs.
    */
    private Map<String, String> metadata = new HashMap<>();

    /**
    * Thread retention policy
    */
    private ChatRetentionPolicy retentionPolicy;

    private String idempotencyToken;

    /**
     * Gets the chat thread topic.
     *
     * @return the topic.
     */
    public String getTopic() {
        return this.topic;
    }

    /**
     * Gets participants to be added to the chat thread.
     *
     * @return the participants.
     */
    public List<ChatParticipant> getParticipants() {
        return this.participants;
    }

    /**
     * Gets participants to be added to the chat thread.
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
     * @return the CreateChatThreadOptions object itself.
     */
    public CreateChatThreadOptions setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
        return this;
    }

    /**
     * Gets the thread retention policy.
     *
     * @return the retentionPolicy value.
     */
    public ChatRetentionPolicy getRetentionPolicy() {
        return this.retentionPolicy;
    }

    /**
     * Sets the thread retention policy.
     *
     * @param retentionPolicy the retention policy to set.
     * @return the CreateChatThreadOptions object itself.
     */
    public CreateChatThreadOptions setRetentionPolicy(ChatRetentionPolicy retentionPolicy) {
        this.retentionPolicy = retentionPolicy;
        return this;
    }

    /**
     * Gets the idempotencyToken property
     *
     * @return the idempotencyToken.
     */
    public String getIdempotencyToken() {
        return this.idempotencyToken;
    }

    /**
     * Sets the idempotencyToken property:
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
     * Creates a new instance of CreateChatThreadOptions.
     * @param topic the topic value to set.
     */
    public CreateChatThreadOptions(String topic) {
        this.topic = topic;
        this.idempotencyToken = UUID.randomUUID().toString();
    }
}
