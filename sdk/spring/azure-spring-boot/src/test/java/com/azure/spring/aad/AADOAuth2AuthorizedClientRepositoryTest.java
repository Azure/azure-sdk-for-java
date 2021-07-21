// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.aad;

import com.azure.spring.aad.webapi.AADResourceServerWebSecurityConfigurerAdapter;
import com.azure.spring.aad.webapp.AADWebSecurityConfigurerAdapter;
import com.azure.spring.autoconfigure.aad.AADAutoConfiguration;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.client.OAuth2AuthorizationContext;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.core.AbstractOAuth2Token;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.server.resource.BearerTokenAuthenticationToken;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

public class AADOAuth2AuthorizedClientRepositoryTest {

    @Test
    public void webApplicationLoadInitAzureAuthzClient() {
        try (MockedStatic<RequestContextHolder> requestContextHolder =
                 mockStatic(RequestContextHolder.class, Mockito.CALLS_REAL_METHODS)) {
            new WebApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(AADAutoConfiguration.class))
                .withClassLoader(new FilteredClassLoader(BearerTokenAuthenticationToken.class))
                .withPropertyValues(
                    "azure.activedirectory.client-id = fake-client-id",
                    "azure.activedirectory.client-secret = fake-client-secret",
                    "azure.activedirectory.tenant-id = fake-tenant-id",
                    "azure.activedirectory.authorization-clients.graph.scopes = Calendars.Read",
                    "azure.activedirectory.base-uri = http://localhost/")
                .run(context -> {
                    AADClientRegistrationRepository clientRepo =
                        context.getBean(AADClientRegistrationRepository.class);
                    ClientRegistration azure = clientRepo.findByRegistrationId("azure");
                    JacksonHttpSessionOAuth2AuthorizedClientRepository actualDelegated =
                        new JacksonHttpSessionOAuth2AuthorizedClientRepository();

                    OAuth2AuthorizedClientRepository authorizedRepo = new AADOAuth2AuthorizedClientRepository(
                        clientRepo,
                        actualDelegated,
                        OAuth2AuthorizationContext::getAuthorizedClient);
                    MockHttpServletRequest request = new MockHttpServletRequest();
                    MockHttpServletResponse response = new MockHttpServletResponse();

                    ServletRequestAttributes attributes = mock(ServletRequestAttributes.class);
                    requestContextHolder.when(RequestContextHolder::currentRequestAttributes).thenReturn(attributes);
                    when(attributes.getResponse()).thenReturn(response);

                    Authentication authentication = createAuthentication();
                    authorizedRepo.saveAuthorizedClient(
                        createAuthorizedClient(azure),
                        authentication,
                        request,
                        response);

                    OAuth2AuthorizedClient client = authorizedRepo.loadAuthorizedClient(
                        "graph",
                        authentication,
                        request);
                    OAuth2AuthorizedClient sameClient = actualDelegated.loadAuthorizedClient(
                        "graph",
                        authentication,
                        request);
                    assertClient(client);
                    assertTrue(isTokenExpired(client.getAccessToken()));
                    isSameOAuth2AuthorizedClient(client, sameClient);
                });
        }
    }

    @Test
    public void webApplicationSaveAndLoadAzureAuthzClient() {
        try (MockedStatic<RequestContextHolder> requestContextHolder =
                 mockStatic(RequestContextHolder.class, Mockito.CALLS_REAL_METHODS)) {
            new WebApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(AADAutoConfiguration.class))
                .withClassLoader(new FilteredClassLoader(BearerTokenAuthenticationToken.class))
                .withPropertyValues(
                    "azure.activedirectory.client-id = fake-client-id",
                    "azure.activedirectory.client-secret = fake-client-secret",
                    "azure.activedirectory.tenant-id = fake-tenant-id",
                    "azure.activedirectory.authorization-clients.graph.scopes = Calendars.Read",
                    "azure.activedirectory.base-uri = http://localhost/")
                .run(context -> {
                    AADClientRegistrationRepository clientRepo =
                        context.getBean(AADClientRegistrationRepository.class);
                    ClientRegistration graph = clientRepo.findByRegistrationId("graph");
                    JacksonHttpSessionOAuth2AuthorizedClientRepository actualDelegated =
                        new JacksonHttpSessionOAuth2AuthorizedClientRepository();
                    OAuth2AuthorizedClientRepository authorizedRepo = new AADOAuth2AuthorizedClientRepository(
                        clientRepo,
                        actualDelegated,
                        OAuth2AuthorizationContext::getAuthorizedClient);
                    MockHttpServletRequest request = new MockHttpServletRequest();
                    MockHttpServletResponse response = new MockHttpServletResponse();
                    ServletRequestAttributes attributes = mock(ServletRequestAttributes.class);
                    requestContextHolder.when(RequestContextHolder::currentRequestAttributes).thenReturn(attributes);
                    when(attributes.getResponse()).thenReturn(response);

                    Authentication authentication = createAuthentication();
                    authorizedRepo.saveAuthorizedClient(
                        createAuthorizedClient(graph),
                        authentication,
                        request,
                        response);

                    OAuth2AuthorizedClient client = authorizedRepo.loadAuthorizedClient(
                        "graph",
                        authentication,
                        request);
                    OAuth2AuthorizedClient sameClient = actualDelegated.loadAuthorizedClient(
                        "graph",
                        authentication,
                        request);
                    assertClient(client);
                    isSameOAuth2AuthorizedClient(client, sameClient);
                });
        }
    }

    /**
     * Save Obo client registration into session, and load the client.
     */
    @Disabled
    @Test
    public void resourceServerWithOboSaveAndLoadAuthzClient() {
        new WebApplicationContextRunner()
            .withConfiguration(
                AutoConfigurations.of(AADAutoConfiguration.class))
            .withPropertyValues(
                "azure.activedirectory.client-id = fake-client-id",
                "azure.activedirectory.client-secret = fake-client-secret",
                "azure.activedirectory.tenant-id = fake-tenant-id",
                "azure.activedirectory.authorization-clients.graph.scopes = Calendars.Read",
                "azure.activedirectory.base-uri = http://localhost/")
            .run(context -> {
                AADClientRegistrationRepository clientRepo =
                    context.getBean(AADClientRegistrationRepository.class);
                ClientRegistration graph = clientRepo.findByRegistrationId("graph");
                JacksonHttpSessionOAuth2AuthorizedClientRepository authorizedClientRepository =
                    new JacksonHttpSessionOAuth2AuthorizedClientRepository();
                OAuth2AuthorizedClientRepository authorizedRepo = new AADOAuth2AuthorizedClientRepository(
                    clientRepo,
                    authorizedClientRepository,
                    OAuth2AuthorizationContext::getAuthorizedClient);
                MockHttpServletRequest request = new MockHttpServletRequest();
                MockHttpServletResponse response = new MockHttpServletResponse();
                Authentication authentication = createAuthentication();
                authorizedRepo.saveAuthorizedClient(
                    createAuthorizedClient(graph),
                    authentication,
                    request,
                    response);
                // Todo (v-moaryc) Add the deserializer of grant type 'on_behalf_of' when loading OAuth2AuthorizedClient for OBO client.
                OAuth2AuthorizedClient client = authorizedRepo.loadAuthorizedClient(
                    "graph",
                    authentication,
                    request);
                assertNotNull(client);
            });
    }

    @Test
    public void webApplicationAndResourceServerSaveAndLoadAuthzClient() {
        try (MockedStatic<RequestContextHolder> requestContextHolder =
                 mockStatic(RequestContextHolder.class, Mockito.CALLS_REAL_METHODS)) {
            new WebApplicationContextRunner()
                .withConfiguration(
                    AutoConfigurations.of(
                        AADAutoConfiguration.class,
                        AADWebApplicationAndResourceServerConfig.class)
                )
                .withPropertyValues(
                    "azure.activedirectory.client-id = fake-client-id",
                    "azure.activedirectory.client-secret = fake-client-secret",
                    "azure.activedirectory.tenant-id = fake-tenant-id",
                    "azure.activedirectory.authorization-clients.graph.scopes = Calendars.Read",
                    "azure.activedirectory.authorization-clients.graph.authorization-grant-type = authorization_code",
                    "azure.activedirectory.authorization-clients.test.scopes = api://web-api/test.ExampleScope",
                    "azure.activedirectory.authorization-clients.test.authorization-grant-type = client_credentials",
                    "azure.activedirectory.base-uri = http://localhost/",
                    "azure.activedirectory.application-type=web_application_and_resource_server")
                .run(context -> {
                    AADClientRegistrationRepository clientRepo =
                        context.getBean(AADClientRegistrationRepository.class);
                    ClientRegistration graph = clientRepo.findByRegistrationId("graph");
                    ClientRegistration test = clientRepo.findByRegistrationId("test");
                    JacksonHttpSessionOAuth2AuthorizedClientRepository actualWebAppDelegated =
                        new JacksonHttpSessionOAuth2AuthorizedClientRepository();
                    AADOAuth2AuthorizedClientRepository authorizedRepo = new AADOAuth2AuthorizedClientRepository(
                        clientRepo,
                        actualWebAppDelegated,
                        OAuth2AuthorizationContext::getAuthorizedClient);

                    MockHttpServletRequest request = new MockHttpServletRequest();
                    MockHttpServletResponse response = new MockHttpServletResponse();
                    ServletRequestAttributes attributes = mock(ServletRequestAttributes.class);
                    requestContextHolder.when(RequestContextHolder::currentRequestAttributes).thenReturn(attributes);
                    when(attributes.getResponse()).thenReturn(response);

                    Authentication authentication = createAuthentication();
                    AnonymousAuthenticationToken anonymousAuthenticationToken = new AnonymousAuthenticationToken(
                        "anonymous", "anonymousUser", AuthorityUtils.createAuthorityList("ROLE_USER"));
                    authorizedRepo.saveAuthorizedClient(
                        createAuthorizedClient(graph),
                        authentication,
                        request,
                        response);
                    authorizedRepo.saveAuthorizedClient(
                        createAuthorizedClient(test),
                        anonymousAuthenticationToken,
                        request,
                        response);

                    OAuth2AuthorizedClient graphClient = authorizedRepo.loadAuthorizedClient(
                        "graph",
                        authentication,
                        request);
                    OAuth2AuthorizedClient sameGraphClient = actualWebAppDelegated.loadAuthorizedClient(
                        "graph",
                        authentication,
                        request);
                    assertClient(graphClient);
                    isSameOAuth2AuthorizedClient(graphClient, sameGraphClient);

                    OAuth2AuthorizedClient testClient = authorizedRepo.loadAuthorizedClient(
                        "test",
                        anonymousAuthenticationToken,
                        request);
                    OAuth2AuthorizedClient sameTestClient = actualWebAppDelegated.loadAuthorizedClient(
                        "test",
                        anonymousAuthenticationToken,
                        request);
                    isSameOAuth2AuthorizedClient(testClient, sameTestClient);
                });
        }
    }

    @EnableWebSecurity
    @EnableGlobalMethodSecurity(prePostEnabled = true)
    public static class AADWebApplicationAndResourceServerConfig {

        @Order(1)
        @Configuration
        public static class ApiWebSecurityConfigurationAdapter extends AADResourceServerWebSecurityConfigurerAdapter {
            protected void configure(HttpSecurity http) throws Exception {
                super.configure(http);
                http.antMatcher("/api/**")
                    .authorizeRequests().anyRequest().authenticated();
            }
        }

        @Configuration
        public static class HtmlWebSecurityConfigurerAdapter extends AADWebSecurityConfigurerAdapter {

            @Override
            protected void configure(HttpSecurity http) throws Exception {
                super.configure(http);
                // @formatter:off
                http.authorizeRequests()
                    .antMatchers("/login").permitAll()
                    .anyRequest().authenticated();
                // @formatter:on
            }
        }
    }

    private void assertClient(OAuth2AuthorizedClient client) {
        assertNotNull(client);
        assertNotNull(client.getAccessToken());
        assertNotNull(client.getRefreshToken());
        assertEquals("fake-refresh-token", client.getRefreshToken().getTokenValue());
    }

    private OAuth2AuthorizedClient createAuthorizedClient(ClientRegistration client) {
        return new OAuth2AuthorizedClient(
            client,
            "fake-principal-name",
            createAccessToken(),
            createRefreshToken());
    }

    private OAuth2AccessToken createAccessToken() {
        return new OAuth2AccessToken(
            OAuth2AccessToken.TokenType.BEARER,
            "fake-access-token",
            Instant.MIN,
            Instant.MAX
        );
    }

    private OAuth2RefreshToken createRefreshToken() {
        return new OAuth2RefreshToken("fake-refresh-token", Instant.MIN);
    }

    private Authentication createAuthentication() {
        PreAuthenticatedAuthenticationToken authenticationToken =
            new PreAuthenticatedAuthenticationToken("fake-user", "fake-crednetial");
        authenticationToken.setAuthenticated(true);
        return authenticationToken;
    }

    private boolean isTokenExpired(OAuth2AccessToken token) {
        return Optional.ofNullable(token)
            .map(AbstractOAuth2Token::getExpiresAt)
            .map(expiresAt -> expiresAt.isBefore(Instant.now()))
            .orElse(false);
    }

    private void isSameOAuth2AuthorizedClient(OAuth2AuthorizedClient srcClient, OAuth2AuthorizedClient destClient) {
        assertNotNull(srcClient);
        assertNotNull(destClient);
        assertEquals(srcClient.getAccessToken().getTokenValue(), destClient.getAccessToken().getTokenValue());
        assertEquals(srcClient.getPrincipalName(), destClient.getPrincipalName());
        assertEquals(srcClient.getClientRegistration().getClientId(),
            destClient.getClientRegistration().getClientId());
        assertEquals(srcClient.getClientRegistration().getClientSecret(),
            destClient.getClientRegistration().getClientSecret());
    }
}
