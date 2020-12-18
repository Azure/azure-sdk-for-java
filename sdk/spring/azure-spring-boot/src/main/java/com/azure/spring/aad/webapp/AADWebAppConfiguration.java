// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.aad.webapp;

import com.azure.spring.autoconfigure.aad.AADAuthenticationProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.ObjectPostProcessor;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProvider;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.endpoint.DefaultRefreshTokenTokenResponseClient;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.web.method.annotation.OAuth2AuthorizedClientArgumentResolver;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.springframework.core.Ordered.HIGHEST_PRECEDENCE;

/**
 * Configure the necessary beans used for aad authentication and authorization.
 */
@Configuration
@ConditionalOnMissingClass({ "org.springframework.security.oauth2.server.resource.BearerTokenAuthenticationToken" })
@ConditionalOnClass(ClientRegistrationRepository.class)
@EnableConfigurationProperties(AADAuthenticationProperties.class)
@ConditionalOnProperty(prefix = "azure.activedirectory.user-group", value = "allowed-groups")
public class AADWebAppConfiguration {

    private static final String AZURE_CLIENT_REGISTRATION_ID = "azure";

    @Autowired
    private AADAuthenticationProperties properties;

    @Bean
    @ConditionalOnMissingBean({ ClientRegistrationRepository.class, AADWebAppClientRegistrationRepository.class })
    public AADWebAppClientRegistrationRepository clientRegistrationRepository() {
        return new AADWebAppClientRegistrationRepository(
            createDefaultClient(),
            createAuthzClients(),
            properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public OAuth2AuthorizedClientRepository authorizedClientRepository(AADWebAppClientRegistrationRepository repo) {
        return new AzureAuthorizedClientRepository(repo);
    }

    @Bean
    public OAuth2AuthorizedClientManager authorizedClientManager(
        ClientRegistrationRepository clientRegistrationRepository,
        OAuth2AuthorizedClientRepository authorizedClientRepository) {

        DefaultRefreshTokenTokenResponseClient responseClient = new DefaultRefreshTokenTokenResponseClient();
        responseClient.setRequestEntityConverter(
            new AzureOauth2RefreshTokenGrantRequestEntityConverter());

        OAuth2AuthorizedClientProvider authorizedClientProvider =
            OAuth2AuthorizedClientProviderBuilder.builder()
                .authorizationCode()
                .refreshToken(configurer -> configurer.accessTokenResponseClient(responseClient))
                .clientCredentials()
                .password()
                .build();

        DefaultOAuth2AuthorizedClientManager authorizedClientManager =
            new DefaultOAuth2AuthorizedClientManager(
                clientRegistrationRepository, authorizedClientRepository);
        authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider);
        return authorizedClientManager;
    }

    @Bean
    public OAuth2UserService<OidcUserRequest, OidcUser> oidcUserService(AADAuthenticationProperties properties) {
        return new AzureActiveDirectoryOAuth2UserService(properties);
    }

    private AzureClientRegistration createDefaultClient() {
        ClientRegistration.Builder builder = createClientBuilder(AZURE_CLIENT_REGISTRATION_ID);
        builder.scope(allScopes());
        ClientRegistration client = builder.build();

        return new AzureClientRegistration(client, accessTokenScopes());
    }

    private Set<String> allScopes() {
        Set<String> result = accessTokenScopes();
        for (AuthorizationProperties authProperties : properties.getAuthorization().values()) {
            if (!authProperties.isOnDemand()) {
                result.addAll(authProperties.getScopes());
            }
        }
        return result;
    }

    private Set<String> accessTokenScopes() {
        Set<String> result = openidScopes();
        if (properties.allowedGroupsConfigured()) {
            result.add("https://graph.microsoft.com/User.Read");
        }
        addAzureConfiguredScopes(result);
        return result;
    }

    private void addAzureConfiguredScopes(Set<String> result) {
        AuthorizationProperties azureProperties = properties.getAuthorization().get(AZURE_CLIENT_REGISTRATION_ID);
        if (azureProperties != null) {
            result.addAll(azureProperties.getScopes());
        }
    }

    private Set<String> openidScopes() {
        Set<String> result = new HashSet<>();
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
            if (AZURE_CLIENT_REGISTRATION_ID.equals(name)) {
                continue;
            }

            AuthorizationProperties authz = properties.getAuthorization().get(name);
            result.add(createClientBuilder(name, authz));
        }
        return result;
    }

    private ClientRegistration createClientBuilder(String id, AuthorizationProperties authz) {
        ClientRegistration.Builder result = createClientBuilder(id);
        List<String> scopes = authz.getScopes();
        if (authz.isOnDemand()) {
            if (!scopes.contains("openid")) {
                scopes.add("openid");
            }
            if (!scopes.contains("profile")) {
                scopes.add("profile");
            }
        }
        result.scope(scopes);
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

        Map<String, Object> configurationMetadata = new LinkedHashMap<>();
        String endSessionEndpoint = endpoints.endSessionEndpoint(properties.getTenantId());
        configurationMetadata.put("end_session_endpoint", endSessionEndpoint);
        result.providerConfigurationMetadata(configurationMetadata);

        return result;
    }

    /**
     * Sample configuration to make AzureActiveDirectoryOAuth2UserService take effect.
     */
    @Configuration
    @ConditionalOnBean(ObjectPostProcessor.class)
    @ConditionalOnMissingBean(WebSecurityConfigurerAdapter.class)
    public static class DefaultAzureOAuth2Configuration extends AzureOAuth2Configuration {

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            super.configure(http);
        }
    }

    /**
     * Temp solution to make RefreshTokenGrantRequestEntityConverter take effect.
     * TODO: remove this logic after spring-security can inject OAuth2AuthorizedClientManager
     * to OAuth2AuthorizedClientArgumentResolver
     * issue: https://github.com/spring-projects/spring-security/issues/8700
     */
    @Order(HIGHEST_PRECEDENCE)
    @Configuration
    public static class AzureWebMvcContext implements WebMvcConfigurer {

        @Autowired
        OAuth2AuthorizedClientManager clientManager;

        @Override
        public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
            argumentResolvers.add(new OAuth2AuthorizedClientArgumentResolver(clientManager));
        }
    }

}
