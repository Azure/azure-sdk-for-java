// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.models;

import reactor.core.publisher.Mono;

/**
 * Provides and manages cached {@link SessionCredential session credentials} for storage containers.
 * <p>
 * Implementations create, invalidate, and refresh credentials. The SDK uses the provider to sign eligible
 * requests and falls back to bearer authentication when a session cannot be used. Configure a provider with
 * {@link SessionOptions#setSessionProvider(SessionProvider)}.
 * <ol>
 * <li>{@link #getSession} and {@link #getSessionAsync} return a credential, creating or refreshing it as needed.</li>
 * <li>{@link #invalidateSession} evicts a credential rejected by the service.</li>
 * <li>{@link #refreshSession} proactively refreshes a credential when it is expiring.</li>
 * </ol>
 *
 * Implementations must be thread-safe. Methods may be called concurrently for different containers, identified
 * by the {@link SessionRequestContext} passed to each method. {@link #invalidateSession} must compare and
 * invalidate atomically, and {@link #refreshSession} must return without waiting for the refresh to complete.
 * <p>
 * Synchronous blob clients call {@link #getSession}; asynchronous blob clients call {@link #getSessionAsync}.
 * The default synchronous method blocks on the asynchronous method, so implementations only need to provide
 * asynchronous retrieval. The built-in provider shares its cache and in-flight acquisition between both methods.
 *
 * <pre>{@code
 * SessionCredential session = provider.getSession(context);
 * Mono<SessionCredential> asyncSession = provider.getSessionAsync(context);
 * }</pre>
 *
 * @see SessionCredential
 * @see SessionRequestContext
 * @see SessionOptions
 */
public interface SessionProvider {

    /**
     * Asynchronously returns a valid session credential, creating or refreshing it as needed.
     *
     * @param context the request-scoped session parameters.
     * @return a cold publisher that emits the session credential.
     */
    Mono<SessionCredential> getSessionAsync(SessionRequestContext context);

    /**
     * Synchronously returns a valid session credential, creating or refreshing it as needed.
     * <p>
     * The default implementation subscribes to {@link #getSessionAsync(SessionRequestContext)} and blocks until
     * it completes. This method must not be called from a non-blocking Reactor thread.
     * Implementations may override it to provide synchronous retrieval while preserving the same caching,
     * invalidation, and refresh behavior as the asynchronous method.
     *
     * @param context the request-scoped session parameters.
     * @return the session credential.
     */
    default SessionCredential getSession(SessionRequestContext context) {
        return getSessionAsync(context).block();
    }

    /**
     * Atomically invalidates the rejected credential if it is still current.
     *
     * @param context the request-scoped session parameters.
     * @param rejectedCredential the credential the service rejected with HTTP 401.
     * @return {@code true} if this call invalidated the credential; {@code false} if it was already replaced.
     */
    boolean invalidateSession(SessionRequestContext context, SessionCredential rejectedCredential);

    /**
     * Requests a non-blocking refresh of the current session credential.
     * <p>
     * This method must return immediately without waiting for the refresh to complete and must not throw.
     *
     * @param context the request-scoped session parameters.
     */
    void refreshSession(SessionRequestContext context);
}
