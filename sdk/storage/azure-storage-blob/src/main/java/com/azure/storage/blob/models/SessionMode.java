// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.models;

/**
 * Defines whether the SDK uses session-based authentication when sending requests to a container.
 * <p>
 * A session is a temporary security context scoped to a container that amortizes authentication
 * and authorization cost across many requests by signing them with a lightweight HMAC key instead
 * of a full bearer token.
 * {@link #ENABLED}
 * {@link #DISABLED}
 */
public enum SessionMode {

    /**
     * The SDK creates a session on the first eligible request and, when using the built-in session provider,
     * keeps an active session until it receives no requests for 5 minutes. This is the default. If a session
     * cannot be created, or the service answers a session-signed request with HTTP 400 or 401, the SDK
     * transparently falls back to bearer token authentication for that request. Repeated failures stop the
     * SDK from using sessions for that account for five minutes; during that window its requests are
     * authenticated with bearer tokens without attempting to create a session.
     */
    ENABLED,

    /**
     * Always use bearer token authentication. No session tokens are ever created or used.
     */
    DISABLED

}
