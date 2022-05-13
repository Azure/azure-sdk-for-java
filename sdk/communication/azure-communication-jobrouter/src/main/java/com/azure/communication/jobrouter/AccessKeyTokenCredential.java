package com.azure.communication.jobrouter;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;

public class AccessKeyTokenCredential implements TokenCredential {
    String accessKey;

    public AccessKeyTokenCredential(String accessKey) {
        this.accessKey = accessKey;
    }

    @Override
    public Mono<AccessToken> getToken(TokenRequestContext request) {
        return Mono.just(new AccessToken(this.accessKey, OffsetDateTime.MAX));
    }
}
