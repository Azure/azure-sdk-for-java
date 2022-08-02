package com.azure.spring.cloud.service.implementation.identity.api.token;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import reactor.core.publisher.Mono;

import java.util.Objects;

class AccessTokenResolverImpl implements AccessTokenResolver {

    private AccessTokenResolverOptions options;

    AccessTokenResolverImpl() {
        this.options = new AccessTokenResolverOptions();
    }

    AccessTokenResolverImpl(AccessTokenResolverOptions options) {
        this.options = options;
    }

    @Override
    public Mono<AccessToken> apply(TokenCredential tokenCredential) {
        Objects.requireNonNull(tokenCredential);
        TokenRequestContext request = new TokenRequestContext();
        request.setTenantId(this.options.getTenantId());
        request.setClaims(this.options.getClaims());
        request.addScopes(this.options.getScopes());
        return tokenCredential.getToken(request);
    }
}
