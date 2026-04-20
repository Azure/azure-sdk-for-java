// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.models;

/**
 * Options bag that configures session-based authentication on blob storage builders.
 * <p>
 * Sessions amortize authentication and authorization cost across many requests by signing them
 * with a lightweight HMAC key instead of a full bearer token.
 *
 * @see SessionMode
 */
public final class SessionOptions {

    private SessionMode sessionMode;

    /**
     * Creates a new {@link SessionOptions} instance with default values.
     */
    public SessionOptions() {
    }

    /**
     * Gets the session mode.
     *
     * @return the {@link SessionMode}, or {@code null} if not set.
     */
    public SessionMode getSessionMode() {
        return sessionMode;
    }

    /**
     * Sets the session mode.
     *
     * @param sessionMode the {@link SessionMode} to set.
     * @return the updated {@link SessionOptions} object.
     */
    public SessionOptions setSessionMode(SessionMode sessionMode) {
        this.sessionMode = sessionMode;
        return this;
    }
}
