// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.aad;

import com.nimbusds.jose.jwk.JWK;
import org.springframework.security.oauth2.client.registration.ClientRegistration;

import java.util.function.Function;

/**
 * Resolver interface to resolve a function that returns a {@link JWK} implementation through a {@link ClientRegistration}.
 * @since 4.3.0
 */
@FunctionalInterface
public interface OAuth2ClientAuthenticationJWKResolver {

    /**
     * @return a resolver function.
     */
    Function<ClientRegistration, JWK> resolve();
}
