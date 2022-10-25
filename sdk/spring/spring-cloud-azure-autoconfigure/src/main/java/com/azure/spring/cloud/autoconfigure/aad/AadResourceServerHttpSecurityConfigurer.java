// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.aad;

import com.azure.spring.cloud.autoconfigure.aad.implementation.jwt.AadJwtGrantedAuthoritiesConverter;
import com.azure.spring.cloud.autoconfigure.aad.properties.AadResourceServerProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.resource.OAuth2ResourceServerConfigurer;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.util.StringUtils;

/**
 * HTTP security configurer class for Azure Active Directory resource server scenario, used to
 * make {@link OAuth2ResourceServerConfigurer.JwtConfigurer } and {@link AadJwtGrantedAuthoritiesConverter} take effect.
 *
 */
public class AadResourceServerHttpSecurityConfigurer extends AbstractHttpConfigurer<AadResourceServerHttpSecurityConfigurer, HttpSecurity> {

    private AadResourceServerProperties properties;

    @Override
    public void init(HttpSecurity builder) throws Exception {
        super.init(builder);
        ApplicationContext context = builder.getSharedObject(ApplicationContext.class);
        this.properties = context.getBean(AadResourceServerProperties.class);
        // @formatter:off
        builder.oauth2ResourceServer()
                   .jwt()
                   .jwtAuthenticationConverter(jwtAuthenticationConverter());
        // @formatter:off
    }

    public static AadResourceServerHttpSecurityConfigurer aadResourceServer() {
        return new AadResourceServerHttpSecurityConfigurer();
    }

    private Converter<Jwt, AbstractAuthenticationToken> jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        if (StringUtils.hasText(properties.getPrincipalClaimName())) {
            converter.setPrincipalClaimName(properties.getPrincipalClaimName());
        }
        converter.setJwtGrantedAuthoritiesConverter(
            new AadJwtGrantedAuthoritiesConverter(properties.getClaimToAuthorityPrefixMap()));
        return converter;
    }
}
