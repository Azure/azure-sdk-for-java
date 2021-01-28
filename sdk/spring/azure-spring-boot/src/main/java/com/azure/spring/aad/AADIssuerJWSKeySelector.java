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
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.util.StringUtils;


public class AADIssuerJWSKeySelector implements JWTClaimsSetAwareJWSKeySelector<SecurityContext> {

    /**
     * The default HTTP connect timeout for JWK set retrieval, in milliseconds. Set to 500 milliseconds.
     */
    public static final int DEFAULT_HTTP_CONNECT_TIMEOUT = 500;

    /**
     * The default HTTP read timeout for JWK set retrieval, in milliseconds. Set to 500 milliseconds.
     */
    public static final int DEFAULT_HTTP_READ_TIMEOUT = 500;

    /**
     * The default HTTP entity size limit for JWK set retrieval, in bytes. Set to 50 KBytes.
     */
    public static final int DEFAULT_HTTP_SIZE_LIMIT = 50 * 1024;

    private Set<String> trustedIssuers;

    private int connectTimeout;

    private int readTimeout;

    private int sizeLimit;

    private final Map<String, JWSKeySelector<SecurityContext>> selectors = new ConcurrentHashMap<>();

    public AADIssuerJWSKeySelector(Set<String> trustedIssuers) {
        this(trustedIssuers,
            DEFAULT_HTTP_CONNECT_TIMEOUT,
            DEFAULT_HTTP_READ_TIMEOUT,
            DEFAULT_HTTP_SIZE_LIMIT);
    }

    public AADIssuerJWSKeySelector(Set<String> trustedIssuers, int connectTimeout, int readTimeout, int sizeLimit) {
        this.trustedIssuers = trustedIssuers;
        this.connectTimeout = connectTimeout;
        this.readTimeout = readTimeout;
        this.sizeLimit = sizeLimit;
    }


    @Override
    public List<? extends Key> selectKeys(JWSHeader header, JWTClaimsSet claimsSet, SecurityContext context)
        throws KeySourceException {
        String iss = (String) claimsSet.getClaim(AADTokenClaim.ISS);
        if (trustedIssuers.contains(iss)) {
            String tfp = (String) claimsSet.getClaim(AADTokenClaim.TFP);
            if (StringUtils.isEmpty(tfp)) {
                return this.selectors.computeIfAbsent(iss, this::fromIssuer).selectJWSKeys(header, context);
            }
            String b2cIss = b2cUrlPrefixConversion(iss, tfp);
            return this.selectors.computeIfAbsent(b2cIss, this::fromIssuer).selectJWSKeys(header, context);
        }
        throw new IllegalArgumentException(
            "The current issuer is not included in the trustedIssuers, no JWS key selector is configured.");
    }

    private String b2cUrlPrefixConversion(String iss, String tfp) {
        if (iss.contains(tfp)) return iss;
        int len = iss.lastIndexOf("/v2.0");
        StringBuffer operatorStr = new StringBuffer(iss);
        String b2cUrlPrefixConversion = operatorStr.insert(len, "/" + tfp).toString();
        return b2cUrlPrefixConversion;
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
