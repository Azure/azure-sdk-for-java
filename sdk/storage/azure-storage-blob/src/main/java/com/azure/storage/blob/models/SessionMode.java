// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.models;

/**
 * Defines the session management strategy used by the SDK when sending requests to a container.
 * <p>
 * A session is a temporary security context scoped to a container that amortizes authentication
 * and authorization cost across many requests by signing them with a lightweight HMAC key instead
 * of a full bearer token.
 * {@link #NONE}
 * {@link #SINGLE_SPECIFIED_CONTAINER}
 * {@link #AUTO}
 */
public enum SessionMode {

    /**
     * Always use bearer token authentication. No session tokens are used.
     */
    NONE,

    /**
     * Default behavior. This is currently equivalent to {@link #NONE}
     */
    AUTO,

    /**
     * The SDK creates a session on the first request and keeps an active session until it
     * receives no requests for 5 minutes.
     */
    SINGLE_SPECIFIED_CONTAINER;

    /**
     * Resolves {@link #AUTO} to its current effective mode. Today {@code AUTO} maps to
     * {@link #NONE}; this may change in a future release without breaking callers that
     * use {@code resolve()} consistently.
     * @return returns the effective session mode, never {@code AUTO}
     */
    public SessionMode resolve() {
        return this == AUTO ? NONE : this;
    }

}
