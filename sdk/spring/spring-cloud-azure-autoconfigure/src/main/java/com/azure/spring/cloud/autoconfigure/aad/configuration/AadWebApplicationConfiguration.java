// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.aad.configuration;

import com.azure.spring.cloud.autoconfigure.aad.AadWebSecurityConfigurerAdapter;
import com.azure.spring.cloud.autoconfigure.aad.implementation.conditions.WebApplicationCondition;
import com.azure.spring.cloud.autoconfigure.aad.implementation.webapp.AadOAuth2UserService;
import com.azure.spring.cloud.autoconfigure.aad.properties.AadAuthenticationProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.web.client.RestTemplateBuilder;
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
 * Configure the necessary beans used for Azure AD authentication and authorization.
 */
@Configuration(proxyBeanMethods = false)
@Conditional(WebApplicationCondition.class)
public class AadWebApplicationConfiguration {

    private final RestTemplateBuilder restTemplateBuilder;

    /**
     * Creates a new instance of {@link AadWebApplicationConfiguration}.
     *
     * @param restTemplateBuilder the RestTemplateBuilder
     *
     */
    public AadWebApplicationConfiguration(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplateBuilder = restTemplateBuilder;
    }

    /**
     * Declare OAuth2UserService bean.
     *
     * @param properties the Azure AD authentication properties
     * @return OAuth2UserService bean
     */
    @Bean
    @ConditionalOnMissingBean
    public OAuth2UserService<OidcUserRequest, OidcUser> oidcUserService(AadAuthenticationProperties properties) {
        return new AadOAuth2UserService(properties, restTemplateBuilder);
    }

    /**
     * Sample configuration to make AzureActiveDirectoryOAuth2UserService take effect.
     */
    @EnableWebSecurity
    @EnableGlobalMethodSecurity(prePostEnabled = true)
    @ConditionalOnMissingBean(WebSecurityConfigurerAdapter.class)
    @ConditionalOnExpression("!'${spring.cloud.azure.active-directory.application-type}'.equalsIgnoreCase('web_application_and_resource_server')")
    public static class DefaultAadWebSecurityConfigurerAdapter extends AadWebSecurityConfigurerAdapter {

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
