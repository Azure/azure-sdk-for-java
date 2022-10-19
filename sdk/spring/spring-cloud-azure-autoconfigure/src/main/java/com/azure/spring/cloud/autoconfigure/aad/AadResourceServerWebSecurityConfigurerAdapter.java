// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.aad;

import com.azure.spring.cloud.autoconfigure.aad.implementation.jwt.AadJwtGrantedAuthoritiesConverter;
import com.azure.spring.cloud.autoconfigure.aad.properties.AadResourceServerProperties;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.resource.OAuth2ResourceServerConfigurer;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.util.StringUtils;

/**
 * Abstract configuration class, used to make {@link OAuth2ResourceServerConfigurer.JwtConfigurer } and {@link AadJwtGrantedAuthoritiesConverter} take effect.
 *
 */
public abstract class AadResourceServerWebSecurityConfigurerAdapter {

    /**
     * A security builder to configure the SecurityFilterChain instance.
     */
    protected final HttpSecurity httpSecurity;

    final AadResourceServerProperties properties;

    /**
     * Creates a new instance
     * @param properties the {@link AadResourceServerProperties} to configure the OAuth2 clients
     * @param httpSecurity the security builder to configure the SecurityFilterChain instance.
     */
    public AadResourceServerWebSecurityConfigurerAdapter(AadResourceServerProperties properties, HttpSecurity httpSecurity) {
        this.properties = properties;
        this.httpSecurity = httpSecurity;
    }

    /**
     * The subclass can extend the HttpSecurity configuration if necessary, it will be invoked by {@link AadResourceServerWebSecurityConfigurerAdapter#build()}.
     * @param http the {@link HttpSecurity} to use
     * @throws Exception Configuration failed
     */
    protected void configure(HttpSecurity http) throws Exception {

    }

    /**
     * Build a SecurityFilterChain instance.
     *
     * @return a default SecurityFilterChain instance
     * @throws Exception Configuration failed
     */
    public SecurityFilterChain build() throws Exception {
        // @formatter:off
        httpSecurity.oauth2ResourceServer()
                    .jwt()
                    .jwtAuthenticationConverter(jwtAuthenticationConverter());
        // @formatter:off
        return httpSecurity.build();
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
