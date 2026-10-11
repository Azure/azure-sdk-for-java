// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.models;

/**
 * Configures session-based authentication for Blob and Data Lake clients.
 *
 * @see SessionOptions.SessionMode
 */
public final class SessionOptions {

    private SessionMode sessionMode = SessionMode.AUTO;
    private String accountName;
    private SessionProvider sessionProvider;

    /**
     * Creates session options in {@link SessionMode#AUTO} mode.
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
     * Gets the storage account name used for session signing.
     *
     * @return the account name, or {@code null} if it will be determined from the endpoint.
     */
    public String getAccountName() {
        return accountName;
    }

    /**
     * Sets the storage account name used for session signing. This overrides the name parsed from the endpoint and is
     * useful with custom domains.
     *
     * @param accountName the storage account name.
     * @return the updated {@link SessionOptions} object.
     */
    public SessionOptions setAccountName(String accountName) {
        this.accountName = accountName;
        return this;
    }

    /**
     * Gets the provider used by this client.
     *
     * @return the supplied provider, or {@code null} if the SDK will create one.
     */
    public SessionProvider getSessionProvider() {
        return sessionProvider;
    }

    /**
     * Sets an SDK-owned provider, such as {@link ContainerSessionProvider}. Share it across Blob or Data Lake clients
     * using the same account endpoint and network context to reuse session state. When {@code null}, the SDK creates a
     * provider for this client hierarchy.
     * <p>
     * Supplying a provider does not enable sessions; also select {@link SessionMode#ENABLED}. The built-in provider
     * uses the default HTTP transport and client configuration to acquire sessions.
     *
     * @param sessionProvider the SDK provider, or {@code null} to create a provider for this client hierarchy.
     * @return the updated {@link SessionOptions} object.
     */
    public SessionOptions setSessionProvider(SessionProvider sessionProvider) {
        this.sessionProvider = sessionProvider;
        return this;
    }

    /**
     * Defines whether clients use session-based authentication for eligible requests.
     */
    public enum SessionMode {

        /**
         * Default behavior. Currently resolves to {@link #DISABLED}; this may change in a future release.
         */
        AUTO,

        /**
         * Disables session authentication. Clients configured with a
         * {@link com.azure.core.credential.TokenCredential} use bearer-token authentication.
         */
        DISABLED,

        /**
         * Enables session authentication for eligible requests on clients configured with a
         * {@link com.azure.core.credential.TokenCredential}. The built-in provider requires an account name, which is
         * determined from the endpoint or {@link SessionOptions#getAccountName()}.
         */
        ENABLED
    }
}
