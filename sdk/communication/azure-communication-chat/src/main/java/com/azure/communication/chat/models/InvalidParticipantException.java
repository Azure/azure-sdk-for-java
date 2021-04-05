// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.chat.models;

import com.azure.core.exception.AzureException;

/**
 * Exception when a participant cannot be added to a chat thread
 */
public final class InvalidParticipantException extends AzureException {

    private final transient ChatError chatError;

    /**
     * Constructs a new InvalidParticipantException
     *
     * @param chatError the ChatError underlying this exception
     */
    public InvalidParticipantException(ChatError chatError) {
        super(chatError.getMessage());
        this.chatError = chatError;
    }

    /**
     * Gets the underlying ChatError returned from the server
     *
     * @return the CHatError
     */
    public ChatError getChatError() {
        return chatError;
    }
}
