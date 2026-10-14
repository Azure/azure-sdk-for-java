// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.implementation.util;

import com.azure.core.http.HttpRequest;
import reactor.core.publisher.Mono;

/**
 * Internal contract for session credential acquisition and lifecycle management.
 */
abstract class SessionProvider {
    abstract boolean isRequestEligible(HttpRequest request);

    abstract Mono<SessionCredential> getSessionAsync(SessionRequestContext context);

    SessionCredential getSession(SessionRequestContext context) {
        return getSessionAsync(context).block();
    }

    abstract boolean invalidateSession(SessionRequestContext context, SessionCredential credential);

    abstract void refreshSession(SessionRequestContext context);
}
