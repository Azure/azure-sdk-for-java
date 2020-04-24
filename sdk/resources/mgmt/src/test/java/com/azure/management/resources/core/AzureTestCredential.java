// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.management.resources.core;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;

public class AzureTestCredential implements TokenCredential {

    public AzureTestCredential() {
    }

    @Override
    public Mono<AccessToken> getToken(TokenRequestContext request) {
        return Mono.just(new AccessToken("https:/asdd.com", OffsetDateTime.MAX));
    }
}
