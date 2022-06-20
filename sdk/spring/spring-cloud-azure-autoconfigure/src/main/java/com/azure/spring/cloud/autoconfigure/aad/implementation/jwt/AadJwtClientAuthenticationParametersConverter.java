// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.aad.implementation.jwt;

import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.KeyType;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.oauth2.client.endpoint.AbstractOAuth2AuthorizationGrantRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.OAuth2AuthorizationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.jose.jws.JwsAlgorithm;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.util.Assert;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * A {@link Converter} that customizes the OAuth 2.0 Access Token Request parameters by adding a signed JSON Web Token
 * (JWS) to be used for client authentication at the Azure AD Authorization Server's Token Endpoint.
 *
 * @param <T> the type of {@link AbstractOAuth2AuthorizationGrantRequest}
 * @since 4.3.0
 */
public final class AadJwtClientAuthenticationParametersConverter<T extends AbstractOAuth2AuthorizationGrantRequest>
    implements Converter<T, MultiValueMap<String, String>> {

    private static final String INVALID_KEY_ERROR_CODE = "invalid_key";

    private static final String INVALID_ALGORITHM_ERROR_CODE = "invalid_algorithm";

    public static final String CLIENT_ASSERTION_TYPE_VALUE = "urn:ietf:params:oauth:client-assertion-type:jwt-bearer";

    private final Function<ClientRegistration, JWK> jwkResolver;

    private final Map<String, JwsEncoderHolder> jwsEncoders = new ConcurrentHashMap<>();

    /**
     * Constructs a {@code AadJwtClientAuthenticationParametersConverter} using the provided parameters.
     *
     * @param jwkResolver the resolver that provides the {@code com.nimbusds.jose.jwk.JWK} associated to the {@link
     * ClientRegistration client}
     */
    public AadJwtClientAuthenticationParametersConverter(Function<ClientRegistration, JWK> jwkResolver) {
        Assert.notNull(jwkResolver, "jwkResolver cannot be null");
        this.jwkResolver = jwkResolver;
    }

    @Override
    public MultiValueMap<String, String> convert(T authorizationGrantRequest) {
        Assert.notNull(authorizationGrantRequest, "authorizationGrantRequest cannot be null");

        ClientRegistration clientRegistration = authorizationGrantRequest.getClientRegistration();
        if (!ClientAuthenticationMethod.PRIVATE_KEY_JWT.equals(clientRegistration.getClientAuthenticationMethod())) {
            return null;
        }

        JWK jwk = this.jwkResolver.apply(clientRegistration);
        if (jwk == null) {
            OAuth2Error oauth2Error = new OAuth2Error(INVALID_KEY_ERROR_CODE,
                "Failed to resolve JWK signing key for client registration '"
                    + clientRegistration.getRegistrationId() + "'.",
                null);
            throw new OAuth2AuthorizationException(oauth2Error);
        }

        JwsAlgorithm jwsAlgorithm = resolveAlgorithm(jwk);
        if (jwsAlgorithm == null) {
            OAuth2Error oauth2Error = new OAuth2Error(INVALID_ALGORITHM_ERROR_CODE,
                "Unable to resolve JWS (signing) algorithm from JWK associated to client registration '"
                    + clientRegistration.getRegistrationId() + "'.",
                null);
            throw new OAuth2AuthorizationException(oauth2Error);
        }

        Map<String, Object> jwsHeader = new HashMap<>();
        jwsHeader.put("typ", "JWT");
        jwsHeader.put("alg", SignatureAlgorithm.RS256.getName());
        jwsHeader.put("x5t", jwk.getX509CertThumbprint().toString());

        Map<String, Object> jwtClaimsSet = new HashMap<>();
        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plus(Duration.ofSeconds(60L));
        jwtClaimsSet.put(JwtClaimNames.ISS, clientRegistration.getClientId());
        jwtClaimsSet.put(JwtClaimNames.SUB, clientRegistration.getClientId());
        jwtClaimsSet.put(JwtClaimNames.AUD,
            Collections.singletonList(clientRegistration.getProviderDetails().getTokenUri()));
        jwtClaimsSet.put(JwtClaimNames.JTI, UUID.randomUUID().toString());
        jwtClaimsSet.put(JwtClaimNames.IAT, issuedAt);
        jwtClaimsSet.put(JwtClaimNames.EXP, expiresAt);

        JwsEncoderHolder jwsEncoderHolder = this.jwsEncoders.compute(clientRegistration.getRegistrationId(),
            (clientRegistrationId, currentJwsEncoderHolder) -> {
                if (currentJwsEncoderHolder != null && currentJwsEncoderHolder.getJwk().equals(jwk)) {
                    return currentJwsEncoderHolder;
                }
                JWKSource<SecurityContext> jwkSource = new ImmutableJWKSet<>(new JWKSet(jwk));
                return new JwsEncoderHolder(new AadJwtEncoder(jwkSource), jwk);
            });

        AadJwtEncoder jwtEncoder = jwsEncoderHolder.getJwtEncoder();

        Jwt jwt = jwtEncoder.encode(jwsHeader, jwtClaimsSet);

        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
        parameters.set(OAuth2ParameterNames.CLIENT_ASSERTION_TYPE, CLIENT_ASSERTION_TYPE_VALUE);
        parameters.set(OAuth2ParameterNames.CLIENT_ASSERTION, jwt.getTokenValue());
        return parameters;
    }

    private static JwsAlgorithm resolveAlgorithm(JWK jwk) {
        JwsAlgorithm jwsAlgorithm = null;

        if (jwk.getAlgorithm() != null) {
            jwsAlgorithm = SignatureAlgorithm.from(jwk.getAlgorithm().getName());
            if (jwsAlgorithm == null) {
                jwsAlgorithm = MacAlgorithm.from(jwk.getAlgorithm().getName());
            }
        }
        if (jwsAlgorithm == null && KeyType.RSA.equals(jwk.getKeyType())) {
            jwsAlgorithm = SignatureAlgorithm.RS256;
        }
        return jwsAlgorithm;
    }

    private static final class JwsEncoderHolder {

        private final AadJwtEncoder jwtEncoder;

        private final JWK jwk;

        private JwsEncoderHolder(AadJwtEncoder jwtEncoder, JWK jwk) {
            this.jwtEncoder = jwtEncoder;
            this.jwk = jwk;
        }

        private AadJwtEncoder getJwtEncoder() {
            return this.jwtEncoder;
        }

        private JWK getJwk() {
            return this.jwk;
        }

    }

}
