// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.aad.implementation.oauth2;

import com.nimbusds.jose.jwk.JWK;
import org.springframework.security.oauth2.client.registration.ClientRegistration;

/**
 * Resolver interface to resolve a {@link JWK} implementation through a {@link ClientRegistration}.
 * @since 4.3.0
 */
@FunctionalInterface
public interface OAuth2ClientAuthenticationJwkResolver {

    /**
     * @param clientRegistration the client registration.
     * @return a a {@link JWK}.
     */
    JWK resolve(ClientRegistration clientRegistration);
}
