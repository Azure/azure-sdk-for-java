// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.aad.implementation.jwt;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.factories.DefaultJWSSignerFactory;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKMatcher;
import com.nimbusds.jose.jwk.JWKSelector;
import com.nimbusds.jose.jwk.KeyType;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jose.produce.JWSSignerFactory;
import com.nimbusds.jose.util.Base64URL;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A JWT encoder that encodes a JSON Web Token (JWT) using the JSON Web Signature (JWS)
 * Compact Serialization format. The private/secret key used for signing the JWS is
 * supplied by the {@code com.nimbusds.jose.jwk.source.JWKSource} provided via the
 * constructor.
 *
 * <p>
 * <b>NOTE:</b> A specially customized version for Azure AD based on 'NimbusJwsEncoder' or 'JwtEncoder' implementation.
 * It is compatible with spring-security 5.5.X and 5.6.X versions, it can be replaced by 'NimbusJwsEncoder' when the minimum supported version is 5.6.X.
 *
 * @since 4.3.0
 */
public final class AadJwtEncoder {

    private static final String ENCODING_ERROR_MESSAGE_TEMPLATE = "An error occurred while attempting to encode the Jwt: %s";

    private static final JWSSignerFactory JWS_SIGNER_FACTORY = new DefaultJWSSignerFactory();

    private final Map<JWK, JWSSigner> jwsSigners = new ConcurrentHashMap<>();

    private final JWKSource<SecurityContext> jwkSource;

    /**
     * Constructs a {@code NimbusJwtEncoder} using the provided parameters.
     *
     * @param jwkSource the {@code com.nimbusds.jose.jwk.source.JWKSource}
     */
    public AadJwtEncoder(JWKSource<SecurityContext> jwkSource) {
        Assert.notNull(jwkSource, "jwkSource cannot be null");
        this.jwkSource = jwkSource;
    }

    public Jwt encode(Map<String, Object> jwsHeader, Map<String, Object> jwtClaimsSet) throws JwtException {
        Assert.notNull(jwsHeader, "jwsHeader cannot be null");
        Assert.notNull(jwtClaimsSet, "jwtClaimsSet cannot be null");
        JWK jwk = selectJwk(jwsHeader);
        String jws = serialize(jwsHeader, jwtClaimsSet, jwk);
        return new Jwt(jws,
            (Instant) jwtClaimsSet.get(JwtClaimNames.IAT),
            (Instant) jwtClaimsSet.get(JwtClaimNames.EXP),
            jwsHeader, jwtClaimsSet);
    }

    private JWK selectJwk(Map<String, Object> jwsHeader) {
        List<JWK> jwks;
        try {
            JWKSelector jwkSelector = new JWKSelector(createJwkMatcher(jwsHeader));
            jwks = this.jwkSource.get(jwkSelector, null);
        } catch (Exception ex) {
            throw new JwtException(String.format(ENCODING_ERROR_MESSAGE_TEMPLATE,
                    "Failed to select a JWK signing key -> " + ex.getMessage()), ex);
        }

        if (jwks.size() > 1) {
            throw new JwtException(String.format(ENCODING_ERROR_MESSAGE_TEMPLATE,
                    "Found multiple JWK signing keys for algorithm '" + jwsHeader.get("alg") + "'"));
        }

        if (jwks.isEmpty()) {
            throw new JwtException(
                    String.format(ENCODING_ERROR_MESSAGE_TEMPLATE, "Failed to select a JWK signing key"));
        }

        return jwks.get(0);
    }

    private String serialize(Map<String, Object> headers, Map<String, Object> claims, JWK jwk) {
        JWSHeader jwsHeader = convertHeader(headers);
        JWTClaimsSet jwtClaimsSet = convertClaims(claims);

        JWSSigner jwsSigner = this.jwsSigners.computeIfAbsent(jwk, AadJwtEncoder::createSigner);

        SignedJWT signedJwt = new SignedJWT(jwsHeader, jwtClaimsSet);
        try {
            signedJwt.sign(jwsSigner);
        } catch (JOSEException ex) {
            throw new JwtException(
                    String.format(ENCODING_ERROR_MESSAGE_TEMPLATE, "Failed to sign the JWT -> " + ex.getMessage()), ex);
        }
        return signedJwt.serialize();
    }

