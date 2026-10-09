// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.models;

import com.azure.storage.blob.implementation.accesshelpers.SessionProviderAccessHelper;
import com.azure.core.http.HttpRequest;
import com.azure.storage.blob.implementation.util.SessionCredential;
import com.azure.storage.blob.implementation.util.SessionRequestContext;
import reactor.core.publisher.Mono;

/**
 * Manages session authentication for storage containers.
 * <p>
 * Create a {@link ContainerSessionProvider} and supply it to
 * {@link SessionOptions#setSessionProvider(SessionProvider)} to share sessions across independently built clients.
 * Providers are implemented by the SDK, not by applications. Session acquisition, signing, refresh, and fallback
 * to bearer authentication are managed internally.
 * @see SessionOptions
 */
public abstract class SessionProvider {
    static {
        SessionProviderAccessHelper.setAccessor(provider -> new SessionProviderAccessHelper() {
            @Override
            public boolean isRequestEligible(HttpRequest request) {
                return provider.isRequestEligible(request);
            }

            @Override
            public Mono<SessionCredential> getSessionAsync(SessionRequestContext context) {
                return provider.getSessionAsync(context);
            }

            @Override
            public SessionCredential getSession(SessionRequestContext context) {
                return provider.getSession(context);
            }

            @Override
            public boolean invalidateSession(SessionRequestContext context, SessionCredential credential) {
                return provider.invalidateSession(context, credential);
            }

            @Override
            public void refreshSession(SessionRequestContext context) {
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
