// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.aad.webapi;

import com.azure.spring.aad.AADJwtGrantedAuthoritiesConverter;
import com.azure.spring.aad.AADOAuth2AuthenticatedPrincipal;
import com.azure.spring.aad.implementation.constants.AADTokenClaim;
import com.azure.spring.aad.implementation.constants.AuthorityPrefix;
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
 * A {@link Converter} that takes a {@link Jwt} and converts it into a {@link BearerTokenAuthentication}.
 */
public class AADJwtBearerTokenAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final Converter<Jwt, Collection<GrantedAuthority>> converter;
    private final String principalClaimName;

    /**
     * Construct AADJwtBearerTokenAuthenticationConverter by AADTokenClaim.SUB and
     * DEFAULT_CLAIM_TO_AUTHORITY_PREFIX_MAP.
     */
    public AADJwtBearerTokenAuthenticationConverter() {
        this(AADTokenClaim.SUB, AADResourceServerProperties.DEFAULT_CLAIM_TO_AUTHORITY_PREFIX_MAP);
    }

    /**
     * Construct AADJwtBearerTokenAuthenticationConverter with the authority claim.
     *
     * @param authoritiesClaimName authority claim name
     */
    public AADJwtBearerTokenAuthenticationConverter(String authoritiesClaimName) {
        this(authoritiesClaimName, AuthorityPrefix.SCOPE);
    }

    /**
     * Construct AADJwtBearerTokenAuthenticationConverter with the authority claim name and prefix.
     *
     * @param authoritiesClaimName authority claim name
     * @param authorityPrefix the prefix name of the authority
     */
    public AADJwtBearerTokenAuthenticationConverter(String authoritiesClaimName,
                                                    String authorityPrefix) {
        this(null, buildClaimToAuthorityPrefixMap(authoritiesClaimName, authorityPrefix));
    }

    /**
     * Using spring security provides JwtGrantedAuthoritiesConverter, it can resolve the access token of scp or roles.
     *
     * @param principalClaimName authorities claim name
     * @param claimToAuthorityPrefixMap the authority name and prefix map
     */
    public AADJwtBearerTokenAuthenticationConverter(String principalClaimName,
                                                    Map<String, String> claimToAuthorityPrefixMap) {
        Assert.notNull(claimToAuthorityPrefixMap, "claimToAuthorityPrefixMap cannot be null");
        this.principalClaimName = principalClaimName;
        this.converter = new AADJwtGrantedAuthoritiesConverter(claimToAuthorityPrefixMap);
    }

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        OAuth2AccessToken accessToken = new OAuth2AccessToken(
            OAuth2AccessToken.TokenType.BEARER, jwt.getTokenValue(), jwt.getIssuedAt(), jwt.getExpiresAt());
        Map<String, Object> claims = jwt.getClaims();
        Collection<GrantedAuthority> authorities = converter.convert(jwt);
        OAuth2AuthenticatedPrincipal principal = new AADOAuth2AuthenticatedPrincipal(
            jwt.getHeaders(), claims, authorities, jwt.getTokenValue(), (String) claims.get(principalClaimName));
        return new BearerTokenAuthentication(principal, accessToken, authorities);
    }

    private static Map<String, String> buildClaimToAuthorityPrefixMap(String authoritiesClaimName,
                                                               String authorityPrefix) {
        Assert.notNull(authoritiesClaimName, "authoritiesClaimName cannot be null");
        Assert.notNull(authorityPrefix, "authorityPrefix cannot be null");
        Map<String, String> claimToAuthorityPrefixMap = new HashMap<>();
        claimToAuthorityPrefixMap.put(authoritiesClaimName, authorityPrefix);
        return claimToAuthorityPrefixMap;
    }
}
