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

/**
 * A {@link Converter} that takes a {@link Jwt} and converts it into a {@link BearerTokenAuthentication}.
 */
public class AADJwtBearerTokenAuthenticationConverter extends AbstractJwtBearerTokenAuthenticationConverter {

    private static final String DEFAULT_PRINCIPAL_CLAIM_NAME = "sub";

    /**
     * Use AADJwtGrantedAuthoritiesConverter, it can resolve the access token of scp and roles.
     */
    public AADJwtBearerTokenAuthenticationConverter() {
        super();
        principalClaimName = DEFAULT_PRINCIPAL_CLAIM_NAME;
    }

    /**
     * Using spring security provides JwtGrantedAuthoritiesConverter, it can resolve the access token of scp or roles.
     *
     * @param authoritiesClaimName authorities claim name
     */
    public AADJwtBearerTokenAuthenticationConverter(String authoritiesClaimName) {
        this(authoritiesClaimName, DEFAULT_AUTHORITY_PREFIX);
    }

    public AADJwtBearerTokenAuthenticationConverter(String authoritiesClaimName, String authorityPrefix) {
        super(authoritiesClaimName, authorityPrefix);
        principalClaimName = DEFAULT_PRINCIPAL_CLAIM_NAME;
    }

    @Override
    protected OAuth2AuthenticatedPrincipal getAuthenticatedPrincipal(Map<String, Object> headers,
                                                                     Map<String, Object> claims, Collection<GrantedAuthority> authorities, String tokenValue) {
        return new AADOAuth2AuthenticatedPrincipal(
            headers, claims, authorities, tokenValue, principalClaimName);
    }
}
