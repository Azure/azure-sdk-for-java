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
     * Get the chatThread property: The chatThread property.
     *
     * @return the chatThread value.
     */
    public ChatThread getChatThread() {
        return this.chatThread;
    }

    /**
     * Set the chatThread property: The chatThread property.
     *
     * @param chatThread the thread value to set.
     * @return the CreateChatThreadResult object itself.
     */
    public CreateChatThreadResult setChatThread(ChatThread chatThread) {
        this.chatThread = chatThread;
        return this;
    }

    /**
     * Get the errors property: The errors property.
     *
     * @return the errors value.
     */
    public CreateChatThreadErrors getErrors() {
        return this.errors;
    }

    /**
     * Set the errors property: The errors property.
     *
     * @param errors the errors value to set.
     * @return the CreateChatThreadResult object itself.
     */
    public CreateChatThreadResult setErrors(CreateChatThreadErrors errors) {
        this.errors = errors;
        return this;
    }
}
