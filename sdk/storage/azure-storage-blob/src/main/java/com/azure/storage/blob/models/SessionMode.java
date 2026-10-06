// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.models;

/**
 * Defines whether the SDK uses session-based authentication when sending requests to a container.
 * <p>
 * A session is a temporary security context scoped to a container that amortizes authentication
 * and authorization cost across many requests by signing them with a lightweight HMAC key instead
 * of a full bearer token.
 * {@link #AUTO}
 * {@link #ENABLED}
 * {@link #DISABLED}
 */
public enum SessionMode {

    /**
     * Default. The session authentication behavior is determined by the client library
     * and may be updated in future releases.
     */
    AUTO,

    /**
     * Always use bearer token authentication. No session tokens are used.
     */
    DISABLED,

    /**
     * Opt in to session token authentication for all containers.
     * Each container gets its own cached session token when using the built-in session provider.
     * Requires a storage account name; client construction throws if one cannot be
     * determined from either {@link SessionOptions#getAccountName()} or the client endpoint.
     */
    ENABLED

}
