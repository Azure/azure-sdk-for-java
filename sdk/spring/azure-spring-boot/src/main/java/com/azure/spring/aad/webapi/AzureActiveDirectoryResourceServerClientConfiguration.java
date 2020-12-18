// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.aad.webapi;

import com.azure.spring.aad.webapp.AuthorizationServerEndpoints;
import com.azure.spring.autoconfigure.aad.AADAuthenticationProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnResource;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
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
public class AzureActiveDirectoryResourceServerClientConfiguration {

    @Autowired
    private AADAuthenticationProperties properties;

    @Bean
    @ConditionalOnMissingBean({ ClientRegistrationRepository.class, AADOboClientRegistrationRepository.class })
    public AADOboClientRegistrationRepository oboClientRegistrationRepository() {
        return new AADOboClientRegistrationRepository(createAuthzClients());
    }

    /**
     * Use AADOboClientRegistrationRepository to create AADOAuth2OboAuthorizedClientRepository
     * @param repo client registration
     * @return AADOAuth2OboAuthorizedClientRepository Bean
     */
    @Bean
    @ConditionalOnMissingBean
    public OAuth2AuthorizedClientRepository oAuth2AuthorizedClientRepository(AADOboClientRegistrationRepository repo) {
        return new AADOAuth2OboAuthorizedClientRepository(repo);
    }

    public List<ClientRegistration> createAuthzClients() {
        List<ClientRegistration> result = new ArrayList<>();
        for (String name : properties.getWebApiClients().keySet()) {
            AADOboAuthorizationProperties authz = properties.getWebApiClients().get(name);
            result.add(createClientBuilder(name, authz));
        }
        return result;
    }

    private ClientRegistration createClientBuilder(String id, AADOboAuthorizationProperties authz) {
        ClientRegistration.Builder result = createClientBuilder(id);
        result.scope(authz.getScopes());
        return result.build();
    }

    private ClientRegistration.Builder createClientBuilder(String id) {
        ClientRegistration.Builder result = ClientRegistration.withRegistrationId(id);
        result.authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN);
        result.redirectUriTemplate("{baseUrl}/login/oauth2/code/{registrationId}");

        result.clientId(properties.getClientId());
        result.clientSecret(properties.getClientSecret());

        AuthorizationServerEndpoints endpoints =
            new AuthorizationServerEndpoints(properties.getAuthorizationServerUri());
        result.authorizationUri(endpoints.authorizationEndpoint(properties.getTenantId()));
        result.tokenUri(endpoints.tokenEndpoint(properties.getTenantId()));
        result.jwkSetUri(endpoints.jwkSetEndpoint(properties.getTenantId()));

        return result;
    }

}
