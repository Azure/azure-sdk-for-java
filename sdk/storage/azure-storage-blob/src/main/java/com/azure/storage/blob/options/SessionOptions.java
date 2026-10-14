// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.options;

import com.azure.core.credential.TokenCredential;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.implementation.accesshelpers.SessionOptionsAccessor;

import java.util.Objects;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

/**
 * Configures session-based authentication for Blob and Data Lake clients.
 *
 * @see SessionOptions.SessionMode
 */
public final class SessionOptions {

    private static final ClientLogger LOGGER = new ClientLogger(SessionOptions.class);

    private SessionMode sessionMode = SessionMode.AUTO;
    private String accountName;
    private final String endpoint;
    private final TokenCredential credential;
    private final Lock sessionClientLock = new ReentrantLock();
    private Object sessionClient;

    static {
        SessionOptionsAccessor.setAccessor(new SessionOptionsAccessor.SessionOptionsAccessorImpl() {
            @Override
            public String getEndpoint(SessionOptions options) {
                return options.endpoint;
            }

            @Override
            public TokenCredential getCredential(SessionOptions options) {
                return options.credential;
            }

            @Override
            public <T> T getOrCreateSessionClient(SessionOptions options, Class<T> clientType,
                Supplier<? extends T> clientFactory) {
                return options.getOrCreateSessionClient(clientType, clientFactory);
            }
        });
    }

    /**
     * Creates session options in {@link SessionMode#AUTO} mode.
     */
    public SessionOptions() {
        this.endpoint = null;
        this.credential = null;
    }

    /**
     * Creates session options with the endpoint and token credential used to acquire session credentials.
     * <p>
     * Sessions remain disabled unless {@link #setSessionMode(SessionMode)} is set to
     * {@link SessionMode#ENABLED}. Reuse this options instance across clients to share acquired session credentials.
     *
     * @param endpoint The storage account endpoint used to acquire sessions.
     * @param credential The token credential used to acquire sessions.
     * @throws NullPointerException if {@code endpoint} or {@code credential} is null.
     */
    public SessionOptions(String endpoint, TokenCredential credential) {
        this.endpoint = Objects.requireNonNull(endpoint, "'endpoint' cannot be null.");
        this.credential = Objects.requireNonNull(credential, "'credential' cannot be null.");
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

    <T> T getOrCreateSessionClient(Class<T> clientType, Supplier<? extends T> clientFactory) {
        sessionClientLock.lock();
        try {
            if (sessionClient == null) {
                sessionClient = Objects.requireNonNull(clientFactory.get(), "'clientFactory' returned null.");
            }
            if (!clientType.isInstance(sessionClient)) {
                throw LOGGER.logExceptionAsError(
                    new IllegalStateException("Session client was initialized with an incompatible type."));
            }
            return clientType.cast(sessionClient);
        } finally {
            sessionClientLock.unlock();
        }
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
         * {@link TokenCredential}. Session authentication requires an account name, which is
         * determined from the endpoint or {@link SessionOptions#getAccountName()}.
         */
        ENABLED
    }
}
