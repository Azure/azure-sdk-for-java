// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.implementation.util;

import com.azure.core.http.HttpRequest;
import com.azure.storage.blob.BlobUrlParts;
import reactor.core.publisher.Mono;

/**
 * Internal contract for session credential acquisition and lifecycle management.
 */
abstract class SessionProvider {
    /**
     * Gets parsed URL parts when the request is eligible for session authentication.
     *
     * @param request The request to evaluate.
     * @return The parsed URL parts if the request is eligible; otherwise, {@code null}.
     */
    abstract BlobUrlParts getEligibleRequestParts(HttpRequest request);

    abstract Mono<SessionCredential> getSessionAsync(SessionRequestContext context);

    SessionCredential getSession(SessionRequestContext context) {
        return getSessionAsync(context).block();
    }

    abstract boolean invalidateSession(SessionRequestContext context, SessionCredential credential);

    abstract void refreshSession(SessionRequestContext context);
}
