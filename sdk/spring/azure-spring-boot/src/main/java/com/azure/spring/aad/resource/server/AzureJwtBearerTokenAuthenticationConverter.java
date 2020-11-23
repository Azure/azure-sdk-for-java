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
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

/**
 * A {@link Converter} that takes a {@link Jwt} and converts it into a {@link PreAuthenticatedAuthenticationToken}.
 */
public class AzureJwtBearerTokenAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AzureJwtBearerTokenAuthenticationConverter.class);
    private Converter<Jwt, Collection<GrantedAuthority>> jwtGrantedAuthoritiesConverter
        = new JwtGrantedAuthoritiesConverter();

    public AzureJwtBearerTokenAuthenticationConverter() {
    }

    protected Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
        return this.jwtGrantedAuthoritiesConverter.convert(jwt);
    }

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        Collection<GrantedAuthority> authorities = extractAuthorities(jwt);
        JWTClaimsSet.Builder builder = new Builder();
        for (Entry<String, Object> entry : jwt.getClaims().entrySet()) {
            builder.claim(entry.getKey(), entry.getValue());
        }
        JWTClaimsSet jwtClaimsSet = builder.build();
        JWSObject jwsObject = null;
        try {
            jwsObject = JWSObject.parse(jwt.getTokenValue());
        } catch (ParseException e) {
            LOGGER.error(
                e.getMessage() + ". When create an instance of JWSObject, an exception is resolved on the token.");
        }
        UserPrincipal userPrincipal = new UserPrincipal(jwt.getTokenValue(), jwsObject, jwtClaimsSet);
        return new PreAuthenticatedAuthenticationToken(userPrincipal, null, authorities);
    }

    public void setJwtGrantedAuthoritiesConverter(
        Converter<Jwt, Collection<GrantedAuthority>> jwtGrantedAuthoritiesConverter) {
        this.jwtGrantedAuthoritiesConverter = jwtGrantedAuthoritiesConverter;
    }
}
