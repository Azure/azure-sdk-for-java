// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.jdbc;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.azure.identity.ManagedIdentityCredential;
import com.azure.identity.ManagedIdentityCredentialBuilder;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CachedManagedIdentityCredential implements TokenCredential {

    // to one JVM, one system assigned managed identity, many client assigned managed identity
    private static ManagedIdentityCredential credential;
    private static Map<String, Mono<AccessToken>> accessTokenMap = new ConcurrentHashMap<>();

    public CachedManagedIdentityCredential(String clientId) {
        ManagedIdentityCredentialBuilder builder = new ManagedIdentityCredentialBuilder();
        if (StringUtils.hasText(clientId)) {
            builder.clientId(clientId);
        }
        credential = builder.build();
    }


    @Override
    public Mono<AccessToken> getToken(TokenRequestContext tokenRequestContext) {
        // todo
        String key = tokenRequestContext.getClaims()
            + tokenRequestContext.getTenantId()
            + tokenRequestContext.getScopes();
        if (accessTokenMap.get(key) != null) {
            Mono<AccessToken> monoAccessToken = accessTokenMap.get(key);
            AccessToken accessToken = monoAccessToken.block(Duration.ofSeconds(30));
            if (accessToken != null && !accessToken.isExpired()) {
                return monoAccessToken;
            }
        }

        Mono<AccessToken> cacheToken = credential.getToken(tokenRequestContext).cache();
        accessTokenMap.put(key, cacheToken);
        return cacheToken;
    }
}
