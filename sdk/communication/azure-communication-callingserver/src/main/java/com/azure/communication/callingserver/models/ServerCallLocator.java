// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.models;

import com.azure.core.annotation.Immutable;

/** The server call locator. */
@Immutable
public final class ServerCallLocator extends CallLocator {
    /**
     * The server call id.
     */
    private final String serverCallId;

    /**
     * Get the server call id.
     *
     * @return the server call id of the ServerCallLocator object itself
     */
    public String getServerCallId() {
        return serverCallId;
    }

    /**
     * Initializes a new instance of ServerCallLocator
     *
     * @param serverCallId The server call id.
     * @throws IllegalArgumentException if either parameter is null.
     */
    public ServerCallLocator(String serverCallId) {
        if (serverCallId == null) {
            throw new IllegalArgumentException("serverCallId cannot be null");
        }
        this.serverCallId = serverCallId;
    }
}
