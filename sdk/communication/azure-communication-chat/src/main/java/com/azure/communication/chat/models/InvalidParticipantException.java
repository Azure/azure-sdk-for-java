// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.chat.models;

import com.azure.core.exception.AzureException;

public class InvalidParticipantException extends AzureException {

    private ChatError chatError;

    public InvalidParticipantException(ChatError chatError) {
        super(chatError.getMessage());
        this.chatError = chatError;
    }

    public ChatError getChatError() {
        return chatError;
    }
}
