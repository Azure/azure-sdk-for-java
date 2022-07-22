// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.aad;

import com.azure.spring.cloud.autoconfigure.aad.implementation.jwt.AadJwtGrantedAuthoritiesConverter;
import com.azure.spring.cloud.autoconfigure.aad.implementation.jwt.AadReactiveJwtGrantedAuthoritiesConverter;
import com.azure.spring.cloud.autoconfigure.aad.properties.AadResourceServerProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.util.StringUtils;

/**
 * Abstract configuration class, used to make JwtConfigurer and AADJwtBearerTokenAuthenticationConverter take effect.
 */
public abstract class AadResourceServerWebFluxSecurityConfiguration {

    @Autowired
    AadResourceServerProperties properties;

    /**
     * configure
     *
     * @param http the {@link ServerHttpSecurity} to use.
     * @return the {@link ServerHttpSecurity} to use.
     */
    protected ServerHttpSecurity serverHttpSecurity(ServerHttpSecurity http) {
        // @formatter:off
        return http.oauth2ResourceServer(server -> server
                .jwt()
                .jwtAuthenticationConverter(jwtAuthenticationConverter())
            );
        // @formatter:off
    }

    private AadReactiveJwtGrantedAuthoritiesConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        if (StringUtils.hasText(properties.getPrincipalClaimName())) {
            converter.setPrincipalClaimName(properties.getPrincipalClaimName());
        }
        converter.setJwtGrantedAuthoritiesConverter(
            new AadJwtGrantedAuthoritiesConverter(properties.getClaimToAuthorityPrefixMap()));
        return new AadReactiveJwtGrantedAuthoritiesConverter(converter);
    }
}
