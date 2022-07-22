// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.jdbc.nativejdbc;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import javax.validation.constraints.NotNull;
import java.time.Duration;

public class CachedTokenCredential implements TokenCredential {
    private static final Logger LOGGER = LoggerFactory.getLogger(CachedTokenCredential.class);

    // to one JVM, one system assigned managed identity, many client assigned managed identity

    private static Cache<String, AccessToken> myCaffeineCache;
    private TokenCredential tokenCredential;

    @NotNull
    public CachedTokenCredential(TokenCredential tokenCredential) {
        this.tokenCredential = tokenCredential;
        if (myCaffeineCache == null) {
            myCaffeineCache = Caffeine.newBuilder().maximumSize(100)
                .expireAfterWrite(Duration.ofSeconds(60)).build();
        }
    }

    public TokenCredential getTokenCredential() {
        return tokenCredential;
    }

    @Override
    public Mono<AccessToken> getToken(TokenRequestContext tokenRequestContext) {
        // @todo
        String key = tokenCredential.getClass().getSimpleName() + ":"
            + tokenRequestContext.getScopes() + ":"
            + tokenRequestContext.getTenantId() + ":"
            + tokenRequestContext.getClaims();
        LOGGER.info("key==" + key);
        //
        AccessToken accessToken = myCaffeineCache.getIfPresent(key);
        if (accessToken != null && !accessToken.isExpired()) {
            return Mono.just(accessToken);
        } else {
            return tokenCredential
                            .getToken(tokenRequestContext)
                            .doOnNext(response -> myCaffeineCache.put(key, response));
        }
    }

}
