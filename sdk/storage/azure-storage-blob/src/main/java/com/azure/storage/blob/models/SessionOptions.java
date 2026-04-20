// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.models;

import com.azure.storage.blob.BlobServiceVersion;

/**
 * Options bag that groups session-related parameters for configuring session-based authentication
 * on blob storage builders.
 * <p>
 * Sessions amortize authentication and authorization cost across many requests by signing them
 * with a lightweight HMAC key instead of a full bearer token.
 *
 * @see SessionMode
 */
public final class SessionOptions {

    private SessionMode sessionMode;
    private String containerName;
    private BlobServiceVersion serviceVersion;

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

    /**
     * Gets the container name used for session creation.
     *
     * @return the container name, or {@code null} if not set.
     */
    public String getContainerName() {
        return containerName;
    }

    /**
     * Sets the container name used for session creation.
     *
     * @param containerName the container name.
     * @return the updated {@link SessionOptions} object.
     */
    public SessionOptions setContainerName(String containerName) {
        this.containerName = containerName;
        return this;
    }

    /**
     * Gets the service version used for session creation.
     *
     * @return the {@link BlobServiceVersion}, or {@code null} if not set.
     */
    public BlobServiceVersion getServiceVersion() {
        return serviceVersion;
    }

    /**
     * Sets the service version used for session creation.
     *
     * @param serviceVersion the {@link BlobServiceVersion}.
     * @return the updated {@link SessionOptions} object.
     */
    public SessionOptions setServiceVersion(BlobServiceVersion serviceVersion) {
        this.serviceVersion = serviceVersion;
        return this;
    }
}
