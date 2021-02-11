// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.aad.webapp;

import com.azure.spring.aad.AADAuthorizationServerEndpoints;
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
import org.springframework.security.config.annotation.ObjectPostProcessor;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.azure.spring.aad.AADClientRegistrationRepository.AZURE_CLIENT_REGISTRATION_ID;

/**
 * Configure the necessary beans used for aad authentication and authorization.
 */
@Configuration
@ConditionalOnMissingClass({ "org.springframework.security.oauth2.server.resource.BearerTokenAuthenticationToken" })
@ConditionalOnClass(ClientRegistrationRepository.class)
@ConditionalOnProperty("azure.activedirectory.client-id")
@EnableConfigurationProperties(AADAuthenticationProperties.class)
public class AADWebAppConfiguration {

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
        return new AADOAuth2AuthorizedClientRepository(repo);
    }

    @Bean
    public OAuth2UserService<OidcUserRequest, OidcUser> oidcUserService(AADAuthenticationProperties properties) {
        return new AADOAuth2UserService(properties);
    }

    private AzureClientRegistration createDefaultClient() {
        ClientRegistration.Builder builder = createClientBuilder(AZURE_CLIENT_REGISTRATION_ID);
        Set<String> authorizationCodeScopes = authorizationCodeScopes();
        builder.scope(authorizationCodeScopes);
        ClientRegistration client = builder.build();
        Set<String> accessTokenScopes = accessTokenScopes();
        if (resourceServerCount(accessTokenScopes) == 0 && resourceServerCount((authorizationCodeScopes)) > 1) {
            // AAD server will return error if:
            // 1. authorizationCodeScopes have more than one resource server.
            // 2. accessTokenScopes have no resource server
            accessTokenScopes.add(properties.getGraphBaseUri() + "User.Read");
        }
        return new AzureClientRegistration(client, accessTokenScopes);
    }

    public static int resourceServerCount(Set<String> scopes) {
        return (int) scopes.stream()
                           .filter(scope -> scope.contains("/"))
                           .map(scope -> scope.substring(0, scope.lastIndexOf('/')))
                           .distinct()
                           .count();
    }

    private Set<String> authorizationCodeScopes() {
        Set<String> result = accessTokenScopes();
        for (AuthorizationClientProperties authProperties : properties.getAuthorizationClients().values()) {
            if (!authProperties.isOnDemand()) {
                result.addAll(authProperties.getScopes());
            }
        }
        return result;
    }

    private Set<String> accessTokenScopes() {
        Set<String> result = Optional.of(properties)
                                     .map(AADAuthenticationProperties::getAuthorizationClients)
                                     .map(clients -> clients.get(AZURE_CLIENT_REGISTRATION_ID))
                                     .map(AuthorizationClientProperties::getScopes)
                                     .map(Collection::stream)
                                     .orElseGet(Stream::empty)
                                     .collect(Collectors.toSet());
        result.addAll(openidScopes());
        if (properties.allowedGroupsConfigured()) {
            // The 2 scopes are need to get group name from graph.
            result.add(properties.getGraphBaseUri() + "User.Read");
            result.add(properties.getGraphBaseUri() + "Directory.Read.All");
        }
        return result;
    }

    private Set<String> openidScopes() {
        Set<String> result = new HashSet<>();
        result.add("openid");
        result.add("profile");

        if (!properties.getAuthorizationClients().isEmpty()) {
            result.add("offline_access");
        }
        return result;
    }

    private List<ClientRegistration> createAuthzClients() {
        List<ClientRegistration> result = new ArrayList<>();
        for (String name : properties.getAuthorizationClients().keySet()) {
            if (AZURE_CLIENT_REGISTRATION_ID.equals(name)) {
                continue;
            }

            AuthorizationClientProperties authz = properties.getAuthorizationClients().get(name);
            result.add(createClientBuilder(name, authz));
        }
        return result;
    }

    private ClientRegistration createClientBuilder(String id, AuthorizationClientProperties authz) {
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
        result.redirectUriTemplate("{baseUrl}/login/oauth2/code/");

        result.clientId(properties.getClientId());
        result.clientSecret(properties.getClientSecret());

        AADAuthorizationServerEndpoints endpoints =
            new AADAuthorizationServerEndpoints(properties.getBaseUri(), properties.getTenantId());
        result.authorizationUri(endpoints.authorizationEndpoint());
        result.tokenUri(endpoints.tokenEndpoint());
        result.jwkSetUri(endpoints.jwkSetEndpoint());

        Map<String, Object> configurationMetadata = new LinkedHashMap<>();
        String endSessionEndpoint = endpoints.endSessionEndpoint();
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
    public static class DefaultAADWebSecurityConfigurerAdapter extends AADWebSecurityConfigurerAdapter {

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            super.configure(http);
            http.authorizeRequests()
                .antMatchers("/login").permitAll()
                .anyRequest().authenticated();
        }
    }

}
