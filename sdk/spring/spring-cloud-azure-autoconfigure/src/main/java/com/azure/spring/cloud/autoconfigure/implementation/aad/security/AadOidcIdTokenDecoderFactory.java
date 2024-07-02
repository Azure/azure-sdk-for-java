// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.aad.security;

import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoderFactory;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.web.client.RestOperations;

/**
 * A factory that provides a {@link JwtDecoder} used for {@link OidcIdToken} signature verification.
 *
 * @see <a href="https://learn.microsoft.com/azure/active-directory/develop/id-tokens">azure-active-directory id-tokens</a>
 */
public class AadOidcIdTokenDecoderFactory implements JwtDecoderFactory<ClientRegistration> {

    private final JwtDecoder jwtDecoder;

    /**
     *
     * @param jwkSetUri The uri of the jwk set. For example:
     *                 <a href="https://login.microsoftonline.com/common/discovery/v2.0/keys">
     *                     https://login.microsoftonline.com/common/discovery/v2.0/keys</a>
     * @param restOperations The RestOperations used to retrieve jwk from jwkSetUri.
     */
    public AadOidcIdTokenDecoderFactory(String jwkSetUri, RestOperations restOperations) {
        this.jwtDecoder = NimbusJwtDecoder
                .withJwkSetUri(jwkSetUri)
                .jwsAlgorithm(SignatureAlgorithm.RS256)
                .restOperations(restOperations)
                .build();
    }

    @Override
    public JwtDecoder createDecoder(ClientRegistration context) {
        return jwtDecoder;
    }
}
