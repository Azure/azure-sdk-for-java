// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.aad.webapi;

import com.azure.spring.aad.ClientRegistrationInitialization;
import com.azure.spring.aad.webapp.AzureClientRegistrationRepository;
import com.azure.spring.autoconfigure.aad.AADAuthenticationProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnResource;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.web.OAuth2LoginAuthenticationFilter;
import org.springframework.security.oauth2.server.resource.BearerTokenAuthenticationToken;

/**
 * <p>
 * The configuration will not be activated if no {@link OAuth2LoginAuthenticationFilter} class provided.
 * </p>
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnResource(resources = "classpath:aad.enable.config")
@EnableConfigurationProperties({ AADAuthenticationProperties.class })
@ConditionalOnClass({BearerTokenAuthenticationToken.class, OAuth2LoginAuthenticationFilter.class})
@ConditionalOnProperty(prefix = "azure.activedirectory", value = { "client-id", "client-secret", "tenant-id" })
public class AzureActiveDirectoryResourceServerClientConfiguration {

    @Autowired
    private AADAuthenticationProperties aadAuthenticationProperties;

    @Bean
    @ConditionalOnMissingBean({ ClientRegistrationRepository.class, AzureClientRegistrationRepository.class })
    public AzureClientRegistrationRepository clientRegistrationRepository() {
        ClientRegistrationInitialization clientRegistrationInitialization =
            new ClientRegistrationInitialization(aadAuthenticationProperties);
        return new AzureClientRegistrationRepository(
            clientRegistrationInitialization.createDefaultClient(),
            clientRegistrationInitialization.createAuthzClients(),
            aadAuthenticationProperties);
    }

    /**
     * Use AzureClientRegistrationRepository to create AADOAuth2OboAuthorizedClientRepository
     * @param repo client registration
     * @return AADOAuth2OboAuthorizedClientRepository Bean
     */
    @Bean
    @ConditionalOnMissingBean
    public OAuth2AuthorizedClientRepository oAuth2AuthorizedClientRepository(AzureClientRegistrationRepository repo) {
        return new AADOAuth2OboAuthorizedClientRepository(repo);
    }
}