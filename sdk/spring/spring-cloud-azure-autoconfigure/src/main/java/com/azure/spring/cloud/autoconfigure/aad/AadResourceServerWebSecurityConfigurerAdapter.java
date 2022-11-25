// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.aad;

import com.azure.spring.cloud.autoconfigure.aad.implementation.jwt.AadJwtGrantedAuthoritiesConverter;
import com.azure.spring.cloud.autoconfigure.aad.properties.AadResourceServerProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.resource.OAuth2ResourceServerConfigurer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.util.StringUtils;

import java.util.Collection;

/**
 * Abstract configuration class, used to make JwtConfigurer and AADJwtBearerTokenAuthenticationConverter take effect.
 *
 * @see WebSecurityConfigurerAdapter
 */
public abstract class AadResourceServerWebSecurityConfigurerAdapter extends WebSecurityConfigurerAdapter {

    @Autowired
    private AadResourceServerProperties properties;

    private Converter<Jwt, Collection<GrantedAuthority>> jwtGrantedAuthorityConverter;

    /**
     * Creates a new instance with the default configuration.
     */
    public AadResourceServerWebSecurityConfigurerAdapter() {
    }

    /**
     * Sets the Azure AD properties and custom granted authority converter to creates a new instance,
     * the custom granted authority converter can be null.
     *
     * @param properties the Azure AD properties for Resource Server
     * @param jwtGrantedAuthorityConverter the custom converter for JWT granted authority
     */
    public AadResourceServerWebSecurityConfigurerAdapter(AadResourceServerProperties properties,
                                                         Converter<Jwt, Collection<GrantedAuthority>> jwtGrantedAuthorityConverter) {
        this.properties = properties;
        this.jwtGrantedAuthorityConverter = jwtGrantedAuthorityConverter;
    }

    /**
     * Apply the {@link OAuth2ResourceServerConfigurer}  for Azure AD OAuth2 Resource Server scenario.
     *
     * @param http the {@link HttpSecurity} to use
     * @throws Exception Configuration failed
     *
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // @formatter:off
        http.oauth2ResourceServer()
            .jwt()
            .jwtAuthenticationConverter(jwtAuthenticationConverter());
        // @formatter:off
    }

    private Converter<Jwt, AbstractAuthenticationToken> jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        if (StringUtils.hasText(properties.getPrincipalClaimName())) {
            converter.setPrincipalClaimName(properties.getPrincipalClaimName());
        }
        if (this.jwtGrantedAuthorityConverter != null) {
            converter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthorityConverter);
        } else {
            converter.setJwtGrantedAuthoritiesConverter(
                new AadJwtGrantedAuthoritiesConverter(properties.getClaimToAuthorityPrefixMap()));
        }
        return converter;
    }

    /**
     * Custom a JWT granted authority converter.
     * @param jwtGrantedAuthorityConverter the custom converter
     */
    public void setJwtGrantedAuthorityConverter(
        Converter<Jwt, Collection<GrantedAuthority>> jwtGrantedAuthorityConverter) {
        this.jwtGrantedAuthorityConverter = jwtGrantedAuthorityConverter;
    }
}
