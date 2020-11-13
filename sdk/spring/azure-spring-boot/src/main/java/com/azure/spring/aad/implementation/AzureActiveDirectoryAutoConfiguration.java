// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.aad.implementation;

import com.azure.spring.autoconfigure.aad.AADAuthenticationProperties;
import com.azure.spring.autoconfigure.aad.AADOAuth2UserService;
import com.azure.spring.autoconfigure.aad.GraphWebClient;
import com.azure.spring.autoconfigure.aad.ServiceEndpointsProperties;
import com.azure.spring.telemetry.TelemetrySender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnResource;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
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
import org.springframework.util.ClassUtils;
import org.springframework.web.reactive.function.client.WebClient;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.azure.spring.telemetry.TelemetryData.SERVICE_NAME;
import static com.azure.spring.telemetry.TelemetryData.getClassPackageSimpleName;

@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnResource(resources = "classpath:aad.enable.config")
@ConditionalOnClass(ClientRegistrationRepository.class)
@ConditionalOnProperty(prefix = "azure.activedirectory", value = {"client-id", "client-secret", "tenant-id"})
@PropertySource(value = "classpath:service-endpoints.properties")
@EnableConfigurationProperties({ AADAuthenticationProperties.class, ServiceEndpointsProperties.class })
public class AzureActiveDirectoryAutoConfiguration {

    private static final String DEFAULT_CLIENT = "azure";

    @Autowired
    private AADAuthenticationProperties aadAuthenticationProperties;
    @Autowired
    private ServiceEndpointsProperties serviceEndpointsProperties;

    @Bean
    @ConditionalOnMissingBean({ ClientRegistrationRepository.class, AzureClientRegistrationRepository.class })
    public AzureClientRegistrationRepository clientRegistrationRepository() {
        return new AzureClientRegistrationRepository(
            createDefaultClient(),
            createClientRegistrations()
        );
    }

    @Bean
    @ConditionalOnMissingBean
    public OAuth2AuthorizedClientRepository authorizedClientRepository(AzureClientRegistrationRepository repo) {
        return new AzureOAuth2AuthorizedClientRepository(repo);
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
            aadAuthenticationProperties,
            serviceEndpointsProperties,
            webClient
        );
    }

    @Bean
    @ConditionalOnProperty(prefix = "azure.activedirectory.user-group", value = "allowed-groups")
    public OAuth2UserService<OidcUserRequest, OidcUser> oidcUserService(GraphWebClient graphWebClient) {
        return new AADOAuth2UserService(graphWebClient);
    }

    private DefaultClient createDefaultClient() {
        ClientRegistration clientRegistration = toClientRegistrationBuilder(DEFAULT_CLIENT)
            .scope(allScopes())
            .build();
        return new DefaultClient(clientRegistration, defaultScopes());
    }

    private String[] allScopes() {
        List<String> result = openidScopes();
        for (AuthorizationProperties properties : aadAuthenticationProperties.getAuthorization().values()) {
            result.addAll(properties.scopes());
        }
        return result.toArray(new String[0]);
    }

    private String[] defaultScopes() {
        List<String> result = openidScopes();
        AuthorizationProperties authorizationProperties =
            aadAuthenticationProperties.getAuthorization().get(DEFAULT_CLIENT);
        if (authorizationProperties != null) {
            result.addAll(authorizationProperties.scopes());
        }
        return result.toArray(new String[0]);
    }

    private List<String> openidScopes() {
        List<String> result = new ArrayList<>();
        result.add("openid");
        result.add("profile");
        if (!aadAuthenticationProperties.getAuthorization().isEmpty()) {
            result.add("offline_access");
        }
        return result;
    }

    private List<ClientRegistration> createClientRegistrations() {
        List<ClientRegistration> result = new ArrayList<>();
        for (String name : aadAuthenticationProperties.getAuthorization().keySet()) {
            if (DEFAULT_CLIENT.equals(name)) {
                continue;
            }
            AuthorizationProperties authorizationProperties =
                aadAuthenticationProperties.getAuthorization().get(name);
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
        AuthorizationServerEndpoints endpoints =
            new AuthorizationServerEndpoints(aadAuthenticationProperties.getAuthorizationServerUri());
        String tenantId = aadAuthenticationProperties.getTenantId();
        return ClientRegistration.withRegistrationId(registrationId)
                                 .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                                 .redirectUriTemplate("{baseUrl}/login/oauth2/code/{registrationId}")
                                 .clientId(aadAuthenticationProperties.getClientId())
                                 .clientSecret(aadAuthenticationProperties.getClientSecret())
                                 .authorizationUri(endpoints.authorizationEndpoint(tenantId))
                                 .tokenUri(endpoints.tokenEndpoint(tenantId))
                                 .jwkSetUri(endpoints.jwkSetEndpoint(tenantId));
    }

    @Configuration
    @ConditionalOnMissingBean(WebSecurityConfigurerAdapter.class)
    @EnableWebSecurity
    public static class DefaultAzureOAuth2WebSecurityConfigurerAdapter extends AzureOAuth2WebSecurityConfigurerAdapter {

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            super.configure(http);
            http.authorizeRequests().anyRequest().authenticated();
        }
    }

    @PostConstruct
    private void sendTelemetry() {
        if (aadAuthenticationProperties.isAllowTelemetry()) {
            final Map<String, String> events = new HashMap<>();
            final TelemetrySender sender = new TelemetrySender();
            events.put(SERVICE_NAME, getClassPackageSimpleName(AzureActiveDirectoryAutoConfiguration.class));
            sender.send(ClassUtils.getUserClass(getClass()).getSimpleName(), events);
        }
    }
}
