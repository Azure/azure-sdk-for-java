// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.aadb2c.security;

import com.azure.spring.cloud.autoconfigure.implementation.aad.security.jwt.AadJwtIssuerValidator;
import com.azure.spring.cloud.autoconfigure.implementation.aadb2c.configuration.properties.AadB2cProperties;
import com.azure.spring.cloud.autoconfigure.implementation.aadb2c.security.jwt.AadB2cTrustedIssuerRepository;
import org.springframework.security.oauth2.client.oidc.authentication.OidcIdTokenValidator;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoderFactory;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestOperations;

import java.util.ArrayList;
import java.util.List;

/**
 * A factory that provides a {@link JwtDecoder} used for {@link OidcIdToken} signature verification and claim validation.
 * <p>
 * Besides verifying the token signature, the decoder validates the standard OpenID Connect ID token claims (audience,
 * expiry, issued-at and subject) via {@link OidcIdTokenValidator} and additionally validates the {@code iss} (issuer)
 * claim against the trusted Azure AD B2C issuers, so that only tokens issued by the configured B2C tenant and user
 * flows are accepted.
 */
public class AadB2cOidcIdTokenDecoderFactory implements JwtDecoderFactory<ClientRegistration> {

    private final RestOperations restOperations;

    private final AadB2cProperties properties;

    /**
     *
     * @param restOperations The RestOperations used to retrieve jwk from jwkSetUri.
     * @param properties The AAD B2C properties used to build the ID token validators.
     */
    public AadB2cOidcIdTokenDecoderFactory(RestOperations restOperations, AadB2cProperties properties) {
        Assert.notNull(restOperations, "restOperations cannot be null");
        Assert.notNull(properties, "properties cannot be null");
        this.restOperations = restOperations;
        this.properties = properties;
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
        NimbusJwtDecoder decoder = NimbusJwtDecoder
                .withJwkSetUri(jwkSetUri)
                .jwsAlgorithm(SignatureAlgorithm.RS256)
                .restOperations(restOperations)
                .build();
        decoder.setJwtValidator(createJwtValidator(clientRegistration));
        return decoder;
    }

    private OAuth2TokenValidator<Jwt> createJwtValidator(ClientRegistration clientRegistration) {
        List<OAuth2TokenValidator<Jwt>> validators = new ArrayList<>();
        // Validates the standard OIDC ID token claims: audience (must contain the client id), expiry, issued-at,
        // subject and authorized party.
        validators.add(new OidcIdTokenValidator(clientRegistration));
        // Validates the issuer against the trusted Azure AD B2C issuers (tenant and user flows).
        validators.add(new AadJwtIssuerValidator(new AadB2cTrustedIssuerRepository(properties)));
        return new DelegatingOAuth2TokenValidator<>(validators);
    }
}
