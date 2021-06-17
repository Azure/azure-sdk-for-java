// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.aad.webapi;

import com.azure.spring.common.AbstractJwtBearerTokenAuthenticationConverter;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthentication;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

import static com.azure.spring.common.AADJwtGrantedAuthoritiesConverter.DEFAULT_CLAIM_TO_AUTHORITY_PREFIX_MAP;

/**
 * A {@link Converter} that takes a {@link Jwt} and converts it into a {@link BearerTokenAuthentication}.
 */
public class AADJwtBearerTokenAuthenticationConverter extends AbstractJwtBearerTokenAuthenticationConverter {

    /**
     * Use AADJwtGrantedAuthoritiesConverter, it can resolve the access token of scp and roles.
     */
    public AADJwtBearerTokenAuthenticationConverter() {
        this(DEFAULT_PRINCIPAL_CLAIM_NAME, DEFAULT_CLAIM_TO_AUTHORITY_PREFIX_MAP);
    }

    /**
     * Using spring security provides JwtGrantedAuthoritiesConverter, it can resolve the access token of scp or roles.
     *
     * @param principalClaimName authorities claim name
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