    private static JWKMatcher createJwkMatcher(Map<String, Object> jwsHeader) {
        JWSAlgorithm jwsAlgorithm = JWSAlgorithm.parse((String) jwsHeader.get("alg"));

        if (JWSAlgorithm.Family.RSA.contains(jwsAlgorithm)) {
            // @formatter:off
            return new JWKMatcher.Builder()
                    .keyType(KeyType.forAlgorithm(jwsAlgorithm))
                    .keyUses(KeyUse.SIGNATURE, null)
                    .algorithms(jwsAlgorithm, null)
                    .x509CertSHA256Thumbprint(Base64URL.from((String) jwsHeader.get("x5t#S256")))
                    .build();
            // @formatter:on
        }
        return null;
    }

    private static JWSSigner createSigner(JWK jwk) {
        try {
            return JWS_SIGNER_FACTORY.createJWSSigner(jwk);
        } catch (JOSEException ex) {
            throw new JwtException(String.format(ENCODING_ERROR_MESSAGE_TEMPLATE,
                    "Failed to create a JWS Signer -> " + ex.getMessage()), ex);
        }
    }

    @SuppressWarnings("unchecked")
    private static JWSHeader convertHeader(Map<String, Object> headers) {
        JWSHeader.Builder builder = new JWSHeader.Builder(JWSAlgorithm.parse((String) headers.get("alg")));
        Map<String, Object> jwk = (Map<String, Object>) headers.get("jwk");
        if (!CollectionUtils.isEmpty(jwk)) {
            try {
                builder.jwk(JWK.parse(jwk));
            } catch (Exception ex) {
                throw new     JwtException(String.format(ENCODING_ERROR_MESSAGE_TEMPLATE,
                        "Unable to convert 'jku' JOSE header"), ex);
            }
        }
        String keyId = (String) headers.get("kid");
        if (StringUtils.hasText(keyId)) {
            builder.keyID(keyId);
        }

        String x509SHA1Thumbprint = (String) headers.get("x5t");
        if (StringUtils.hasText(x509SHA1Thumbprint)) {
            builder.x509CertThumbprint(new Base64URL(x509SHA1Thumbprint));
        }

        String type = (String) headers.get("typ");
        if (StringUtils.hasText(type)) {
            builder.type(new JOSEObjectType(type));
        }

        Map<String, Object> customHeaders = new HashMap<>();
        headers.forEach((name, value) -> {
            if (!JWSHeader.getRegisteredParameterNames().contains(name)) {
                customHeaders.put(name, value);
            }
        });
        if (!customHeaders.isEmpty()) {
            builder.customParams(customHeaders);
        }

        return builder.build();
    }

    @SuppressWarnings("unchecked")
    private static JWTClaimsSet convertClaims(Map<String, Object> claims) {
        JWTClaimsSet.Builder builder = new JWTClaimsSet.Builder();
        Object issuer = claims.get(JwtClaimNames.ISS);
        if (issuer != null) {
            builder.issuer(issuer.toString());
        }

        String subject = (String) claims.get(JwtClaimNames.SUB);
        if (StringUtils.hasText(subject)) {
            builder.subject(subject);
        }

        List<String> audience = (List<String>) claims.get(JwtClaimNames.AUD);
        if (!CollectionUtils.isEmpty(audience)) {
            builder.audience(audience);
        }

        Instant expiresAt = (Instant) claims.get(JwtClaimNames.EXP);
        if (expiresAt != null) {
            builder.expirationTime(Date.from(expiresAt));
        }

        Instant notBefore = (Instant) claims.get(JwtClaimNames.NBF);
        if (notBefore != null) {
            builder.notBeforeTime(Date.from(notBefore));
        }

        Instant issuedAt = (Instant) claims.get(JwtClaimNames.IAT);
        if (issuedAt != null) {
            builder.issueTime(Date.from(issuedAt));
        }

        String jwtId = (String) claims.get(JwtClaimNames.JTI);
        if (StringUtils.hasText(jwtId)) {
            builder.jwtID(jwtId);
        }

        Map<String, Object> customClaims = new HashMap<>();
        claims.forEach((name, value) -> {
            if (!JWTClaimsSet.getRegisteredNames().contains(name)) {
                customClaims.put(name, value);
            }
        });
        if (!customClaims.isEmpty()) {
            customClaims.forEach(builder::claim);
        }

        return builder.build();
    }
}
