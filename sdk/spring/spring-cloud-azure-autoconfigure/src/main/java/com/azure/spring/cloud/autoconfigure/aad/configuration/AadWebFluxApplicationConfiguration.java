// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.aad.configuration;

import com.azure.spring.cloud.autoconfigure.aad.AadReactiveAuthenticationManager;
import com.azure.spring.cloud.autoconfigure.aad.AadReactiveSecurityContextRepository;
import com.azure.spring.cloud.autoconfigure.aad.AadWebFluxSecurityConfiguration;
import com.azure.spring.cloud.autoconfigure.aad.filter.UserPrincipalManager;
import com.azure.spring.cloud.autoconfigure.aad.implementation.webapp.AadOAuth2UserService;
import com.azure.spring.cloud.autoconfigure.aad.implementation.webapp.AadReactiveOAuth2UserService;
import com.azure.spring.cloud.autoconfigure.aad.properties.AadAuthenticationProperties;
import com.azure.spring.cloud.autoconfigure.aad.properties.AadAuthorizationServerEndpoints;
import com.nimbusds.jose.util.DefaultResourceRetriever;
import com.nimbusds.jose.util.ResourceRetriever;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.userinfo.ReactiveOAuth2UserService;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;

/**
 * Configure the necessary beans used for Azure AD authentication and authorization.
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
public class AadWebFluxApplicationConfiguration {

    /**
     * Declare OAuth2UserService bean.
     *
     * @param properties the Azure AD authentication properties
     * @return OAuth2UserService bean
     */
    @Bean
    @ConditionalOnMissingBean
    public ReactiveOAuth2UserService<OidcUserRequest, OidcUser> reactiveOidcUserService(AadAuthenticationProperties properties) {
        return new AadReactiveOAuth2UserService(new AadOAuth2UserService(properties));
    }

    /**
     * Declare authentication manager.
     *
     * @return OAuth2UserService bean
     */
    @Bean
    @ConditionalOnMissingBean
    public ReactiveAuthenticationManager reactiveAuthenticationManager() {
        return new AadReactiveAuthenticationManager();
    }

    /**
     * Declare authentication manager.
     *
     * @param properties the Azure AD authentication properties
     * @param authenticationManager the authentication manager.
     * @param resourceRetriever The resource retriever.
     * @return OAuth2UserService bean
     */
    @Bean
    @ConditionalOnMissingBean
    public ServerSecurityContextRepository reactiveSecurityContextRepository(AadAuthenticationProperties properties, AadReactiveAuthenticationManager authenticationManager, ResourceRetriever resourceRetriever) {
        AadAuthorizationServerEndpoints endpoints = new AadAuthorizationServerEndpoints(properties.getProfile().getEnvironment().getActiveDirectoryEndpoint(),
            properties.getProfile().getTenantId());
        UserPrincipalManager principalManager = new UserPrincipalManager(
            endpoints,
            properties,
            resourceRetriever,
            true
        );
        return new AadReactiveSecurityContextRepository(properties, endpoints, principalManager, authenticationManager);
    }

    /**
     * Declare JWT ResourceRetriever bean.
     *
     * @param properties The authentication properties.
     * @return JWT ResourceRetriever bean
     */
    @Bean
    @ConditionalOnMissingBean(ResourceRetriever.class)
    public ResourceRetriever jwtResourceRetriever(AadAuthenticationProperties properties) {
        return new DefaultResourceRetriever(
            (int) properties.getJwtConnectTimeout().toMillis(),
            (int) properties.getJwtReadTimeout().toMillis(),
            properties.getJwtSizeLimit()
        );
    }

    /**
     * Sample configuration to make AzureActiveDirectoryOAuth2UserService take effect.
     */
    @EnableWebFluxSecurity
    @EnableReactiveMethodSecurity
    @ConditionalOnMissingBean(SecurityWebFilterChain.class)
    @ConditionalOnExpression("!'${spring.cloud.azure.active-directory.application-type}'.equalsIgnoreCase('web_application_and_resource_server')")
    public static class DefaultAadWebFluxSecurityConfiguration extends AadWebFluxSecurityConfiguration {

        /**
         * configure
         *
         * @param http the {@link ServerHttpSecurity} to use
         * @return the filter chain
         */
        @Bean
        public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
            return super.serverHttpSecurity(http)
                .authorizeExchange(authorize -> authorize
                    .pathMatchers("/login").permitAll()
                    .anyExchange().authenticated()
                ).build();
        }
    }
}
