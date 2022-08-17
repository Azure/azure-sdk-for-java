// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.providers.jdbc.implementation.credential;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.azure.identity.providers.jdbc.api.cache.Cache;
import com.azure.identity.providers.jdbc.api.credential.descriptor.CacheKeyDescriptor;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

/**
 * TokenCredential that can cache AccessToken.
 */
public class CacheableTokenCredential implements TokenCredential {

    private final TokenCredential delegate;
    private final Cache<String, AccessToken> cache;
    private final TokenCredentialProviderOptions options;
    private static final String SUB_KEY_DELIMITER = "_";
    private static final String KEY_DELIMITER = "/";

    public CacheableTokenCredential(Cache<String, AccessToken> cache,
                                    TokenCredential tokenCredential,
                                    TokenCredentialProviderOptions options) {
        Objects.requireNonNull(cache);
        Objects.requireNonNull(tokenCredential);
        Objects.requireNonNull(options);

        this.delegate = tokenCredential;
        this.cache = cache;
        this.options = options;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Mono<AccessToken> getToken(TokenRequestContext request) {
        String cacheKey = getCacheKey(request);
        return Mono.defer(() -> {
            AccessToken accessToken = cache.get(cacheKey);
            if (accessToken != null && !accessToken.isExpired()) {
                return Mono.just(accessToken);
            } else {
                return Mono.empty();
            }
        }).switchIfEmpty(Mono.defer(() -> this.delegate.getToken(request))
            .doOnNext(token -> cache.put(cacheKey, token)));
    }

    private String getCacheKey(TokenRequestContext request) {
        List<String> credentialKeyValues = Arrays.stream(getTokenCredentialKeyDescriptors())
                    .map(descriptor -> descriptor.getGetter().apply(this.options))
                    .collect(Collectors.toList());

        String tokenCredentialKey = String.join(SUB_KEY_DELIMITER, credentialKeyValues);
        String tokenRequestContextKey = String.join(SUB_KEY_DELIMITER, request.getTenantId(), request.getClaims(),
            request.getScopes().toString());
        return String.join(KEY_DELIMITER, tokenCredentialKey, tokenRequestContextKey);
    }

    private CacheKeyDescriptor.Descriptor[] getTokenCredentialKeyDescriptors() {
        ServiceLoader<CacheKeyDescriptor> cacheKeyDescriptors = ServiceLoader.load(CacheKeyDescriptor.class);
        for (CacheKeyDescriptor cacheKeyDescriptor : cacheKeyDescriptors) {
            if (cacheKeyDescriptor.support(this)) {
                return cacheKeyDescriptor.getTokenCredentialKeyDescriptors();
            }
        }
        return CacheKeyDescriptor.Descriptor.values();
    }

}
