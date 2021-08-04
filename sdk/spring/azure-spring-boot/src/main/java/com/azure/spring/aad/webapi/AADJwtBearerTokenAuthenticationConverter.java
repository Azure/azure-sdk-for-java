// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.aad.webapi;

import com.azure.spring.aad.AADOAuth2AuthenticatedPrincipal;
import com.azure.spring.aad.AbstractJwtBearerTokenAuthenticationConverter;
import com.azure.spring.aad.implementation.constants.AADTokenClaim;
import com.azure.spring.aad.implementation.constants.AuthorityPrefix;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthentication;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;


/**
 * A {@link Converter} that takes a {@link Jwt} and converts it into a {@link BearerTokenAuthentication}.
 */
public class AADJwtBearerTokenAuthenticationConverter extends AbstractJwtBearerTokenAuthenticationConverter {

    /**
     * Construct AADJwtBearerTokenAuthenticationConverter by AADTokenClaim.SUB and DEFAULT_CLAIM_TO_AUTHORITY_PREFIX_MAP.
     */
    public AADJwtBearerTokenAuthenticationConverter() {
        this(AADTokenClaim.SUB, AADResourceServerProperties.DEFAULT_CLAIM_TO_AUTHORITY_PREFIX_MAP);
    }

    /**
     * Construct AADJwtBearerTokenAuthenticationConverter with the authority claim.
     * @param authoritiesClaimName authority claim name
     */
    public AADJwtBearerTokenAuthenticationConverter(String authoritiesClaimName) {
        this(authoritiesClaimName, AuthorityPrefix.SCOPE);
    }

    /**
     * Construct AADJwtBearerTokenAuthenticationConverter with the authority claim name and prefix.
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
        super(principalClaimName, claimToAuthorityPrefixMap);
    }

    @Override
    protected OAuth2AuthenticatedPrincipal getAuthenticatedPrincipal(Map<String, Object> headers,
                                                                     Map<String, Object> claims,
                                                                     Collection<GrantedAuthority> authorities,
                                                                     String tokenValue) {
        String name = Optional.ofNullable(principalClaimName)
                              .map(n -> (String) claims.get(n))
                              .orElseGet(() -> (String) claims.get("sub"));
        return new AADOAuth2AuthenticatedPrincipal(headers, claims, authorities, tokenValue, name);
    }
}
