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
import java.util.concurrent.ConcurrentHashMap;

/**
 * Selecting key candidates for processing a signed JWT which provides access to the JWT claims set in addition to the
 * JWS header.
 */
public class AADIssuerJWSKeySelector implements JWTClaimsSetAwareJWSKeySelector<SecurityContext> {

    private final AADTrustedIssuerRepository trustedIssuers;

    private final int connectTimeout;

    private final int readTimeout;

    private final int sizeLimit;

    private final Map<String, JWSKeySelector<SecurityContext>> selectors = new ConcurrentHashMap<>();

    public AADIssuerJWSKeySelector(AADTrustedIssuerRepository trustedIssuers, int connectTimeout,
                                   int readTimeout, int sizeLimit) {
        this.trustedIssuers = trustedIssuers;
        this.connectTimeout = connectTimeout;
        this.readTimeout = readTimeout;
        this.sizeLimit = sizeLimit;
    }

    @Override
    public List<? extends Key> selectKeys(JWSHeader header, JWTClaimsSet claimsSet, SecurityContext context)
        throws KeySourceException {
        String iss = (String) claimsSet.getClaim(AADTokenClaim.ISS);
        if (trustedIssuers.getTrustedIssuers().contains(iss)) {
            return selectors.computeIfAbsent(iss, this::fromIssuer).selectJWSKeys(header, context);
        }
        throw new IllegalArgumentException("The issuer: '" + iss + "' is not registered in trusted issuer repository,"
            + " so cannot create JWSKeySelector.");
    }

    private JWSKeySelector<SecurityContext> fromIssuer(String issuer) {
        Map<String, Object> configurationForOidcIssuerLocation =
            AADJwtDecoderProviderConfiguration.getConfigurationForOidcIssuerLocation(issuer);
        String uri = configurationForOidcIssuerLocation.get("jwks_uri").toString();
        DefaultResourceRetriever jwkSetRetriever = new DefaultResourceRetriever(connectTimeout, readTimeout,
            sizeLimit);
        try {
            JWKSource<SecurityContext> jwkSource = new RemoteJWKSet<>(new URL(uri), jwkSetRetriever);
            return JWSAlgorithmFamilyJWSKeySelector.fromJWKSource(jwkSource);
        } catch (Exception ex) {
            throw new IllegalArgumentException("The issuer: '" + issuer + "' were unable to create a Key Source.", ex);
        }
    }

}
