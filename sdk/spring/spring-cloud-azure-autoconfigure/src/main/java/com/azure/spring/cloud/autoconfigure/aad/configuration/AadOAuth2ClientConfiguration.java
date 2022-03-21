// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.aad.configuration;

import com.azure.spring.cloud.autoconfigure.aad.implementation.conditions.ClientRegistrationCondition;
import com.azure.spring.cloud.autoconfigure.aad.implementation.conditions.ResourceServerWithOBOCondition;
import com.azure.spring.cloud.autoconfigure.aad.implementation.conditions.WebApplicationAndResourceServerCondition;
import com.azure.spring.cloud.autoconfigure.aad.implementation.conditions.WebApplicationWithoutResourceServerCondition;
import com.azure.spring.cloud.autoconfigure.aad.AadClientRegistrationRepository;
import com.azure.spring.cloud.autoconfigure.aad.implementation.oauth2.JacksonHttpSessionOAuth2AuthorizedClientRepository;
import com.azure.spring.cloud.autoconfigure.aad.implementation.webapi.AadOboOAuth2AuthorizedClientProvider;
import com.azure.spring.cloud.autoconfigure.aad.implementation.webapp.AadAzureDelegatedOAuth2AuthorizedClientProvider;
import com.azure.spring.cloud.autoconfigure.aad.properties.AadAuthenticationProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProvider;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.RefreshTokenOAuth2AuthorizedClientProvider;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;

/**
 * <p>
 * The configuration will not be activated if no {@link ClientRegistration} classes provided.
 * </p>
 */
@Configuration(proxyBeanMethods = false)
public class AadOAuth2ClientConfiguration {

    /**
     * OAuth2 client configuration for AAD.
     */
    @Configuration(proxyBeanMethods = false)
    @Conditional(ClientRegistrationCondition.class)
    public static class OAuth2ClientRepositoryConfiguration {

        /**
         * Declare ClientRegistrationRepository bean.
         *
         * @param properties the AAD authentication properties
         * @return ClientRegistrationRepository bean
         */
        @Bean
        @ConditionalOnMissingBean
        public ClientRegistrationRepository clientRegistrationRepository(AadAuthenticationProperties properties) {
            return new AadClientRegistrationRepository(properties);
        }

        /**
         * Declare OAuth2AuthorizedClientRepository bean.
         *
         * @return OAuth2AuthorizedClientRepository bean
         */
        @Bean
        @ConditionalOnMissingBean
        public OAuth2AuthorizedClientRepository oAuth2AuthorizedClientRepository() {
            return new JacksonHttpSessionOAuth2AuthorizedClientRepository();
        }
    }

    /**
     * Web application scenario, OAuth2AuthorizedClientManager configuration for AAD.
     */
    @Configuration(proxyBeanMethods = false)
    @Conditional(WebApplicationWithoutResourceServerCondition.class)
    public static class WebApplicationWithoutResourceServerOAuth2AuthorizedClientManagerConfiguration {

        /**
         * Declare OAuth2AuthorizedClientManager bean for Resource Server with OBO scenario.
         *
         * @param clientRegistrations the client registration repository
         * @param authorizedClients the OAuth2 authorized client repository
         * @return OAuth2AuthorizedClientManager bean
         */
        @Bean
        @ConditionalOnMissingBean
        public OAuth2AuthorizedClientManager authorizedClientManager(ClientRegistrationRepository clientRegistrations,
                                                                     OAuth2AuthorizedClientRepository authorizedClients) {
            DefaultOAuth2AuthorizedClientManager manager =
                new DefaultOAuth2AuthorizedClientManager(clientRegistrations, authorizedClients);
            AadAzureDelegatedOAuth2AuthorizedClientProvider azureDelegatedProvider =
                new AadAzureDelegatedOAuth2AuthorizedClientProvider(
                    new RefreshTokenOAuth2AuthorizedClientProvider(),
                    authorizedClients);
            OAuth2AuthorizedClientProvider authorizedClientProviders =
                OAuth2AuthorizedClientProviderBuilder.builder()
                                                     .authorizationCode()
                                                     .refreshToken()
                                                     .clientCredentials()
                                                     .password()
                                                     .provider(azureDelegatedProvider)
                                                     .build();
            manager.setAuthorizedClientProvider(authorizedClientProviders);
            return manager;
        }
    }

    /**
     * Resource server with OBO scenario, OAuth2AuthorizedClientManager configuration for AAD.
     */
    @Configuration(proxyBeanMethods = false)
    @Conditional(ResourceServerWithOBOCondition.class)
    public static class ResourceServerWithOBOOAuth2AuthorizedClientManagerConfiguration {

        /**
         * Declare OAuth2AuthorizedClientManager bean for Resource Server with OBO scenario.
         *
         * @param clientRegistrations the client registration repository
         * @param authorizedClients the OAuth2 authorized client repository
         * @return OAuth2AuthorizedClientManager bean
         */
        @Bean
        @ConditionalOnMissingBean
        public OAuth2AuthorizedClientManager authorizedClientManager(ClientRegistrationRepository clientRegistrations,
                                                                     OAuth2AuthorizedClientRepository authorizedClients) {
            DefaultOAuth2AuthorizedClientManager manager =
                new DefaultOAuth2AuthorizedClientManager(clientRegistrations, authorizedClients);
            AadOboOAuth2AuthorizedClientProvider oboProvider = new AadOboOAuth2AuthorizedClientProvider();
            OAuth2AuthorizedClientProvider authorizedClientProviders =
                OAuth2AuthorizedClientProviderBuilder.builder()
                                                     .authorizationCode()
                                                     .refreshToken()
                                                     .clientCredentials()
                                                     .password()
                                                     .provider(oboProvider)
                                                     .build();
            manager.setAuthorizedClientProvider(authorizedClientProviders);
            return manager;
        }
    }

    /**
     * Web application and resource server scenario, OAuth2AuthorizedClientManager configuration for AAD.
     */
    @Configuration(proxyBeanMethods = false)
    @Conditional(WebApplicationAndResourceServerCondition.class)
    public static class WebApplicationAndResourceServiceOAuth2AuthorizedClientManagerConfiguration {

        /**
         * Declare OAuth2AuthorizedClientManager bean.
         *
         * @param clientRegistrations the client registration repository
         * @param authorizedClients the OAuth2 authorized client repository
         * @return OAuth2AuthorizedClientManager bean
         */
        @Bean
        @ConditionalOnMissingBean
        public OAuth2AuthorizedClientManager authorizedClientManager(ClientRegistrationRepository clientRegistrations,
                                                                     OAuth2AuthorizedClientRepository authorizedClients) {
            DefaultOAuth2AuthorizedClientManager manager =
                new DefaultOAuth2AuthorizedClientManager(clientRegistrations, authorizedClients);
            AadAzureDelegatedOAuth2AuthorizedClientProvider azureDelegatedProvider =
                new AadAzureDelegatedOAuth2AuthorizedClientProvider(
                    new RefreshTokenOAuth2AuthorizedClientProvider(),
                    authorizedClients);
            AadOboOAuth2AuthorizedClientProvider oboProvider = new AadOboOAuth2AuthorizedClientProvider();
            OAuth2AuthorizedClientProvider authorizedClientProviders =
                OAuth2AuthorizedClientProviderBuilder.builder()
                                                     .authorizationCode()
                                                     .refreshToken()
                                                     .clientCredentials()
                                                     .password()
                                                     .provider(azureDelegatedProvider)
                                                     .provider(oboProvider)
                                                     .build();
            manager.setAuthorizedClientProvider(authorizedClientProviders);
            return manager;
        }
    }
}
