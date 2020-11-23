// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.aad.implementation;

import com.azure.spring.autoconfigure.aad.AADAuthenticationProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;

@Configuration
@ConditionalOnClass(ClientRegistrationRepository.class)
@EnableConfigurationProperties(AADAuthenticationProperties.class)
public class AzureActiveDirectoryConfiguration {

    private static final String DEFAULT_CLIENT = "azure";

    @Autowired
    private AADAuthenticationProperties properties;

    @Bean
    @ConditionalOnMissingBean({ ClientRegistrationRepository.class, AzureClientRegistrationRepository.class })
    public AzureClientRegistrationRepository clientRegistrationRepository() {
        return new AzureClientRegistrationRepository(
            createDefaultClient(),
            createAuthzClients());
    }

    @Bean
    @ConditionalOnMissingBean
    public OAuth2AuthorizedClientRepository authorizedClientRepository(AzureClientRegistrationRepository repo) {
        return new AzureAuthorizedClientRepository(repo);
    }

    @Bean
    @ConditionalOnMissingBean
    WebClient webClient(
        ClientRegistrationRepository clientRegistrationRepository,
        OAuth2AuthorizedClientRepository oAuth2AuthorizedClientRepository
    ) {
        OAuth2AuthorizedClientManager oAuth2AuthorizedClientManager = new DefaultOAuth2AuthorizedClientManager(
            clientRegistrationRepository,
            oAuth2AuthorizedClientRepository
        );
        ServletOAuth2AuthorizedClientExchangeFilterFunction servletOAuth2AuthorizedClientExchangeFilterFunction =
            new ServletOAuth2AuthorizedClientExchangeFilterFunction(oAuth2AuthorizedClientManager);
        return WebClient.builder()
                        .apply(servletOAuth2AuthorizedClientExchangeFilterFunction.oauth2Configuration())
                        .build();
    }

    @Bean
    @ConditionalOnMissingBean
    GraphWebClient graphWebClient(WebClient webClient) {
        return new GraphWebClient(
            properties,
            webClient
        );
    }

    @Bean
    @ConditionalOnProperty(prefix = "azure.activedirectory.user-group", value = "allowed-groups")
    public OAuth2UserService<OidcUserRequest, OidcUser> oidcUserService(GraphWebClient graphWebClient) {
        return new AzureActiveDirectoryOAuth2UserService(graphWebClient);
    }

    private DefaultClient createDefaultClient() {
        ClientRegistration.Builder builder = createClientBuilder(DEFAULT_CLIENT);
        builder.scope(allScopes());
        ClientRegistration client = builder.build();

        return new DefaultClient(client, defaultScopes());
    }

    private String[] allScopes() {
        List<String> result = openidScopes();
        for (AuthorizationProperties authz : properties.getAuthorization().values()) {
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
        AuthorizationProperties authz = properties.getAuthorization().get(DEFAULT_CLIENT);
        if (authz != null) {
            result.addAll(authz.getScopes());
        }
    }

    private List<String> openidScopes() {
        List<String> result = new ArrayList<>();
        result.add("openid");
        result.add("profile");

        if (!properties.getAuthorization().isEmpty()) {
            result.add("offline_access");
        }
        return result;
    }

    private List<ClientRegistration> createAuthzClients() {
        List<ClientRegistration> result = new ArrayList<>();
        for (String name : properties.getAuthorization().keySet()) {
            if (DEFAULT_CLIENT.equals(name)) {
                continue;
            }

            AuthorizationProperties authz = properties.getAuthorization().get(name);
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

        result.clientId(properties.getClientId());
        result.clientSecret(properties.getClientSecret());

        AuthorizationServerEndpoints endpoints =
            new AuthorizationServerEndpoints(properties.getAuthorizationServerUri());
        result.authorizationUri(endpoints.authorizationEndpoint(properties.getTenantId()));
        result.tokenUri(endpoints.tokenEndpoint(properties.getTenantId()));
        result.jwkSetUri(endpoints.jwkSetEndpoint(properties.getTenantId()));

        return result;
    }

    @Configuration
    @ConditionalOnMissingBean(WebSecurityConfigurerAdapter.class)
    public static class DefaultAzureOAuth2Configuration extends AzureOAuth2Configuration {

        @Autowired
        private OAuth2UserService<OidcUserRequest, OidcUser> oidcUserService;

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            super.configure(http);
            http.authorizeRequests()
                .anyRequest().authenticated()
                .and()
                .oauth2Login()
                .userInfoEndpoint()
                .oidcUserService(oidcUserService);
        }
    }
}
