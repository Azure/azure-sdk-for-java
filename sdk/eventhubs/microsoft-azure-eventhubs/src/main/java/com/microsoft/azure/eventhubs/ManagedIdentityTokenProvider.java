// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.eventhubs;

import java.io.IOException;
import java.text.ParseException;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import com.microsoft.azure.credentials.MSICredentials;
import com.microsoft.azure.eventhubs.impl.ClientConstants;

public class ManagedIdentityTokenProvider implements ITokenProvider {
    static final MSICredentials CREDENTIALS = new MSICredentials();
    
    @Override
    public CompletableFuture<SecurityToken> getToken(final String resource, final Duration timeout) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String rawToken = ManagedIdentityTokenProvider.CREDENTIALS.getToken(ClientConstants.EVENTHUBS_AUDIENCE);
                return new JsonSecurityToken(rawToken, resource);
            } catch (IOException | ParseException e) {
                throw new CompletionException(e);
            }
        });
    }
}
