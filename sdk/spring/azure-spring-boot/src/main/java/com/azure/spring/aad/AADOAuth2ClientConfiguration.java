// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.aad;

import com.azure.spring.aad.webapi.AADOBOOAuth2AuthorizedClientProvider;
import com.azure.spring.aad.webapp.AADAzureDelegatedOAuth2AuthorizedClientProvider;
import com.azure.spring.autoconfigure.aad.AADAuthenticationProperties;
import com.azure.spring.autoconfigure.condition.aad.ClientRegistrationCondition;
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
@Conditional(ClientRegistrationCondition.class)
public class AADOAuth2ClientConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public AADClientRegistrationRepository clientRegistrationRepository(AADAuthenticationProperties properties) {
        return new AADClientRegistrationRepository(properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public OAuth2AuthorizedClientRepository oAuth2AuthorizedClientRepository() {
        return new JacksonHttpSessionOAuth2AuthorizedClientRepository();
    }

    @Bean
    @ConditionalOnMissingBean
    public OAuth2AuthorizedClientManager authorizedClientManager(ClientRegistrationRepository clientRegistrations,
                                                                 OAuth2AuthorizedClientRepository authorizedClients) {
        DefaultOAuth2AuthorizedClientManager manager =
            new DefaultOAuth2AuthorizedClientManager(clientRegistrations, authorizedClients);
        AADAzureDelegatedOAuth2AuthorizedClientProvider azureDelegatedProvider =
            new AADAzureDelegatedOAuth2AuthorizedClientProvider(
                new RefreshTokenOAuth2AuthorizedClientProvider(),
                authorizedClients);
        AADOBOOAuth2AuthorizedClientProvider oboProvider = new AADOBOOAuth2AuthorizedClientProvider();
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
