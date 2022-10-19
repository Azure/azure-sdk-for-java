// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.aad.configuration;

import com.azure.spring.cloud.autoconfigure.aad.AadWebSecurityConfigurerAdapter;
import com.azure.spring.cloud.autoconfigure.aad.implementation.conditions.WebApplicationCondition;
import com.azure.spring.cloud.autoconfigure.aad.implementation.oauth2.OAuth2ClientAuthenticationJwkResolver;
import com.azure.spring.cloud.autoconfigure.aad.implementation.webapp.AadOAuth2UserService;
import com.azure.spring.cloud.autoconfigure.aad.properties.AadAuthenticationProperties;
import com.azure.spring.cloud.autoconfigure.aad.properties.AadResourceServerProperties;
import com.nimbusds.jose.jwk.JWK;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.SecurityFilterChain;

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
     * Default security configurer of the web application for using AAD authentication and authorization.
     */
    static class DefaultAadWebSecurityConfigurerAdapter extends AadWebSecurityConfigurerAdapter {

        /**
         * Creates a new instance
         *
         * @param properties the {@link AadAuthenticationProperties} to configure the OAuth2 clients
         * @param httpSecurity the security builder to configure the SecurityFilterChain instance.
         * @param repo the OAuth2 client repository
         * @param restTemplateBuilder the rest template builder
         * @param oidcUserService the user service to load OAuth2 user info
         * @param jwkResolvers the resolvers to resolve a {@link JWK}.
         */
        DefaultAadWebSecurityConfigurerAdapter(AadAuthenticationProperties properties,
                                                      HttpSecurity httpSecurity, ClientRegistrationRepository repo,
                                                      RestTemplateBuilder restTemplateBuilder,
                                                      OAuth2UserService<OidcUserRequest, OidcUser> oidcUserService,
                                                      ObjectProvider<OAuth2ClientAuthenticationJwkResolver> jwkResolvers) {
            super(properties, httpSecurity, repo, restTemplateBuilder, oidcUserService, jwkResolvers);
        }

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http.authorizeRequests()
                .antMatchers("/login").permitAll()
                .anyRequest().authenticated();
        }
    }

    /**
     * The default security configuration of the web application, user can write another configuration bean to override it.
     */
    @Configuration(proxyBeanMethods = false)
    @EnableWebSecurity
    @EnableGlobalMethodSecurity(prePostEnabled = true)
    @ConditionalOnMissingBean(SecurityFilterChain.class)
    @ConditionalOnExpression("!'${spring.cloud.azure.active-directory.application-type}'.equalsIgnoreCase('web_application_and_resource_server')")
    public static class DefaultAadWebSecurityConfiguration {

        /**
         * Create the {@link SecurityFilterChain} instance of the web application for Spring Security Filter Chain.
         * @param properties the {@link AadResourceServerProperties} to use
         * @param httpSecurity the {@link HttpSecurity} to use
         * @param repo the OAuth2 client repository
         * @param restTemplateBuilder the rest template builder
         * @param oidcUserService the user service to load OAuth2 user info
         * @param jwkResolvers the resolvers to resolve a {@link JWK}.
         * @return the {@link SecurityFilterChain} instance
         * @throws Exception Configuration failed
         */
        @Bean
        public SecurityFilterChain filterChain(AadAuthenticationProperties properties,
                                               HttpSecurity httpSecurity,
                                               ClientRegistrationRepository repo,
                                               RestTemplateBuilder restTemplateBuilder,
                                               OAuth2UserService<OidcUserRequest, OidcUser> oidcUserService,
                                               ObjectProvider<OAuth2ClientAuthenticationJwkResolver> jwkResolvers) throws Exception {
            return new DefaultAadWebSecurityConfigurerAdapter(
                properties, httpSecurity, repo, restTemplateBuilder, oidcUserService, jwkResolvers)
                .build();
        }
    }
}
