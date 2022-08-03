// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.service.implementation.identity.api.token;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import reactor.core.publisher.Mono;

import java.util.function.Function;

@FunctionalInterface
public interface AccessTokenResolver extends Function<TokenCredential, Mono<AccessToken>> {

    static AccessTokenResolver createDefault(AccessTokenResolverOptions options) {
        return new AccessTokenResolverImpl(options);
    }

}
