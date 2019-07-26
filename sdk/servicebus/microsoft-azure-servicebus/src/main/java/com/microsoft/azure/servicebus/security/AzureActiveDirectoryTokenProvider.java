// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.servicebus.security;

import java.util.concurrent.CompletableFuture;

/**
 * This is a token provider that obtains tokens from Azure Active Directory. It supports multiple modes of authentication with active directory
 * to obtain tokens.
 * @since 1.2.0
 *
 */
public class AzureActiveDirectoryTokenProvider extends TokenProvider {
    private final AuthenticationCallback authCallback;
    private final String authority;
    private final Object authCallbackState;
    
    AzureActiveDirectoryTokenProvider(AuthenticationCallback callback, String authority, Object callbackState) {
        this.authCallback = callback;
        this.authority = authority;
        this.authCallbackState = callbackState;
    }

    @Override
    public CompletableFuture<SecurityToken> getSecurityTokenAsync(String audience) {
        return this.authCallback.acquireTokenAsync(audience, this.authority, this.authCallbackState);
    }
    
    @FunctionalInterface
    public interface AuthenticationCallback {
        /**
         * A user defined method for obtaining an access token.
         * @param audience The target resource that the access token will be granted for.
         * @param authority The resource that will validate the the access token.
         * @param state Parameter that may be used as part of the custom acquireToken process.
         * @return A CompletableFuture which returns a valid security token.
         */
        CompletableFuture<SecurityToken> acquireTokenAsync(String audience, String authority, Object state);
    }
}
