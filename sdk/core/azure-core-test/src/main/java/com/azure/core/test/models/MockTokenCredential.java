// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.test.models;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;

/**
 * This class attempts to mimic the behavior of {@code TokenCredential} without making a network call
 * with dummy credentials.
 */
public class MockTokenCredential implements TokenCredential {
    @Override
    public Mono<AccessToken> getToken(TokenRequestContext request) {
        return Mono.just(new AccessToken("mockToken", OffsetDateTime.now().plusHours(2)));    }
}
