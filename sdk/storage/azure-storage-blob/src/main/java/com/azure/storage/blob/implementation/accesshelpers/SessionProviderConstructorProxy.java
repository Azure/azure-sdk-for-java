// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.implementation.accesshelpers;

import com.azure.storage.blob.models.SessionProvider;
import com.azure.storage.blob.models.ContainerSessionProvider;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpRequest;
import com.azure.storage.blob.BlobServiceVersion;
import com.azure.storage.blob.implementation.util.SessionCredential;
import com.azure.storage.blob.implementation.util.SessionRequestContext;
import reactor.core.publisher.Mono;
import java.time.Clock;

/**
 * Provides access to SDK-owned session construction and operations across package boundaries.
 */
public final class SessionProviderConstructorProxy {
    private static volatile SessionProviderConstructorAccessor accessor;

    private SessionProviderConstructorProxy() {
    }

    /**
     * Defines operations implemented by the model package.
     */
    public interface SessionProviderConstructorAccessor {
        ContainerSessionProvider create(HttpPipeline pipeline, String endpoint, BlobServiceVersion version,
            String accountName, Clock clock);

        boolean isRequestEligible(SessionProvider provider, HttpRequest request);

        Mono<SessionCredential> getSessionAsync(SessionProvider provider, SessionRequestContext context);

        SessionCredential getSession(SessionProvider provider, SessionRequestContext context);

        boolean invalidateSession(SessionProvider provider, SessionRequestContext context,
            SessionCredential credential);

        void refreshSession(SessionProvider provider, SessionRequestContext context);
    }

    /**
     * Registers the model accessor.
     * @param accessor The accessor.
     */
    public static void setAccessor(SessionProviderConstructorAccessor accessor) {
        SessionProviderConstructorProxy.accessor = accessor;
    }

    /**
     * Gets the shared accessor, initializing the provider type if necessary.
     * @return The accessor used for all providers.
     */
    public static SessionProviderConstructorAccessor getAccessor() {
        if (accessor == null) {
            try {
                Class.forName(SessionProvider.class.getName(), true, SessionProvider.class.getClassLoader());
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException(e);
            }
        }
        return accessor;
    }

    /**
     * Creates the SDK provider with the default client's pipeline and an injectable clock.
     * @param pipeline The secret-safe bearer pipeline.
     * @param endpoint The account endpoint.
     * @param version The service version.
     * @param accountName The signing account name.
     * @param clock The cache clock.
     * @return The SDK provider.
     */
    public static ContainerSessionProvider create(HttpPipeline pipeline, String endpoint, BlobServiceVersion version,
        String accountName, Clock clock) {
        return getAccessor().create(pipeline, endpoint, version, accountName, clock);
    }

    /**
     * Checks eligibility.
     * @param provider The provider.
     * @param request The request.
     * @return Whether sessions are eligible.
     */
    public static boolean isRequestEligible(SessionProvider provider, HttpRequest request) {
        return getAccessor().isRequestEligible(provider, request);
    }

    /**
     * Acquires a session asynchronously.
     * @param provider The provider.
     * @param context The container and account.
     * @return The session, or empty for bearer fallback.
     */
    public static Mono<SessionCredential> getSessionAsync(SessionProvider provider, SessionRequestContext context) {
        return getAccessor().getSessionAsync(provider, context);
    }

    /**
     * Acquires a session synchronously.
     * @param provider The provider.
     * @param context The container and account.
     * @return The session, or null for bearer fallback.
     */
    public static SessionCredential getSession(SessionProvider provider, SessionRequestContext context) {
        return getAccessor().getSession(provider, context);
    }

    /**
     * Invalidates the current session.
     * @param provider The provider.
     * @param context The container and account.
     * @param credential The rejected session.
     * @return Whether the session was current.
     */
    public static boolean invalidateSession(SessionProvider provider, SessionRequestContext context,
        SessionCredential credential) {
        return getAccessor().invalidateSession(provider, context, credential);
    }

    /**
     * Requests background refresh.
     * @param provider The provider.
     * @param context The container and account.
     */
    public static void refreshSession(SessionProvider provider, SessionRequestContext context) {
        getAccessor().refreshSession(provider, context);
    }
}
