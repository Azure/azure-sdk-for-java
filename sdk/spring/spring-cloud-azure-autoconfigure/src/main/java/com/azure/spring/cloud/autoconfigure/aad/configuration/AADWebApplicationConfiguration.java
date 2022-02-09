// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.aad.configuration;

import com.azure.spring.cloud.autoconfigure.aad.AADWebSecurityConfigurerAdapter;
import com.azure.spring.cloud.autoconfigure.aad.implementation.conditions.AllWebApplicationCondition;
import com.azure.spring.cloud.autoconfigure.aad.implementation.webapp.AADOAuth2UserService;
import com.azure.spring.cloud.autoconfigure.aad.properties.AADAuthenticationProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

/**
 * Configure the necessary beans used for aad authentication and authorization.
 */
@Configuration(proxyBeanMethods = false)
@Conditional(AllWebApplicationCondition.class)
public class AADWebApplicationConfiguration {

    @Autowired
    private AADAuthenticationProperties properties;

    /**
     * Declare OAuth2UserService bean.
     *
     * @param properties the AAD authentication properties
     * @return OAuth2UserService bean
     */
    @Bean
    @ConditionalOnMissingBean
    public OAuth2UserService<OidcUserRequest, OidcUser> oidcUserService(AADAuthenticationProperties properties) {
        return new AADOAuth2UserService(properties);
    }

    /**
     * Sample configuration to make AzureActiveDirectoryOAuth2UserService take effect.
     */
    @Configuration(proxyBeanMethods = false)
    @EnableWebSecurity
    @EnableGlobalMethodSecurity(prePostEnabled = true)
    @ConditionalOnMissingBean(WebSecurityConfigurerAdapter.class)
    @ConditionalOnExpression("!'${spring.cloud.azure.active-directory.application-type}'.equalsIgnoreCase('web_application_and_resource_server')")
    public static class DefaultAADWebSecurityConfigurerAdapter extends AADWebSecurityConfigurerAdapter {

        /**
         * configure
         *
         * @param http the {@link HttpSecurity} to use
         * @throws Exception Configuration failed
         */
        @Override
        protected void configure(HttpSecurity http) throws Exception {
            super.configure(http);
            http.authorizeRequests()
                .antMatchers("/login").permitAll()
                .anyRequest().authenticated();
        }
    }
}
