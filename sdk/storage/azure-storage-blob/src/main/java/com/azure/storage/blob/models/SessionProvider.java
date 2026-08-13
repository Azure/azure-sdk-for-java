// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.models;

import reactor.core.publisher.Mono;

/**
 * Provides and manages cached {@link SessionCredential session credentials} for storage containers.
 * <p>
 * Implement this interface to bring your own session creation and caching logic - for example, proxying
 * CreateSession calls through another service or sharing a credential cache across clients - while still
 * benefiting from this SDK's request signing and account-level cooldown handling. Set an instance via
 * {@link SessionOptions#setSessionProvider(SessionProvider)}, then pass those options to
 * {@link com.azure.storage.blob.BlobServiceClientBuilder#sessionOptions(SessionOptions)}, to have it used in
 * place of the default, built-in provider (which calls the storage service's CreateSession REST API directly
 * and manages its own per-container caching).
 *
 * <h2>Lifecycle</h2>
 * <p>
 * A {@link SessionProvider} implementation is expected to support the full session lifecycle:
 * <ol>
 * <li><strong>Retrieve</strong> - {@link #getSessionAsync} / {@link #getSession} return a usable
 * {@link SessionCredential} for the container described by the request context, minting or refreshing one
 * as needed.</li>
 * <li><strong>Invalidate</strong> - {@link #invalidateSession} is called when the service rejects a
 * previously-issued credential with HTTP 401, giving the implementation the opportunity to evict it so the
 * next retrieval mints a fresh one.</li>
 * <li><strong>Refresh</strong> - {@link #refreshSession} is called when the service signals (via an
 * {@code x-ms-auth-info: session_expiring} response header) that the current session is about to stop being
 * honored, giving the implementation the opportunity to proactively refresh it in the background.</li>
 * </ol>
 * <p>
 * Regardless of the provider used, the SDK always retains ownership of HMAC request signing, bearer-token
 * fallback for ineligible requests, and account-level acquisition cooldown (suppressing further session
 * acquisition attempts for an account for a period after a 400/403/5xx failure). A {@link SessionProvider}
 * implementation is only responsible for producing, invalidating, and refreshing credentials - never for
 * signing requests or deciding when to fall back to bearer authentication.
 *
 * <h2>Thread safety</h2>
 * <p>
 * Implementations must be thread-safe: {@link #getSessionAsync}, {@link #getSession},
 * {@link #invalidateSession}, and {@link #refreshSession} may all be invoked concurrently from multiple
 * pipeline threads. In particular, {@link #invalidateSession} must perform its compare-and-invalidate as a
 * single atomic operation (see its documentation for details), and {@link #refreshSession} must not block.
 *
 * <h2>Scoping</h2>
 * <p>
 * A single {@link SessionProvider} instance may be asked to serve many different containers (and, in
 * principle, multiple accounts) over its lifetime; the {@link SessionRequestContext} passed to each method
 * call identifies which container (and account) the call applies to. Applications may reuse one provider
 * instance across service clients when they intentionally want those clients to share the provider's cache.
 *
 * @see SessionCredential
 * @see SessionRequestContext
 * @see SessionOptions
 */
public interface SessionProvider {

    /**
     * Asynchronously returns a valid cached {@link SessionCredential} for the container described by
     * {@code context}, creating or refreshing the credential when needed.
     *
     * @param context the request-scoped parameters (e.g. container name) the session should be created for.
     * @return a {@link Mono} that emits the resulting {@link SessionCredential}.
     */
    Mono<SessionCredential> getSessionAsync(SessionRequestContext context);

    /**
     * Synchronously returns a valid cached {@link SessionCredential} for the container described by
     * {@code context}, creating or refreshing the credential when needed.
     *
     * @param context the request-scoped parameters (e.g. container name) the session should be created for.
     * @return the resulting {@link SessionCredential}.
     */
    SessionCredential getSession(SessionRequestContext context);

    /**
     * Attempts a compare-and-invalidate on the credential currently held for the container described by
     * {@code context}: if {@code rejectedCredential} is still the active credential, it is atomically
     * replaced so the next call to {@link #getSession} or {@link #getSessionAsync} returns a fresh one.
     * <p>
     * <strong>Thread safety:</strong> Implementations must treat the compare and the invalidate as a
     * single atomic operation. Exactly one thread presenting the same {@code rejectedCredential} should
     * succeed in invalidating it; all later threads presenting the same instance must return {@code false}.
     * <p>
     * <strong>Warning semantics:</strong> The SDK logs a one-time warning when this returns {@code true}
     * (the first invalidation for a given rejected credential) and a verbose message when it returns
     * {@code false} (already replaced).
     *
     * @param context the request-scoped parameters (container, account) identifying the session scope.
     * @param rejectedCredential the credential the service rejected with HTTP 401.
     * @return {@code true} if this call invalidated the credential (first invalidator wins);
     *         {@code false} if the credential was already replaced.
     */
    boolean invalidateSession(SessionRequestContext context, SessionCredential rejectedCredential);

    /**
     * Non-blocking hint that the service has indicated the current session for the container described by
     * {@code context} is about to expire (signalled via an {@code x-ms-auth-info: session_expiring}
     * response header). Implementations should trigger a proactive background refresh immediately so the
     * next request uses a fresh session without an inline latency penalty.
     * <p>
     * <strong>Non-blocking contract:</strong> This method is called from both synchronous and
     * asynchronous response-processing paths and <em>must return immediately</em> without waiting for the
     * refresh to complete. It must not throw.
     *
     * @param context the request-scoped parameters (container, account) identifying the session scope.
     */
    void refreshSession(SessionRequestContext context);
}
