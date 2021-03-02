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
    private ChatThread chatThread;

    /*
     * The participants that failed to be added to the chat thread.
     */
    @JsonProperty(value = "invalidParticipants", access = JsonProperty.Access.WRITE_ONLY)
    private List<CommunicationError> invalidParticipants;

    /**
     * Constructs a new instance of CreateChatThreadResult
     * @param chatThread The chat thread that was created.
     * @param invalidParticipants List of errors that occurred when attempting to create the chat thread.
     */
    public CreateChatThreadResult(ChatThread chatThread, List<CommunicationError> invalidParticipants) {
        this.chatThread = chatThread;
        this.invalidParticipants = invalidParticipants;
    }

    /**
     * Get the chatThread property: The chatThread property.
     *
     * @return the chatThread value.
     */
    public ChatThread getChatThread() {
        return this.chatThread;
    }

    /**
     * Get the invalidParticipants property: The participants that failed to be added to the chat thread.
     *
     * @return the invalidParticipants value.
     */
    public List<CommunicationError> getInvalidParticipants() {
        return this.invalidParticipants;
    }

}
