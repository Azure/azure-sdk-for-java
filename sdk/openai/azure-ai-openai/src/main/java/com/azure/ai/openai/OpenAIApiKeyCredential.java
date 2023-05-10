// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.azure.core.http.policy.BearerTokenAuthenticationPolicy;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;

/**
 * Token credential class for accepting non-Azure OpenAI credential string.
 */
public class OpenAIApiKeyCredential implements TokenCredential {
    private String keyCredential;

    /**
     * Create a non-Azure OpenAI credential token.
     *
     * @param keyCredential non-Azure OpenAI credential.
     */
    public OpenAIApiKeyCredential(String keyCredential) {
        this.keyCredential = keyCredential;
    }

    @Override
    public Mono<AccessToken> getToken(TokenRequestContext request) {
        return Mono.fromCallable(() -> new AccessToken(keyCredential, OffsetDateTime.now().plusDays(180)));
    }
}
