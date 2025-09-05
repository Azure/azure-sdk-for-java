// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.aad.security;

import com.azure.spring.cloud.autoconfigure.implementation.aad.configuration.properties.AadResourceServerProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.SecurityConfigurer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.resource.OAuth2ResourceServerConfigurer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.Collection;

/**
 * HTTP security configurer class for Azure Active Directory resource server scenario, used to
 * make {@link OAuth2ResourceServerConfigurer.JwtConfigurer } and {@link AadJwtGrantedAuthoritiesConverter} take effect.
 *
 */
public class AadResourceServerHttpSecurityConfigurer extends AbstractHttpConfigurer<AadResourceServerHttpSecurityConfigurer, HttpSecurity> {

    private AadResourceServerProperties properties;

    private Converter<Jwt, Collection<GrantedAuthority>> jwtGrantedAuthoritiesConverter;

    @Override
    public void init(HttpSecurity builder) throws Exception {
        super.init(builder);
        ApplicationContext context = builder.getSharedObject(ApplicationContext.class);
        this.properties = context.getBean(AadResourceServerProperties.class);
        // @formatter:off
        builder.oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())));
        // @formatter:off
    }

    /**
     * Default configurer for Resource Server with Azure AD.
     * @return the configurer instance to customize the {@link SecurityConfigurer}
     */
    public static AadResourceServerHttpSecurityConfigurer aadResourceServer() {
        return new AadResourceServerHttpSecurityConfigurer();
    }

    private Converter<Jwt, AbstractAuthenticationToken> jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        if (StringUtils.hasText(properties.getPrincipalClaimName())) {
            converter.setPrincipalClaimName(properties.getPrincipalClaimName());
        }
        if (this.jwtGrantedAuthoritiesConverter != null) {
            converter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter);
        } else {
            converter.setJwtGrantedAuthoritiesConverter(
                new AadJwtGrantedAuthoritiesConverter(properties.getClaimToAuthorityPrefixMap()));
        }
        return converter;
    }

    /**
     * Custom a JWT granted authority converter.
     * @param jwtGrantedAuthoritiesConverter the custom converter
     * @return the AadResourceServerHttpSecurityConfigurer for further customizations
     */
    public AadResourceServerHttpSecurityConfigurer jwtGrantedAuthoritiesConverter(
        Converter<Jwt, Collection<GrantedAuthority>> jwtGrantedAuthoritiesConverter) {
        Assert.notNull(jwtGrantedAuthoritiesConverter, "jwtGrantedAuthoritiesConverter cannot be null");
        this.jwtGrantedAuthoritiesConverter = jwtGrantedAuthoritiesConverter;
        return this;
    }
}
