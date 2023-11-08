// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.aad;

import com.azure.spring.cloud.autoconfigure.aad.implementation.constants.AadJwtClaimNames;
import com.azure.spring.cloud.autoconfigure.aad.implementation.constants.AuthorityPrefix;
import com.azure.spring.cloud.autoconfigure.aad.implementation.jwt.AadJwtGrantedAuthoritiesConverter;
import com.azure.spring.cloud.autoconfigure.aad.implementation.oauth2.AadOAuth2AuthenticatedPrincipal;
import com.azure.spring.cloud.autoconfigure.aad.properties.AadResourceServerProperties;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.util.Assert;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


/**
 * A {@link Converter} that takes a {@link Jwt} and converts it into a {@link BearerTokenAuthentication}.
 *
 * @deprecated use the default converter {@link JwtAuthenticationConverter} instead in {@link AadResourceServerWebSecurityConfigurerAdapter}.
 */
@Deprecated
public class AadJwtBearerTokenAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final Converter<Jwt, Collection<GrantedAuthority>> jwtGrantedAuthoritiesConverter;
    private final String principalClaimName;

    /**
     * Construct AADJwtBearerTokenAuthenticationConverter by AADTokenClaim.SUB and
     * DEFAULT_CLAIM_TO_AUTHORITY_PREFIX_MAP.
     */
    public AadJwtBearerTokenAuthenticationConverter() {
        this(AadJwtClaimNames.SUB, AadResourceServerProperties.DEFAULT_CLAIM_TO_AUTHORITY_PREFIX_MAP);
    }

    /**
     * Construct AADJwtBearerTokenAuthenticationConverter with the authority claim.
     *
     * @param authoritiesClaimName authority claim name
     */
    public AadJwtBearerTokenAuthenticationConverter(String authoritiesClaimName) {
        this(authoritiesClaimName, AuthorityPrefix.SCOPE);
    }

    /**
     * Construct AADJwtBearerTokenAuthenticationConverter with the authority claim name and prefix.
     *
     * @param authoritiesClaimName authority claim name
     * @param authorityPrefix the prefix name of the authority
     */
    public AadJwtBearerTokenAuthenticationConverter(String authoritiesClaimName,
                                                    String authorityPrefix) {
        this(AadJwtClaimNames.SUB, buildClaimToAuthorityPrefixMap(authoritiesClaimName, authorityPrefix));
    }

    /**
     * Using spring security provides JwtGrantedAuthoritiesConverter, it can resolve the access token of scp or roles.
     *
     * @param principalClaimName the claim name for the principal
     * @param claimToAuthorityPrefixMap the authority name and prefix map
     */
    public AadJwtBearerTokenAuthenticationConverter(String principalClaimName,
                                                    Map<String, String> claimToAuthorityPrefixMap) {
        Assert.notNull(claimToAuthorityPrefixMap, "claimToAuthorityPrefixMap cannot be null");
        this.principalClaimName = principalClaimName;
        this.jwtGrantedAuthoritiesConverter = new AadJwtGrantedAuthoritiesConverter(claimToAuthorityPrefixMap);
    }

    /**
     * Using the custom JwtGrantedAuthoritiesConverter.
     *
     * @param jwtGrantedAuthoritiesConverter the custom granted authority converter
     */
    public AadJwtBearerTokenAuthenticationConverter(Converter<Jwt, Collection<GrantedAuthority>> jwtGrantedAuthoritiesConverter) {
        this(AadJwtClaimNames.SUB, jwtGrantedAuthoritiesConverter);
    }

    /**
     * Using the principal claim name and the custom JwtGrantedAuthoritiesConverter.
     *
     * @param principalClaimName the claim name for the principal
     * @param jwtGrantedAuthoritiesConverter the custom granted authority converter
     */
    public AadJwtBearerTokenAuthenticationConverter(String principalClaimName,
                                                    Converter<Jwt, Collection<GrantedAuthority>> jwtGrantedAuthoritiesConverter) {
        Assert.notNull(jwtGrantedAuthoritiesConverter, "jwtGrantedAuthoritiesConverter cannot be null");
        this.principalClaimName = principalClaimName;
        this.jwtGrantedAuthoritiesConverter = jwtGrantedAuthoritiesConverter;
    }

    /**
     * Convert the source object of type {@code Jwt} to target type {@code AbstractAuthenticationToken}.
     *
     * @param jwt the source object to convert, which must be an instance of {@code Jwt} (never {@code null})
     * @return the converted object
     */
    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        OAuth2AccessToken accessToken = new OAuth2AccessToken(
            OAuth2AccessToken.TokenType.BEARER, jwt.getTokenValue(), jwt.getIssuedAt(), jwt.getExpiresAt());
        Map<String, Object> claims = jwt.getClaims();
        Collection<GrantedAuthority> authorities = jwtGrantedAuthoritiesConverter.convert(jwt);
        OAuth2AuthenticatedPrincipal principal = new AadOAuth2AuthenticatedPrincipal(
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
