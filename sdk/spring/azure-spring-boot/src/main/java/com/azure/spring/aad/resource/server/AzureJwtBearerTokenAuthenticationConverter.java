// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.aad.resource.server;

import com.azure.spring.autoconfigure.aad.UserPrincipal;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTClaimsSet.Builder;
import java.text.ParseException;
import java.util.Collection;
import java.util.Map.Entry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

/**
 * A {@link Converter} that takes a {@link Jwt} and converts it into a {@link PreAuthenticatedAuthenticationToken}.
 */
public class AzureJwtBearerTokenAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AzureJwtBearerTokenAuthenticationConverter.class);
    private final JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();

    public AzureJwtBearerTokenAuthenticationConverter() {
    }

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        OAuth2AccessToken accessToken = new OAuth2AccessToken(
            OAuth2AccessToken.TokenType.BEARER, jwt.getTokenValue(), jwt.getIssuedAt(), jwt.getExpiresAt());
        AbstractAuthenticationToken token = this.jwtAuthenticationConverter.convert(jwt);
        Collection<GrantedAuthority> authorities = token.getAuthorities();
        JWTClaimsSet.Builder builder = new Builder();
        for (Entry<String, Object> entry : jwt.getClaims().entrySet()) {
            builder.claim(entry.getKey(), entry.getValue());
        }
        JWTClaimsSet jwtClaimsSet = builder.build();
        JWSObject jwsObject = null;
        try {
            jwsObject = JWSObject.parse(accessToken.getTokenValue());
        } catch (ParseException e) {
            LOGGER.error(
                e.getMessage() + ". When create an instance of JWSObject, an exception is resolved on the token.");
        }
        UserPrincipal userPrincipal = new UserPrincipal(accessToken.getTokenValue(), jwsObject, jwtClaimsSet);
        return new PreAuthenticatedAuthenticationToken(userPrincipal, null, authorities);
    }
}
