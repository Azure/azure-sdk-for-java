// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.models;

import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpRequest;
import com.azure.storage.blob.BlobServiceVersion;
import com.azure.storage.blob.implementation.accesshelpers.SessionProviderConstructorProxy;
import com.azure.storage.blob.implementation.util.SessionCredential;
import com.azure.storage.blob.implementation.util.SessionRequestContext;
import reactor.core.publisher.Mono;

import java.time.Clock;

/**
 * SDK-managed base type for session providers.
 * <p>
 * Use {@link ContainerSessionProvider} and configure it with {@link SessionOptions}. Applications cannot implement
 * this type.
 * @see SessionOptions
 */
public abstract class SessionProvider {
    static {
        SessionProviderConstructorProxy.setAccessor(new SessionProviderConstructorProxy.SessionProviderConstructorAccessor() {
            @Override
            public ContainerSessionProvider create(HttpPipeline pipeline, String endpoint, BlobServiceVersion version,
                String accountName, Clock clock) {
                return new ContainerSessionProvider(pipeline, endpoint, version, accountName, clock);
            }

            @Override
            public boolean isRequestEligible(SessionProvider provider, HttpRequest request) {
                return provider.isRequestEligible(request);
            }

            @Override
            public Mono<SessionCredential> getSessionAsync(SessionProvider provider, SessionRequestContext context) {
                return provider.getSessionAsync(context);
            }

            @Override
            public SessionCredential getSession(SessionProvider provider, SessionRequestContext context) {
                return provider.getSession(context);
            }

            @Override
            public boolean invalidateSession(SessionProvider provider, SessionRequestContext context,
                SessionCredential credential) {
                return provider.invalidateSession(context, credential);
            }

            @Override
            public void refreshSession(SessionProvider provider, SessionRequestContext context) {
                provider.refreshSession(context);
            }
        });
    }

    SessionProvider() {
    }

    abstract boolean isRequestEligible(HttpRequest request);

    abstract Mono<SessionCredential> getSessionAsync(SessionRequestContext context);

    SessionCredential getSession(SessionRequestContext context) {
        return getSessionAsync(context).block();
    }

    abstract boolean invalidateSession(SessionRequestContext context, SessionCredential credential);

    abstract void refreshSession(SessionRequestContext context);
}
