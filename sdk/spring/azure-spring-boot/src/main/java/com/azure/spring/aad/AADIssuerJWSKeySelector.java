// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.aad;


import com.azure.spring.autoconfigure.aad.AADTokenClaim;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.KeySourceException;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.jwk.source.RemoteJWKSet;
import com.nimbusds.jose.proc.JWSAlgorithmFamilyJWSKeySelector;
import com.nimbusds.jose.proc.JWSKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jose.util.DefaultResourceRetriever;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.JWTClaimsSetAwareJWSKeySelector;
import java.net.URL;
import java.security.Key;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Selecting key candidates for processing a signed JWT which
 * provides access to the JWT claims set in addition to the JWS header.
 */
public class AADIssuerJWSKeySelector implements JWTClaimsSetAwareJWSKeySelector<SecurityContext> {

    private AADTrustedIssuerRepository trustedIssuerRepository;

    private int connectTimeout;

    private int readTimeout;

    private int sizeLimit;

    private final Map<String, JWSKeySelector<SecurityContext>> selectors = new ConcurrentHashMap<>();

    public AADIssuerJWSKeySelector(AADTrustedIssuerRepository trustedIssuerRepository, int connectTimeout,
        int readTimeout, int sizeLimit) {
        this.trustedIssuerRepository = trustedIssuerRepository;
        this.connectTimeout = connectTimeout;
        this.readTimeout = readTimeout;
        this.sizeLimit = sizeLimit;
    }

    @Override
    public List<? extends Key> selectKeys(JWSHeader header, JWTClaimsSet claimsSet, SecurityContext context)
        throws KeySourceException {
        String iss = (String) claimsSet.getClaim(AADTokenClaim.ISS);
        if (trustedIssuerRepository.getTrustedIssuers().contains(iss)) {
            return this.selectors.computeIfAbsent(iss, this::fromIssuer).selectJWSKeys(header, context);
        }
        throw new IllegalArgumentException(
            "The current issuer is not included in the trustedIssuers, no JWS key selector is configured.");
    }


    private JWSKeySelector<SecurityContext> fromIssuer(String metadataUrlPrefix) {
        return Optional.ofNullable(metadataUrlPrefix)
            .map(u -> {
                Map<String, Object> configuration = AADJwtDecoderProviderConfiguration
                    .getConfigurationForIssuerLocation(u);
                String jwkSetUrl = withProviderConfiguration(configuration);
                return fromUri(jwkSetUrl);
            })
            .orElseThrow(() -> new IllegalArgumentException("Cannot create JWSKeySelector."));
    }

    private String withProviderConfiguration(Map<String, Object> configuration) {
        return configuration.get("jwks_uri").toString();
    }

    private JWSKeySelector<SecurityContext> fromUri(String uri) {
        try {
            DefaultResourceRetriever jwkSetRetriever = new DefaultResourceRetriever(connectTimeout, readTimeout,
                sizeLimit);
            JWKSource<SecurityContext> jwkSource = new RemoteJWKSet<>(new URL(uri), jwkSetRetriever);
            return JWSAlgorithmFamilyJWSKeySelector.fromJWKSource(jwkSource);
        } catch (Exception ex) {
            throw new IllegalArgumentException(ex);
        }
    }

}
