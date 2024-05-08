// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.azure.identity.implementation.IdentityClientOptions;
import reactor.core.publisher.Mono;

/**
 * The {@link AzurePipelinesCredential} acquires a token using the Azure Devops Pipeline service connection.
 */
public class AzurePipelinesCredential implements TokenCredential {

    private final String serviceConnectionId;

    private final IdentityClientOptions identityClientOptions;

    AzurePipelinesCredential(String serviceConnectionId, IdentityClientOptions identityClientOptions) {
        this.serviceConnectionId = serviceConnectionId;
        this.identityClientOptions = identityClientOptions;
    }
    @Override
    public Mono<AccessToken> getToken(TokenRequestContext request) {
        return null;
    }

    @Override
    public AccessToken getTokenSync(TokenRequestContext request) {
        return null;
    }
}
