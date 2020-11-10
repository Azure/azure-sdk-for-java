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
public class AzureActiveDirectoryAutoConfiguration {

    private static final String DEFAULT_CLIENT = "azure";

    @Autowired
    private AzureActiveDirectoryProperties azureActiveDirectoryProperties;

    @Bean
    @ConditionalOnMissingBean({ ClientRegistrationRepository.class, AzureClientRegistrationRepository.class })
    public AzureClientRegistrationRepository clientRegistrationRepository() {
        return new AzureClientRegistrationRepository(
            createDefaultClient(),
            createClientRegistrations()
        );
    }

    private DefaultClient createDefaultClient() {
        ClientRegistration clientRegistration = toClientRegistrationBuilder(DEFAULT_CLIENT)
            .scope(allScopes())
            .build();
        return new DefaultClient(clientRegistration, defaultScopes());
    }

    private String[] allScopes() {
        List<String> result = openidScopes();
        for (AuthorizationProperties authz : azureActiveDirectoryProperties.getAuthorization().values()) {
            result.addAll(authz.scopes());
        }
        return result.toArray(new String[0]);
    }

    private String[] defaultScopes() {
        List<String> result = openidScopes();
        AuthorizationProperties authorizationProperties =
            azureActiveDirectoryProperties.getAuthorization().get(DEFAULT_CLIENT);
        if (authorizationProperties != null) {
            result.addAll(authorizationProperties.scopes());
        }
        return result.toArray(new String[0]);
    }

    private List<String> openidScopes() {
        List<String> result = new ArrayList<>();
        result.add("openid");
        result.add("profile");
        if (!azureActiveDirectoryProperties.getAuthorization().isEmpty()) {
            result.add("offline_access");
        }
        return result;
    }

    private List<ClientRegistration> createClientRegistrations() {
        List<ClientRegistration> result = new ArrayList<>();
        for (String name : azureActiveDirectoryProperties.getAuthorization().keySet()) {
            if (DEFAULT_CLIENT.equals(name)) {
                continue;
            }
            AuthorizationProperties authorizationProperties =
                azureActiveDirectoryProperties.getAuthorization().get(name);
            result.add(toClientRegistration(name, authorizationProperties));
        }
        return result;
    }

    private ClientRegistration toClientRegistration(String id, AuthorizationProperties authorizationProperties) {
        return toClientRegistrationBuilder(id)
            .scope(authorizationProperties.getScope())
            .build();
    }

    private ClientRegistration.Builder toClientRegistrationBuilder(String registrationId) {
        IdentityEndpoints endpoints = new IdentityEndpoints(azureActiveDirectoryProperties.getUri());
        String tenantId = azureActiveDirectoryProperties.getTenantId();
        return ClientRegistration.withRegistrationId(registrationId)
                                 .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                                 .redirectUriTemplate("{baseUrl}/login/oauth2/code/{registrationId}")
                                 .clientSecret(azureActiveDirectoryProperties.getClientSecret())
                                 .authorizationUri(endpoints.authorizationEndpoint(tenantId))
                                 .tokenUri(endpoints.tokenEndpoint(tenantId))
                                 .jwkSetUri(endpoints.jwkSetEndpoint(tenantId));
    }

    @Bean
    @ConditionalOnMissingBean
    public OAuth2AuthorizedClientRepository authorizedClientRepository(AzureClientRegistrationRepository repo) {
        return new AzureOAuth2AuthorizedClientRepository(repo);
    }

    @Configuration
    @ConditionalOnMissingBean(WebSecurityConfigurerAdapter.class)
    public static class DefaultAzureOAuth2WebSecurityConfigurerAdapter extends AzureOAuth2WebSecurityConfigurerAdapter {

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            super.configure(http);
            http.authorizeRequests().anyRequest().authenticated();
        }
    }
}
