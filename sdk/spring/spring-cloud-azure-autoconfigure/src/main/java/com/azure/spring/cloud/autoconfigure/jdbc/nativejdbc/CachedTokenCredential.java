// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.jdbc.nativejdbc;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import javax.validation.constraints.NotNull;
import java.util.concurrent.ConcurrentHashMap;

public class CachedTokenCredential implements TokenCredential {
    private static final Logger LOGGER = LoggerFactory.getLogger(CachedTokenCredential.class);
    private static ConcurrentHashMap<String, AccessToken> accessTokenCache = new ConcurrentHashMap<>();
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
        String key = tokenCredential.getClass().getSimpleName() + ":"
                    + tokenRequestContext.getScopes() + ":"
                    + tokenRequestContext.getTenantId() + ":"
                    + tokenRequestContext.getClaims();

        LOGGER.debug("accessTokenCache key = " + key);

        AccessToken accessToken = accessTokenCache.get(key);
        if (accessToken != null && !accessToken.isExpired()) {
            return Mono.just(accessToken);
        } else {
            return tokenCredential
                            .getToken(tokenRequestContext)
                            .doOnNext(response -> accessTokenCache.put(key, response));
        }
    }

}
