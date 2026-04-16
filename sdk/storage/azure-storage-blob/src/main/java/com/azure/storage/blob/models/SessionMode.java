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
 * {@link #ALWAYS}
 * {@link #AUTO}
 */
public enum SessionMode {

    /**
     * The SDK never implicitly creates sessions. Use this mode when calling Create Session
     * explicitly or when sending a very small number of requests where the overhead of an
     * extra round-trip is not justified.
     */
    NONE,

    /**
     * The SDK creates a session on the first request and keeps an active session until it
     * receives no requests for 5 minutes.
     */
    ALWAYS,

    /**
     * The SDK creates a session on the second request and keeps an active session until it
     * receives no requests for 5 minutes. This avoids the overhead of session creation for
     * one-shot operations while still benefiting from sessions for repeated access.
     * <p>
     * This is the default mode.
     */
    AUTO
}
