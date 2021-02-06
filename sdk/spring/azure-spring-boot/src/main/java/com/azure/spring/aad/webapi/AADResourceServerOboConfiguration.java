// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.aad.webapi;

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
public class AADResourceServerOboConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(AADResourceServerOboConfiguration.class);

    @Autowired
    private AADAuthenticationProperties properties;

    @Bean
    @ConditionalOnMissingBean({ ClientRegistrationRepository.class })
    public ClientRegistrationRepository clientRegistrationRepository() {
        final List<ClientRegistration> oboClients = createOboClients();
        if (oboClients.isEmpty()) {
            LOGGER.warn("No client registrations are found for AAD Obo.");
            return registrationId -> null;
        }
        return new InMemoryClientRegistrationRepository(oboClients);
    }

    /**
     * Use InMemoryClientRegistrationRepository to create AADOAuth2OboAuthorizedClientRepository
     *
     * @param repo client registration
     * @return AADOAuth2OboAuthorizedClientRepository Bean
     */
    @Bean
    @ConditionalOnMissingBean
    public OAuth2AuthorizedClientRepository oAuth2AuthorizedClientRepository(ClientRegistrationRepository repo) {
        return new AADOAuth2OboAuthorizedClientRepository(repo);
    }

    public List<ClientRegistration> createOboClients() {
        List<ClientRegistration> result = new ArrayList<>();
        for (String name : properties.getAuthorizationClients().keySet()) {
            AuthorizationClientProperties authorizationProperties = properties.getAuthorizationClients().get(name);
            ClientRegistration.Builder builder = createClientBuilder(name);
            builder.scope(authorizationProperties.getScopes());
            result.add(builder.build());
        }
        return result;
    }

    private ClientRegistration.Builder createClientBuilder(String id) {
        ClientRegistration.Builder result = ClientRegistration.withRegistrationId(id);
        result.authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN);
        result.redirectUriTemplate("{baseUrl}/login/oauth2/code/");
        result.clientId(properties.getClientId());
        result.clientSecret(properties.getClientSecret());

        AADAuthorizationServerEndpoints endpoints = new AADAuthorizationServerEndpoints(
            properties.getBaseUri(), properties.getTenantId());
        result.authorizationUri(endpoints.authorizationEndpoint());
        return result;
    }

}
