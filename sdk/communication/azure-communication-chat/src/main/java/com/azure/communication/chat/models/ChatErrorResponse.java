// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.chat.models;

import com.azure.core.annotation.Fluent;

/** The Communication Services error. */
@Fluent
public final class ChatErrorResponse {

    private ChatError error;

    /**
     * Get the error property: The Chat Services error.
     *
     * @return the error value.
     */
    public ChatError getError() {
        return this.error;
    }

    /**
     * Set the error property: The Chat Services error.
     *
     * @param error the error value to set.
     * @return the ChatErrorResponse object itself.
     */
    public ChatErrorResponse setError(ChatError error) {
        this.error = error;
        return this;
    }
}
