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

    private SessionMode sessionMode = SessionMode.AUTO;
    private String containerName;
    private String accountName;

    /**
     * Creates a new {@link SessionOptions} instance with default values.
     */
    public SessionOptions() {
    }

    /**
     * Returns {@code options} if non-null, otherwise a freshly constructed {@link SessionOptions}
     * with default values. Use this helper instead of inlining {@code opts != null ? opts : new SessionOptions()}
     * so default construction stays in one place.
     *
     * @param options the options instance to validate; may be {@code null}.
     * @return {@code options} if non-null; a new default {@link SessionOptions} otherwise.
     */
    public static SessionOptions orDefault(SessionOptions options) {
        return options != null ? options : new SessionOptions();
    }

    /**
     * Gets the session mode.
     *
     * @return the {@link SessionMode}; defaults to {@link SessionMode#AUTO}.
     */
    public SessionMode getSessionMode() {
        return sessionMode;
    }

    /**
     * Sets the session mode. Passing {@code null} resets the mode to {@link SessionMode#AUTO}.
     *
     * @param sessionMode the {@link SessionMode} to set.
     * @return the updated {@link SessionOptions} object.
     */
    public SessionOptions setSessionMode(SessionMode sessionMode) {
        this.sessionMode = sessionMode == null ? SessionMode.AUTO : sessionMode;
        return this;
    }

    /**
     * Gets the container name that the session is scoped to.
     *
     * @return the container name, or {@code null} if not set.
     */
    public String getContainerName() {
        return containerName;
    }

    /**
     * Sets the container name that the session is scoped to. This is required when the session mode
     * is not {@link SessionMode#NONE}.
     *
     * @param containerName the container name.
     * @return the updated {@link SessionOptions} object.
     */
    public SessionOptions setContainerName(String containerName) {
        this.containerName = containerName;
        return this;
    }

    /**
     * Gets the storage account name used for session HMAC signing.
     *
     * @return the account name, or {@code null} if not set (will be parsed from the endpoint URL).
     */
    public String getAccountName() {
        return accountName;
    }

    /**
     * Sets the storage account name used for session HMAC signing. When set, this takes precedence
     * over the account name parsed from the endpoint URL. This is useful for custom domain URLs
     * where the account name cannot be inferred from the hostname.
     *
     * @param accountName the storage account name.
     * @return the updated {@link SessionOptions} object.
     */
    public SessionOptions setAccountName(String accountName) {
        this.accountName = accountName;
        return this;
    }
}
