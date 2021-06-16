// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.common;

import java.util.Collection;
import java.util.Map;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.util.Assert;

/**
 * An abstract {@link Converter} that takes a {@link Jwt} and converts it into a {@link BearerTokenAuthentication}.
 */
public abstract class AbstractJwtBearerTokenAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    protected static final String DEFAULT_AUTHORITY_PREFIX = "SCOPE_";

    protected Converter<Jwt, Collection<GrantedAuthority>> converter;
    protected String principalClaimName;

    /**
     * Use AADJwtGrantedAuthoritiesConverter, it can resolve the access token of scp and roles.
     */
    public AbstractJwtBearerTokenAuthenticationConverter() {
        this.converter = new AADJwtGrantedAuthoritiesConverter();
    }

    /**
     * Using spring security provides JwtGrantedAuthoritiesConverter, it can resolve the access token of scp or roles.
     *
     * @param authoritiesClaimName authorities claim name
     */
    public AbstractJwtBearerTokenAuthenticationConverter(String authoritiesClaimName) {
        this(authoritiesClaimName, DEFAULT_AUTHORITY_PREFIX);
    }

    public AbstractJwtBearerTokenAuthenticationConverter(String authoritiesClaimName, String authorityPrefix) {
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
        OAuth2AuthenticatedPrincipal principal = getAuthenticatedPrincipal(
            jwt.getHeaders(), jwt.getClaims(), authorities, jwt.getTokenValue());
        return new BearerTokenAuthentication(principal, accessToken, authorities);
    }

    /**
     * Construct an instance of OAuth2AuthenticatedPrincipal interface.
     * @param headers Jwt header map
     * @param claims Jwt claims map
     * @param authorities Jwt authorities collection
     * @param tokenValue Jwt token value
     * @return an OAuth2AuthenticatedPrincipal instance.
     */
    protected abstract OAuth2AuthenticatedPrincipal getAuthenticatedPrincipal(Map<String, Object> headers,
                                                                              Map<String, Object> claims,
                                                                              Collection<GrantedAuthority> authorities,
                                                                              String tokenValue);

    public void setConverter(
        Converter<Jwt, Collection<GrantedAuthority>> converter) {
        this.converter = converter;
    }

    public void setPrincipalClaimName(String principalClaimName) {
        Assert.hasText(principalClaimName, "principalClaimName cannot be empty");
        this.principalClaimName = principalClaimName;
    }
}
