// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.core.credential.TokenCredential;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.implementation.accesshelpers.SessionContextAccessor;

import java.util.Objects;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

/**
 * Configures session-based authentication for Blob and Data Lake clients.
 * <p>
 * The context is applied when a client builder constructs a pipeline with a {@link TokenCredential}; a supplied
 * {@link com.azure.core.http.HttpPipeline} is used unchanged. The default mode is {@link SessionMode#AUTO}, which
 * currently disables sessions. Set {@link #setSessionMode(SessionMode)} to {@link SessionMode#ENABLED} to enable them.
 * Session authentication applies only to eligible GET blob requests. Other requests, and requests for which session
 * acquisition fails, use bearer token authentication. Session-signed requests rejected with HTTP 401 are retried with
 * bearer token authentication. Data Lake clients use sessions only through their underlying Blob pipeline, not the DFS
 * pipeline.
 * <p>
 * Session-enabled clients built with the same context instance share a session cache, including clients built
 * later. Reuse this instance across clients targeting the same endpoint with compatible identity and network
 * contexts to reuse cached sessions. Each client acquires sessions through its own pipeline.
 * <p>
 * By default, sessions are acquired using the consuming client's endpoint and credential.
 * {@link #SessionContext(String, TokenCredential)} overrides only the endpoint and credential used for session
 * acquisition; the builder must still be configured with a {@link TokenCredential}. Other requests continue to use the
 * consuming client's settings.
 * <p>
 * Clients capture the session mode and account name when built. Changing these settings affects clients built
 * afterward, not existing clients. The session cache remains shared; it is not copied when a client is built.
 * Use separate context instances for independent caches.
 *
 * <p><strong>Share sessions across independently built clients</strong></p>
 * <!-- src_embed com.azure.storage.blob.SessionContext.create -->
 * <pre>
 * String endpoint = &quot;https:&#47;&#47;account.blob.core.windows.net&quot;;
 * SessionContext sessionContext = new SessionContext&#40;endpoint, credential&#41;
 *     .setSessionMode&#40;SessionContext.SessionMode.ENABLED&#41;;
 *
 * BlobServiceClient firstClient = new BlobServiceClientBuilder&#40;&#41;.endpoint&#40;endpoint&#41;.credential&#40;credential&#41;
 *     .httpClient&#40;httpClient&#41;.sessionContext&#40;sessionContext&#41;.buildClient&#40;&#41;;
 * BlobServiceClient secondClient = new BlobServiceClientBuilder&#40;&#41;.endpoint&#40;endpoint&#41;.credential&#40;credential&#41;
 *     .httpClient&#40;httpClient&#41;.sessionContext&#40;sessionContext&#41;.buildClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.storage.blob.SessionContext.create -->
 *
 * @see SessionContext.SessionMode
 */
public final class SessionContext {

    private static final ClientLogger LOGGER = new ClientLogger(SessionContext.class);

    private SessionMode sessionMode = SessionMode.AUTO;
    private String accountName;
    private final String endpoint;
    private final TokenCredential credential;
    private final Lock sessionProviderLock = new ReentrantLock();
    private Object sessionProvider;

    static {
        SessionContextAccessor.setAccessor(new SessionContextAccessor.SessionContextAccessorImpl() {
            @Override
            public String getEndpoint(SessionContext context) {
                return context.endpoint;
            }

            @Override
            public TokenCredential getCredential(SessionContext context) {
                return context.credential;
            }

            @Override
            public <T> T getOrCreateSessionProvider(SessionContext context, Class<T> providerType,
                Supplier<? extends T> providerFactory) {
                return context.getOrCreateSessionProvider(providerType, providerFactory);
            }
        });
    }

    /**
     * Creates a session context in {@link SessionMode#AUTO} mode.
     * Session-enabled clients using this instance share a session cache. Each client acquires sessions through
     * its own pipeline. Share this context only across clients with compatible identity and network contexts.
     */
    public SessionContext() {
        this.endpoint = null;
        this.credential = null;
    }

    /**
     * Creates a session context with the endpoint and token credential used to acquire session credentials.
     * <p>
     * Sessions are disabled unless the mode is set to {@link SessionMode#ENABLED}. These values override only
     * the acquisition endpoint and credential; CreateSession requests still go through the consuming client's
     * pipeline, and other requests use the consuming client's settings. The builder must still be configured with a
     * {@link TokenCredential}; supplying a credential here does not replace the builder's credential.
     * Clients using this instance share a session cache, so share the context only across clients with compatible
     * identity and network contexts. Use separate instances for independent caches.
     *
     * @param endpoint The storage account endpoint used to acquire sessions.
     * @param credential The token credential used to acquire sessions.
     * @throws NullPointerException if {@code endpoint} or {@code credential} is null.
     */
    public SessionContext(String endpoint, TokenCredential credential) {
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
     * Changes affect clients built afterward, not existing clients.
     *
     * @param sessionMode the {@link SessionMode} to set.
     * @return the updated {@link SessionContext} object.
     */
    public SessionContext setSessionMode(SessionMode sessionMode) {
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
     * required with custom domains. If unset, the name is inferred from a recognized Blob or Data Lake service endpoint
     * when the client is built.
     * Changes affect clients built afterward, not existing clients.
     *
     * @param accountName the storage account name.
     * @return the updated {@link SessionContext} object.
     */
    public SessionContext setAccountName(String accountName) {
        this.accountName = accountName;
        return this;
    }

    <T> T getOrCreateSessionProvider(Class<T> providerType, Supplier<? extends T> providerFactory) {
        sessionProviderLock.lock();
        try {
            if (sessionProvider == null) {
                sessionProvider = Objects.requireNonNull(providerFactory.get(), "'providerFactory' returned null.");
            }
            if (!providerType.isInstance(sessionProvider)) {
                throw LOGGER.logExceptionAsError(
                    new IllegalStateException("Session provider was initialized with an incompatible type."));
            }
            return providerType.cast(sessionProvider);
        } finally {
            sessionProviderLock.unlock();
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
         * determined from the endpoint or {@link SessionContext#getAccountName()}.
         */
        ENABLED
    }
}
