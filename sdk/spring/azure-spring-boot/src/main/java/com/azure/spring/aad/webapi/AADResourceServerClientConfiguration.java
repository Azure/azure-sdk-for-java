// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.aad.webapi;

import com.azure.spring.aad.AADAuthorizationGrantType;
import com.azure.spring.aad.AADAuthorizationServerEndpoints;
import com.azure.spring.aad.webapp.AuthorizationClientProperties;
import com.azure.spring.autoconfigure.aad.AADAuthenticationProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnResource;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.InMemoryOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.web.OAuth2LoginAuthenticationFilter;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.server.resource.BearerTokenAuthenticationToken;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * The configuration will not be activated if no {@link OAuth2LoginAuthenticationFilter} class provided.
 * </p>
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnResource(resources = "classpath:aad.enable.config")
@EnableConfigurationProperties({ AADAuthenticationProperties.class })
@ConditionalOnClass({ BearerTokenAuthenticationToken.class, OAuth2LoginAuthenticationFilter.class })
@ConditionalOnProperty(prefix = "azure.activedirectory", value = "client-id")
public class AADResourceServerClientConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(AADResourceServerClientConfiguration.class);

    @Autowired
    private AADAuthenticationProperties properties;

    @Bean
    @ConditionalOnMissingBean({ ClientRegistrationRepository.class })
    public ClientRegistrationRepository clientRegistrationRepository() {
        final List<ClientRegistration> oboClients = createClients();
        if (oboClients.isEmpty()) {
            LOGGER.warn("No client registrations are found for AAD Client.");
            return registrationId -> null;
        }
        return new InMemoryClientRegistrationRepository(oboClients);
    }

    @Bean
    @ConditionalOnMissingBean
    OAuth2AuthorizedClientService authorizedClientService(ClientRegistrationRepository clientRegistrationRepository) {
        return new InMemoryOAuth2AuthorizedClientService(clientRegistrationRepository);
    }

    /**
     * Use InMemoryClientRegistrationRepository to create AADOAuth2AuthorizedClientRepository
     *
     * @param repo client registration
     * @return AADOAuth2AuthorizedClientRepository Bean
     */
    @Bean
    @ConditionalOnMissingBean
    public OAuth2AuthorizedClientRepository oAuth2AuthorizedClientRepository(
        ClientRegistrationRepository repo,
        OAuth2AuthorizedClientService oAuth2AuthorizedClientService) {
        return new AADResourceServerOAuth2AuthorizedClientRepository(oAuth2AuthorizedClientService, repo);
    }

    public List<ClientRegistration> createClients() {
        List<ClientRegistration> result = new ArrayList<>();
        for (String id : properties.getAuthorizationClients().keySet()) {
            AuthorizationClientProperties authorizationProperties = properties.getAuthorizationClients().get(id);
            if (authorizationProperties.getAuthorizationGrantType() == null) {
                authorizationProperties.setAuthorizationGrantType(AADAuthorizationGrantType.ON_BEHALF_OF
                    .getValue());
                result.add(createOboClientBuilder(id, authorizationProperties));
            } else if (authorizationProperties.getAuthorizationGrantType().equals(AADAuthorizationGrantType
                .CLIENT_CREDENTIALS.getValue())) {
                result.add(createWebClientBuilder(id, authorizationProperties));
            }
        }
        return result;
    }

    private ClientRegistration createOboClientBuilder(String id,
                                                      AuthorizationClientProperties authorizationProperties) {
        ClientRegistration.Builder result = ClientRegistration.withRegistrationId(id);
        result.authorizationGrantType(new AuthorizationGrantType(AADAuthorizationGrantType.ON_BEHALF_OF
            .getValue()));
        result.redirectUri("{baseUrl}/login/oauth2/code/");
        result.clientId(properties.getClientId());
        result.clientSecret(properties.getClientSecret());

        AADAuthorizationServerEndpoints endpoints = new AADAuthorizationServerEndpoints(
            properties.getBaseUri(), properties.getTenantId());
        result.authorizationUri(endpoints.authorizationEndpoint());
        result.scope(authorizationProperties.getScopes());
        return result.build();
    }

    private ClientRegistration createWebClientBuilder(String id,
                                                      AuthorizationClientProperties authorizationProperties) {
        ClientRegistration.Builder result = ClientRegistration.withRegistrationId(id);
        result.authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS);
        result.clientId(properties.getClientId());
        result.clientSecret(properties.getClientSecret());
        AADAuthorizationServerEndpoints endpoints = new AADAuthorizationServerEndpoints(
            properties.getBaseUri(), properties.getTenantId());
        result.tokenUri(endpoints.tokenEndpoint());
        result.scope(authorizationProperties.getScopes());
        return result.build();
    }

}
