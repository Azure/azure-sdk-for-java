// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.aad;

import com.azure.spring.cloud.autoconfigure.aad.properties.AadResourceServerProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

/**
 * Abstract configuration class, used to make JwtConfigurer and AADJwtBearerTokenAuthenticationConverter take effect.
 *
 * @see WebSecurityConfigurerAdapter
 */
public abstract class AadResourceServerWebSecurityConfigurerAdapter extends WebSecurityConfigurerAdapter {

    @Autowired
    AadResourceServerProperties properties;

    /**
     * configure
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
            .jwtAuthenticationConverter(
                new AadJwtBearerTokenAuthenticationConverter(
                    properties.getPrincipalClaimName(), properties.getClaimToAuthorityPrefixMap()));
        // @formatter:off
    }

}
