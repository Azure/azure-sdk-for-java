// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.aadb2c.security;

import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoderFactory;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestOperations;

/**
 * A factory that provides a {@link JwtDecoder} used for {@link OidcIdToken} signature verification.
 *
 */
public class AadB2cOidcIdTokenDecoderFactory implements JwtDecoderFactory<ClientRegistration> {

    private final RestOperations restOperations;

    /**
     *
     * @param restOperations The RestOperations used to retrieve jwk from jwkSetUri.
     */
    public AadB2cOidcIdTokenDecoderFactory(RestOperations restOperations) {
        this.restOperations = restOperations;
    }

    @Override
    public JwtDecoder createDecoder(ClientRegistration clientRegistration) {
        String jwkSetUri = clientRegistration.getProviderDetails().getJwkSetUri();
        if (!StringUtils.hasText(jwkSetUri)) {
            OAuth2Error oauth2Error = new OAuth2Error("missing_signature_verifier",
                    "Failed to find a Signature Verifier for Client Registration: '"
                            + clientRegistration.getRegistrationId()
                            + "'. Check to ensure you have configured the JwkSet URI.",
                    null);
            throw new OAuth2AuthenticationException(oauth2Error, oauth2Error.toString());
        }
        return NimbusJwtDecoder
                .withJwkSetUri(jwkSetUri)
                .jwsAlgorithm(SignatureAlgorithm.RS256)
                .restOperations(restOperations)
                .build();
    }
}
