// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.models;

import com.azure.core.annotation.Immutable;

/** The server call locator. */
@Immutable
public final class ServerCallLocator extends CallLocator {
    /**
     * Get the server call id.
     *
     * @return the server call id of the ServerCallLocator object itself
     */
    public String getServerCallId() {
        return id;
    }

    /**
     * Initializes a new instance of ServerCallLocator
     *
     * @param id The server call id.
     * @throws IllegalArgumentException if either parameter is null.
     */
    public ServerCallLocator(String id) {
        if (id == null) {
            throw new IllegalArgumentException("serverCallId cannot be null");
        }
        this.id = id;
    }
}
