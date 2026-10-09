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
import java.util.function.Function;

/**
 * Accesses SDK-owned session operations across package boundaries.
 */
public abstract class SessionProviderAccessHelper {
    private static Function<SessionProvider, SessionProviderAccessHelper> accessor;
    private static Factory factory;

    /**
     * Creates the internal access bridge.
     */
    protected SessionProviderAccessHelper() {
    }

    /**
     * Registers the model accessor.
     * @param accessor The accessor.
     */
    public static void setAccessor(Function<SessionProvider, SessionProviderAccessHelper> accessor) {
        SessionProviderAccessHelper.accessor = accessor;
    }

    /**
     * Gets the internal operations of an initialized provider.
     * @param provider The provider.
     * @return Its internal operations.
     */
    public static SessionProviderAccessHelper getInternal(SessionProvider provider) {
        return accessor.apply(provider);
    }

    /**
     * Registers the provider constructor.
     * @param factory The constructor.
     */
    public static void setFactory(Factory factory) {
        SessionProviderAccessHelper.factory = factory;
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
        if (factory == null) {
            try {
                Class.forName(ContainerSessionProvider.class.getName(), true,
                    ContainerSessionProvider.class.getClassLoader());
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException(e);
            }
        }
        return factory.create(pipeline, endpoint, version, accountName, clock);
    }

    /**
     * Checks eligibility.
     * @param request The request.
     * @return Whether sessions are eligible.
     */
    public abstract boolean isRequestEligible(HttpRequest request);

    /**
     * Acquires a session asynchronously.
     * @param context The container and account.
     * @return The session, or empty for bearer fallback.
     */
    public abstract Mono<SessionCredential> getSessionAsync(SessionRequestContext context);

    /**
     * Acquires a session synchronously.
     * @param context The container and account.
     * @return The session, or null for bearer fallback.
     */
    public abstract SessionCredential getSession(SessionRequestContext context);

    /**
     * Invalidates the current session.
     * @param context The container and account.
     * @param credential The rejected session.
     * @return Whether the session was current.
     */
    public abstract boolean invalidateSession(SessionRequestContext context, SessionCredential credential);

    /**
     * Requests background refresh.
     * @param context The container and account.
     */
    public abstract void refreshSession(SessionRequestContext context);

    /**
     * Internal provider construction.
     */
    public interface Factory {
        /**
         * Creates a provider.
         * @param pipeline The secret-safe bearer pipeline.
         * @param endpoint The endpoint.
         * @param version The service version.
         * @param accountName The signing account.
         * @param clock The cache clock.
         * @return The provider.
         */
        ContainerSessionProvider create(HttpPipeline pipeline, String endpoint, BlobServiceVersion version,
            String accountName, Clock clock);
    }
}
