// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.autoconfigure.b2c;

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
public class AADB2CJwtBearerTokenAuthenticationConverter extends AbstractJwtBearerTokenAuthenticationConverter {

    /**
     * Use AADJwtGrantedAuthoritiesConverter, it can resolve the access token of scp and roles.
     */
    public AADB2CJwtBearerTokenAuthenticationConverter() {
        super();
    }

    /**
     * Use JwtGrantedAuthoritiesConverter, it can resolve the access token of scp or roles.
     * @param authoritiesClaimName authorities claim name
     */
    public AADB2CJwtBearerTokenAuthenticationConverter(String authoritiesClaimName) {
        this(authoritiesClaimName, DEFAULT_AUTHORITY_PREFIX);
    }

    public AADB2CJwtBearerTokenAuthenticationConverter(String authoritiesClaimName, String authorityPrefix) {
        super(authoritiesClaimName, authorityPrefix);
    }

    @Override
    protected OAuth2AuthenticatedPrincipal getAuthenticatedPrincipal(Map<String, Object> headers,
                                                                     Map<String, Object> claims, Collection<GrantedAuthority> authorities, String tokenValue) {
        if (this.principalClaimName == null) {
            return new AADB2COAuth2AuthenticatedPrincipal(
                headers, claims, authorities, tokenValue);
        }
        String name = (String) claims.get(this.principalClaimName);
        return new AADB2COAuth2AuthenticatedPrincipal(headers,
            claims, authorities, tokenValue, name);
    }
}
