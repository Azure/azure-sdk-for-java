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
     * The SDK creates a session on the first eligible request and keeps an active session until it
     * receives no requests for 5 minutes. This is the default. If session creation or use fails for
     * any reason, the SDK transparently falls back to bearer token authentication.
     */
    ENABLED,

    /**
     * Always use bearer token authentication. No session tokens are ever created or used.
     */
    DISABLED

}
