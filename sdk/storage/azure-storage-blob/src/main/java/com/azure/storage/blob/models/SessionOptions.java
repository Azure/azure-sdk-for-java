// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.models;

/**
 * Options bag that configures session-based authentication for a
 * {@link com.azure.storage.blob.BlobServiceClientBuilder}, {@link com.azure.storage.blob.BlobContainerClientBuilder},
 * {@link com.azure.storage.blob.BlobClientBuilder}, and
 * {@link com.azure.storage.blob.specialized.SpecializedBlobClientBuilder}.
 * <p>
 * Sessions amortize authentication and authorization cost across many requests by signing them
 * with a lightweight HMAC key instead of a full bearer token.
 *
 * @see SessionOptions.SessionMode
 */
public final class SessionOptions {

    private SessionMode sessionMode = SessionMode.AUTO;
    private String accountName;
    private SessionProvider sessionProvider;

    /**
     * Creates a new {@link SessionOptions} instance with {@link SessionMode#AUTO}, which currently disables sessions.
     * This applies to clients configured with a {@link com.azure.core.credential.TokenCredential} and to eligible GET
     * Blob operations.
     */
    public SessionOptions() {
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

    /**
     * Gets the custom provider used to obtain session credentials.
     *
     * @return the custom {@link SessionProvider}, or {@code null} to use the built-in provider.
     */
    public SessionProvider getSessionProvider() {
        return sessionProvider;
    }

    /**
     * Sets the custom provider used to obtain session credentials. When set, the provider is called directly
     * for each eligible request: the SDK does not layer additional caching on top of a custom provider, so
     * the provider is responsible for its own caching and refresh strategy. The SDK retains ownership of
     * HMAC request signing, of choosing between session and bearer authentication, and of pausing session use
     * for a storage account when sessions repeatedly fail against it, as described on {@link SessionProvider}.
     * The same provider instance may be supplied to multiple service client builders to share its cache; that
     * pause, however, is tracked per client pipeline and is not shared by those clients.
     * When {@code null}, the built-in provider is used, which calls the storage service's CreateSession REST
     * API and manages per-container credential caching, proactive refresh, and idle eviction automatically.
     *
     * @param sessionProvider the custom {@link SessionProvider}, or {@code null} to use the built-in provider.
     * @return the updated {@link SessionOptions} object.
     */
    public SessionOptions setSessionProvider(SessionProvider sessionProvider) {
        this.sessionProvider = sessionProvider;
        return this;
    }

    /**
     * Defines whether the SDK uses session-based authentication when sending requests to a container.
     * <p>
     * A session is a temporary security context scoped to a container that amortizes authentication
     * and authorization cost across many requests by signing them with a lightweight HMAC key instead
     * of a full bearer token.
     */
    public enum SessionMode {

        /**
         * Default. The client library chooses the session authentication behavior; currently, this resolves to
         * {@link #DISABLED}, so sessions are opt-in. The default may change in future releases.
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
}
