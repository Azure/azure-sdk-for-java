// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.eventhubs;

import java.text.ParseException;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import com.microsoft.azure.eventhubs.impl.ClientConstants;

public final class AzureActiveDirectoryTokenProvider implements ITokenProvider {
    private final AuthenticationCallback authCallback;
    private final String authority;
    private final Object authCallbackState;

    public AzureActiveDirectoryTokenProvider(
            final AuthenticationCallback authenticationCallback,
            final String authority,
            final Object state) {
        this.authCallbackState = state;
        this.authority = authority;
        this.authCallback = authenticationCallback;
    }

    @Override
    public CompletableFuture<SecurityToken> getToken(String resource, Duration timeout) {
        return this.authCallback.acquireToken(ClientConstants.EVENTHUBS_AUDIENCE, this.authority, this.authCallbackState)
                .thenApply((rawToken) -> {
                    try {
                        return new JsonSecurityToken(rawToken, resource);
                    } catch (ParseException e) {
                        throw new CompletionException(e);
                    }
                });
    }

    public interface AuthenticationCallback {
        CompletableFuture<String> acquireToken(String audience, String authority, Object state);
    }
}
