// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.autoconfigure.b2c;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

/**
 * Abstract configuration class, used to configure B2C OAUTH2 login properties..
 */
public abstract class AADB2CWebSecurityConfigurerAdapter extends WebSecurityConfigurerAdapter {

    @Autowired
    private OAuth2UserService<OidcUserRequest, OidcUser> oidcUserService;
    @Autowired
    protected AADB2CProperties properties;
    @Autowired
    protected OAuth2AuthorizationRequestResolver requestResolver;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // @formatter:off
        http.authorizeRequests()
                .antMatchers("/login").permitAll()
                .anyRequest().authenticated()
                .and()
            .oauth2Login()
                .loginProcessingUrl(properties.getLoginProcessingUrl())
                .userInfoEndpoint()
                    .oidcUserService(oidcUserService)
                    .and()
                .authorizationEndpoint()
                    .authorizationRequestResolver(requestResolver)
                    .and()
            .and()
            .logout()
                .logoutSuccessHandler(b2cLogoutSuccessHandler())
                .and();
        // @formatter:off
    }

    @Bean
    @ConditionalOnMissingBean
    public LogoutSuccessHandler b2cLogoutSuccessHandler() {
        return new AADB2CLogoutSuccessHandler(properties);
    }
}
