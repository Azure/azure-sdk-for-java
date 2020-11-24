// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.aad.implementation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;

import java.util.ArrayList;
import java.util.List;

@Configuration
@ConditionalOnClass(ClientRegistrationRepository.class)
@EnableConfigurationProperties(AzureActiveDirectoryProperties.class)
public class AzureActiveDirectoryConfiguration {

    private static final String DEFAULT_CLIENT = "azure";

    @Autowired
    private AzureActiveDirectoryProperties config;

    @Bean
    @ConditionalOnMissingBean({ ClientRegistrationRepository.class, AzureClientRegistrationRepository.class })
    public AzureClientRegistrationRepository clientRegistrationRepository() {
        return new AzureClientRegistrationRepository(
            createDefaultClient(),
            createAuthzClients());
    }

    private DefaultClient createDefaultClient() {
        ClientRegistration.Builder builder = createClientBuilder(DEFAULT_CLIENT);
        builder.scope(allScopes());
        ClientRegistration client = builder.build();

        return new DefaultClient(client, defaultScopes());
    }

    private String[] allScopes() {
        List<String> result = openidScopes();
        for (AuthorizationProperties authz : config.getAuthorization().values()) {
            result.addAll(authz.getScopes());
        }
        return result.toArray(new String[0]);
    }

    private List<String> defaultScopes() {
        List<String> result = openidScopes();
        addAuthzDefaultScope(result);
        return result;
    }

    private void addAuthzDefaultScope(List<String> result) {
        AuthorizationProperties authz = config.getAuthorization().get(DEFAULT_CLIENT);
        if (authz != null) {
            result.addAll(authz.getScopes());
        }
    }

    private List<String> openidScopes() {
        List<String> result = new ArrayList<>();
        result.add("openid");
        result.add("profile");

        if (!config.getAuthorization().isEmpty()) {
            result.add("offline_access");
        }
        return result;
    }

    private List<ClientRegistration> createAuthzClients() {
        List<ClientRegistration> result = new ArrayList<>();
        for (String name : config.getAuthorization().keySet()) {
            if (DEFAULT_CLIENT.equals(name)) {
                continue;
            }

            AuthorizationProperties authz = config.getAuthorization().get(name);
            result.add(createClientBuilder(name, authz));
        }
        return result;
    }

    private ClientRegistration createClientBuilder(String id, AuthorizationProperties authz) {
        ClientRegistration.Builder result = createClientBuilder(id);
        result.scope(authz.getScopes());
        return result.build();
    }

    private ClientRegistration.Builder createClientBuilder(String id) {
        ClientRegistration.Builder result = ClientRegistration.withRegistrationId(id);
        result.authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE);
        result.redirectUriTemplate("{baseUrl}/login/oauth2/code/{registrationId}");

        result.clientId(config.getClientId());
        result.clientSecret(config.getClientSecret());

        IdentityEndpoints endpoints = new IdentityEndpoints(config.getUri());
        result.authorizationUri(endpoints.authorizationEndpoint(config.getTenantId()));
        result.tokenUri(endpoints.tokenEndpoint(config.getTenantId()));
        result.jwkSetUri(endpoints.jwkSetEndpoint(config.getTenantId()));

        return result;
    }

    @Bean
    @ConditionalOnMissingBean
    public OAuth2AuthorizedClientRepository authorizedClientRepository(AzureClientRegistrationRepository repo) {
        return new AzureAuthorizedClientRepository(repo);
    }

    @Configuration
    @ConditionalOnMissingBean(WebSecurityConfigurerAdapter.class)
    public static class DefaultAzureOAuth2Configuration extends AzureOAuth2Configuration {

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            super.configure(http);
            http.authorizeRequests().anyRequest().authenticated();
        }
    }
}
