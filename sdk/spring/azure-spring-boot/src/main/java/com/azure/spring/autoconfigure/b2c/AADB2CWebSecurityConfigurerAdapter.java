// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.autoconfigure.b2c;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.client.endpoint.DefaultAuthorizationCodeTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
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
    private AADB2CProperties properties;
    @Autowired
    private OAuth2AuthorizationRequestResolver requestResolver;

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
                .tokenEndpoint()
                    .accessTokenResponseClient(accessTokenResponseClient())
                    .and()
            .and()
            .logout()
                .logoutSuccessHandler(b2cLogoutSuccessHandler())
                .and();
        // @formatter:on
    }

    @Bean
    @ConditionalOnMissingBean
    public LogoutSuccessHandler b2cLogoutSuccessHandler() {
        return new AADB2CLogoutSuccessHandler(properties);
    }

    protected OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> accessTokenResponseClient() {
        DefaultAuthorizationCodeTokenResponseClient result = new DefaultAuthorizationCodeTokenResponseClient();
        result.setRequestEntityConverter(
            new AADB2COAuth2AuthzCodeGrantRequestEntityConverter());
        return result;
    }
}
