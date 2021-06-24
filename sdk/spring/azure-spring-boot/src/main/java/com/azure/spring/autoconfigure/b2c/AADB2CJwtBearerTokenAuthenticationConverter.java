// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.autoconfigure.b2c;

import com.azure.spring.aad.AADJwtGrantedAuthoritiesConverter;
import com.azure.spring.aad.AADOAuth2AuthenticatedPrincipal;
import com.azure.spring.aad.AbstractJwtBearerTokenAuthenticationConverter;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthentication;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

import static com.azure.spring.aad.AADJwtGrantedAuthoritiesConverter.DEFAULT_AUTHORITY_PREFIX;
import static com.azure.spring.aad.AADJwtGrantedAuthoritiesConverter.DEFAULT_CLAIM_TO_AUTHORITY_PREFIX_MAP;

/**
 * A {@link Converter} that takes a {@link Jwt} and converts it into a {@link BearerTokenAuthentication}.
 */
public class AADB2CJwtBearerTokenAuthenticationConverter extends AbstractJwtBearerTokenAuthenticationConverter {

    /**
     * Use {@link AADJwtGrantedAuthoritiesConverter}, it can resolve the access token of scp and roles.
     */
    public AADB2CJwtBearerTokenAuthenticationConverter() {
        this(null, DEFAULT_CLAIM_TO_AUTHORITY_PREFIX_MAP);
    }

    /**
     * Construct AADB2CJwtBearerTokenAuthenticationConverter with the authority claim.
     * @param authoritiesClaimName authority claim name
     */
    public AADB2CJwtBearerTokenAuthenticationConverter(String authoritiesClaimName) {
        this(authoritiesClaimName, DEFAULT_AUTHORITY_PREFIX);
    }

    /**
     * Construct AADB2CJwtBearerTokenAuthenticationConverter with the authority claim name and prefix.
     * @param authoritiesClaimName authority claim name
     * @param authorityPrefix the prefix name of the authority
     */
    public AADB2CJwtBearerTokenAuthenticationConverter(String authoritiesClaimName, String authorityPrefix) {
        this(null, buildClaimToAuthorityPrefixMap(authoritiesClaimName, authorityPrefix));
    }

    public AADB2CJwtBearerTokenAuthenticationConverter(String principalClaimName,
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
