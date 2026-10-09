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
     * Session authentication must be enabled explicitly and applies to eligible GET Blob operations on clients
     * configured with a {@link com.azure.core.credential.TokenCredential}.
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
     * Gets the storage account name supplied to the session provider.
     *
     * @return the account name, or {@code null} if not set (will be parsed from the endpoint URL).
     */
    public String getAccountName() {
        return accountName;
    }

    /**
     * Sets the storage account name supplied to the session provider. When set, this takes precedence
     * over the account name parsed from the endpoint URL. This is useful for custom domain URLs
     * where the account name cannot be inferred from the hostname. This override is used for signing with both
     * automatically created and explicitly supplied providers.
     *
     * @param accountName the storage account name.
     * @return the updated {@link SessionOptions} object.
     */
    public SessionOptions setAccountName(String accountName) {
        this.accountName = accountName;
        return this;
    }

    /**
     * Gets the SDK provider used to share sessions.
     *
     * @return the supplied {@link SessionProvider}, or {@code null} to create a provider for this client hierarchy.
     */
    public SessionProvider getSessionProvider() {
        return sessionProvider;
    }

    /**
     * Sets an SDK-owned provider, such as {@link ContainerSessionProvider}, to share sessions across independent
     * Blob and Data Lake clients. The instance is reused without copying or adding a cache. Its per-container
     * credentials, refresh state, and five-minute acquisition cooldowns are shared by all clients using it.
     * A rejected session is invalidated only if current and falls back to bearer for that request without a cooldown.
     * All clients must use the provider's account endpoint and network context.
     * When {@code null}, the SDK creates a provider for this client hierarchy, automatically reusing its transport
     * and policies with secret-safe logging. Providers cannot be implemented by applications.
     *
     * @param sessionProvider the SDK provider, or {@code null} to create a provider for this client hierarchy.
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
         * Disable session token authentication. Clients configured with a
         * {@link com.azure.core.credential.TokenCredential} use bearer token authentication.
         */
        DISABLED,

        /**
         * Opt in to session token authentication for eligible requests across all containers on clients
         * configured with a {@link com.azure.core.credential.TokenCredential}.
         * Each container gets its own cached session token when using the built-in session provider.
         * The built-in provider requires a storage account name; client construction throws if one cannot be
         * determined from either {@link SessionOptions#getAccountName()} or the client endpoint.
         * For custom domains with a supplied provider, set {@link SessionOptions#setAccountName(String)} as well.
         */
        ENABLED
    }
}
