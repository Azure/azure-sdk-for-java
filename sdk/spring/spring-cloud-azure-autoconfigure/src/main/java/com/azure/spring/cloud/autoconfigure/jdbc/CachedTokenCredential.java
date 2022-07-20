// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.jdbc;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import javax.validation.constraints.NotNull;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CachedTokenCredential implements TokenCredential {
    private static final Logger LOGGER = LoggerFactory.getLogger(CachedTokenCredential.class);

    // to one JVM, one system assigned managed identity, many client assigned managed identity
    private static Map<String, Mono<AccessToken>> accessTokenMap = new ConcurrentHashMap<>();
    private TokenCredential tokenCredential;

    @NotNull
    public CachedTokenCredential(TokenCredential tokenCredential) {
        this.tokenCredential = tokenCredential;
    }

    public TokenCredential getTokenCredential() {
        return tokenCredential;
    }

    @Override
    public Mono<AccessToken> getToken(TokenRequestContext tokenRequestContext) {
        // @todo
        String key = tokenCredential.getClass().getSimpleName() + ":"
            + tokenRequestContext.getScopes() + ":"
            + tokenRequestContext.getClaims();
        LOGGER.info("key==" + key);
        if (accessTokenMap.get(key) != null) {
            Mono<AccessToken> monoAccessToken = accessTokenMap.get(key);
            AccessToken accessToken = monoAccessToken.block(Duration.ofSeconds(30));
            LOGGER.info("accessToken=" + accessToken);
           // @todo corner case
            if (accessToken != null && !accessToken.isExpired()) {
                return monoAccessToken;
            }
        }
        Mono<AccessToken> cacheToken = tokenCredential.getToken(tokenRequestContext).cache();
        accessTokenMap.put(key, cacheToken);
        return cacheToken;
    }
}
