// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.aad.webapp;

import org.junit.Test;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.server.resource.BearerTokenAuthenticationToken;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AzureActiveDirectoryConfigurationTest {

    private final WebApplicationContextRunner contextRunner = new WebApplicationContextRunner()
        .withClassLoader(new FilteredClassLoader(BearerTokenAuthenticationToken.class))
        .withUserConfiguration(AzureActiveDirectoryConfiguration.class);

    @Test
    public void clientRegistered() {
        contextRunner
            .withPropertyValues(
                "azure.activedirectory.client-id = fake-client-id",
                "azure.activedirectory.client-secret = fake-client-secret",
                "azure.activedirectory.tenant-id = fake-tenant-id",
                "azure.activedirectory.user-group.allowed-groups = group1, group2")
            .run(context -> {
                ClientRegistrationRepository clientRepo = context.getBean(AzureClientRegistrationRepository.class);
                ClientRegistration azure = clientRepo.findByRegistrationId("azure");

                assertNotNull(azure);
                assertEquals("fake-client-id", azure.getClientId());
                assertEquals("fake-client-secret", azure.getClientSecret());

                AuthorizationServerEndpoints endpoints = new AuthorizationServerEndpoints();
                assertEquals(endpoints.authorizationEndpoint("fake-tenant-id"),
                    azure.getProviderDetails().getAuthorizationUri());
                assertEquals(endpoints.tokenEndpoint("fake-tenant-id"), azure.getProviderDetails().getTokenUri());
                assertEquals(endpoints.jwkSetEndpoint("fake-tenant-id"), azure.getProviderDetails().getJwkSetUri());
                assertEquals("{baseUrl}/login/oauth2/code/{registrationId}", azure.getRedirectUriTemplate());
                assertDefaultScopes(azure, "openid", "profile", "https://graph.microsoft.com/User.Read");
            });
    }

    @Test
    public void clientRequiresPermissionRegistered() {
        contextRunner
            .withPropertyValues(
                "azure.activedirectory.client-id = fake-client-id",
                "azure.activedirectory.client-secret = fake-client-secret",
                "azure.activedirectory.tenant-id = fake-tenant-id",
                "azure.activedirectory.user-group.allowed-groups = group1, group2",
                "azure.activedirectory.authorization.graph.scopes = Calendars.Read"
            )
            .run(context -> {
                ClientRegistrationRepository clientRepo = context.getBean(AzureClientRegistrationRepository.class);
                ClientRegistration azure = clientRepo.findByRegistrationId("azure");
                ClientRegistration graph = clientRepo.findByRegistrationId("graph");

                assertNotNull(azure);
                assertNotNull(graph);
                assertDefaultScopes(azure,
                    "openid", "profile", "offline_access", "https://graph.microsoft.com/User.Read", "Calendars.Read");
                assertDefaultScopes(graph, "Calendars.Read");
            });
    }

    @Test
    public void clientRequiresMultiPermissions() {
        contextRunner
            .withPropertyValues(
                "azure.activedirectory.client-id = fake-client-id",
                "azure.activedirectory.client-secret = fake-client-secret",
                "azure.activedirectory.tenant-id = fake-tenant-id",
                "azure.activedirectory.user-group.allowed-groups = group1, group2",
                "azure.activedirectory.authorization.graph.scopes = Calendars.Read",
                "azure.activedirectory.authorization.arm.scopes = https://management.core.windows.net/user_impersonation"
            )
            .run(context -> {
                ClientRegistrationRepository clientRepo = context.getBean(AzureClientRegistrationRepository.class);
                ClientRegistration azure = clientRepo.findByRegistrationId("azure");
                ClientRegistration graph = clientRepo.findByRegistrationId("graph");
                assertDefaultScopes(
                    azure,
                    "openid",
                    "profile",
                    "offline_access",
                    "Calendars.Read",
                    "https://graph.microsoft.com/User.Read",
                    "https://management.core.windows.net/user_impersonation");
                assertDefaultScopes(graph, "Calendars.Read");
            });
    }

    @Test
    public void clientRequiresPermissionInDefaultClient() {
        contextRunner
            .withPropertyValues(
                "azure.activedirectory.client-id = fake-client-id",
                "azure.activedirectory.client-secret = fake-client-secret",
                "azure.activedirectory.tenant-id = fake-tenant-id",
                "azure.activedirectory.user-group.allowed-groups = group1, group2",
                "azure.activedirectory.authorization.graph.scopes = Calendars.Read"
            )
            .run(context -> {
                ClientRegistrationRepository clientRepo = context.getBean(AzureClientRegistrationRepository.class);
                ClientRegistration azure = clientRepo.findByRegistrationId("azure");
                assertDefaultScopes(azure,
                    "openid", "profile", "offline_access", "https://graph.microsoft.com/User.Read", "Calendars.Read");
            });
    }

    @Test
    public void aadAwareClientRepository() {
        contextRunner
            .withPropertyValues(
                "azure.activedirectory.client-id = fake-client-id",
                "azure.activedirectory.client-secret = fake-client-secret",
                "azure.activedirectory.tenant-id = fake-tenant-id",
                "azure.activedirectory.user-group.allowed-groups = group1, group2",
                "azure.activedirectory.authorization.graph.scopes = Calendars.Read"
            )
            .run(context -> {
                AzureClientRegistrationRepository clientRepo = context.getBean(AzureClientRegistrationRepository.class);
                ClientRegistration azure = clientRepo.findByRegistrationId("azure");
                ClientRegistration graph = clientRepo.findByRegistrationId("graph");
                assertDefaultScopes(
                    clientRepo.getAzureClient(),
                    "openid", "profile", "offline_access", "https://graph.microsoft.com/User.Read"
                );
                assertEquals(clientRepo.getAzureClient().getClient(), azure);

                assertFalse(clientRepo.isAuthzClient(azure));
                assertTrue(clientRepo.isAuthzClient(graph));
                assertFalse(clientRepo.isAuthzClient("azure"));
                assertTrue(clientRepo.isAuthzClient("graph"));

                List<ClientRegistration> clients = collectClients(clientRepo);
                assertEquals(2, clients.size());
                assertEquals("graph", clients.get(0).getRegistrationId());
                assertEquals("azure", clients.get(1).getRegistrationId());
            });
    }

    @Test
    public void defaultClientWithAuthzScope() {
        contextRunner
            .withPropertyValues(
                "azure.activedirectory.client-id = fake-client-id",
                "azure.activedirectory.client-secret = fake-client-secret",
                "azure.activedirectory.tenant-id = fake-tenant-id",
                "azure.activedirectory.user-group.allowed-groups = group1, group2",
                "azure.activedirectory.authorization.azure.scopes = Calendars.Read"
            )
            .run(context -> {
                AzureClientRegistrationRepository clientRepo = context.getBean(AzureClientRegistrationRepository.class);
                assertDefaultScopes(
                    clientRepo.getAzureClient(),
                    "openid", "profile", "offline_access", "https://graph.microsoft.com/User.Read", "Calendars.Read"
                );
            });
    }

    @Test
    public void customizeUri() {
        contextRunner
            .withPropertyValues(
                "azure.activedirectory.client-id = fake-client-id",
                "azure.activedirectory.client-secret = fake-client-secret",
                "azure.activedirectory.tenant-id = fake-tenant-id",
                "azure.activedirectory.authorization-server-uri = http://localhost/"
            )
            .run(context -> {
                AzureClientRegistrationRepository clientRepo = context.getBean(AzureClientRegistrationRepository.class);
                ClientRegistration azure = clientRepo.findByRegistrationId("azure");
                AuthorizationServerEndpoints endpoints = new AuthorizationServerEndpoints("http://localhost/");
                assertEquals(endpoints.authorizationEndpoint("fake-tenant-id"),
                    azure.getProviderDetails().getAuthorizationUri());
                assertEquals(endpoints.tokenEndpoint("fake-tenant-id"), azure.getProviderDetails().getTokenUri());
                assertEquals(endpoints.jwkSetEndpoint("fake-tenant-id"), azure.getProviderDetails().getJwkSetUri());
            });
    }

    @Test
    public void clientRequiresOnDemandPermissions() {
        contextRunner
            .withPropertyValues(
                "azure.activedirectory.client-id = fake-client-id",
                "azure.activedirectory.client-secret = fake-client-secret",
                "azure.activedirectory.tenant-id = fake-tenant-id",
                "azure.activedirectory.user-group.allowed-groups = group1, group2",
                "azure.activedirectory.authorization.graph.scopes = Calendars.Read",
                "azure.activedirectory.authorization.graph.on-demand = true",
                "azure.activedirectory.authorization.arm.scopes = https://management.core.windows.net/user_impersonation"
            )
            .run(context -> {
                AzureClientRegistrationRepository repo = context.getBean(AzureClientRegistrationRepository.class);
                ClientRegistration azure = repo.findByRegistrationId("azure");
                ClientRegistration graph = repo.findByRegistrationId("graph");
                ClientRegistration arm = repo.findByRegistrationId("arm");

                assertNotNull(azure);
                assertDefaultScopes(
                    azure,
                    "openid",
                    "profile",
                    "https://graph.microsoft.com/User.Read",
                    "offline_access",
                    "https://management.core.windows.net/user_impersonation");

                assertFalse(repo.isAuthzClient(graph));
                assertTrue(repo.isAuthzClient(arm));
                assertFalse(repo.isAuthzClient("graph"));
                assertTrue(repo.isAuthzClient("arm"));
            });
    }

    private void assertDefaultScopes(ClientRegistration client, String... scopes) {
        assertEquals(scopes.length, client.getScopes().size());
        for (String s : scopes) {
            assertTrue(client.getScopes().contains(s));
        }
    }

    private void assertDefaultScopes(AzureClientRegistration client, String... expected) {
        assertEquals(expected.length, client.getAccessTokenScopes().size());
        for (String e : expected) {
            assertTrue(client.getAccessTokenScopes().contains(e));
        }
    }

    private List<ClientRegistration> collectClients(Iterable<ClientRegistration> itr) {
        List<ClientRegistration> result = new ArrayList<>();
        itr.forEach(result::add);
        return result;
    }
}
