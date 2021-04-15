// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.autoconfigure.b2c;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.util.Assert;

import java.util.Collection;

/**
 * A {@link Converter} that takes a {@link Jwt} and converts it into a {@link BearerTokenAuthentication}.
 */
public class AADB2CJwtBearerTokenAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private static final String DEFAULT_AUTHORITY_PREFIX = "SCOPE_";

    private Converter<Jwt, Collection<GrantedAuthority>> converter;

    private String principalClaimName;

    /**
     * Use AADB2CJwtGrantedAuthoritiesConverter, it can resolve the access token of scp and roles.
     */
    public AADB2CJwtBearerTokenAuthenticationConverter() {
        this.converter = new AADB2CJwtGrantedAuthoritiesConverter();
    }

    /**
     * Use JwtGrantedAuthoritiesConverter, it can resolve the access token of scp or roles.
     * @param authoritiesClaimName authorities claim name
     */
    public AADB2CJwtBearerTokenAuthenticationConverter(String authoritiesClaimName) {
        this(authoritiesClaimName, DEFAULT_AUTHORITY_PREFIX);
    }

    public AADB2CJwtBearerTokenAuthenticationConverter(String authoritiesClaimName, String authorityPrefix) {
        Assert.notNull(authoritiesClaimName, "authoritiesClaimName cannot be null");
        Assert.notNull(authorityPrefix, "authorityPrefix cannot be null");
        JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        jwtGrantedAuthoritiesConverter.setAuthoritiesClaimName(authoritiesClaimName);
        jwtGrantedAuthoritiesConverter.setAuthorityPrefix(authorityPrefix);
        this.converter = jwtGrantedAuthoritiesConverter;
    }

    protected Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
        return this.converter.convert(jwt);
    }

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        OAuth2AccessToken accessToken = new OAuth2AccessToken(
            OAuth2AccessToken.TokenType.BEARER, jwt.getTokenValue(), jwt.getIssuedAt(), jwt.getExpiresAt());
        Collection<GrantedAuthority> authorities = extractAuthorities(jwt);
        if (this.principalClaimName == null) {
            AADB2COAuth2AuthenticatedPrincipal principal = new AADB2COAuth2AuthenticatedPrincipal(
                jwt.getHeaders(), jwt.getClaims(), authorities, jwt.getTokenValue());
            return new BearerTokenAuthentication(principal, accessToken, authorities);
        }
        String name = jwt.getClaim(this.principalClaimName);
        AADB2COAuth2AuthenticatedPrincipal principal = new AADB2COAuth2AuthenticatedPrincipal(jwt.getHeaders(),
            jwt.getClaims(), authorities, jwt.getTokenValue(), name);
        return new BearerTokenAuthentication(principal, accessToken, authorities);
    }

    public void setJwtGrantedAuthoritiesConverter(
        Converter<Jwt, Collection<GrantedAuthority>> jwtGrantedAuthoritiesConverter) {
        this.converter = jwtGrantedAuthoritiesConverter;
    }

    public void setPrincipalClaimName(String principalClaimName) {
        Assert.hasText(principalClaimName, "principalClaimName cannot be empty");
        this.principalClaimName = principalClaimName;
    }
}
