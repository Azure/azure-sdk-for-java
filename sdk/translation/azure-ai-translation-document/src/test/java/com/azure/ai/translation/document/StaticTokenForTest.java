// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.translation.document;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import reactor.core.publisher.Mono;
import java.time.OffsetDateTime;

public class StaticTokenForTest implements TokenCredential {
    private final AccessToken token;

    StaticTokenForTest(String token) {
        this.token = new AccessToken(token, OffsetDateTime.MAX);
    }

    public Mono<AccessToken> getToken(TokenRequestContext request) {
        return Mono.just(this.token);
    }
}
