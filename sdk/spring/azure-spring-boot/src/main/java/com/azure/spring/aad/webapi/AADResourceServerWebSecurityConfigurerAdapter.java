// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.aad.webapi;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

/**
 * Abstract configuration class, used to make JwtConfigurer and AADJwtBearerTokenAuthenticationConverter take effect.
 */
public abstract class AADResourceServerWebSecurityConfigurerAdapter extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // @formatter:off
        http.oauth2ResourceServer()
            .jwt()
            .jwtAuthenticationConverter(new AADJwtBearerTokenAuthenticationConverter());
        // @formatter:off
    }

}
