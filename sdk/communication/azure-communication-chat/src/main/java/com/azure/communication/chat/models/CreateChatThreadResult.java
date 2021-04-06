// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.chat.models;

import com.azure.core.annotation.Fluent;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/** The CreateChatThreadResult model. */
@Fluent
public final class CreateChatThreadResult {
    /*
     * The thread property.
     */
    @JsonProperty(value = "chatThread")
    private ChatThreadProperties chatThreadProperties;

    /*
     * The participants that failed to be added to the chat thread.
     */
    @JsonProperty(value = "invalidParticipants", access = JsonProperty.Access.WRITE_ONLY)
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

}
