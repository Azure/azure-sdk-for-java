// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.models;

/**
 * Options bag that configures session-based authentication for a
 * {@link com.azure.storage.blob.BlobServiceClientBuilder}.
 * <p>
 * Sessions amortize authentication and authorization cost across many requests by signing them
 * with a lightweight HMAC key instead of a full bearer token.
 *
 * @see SessionMode
 */
public final class SessionOptions {

    private SessionMode sessionMode = SessionMode.ENABLED;
    private String containerName;
    private String accountName;
    private SessionProvider sessionProvider;

    /**
     * Creates a new {@link SessionOptions} instance with default values.
     * This only applies to clients created from a {@link com.azure.storage.blob.BlobServiceClientBuilder}
     * configured with a TokenCredential, and to eligible GET Blob operations made by clients derived from
     * that service client.
     */
    public SessionOptions() {
    }

    /**
     * Gets the session mode.
     *
     * @return the {@link SessionMode}; defaults to {@link SessionMode#ENABLED}.
     */
    public SessionMode getSessionMode() {
        return sessionMode;
    }

    /**
     * Sets the session mode. Passing {@code null} resets the mode to {@link SessionMode#ENABLED}.
     *
     * @param sessionMode the {@link SessionMode} to set.
     * @return the updated {@link SessionOptions} object.
     */
    public SessionOptions setSessionMode(SessionMode sessionMode) {
        this.sessionMode = sessionMode == null ? SessionMode.ENABLED : sessionMode;
        return this;
    }

    /**
     * Gets the container name override used when it cannot be resolved from the request URL.
     *
     * @return the container name, or {@code null} if not set.
     */
    public String getContainerName() {
        return containerName;
    }

    /**
     * Sets the container name override used when it cannot be resolved from the request URL.
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
     * HMAC request signing, bearer-token fallback, and account-level acquisition cooldown.
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
}
