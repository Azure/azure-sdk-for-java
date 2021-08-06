// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.aad;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthentication;
import org.springframework.util.Assert;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * An abstract {@link Converter} that takes a {@link Jwt} and converts it into a {@link BearerTokenAuthentication}.
 */
public abstract class AbstractJwtBearerTokenAuthenticationConverter implements Converter<Jwt,
    AbstractAuthenticationToken> {

    protected Converter<Jwt, Collection<GrantedAuthority>> converter;
    protected String principalClaimName;

    public AbstractJwtBearerTokenAuthenticationConverter(String principalClaimName,
                                                         Map<String, String> claimToAuthorityPrefixMap) {
        Assert.notNull(claimToAuthorityPrefixMap, "claimToAuthorityPrefixMap cannot be null");
        this.principalClaimName = principalClaimName;
        this.converter = new AADJwtGrantedAuthoritiesConverter(claimToAuthorityPrefixMap);
    }

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        OAuth2AccessToken accessToken = new OAuth2AccessToken(
            OAuth2AccessToken.TokenType.BEARER, jwt.getTokenValue(), jwt.getIssuedAt(), jwt.getExpiresAt());
        Collection<GrantedAuthority> authorities = converter.convert(jwt);
        OAuth2AuthenticatedPrincipal principal = getAuthenticatedPrincipal(
            jwt.getHeaders(), jwt.getClaims(), authorities, jwt.getTokenValue());
        return new BearerTokenAuthentication(principal, accessToken, authorities);
    }

    protected static Map<String, String> buildClaimToAuthorityPrefixMap(String authoritiesClaimName,
                                                                        String authorityPrefix) {
        Assert.notNull(authoritiesClaimName, "authoritiesClaimName cannot be null");
        Assert.notNull(authorityPrefix, "authorityPrefix cannot be null");
        Map<String, String> claimToAuthorityPrefixMap = new HashMap<>();
        claimToAuthorityPrefixMap.put(authoritiesClaimName, authorityPrefix);
        return claimToAuthorityPrefixMap;
    }

    /**
     * Construct an instance of OAuth2AuthenticatedPrincipal interface.
     *
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
