// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.aad;

import com.azure.spring.aad.webapi.AADOBOOAuth2AuthorizedClientProvider;
import com.azure.spring.autoconfigure.aad.AADAuthenticationProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnResource;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProvider;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.web.OAuth2LoginAuthenticationFilter;

/**
 * <p>
 * The configuration will not be activated if no {@link OAuth2LoginAuthenticationFilter} class provided.
 * </p>
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnResource(resources = "classpath:aad.enable.config")
@ConditionalOnProperty("azure.activedirectory.client-id")
@Conditional(AADConditions.ClientRegistrationCondition.class)
@EnableConfigurationProperties(AADAuthenticationProperties.class)
public class AADClientConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(AADClientConfiguration.class);

    @Autowired
    private AADAuthenticationProperties properties;

    @Bean
    @ConditionalOnMissingBean({ ClientRegistrationRepository.class })
    public ClientRegistrationRepository clientRegistrationRepository() {
        return new AADClientRegistrationRepository(properties);
    }

    @Bean
    @ConditionalOnMissingBean({ OAuth2AuthorizedClientRepository.class })
    public OAuth2AuthorizedClientRepository oAuth2AuthorizedClientRepository(AADClientRegistrationRepository repo,
                                                                             OAuth2AuthorizedClientService service) {
        return new AADOAuth2AuthorizedClientRepository(repo, service);
    }

    @Bean
    public OAuth2AuthorizedClientManager authorizeClientManager(ClientRegistrationRepository clients,
                                                                OAuth2AuthorizedClientRepository authorizedClients) {

        DefaultOAuth2AuthorizedClientManager manager =
            new DefaultOAuth2AuthorizedClientManager(clients, authorizedClients);
        OAuth2AuthorizedClientProvider authorizedClientProviders =
            OAuth2AuthorizedClientProviderBuilder.builder()
                                                 .authorizationCode()
                                                 .refreshToken()
                                                 .clientCredentials()
                                                 .password()
                                                 .provider(new AADOBOOAuth2AuthorizedClientProvider())
                                                 .build();
        manager.setAuthorizedClientProvider(authorizedClientProviders);
        return manager;
    }


}
