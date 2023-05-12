package com.azure.ai.openai;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;

public final class OpenAITokenCredential implements TokenCredential {

    private final String token;

    public OpenAITokenCredential(String token) {
        if (token == null) {
            throw new IllegalArgumentException("OpenAI token must not be null");
        }
        this.token = token;
    }

    @Override
    public Mono<AccessToken> getToken(TokenRequestContext request) {
        return  Mono.justOrEmpty(new AccessToken(token, OffsetDateTime.now().plusDays(180)));
    }
}
