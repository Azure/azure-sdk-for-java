// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.models;

import com.azure.core.http.HttpRequest;
import com.azure.storage.blob.implementation.util.SessionCredential;
import com.azure.storage.blob.implementation.util.SessionRequestContext;
import reactor.core.publisher.Mono;

/** Test-only seam for mocking the SDK's package-private provider operations. */
public abstract class TestSessionProvider extends SessionProvider {
    @Override
    public abstract boolean isRequestEligible(HttpRequest request);

    @Override
    public abstract Mono<SessionCredential> getSessionAsync(SessionRequestContext context);

    @Override
    public SessionCredential getSession(SessionRequestContext context) {
        return super.getSession(context);
    }

    @Override
    public abstract boolean invalidateSession(SessionRequestContext context, SessionCredential credential);

    @Override
    public abstract void refreshSession(SessionRequestContext context);
}
