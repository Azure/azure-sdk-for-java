// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.client.models;

import com.azure.core.annotation.Immutable;

/**
 * The error from AckMessage.
 */
@Immutable
public final class AckMessageError {

    private final String name;
    private final String message;

    /**
     * Creates a new instance of AckMessageError.
     *
     * @param name the name of the error.
     * @param message the error message.
     */
    public AckMessageError(String name, String message) {
        this.name = name;
        this.message = message;
    }

    /**
     * Gets the name of the error.
     *
     * @return the name of the error.
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the error message.
     *
     * @return the error message.
     */
    public String getMessage() {
        return message;
    }
}
