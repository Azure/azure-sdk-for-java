// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.implementation.aad.security.jwt;


import com.azure.spring.cloud.autoconfigure.implementation.aad.security.constants.AadJwtClaimNames;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.KeySourceException;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.jwk.source.RemoteJWKSet;
import com.nimbusds.jose.proc.JWSAlgorithmFamilyJWSKeySelector;
import com.nimbusds.jose.proc.JWSKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jose.util.ResourceRetriever;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.JWTClaimsSetAwareJWSKeySelector;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.client.RestOperations;

import java.net.URL;
import java.security.Key;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.azure.spring.cloud.autoconfigure.implementation.aad.utils.AadRestTemplateCreator.createRestTemplate;

/**
 * Selecting key candidates for processing a signed JWT which provides access to the JWT claims set in addition to the
 * JWS header.
 */
public class AadIssuerJwsKeySelector implements JWTClaimsSetAwareJWSKeySelector<SecurityContext> {

    private final AadTrustedIssuerRepository trustedIssuerRepo;
    private final Map<String, JWSKeySelector<SecurityContext>> selectors = new ConcurrentHashMap<>();

    private final RestOperations restOperations;

    private final ResourceRetriever resourceRetriever;
    /**
     * Creates a new instance of {@link AadIssuerJwsKeySelector}.
     *
     * @param trustedIssuerRepo the AAD trusted issuer repository
     */
    public AadIssuerJwsKeySelector(RestTemplateBuilder restTemplateBuilder,
                                   AadTrustedIssuerRepository trustedIssuerRepo,
                                   ResourceRetriever resourceRetriever) {
        this.restOperations = createRestTemplate(restTemplateBuilder);
        this.trustedIssuerRepo = trustedIssuerRepo;
        this.resourceRetriever = resourceRetriever;
    }

    @Override
    public List<? extends Key> selectKeys(JWSHeader header, JWTClaimsSet claimsSet, SecurityContext context)
        throws KeySourceException {
        String iss = (String) claimsSet.getClaim(AadJwtClaimNames.ISS);
        if (trustedIssuerRepo.isTrusted(iss)) {
            return selectors.computeIfAbsent(iss, this::fromIssuer).selectJWSKeys(header, context);
        }
        throw new IllegalArgumentException("The issuer: '" + iss + "' is not registered in trusted issuer repository,"
            + " so cannot create JWSKeySelector.");
    }

    @SuppressWarnings("deprecation")
    private JWSKeySelector<SecurityContext> fromIssuer(String issuer) {
        Map<String, Object> configurationForOidcIssuerLocation = AadJwtDecoderProviderConfiguration
            .getConfigurationForOidcIssuerLocation(restOperations, getOidcIssuerLocation(issuer));
        String uri = configurationForOidcIssuerLocation.get("jwks_uri").toString();
        try {
            JWKSource<SecurityContext> jwkSource = new RemoteJWKSet<>(new URL(uri), resourceRetriever);
            return JWSAlgorithmFamilyJWSKeySelector.fromJWKSource(jwkSource);
        } catch (Exception ex) {
            throw new IllegalArgumentException(ex.getMessage(), ex);
        }
    }

    private String getOidcIssuerLocation(String issuer) {
        if (trustedIssuerRepo.hasSpecialOidcIssuerLocation(issuer)) {
            return trustedIssuerRepo.getSpecialOidcIssuerLocation(issuer);
        }
        return issuer;
    }

}
