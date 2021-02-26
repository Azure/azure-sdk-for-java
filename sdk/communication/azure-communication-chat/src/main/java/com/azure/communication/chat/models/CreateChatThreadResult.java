// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.chat.models;

import com.azure.core.annotation.Fluent;
import com.fasterxml.jackson.annotation.JsonProperty;

/** The CreateChatThreadResult model. */
@Fluent
public final class CreateChatThreadResult {
    /*
     * The thread property.
     */
    @JsonProperty(value = "chatThread")
    private ChatThread chatThread;

    /*
     * The errors property.
     */
    @JsonProperty(value = "errors")
    private CreateChatThreadErrors errors;

    /**
     * Constructs a new instance of CreateChatThreadResult
     * @param chatThread The chat thread that was created.
     * @param errors List of errors that occurred when attempting to create the chat thread.
     */
    public CreateChatThreadResult(ChatThread chatThread, CreateChatThreadErrors errors) {
        this.chatThread = chatThread;
        this.errors = errors;
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
     * Get the errors property: The errors property.
     *
     * @return the errors value.
     */
    public CreateChatThreadErrors getErrors() {
        return this.errors;
    }

}
