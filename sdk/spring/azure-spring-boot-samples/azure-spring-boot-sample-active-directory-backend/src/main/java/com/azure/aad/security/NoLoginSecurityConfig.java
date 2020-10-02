// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.aad.security;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

// If "azure.activedirectory.tenant-id" is not configured,
// this bean will take effect to disable login.
@ConditionalOnMissingBean(AADOAuth2LoginSecurityConfig.class)
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class NoLoginSecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
            .antMatchers("/**")
            .permitAll();
    }
}
