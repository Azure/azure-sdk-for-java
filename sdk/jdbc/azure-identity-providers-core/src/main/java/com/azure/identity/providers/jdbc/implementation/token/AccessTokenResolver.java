// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.providers.jdbc.implementation.token;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import reactor.core.publisher.Mono;

import java.util.function.Function;

/**
 * Interface to be implemented by classes wish to provide functionality that
 * takes a TokenCredential as input and outputs a publisher that emits a single access token.
 */
@FunctionalInterface
public interface AccessTokenResolver extends Function<TokenCredential, Mono<AccessToken>> {

    static AccessTokenResolver createDefault(AccessTokenResolverOptions options) {
        return new AccessTokenResolverImpl(options);
    }

}
