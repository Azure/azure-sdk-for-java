// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.aad.webapp;

import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.test.context.support.TestPropertySourceUtils;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AzureActiveDirectoryConfigurationTest {

    private AnnotationConfigApplicationContext getContext() {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        TestPropertySourceUtils.addInlinedPropertiesToEnvironment(
            context,
            "azure.activedirectory.client-id = fake-client-id",
            "azure.activedirectory.client-secret = fake-client-secret",
            "azure.activedirectory.tenant-id = fake-tenant-id",
            "azure.activedirectory.user-group.allowed-groups = group1, group2"
        );
        context.register(AzureActiveDirectoryConfiguration.class);
        return context;
    }

    @Test
    public void clientRegistered() {
        AnnotationConfigApplicationContext context = getContext();
        context.refresh();

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
    }

    @Test
    public void clientRequiresPermissionRegistered() {
        AnnotationConfigApplicationContext context = getContext();
        TestPropertySourceUtils.addInlinedPropertiesToEnvironment(context,
            "azure.activedirectory.authorization.graph.scopes = Calendars.Read");
        context.refresh();

        ClientRegistrationRepository clientRepo = context.getBean(AzureClientRegistrationRepository.class);
        ClientRegistration azure = clientRepo.findByRegistrationId("azure");
        ClientRegistration graph = clientRepo.findByRegistrationId("graph");

        assertNotNull(azure);
        assertNotNull(graph);
        assertDefaultScopes(azure,
            "openid", "profile", "offline_access", "https://graph.microsoft.com/User.Read", "Calendars.Read");
        assertDefaultScopes(graph, "Calendars.Read");
    }

    @Test
    public void clientRequiresMultiPermissions() {
        AnnotationConfigApplicationContext context = getContext();
        TestPropertySourceUtils.addInlinedPropertiesToEnvironment(context,
            "azure.activedirectory.authorization.graph.scopes = Calendars.Read",
            "azure.activedirectory.authorization.arm.scopes = https://management.core.windows.net/user_impersonation"
        );
        context.refresh();

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
    }

    @Test
    public void clientRequiresPermissionInDefaultClient() {
        AnnotationConfigApplicationContext context = getContext();
        TestPropertySourceUtils.addInlinedPropertiesToEnvironment(context,
            "azure.activedirectory.authorization.graph.scopes = Calendars.Read");
        context.refresh();

        ClientRegistrationRepository clientRepo = context.getBean(AzureClientRegistrationRepository.class);
        ClientRegistration azure = clientRepo.findByRegistrationId("azure");
        assertDefaultScopes(azure,
            "openid", "profile", "offline_access", "https://graph.microsoft.com/User.Read", "Calendars.Read");
    }

    @Test
    public void aadAwareClientRepository() {
        AnnotationConfigApplicationContext context = getContext();
        TestPropertySourceUtils.addInlinedPropertiesToEnvironment(context,
            "azure.activedirectory.authorization.graph.scopes = Calendars.Read");
        context.refresh();

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
        assertEquals(1, clients.size());
        assertEquals("azure", clients.get(0).getRegistrationId());
    }

    @Test
    public void defaultClientWithAuthzScope() {
        AnnotationConfigApplicationContext context = getContext();
        TestPropertySourceUtils.addInlinedPropertiesToEnvironment(context,
            "azure.activedirectory.authorization.azure.scopes = Calendars.Read");
        context.refresh();

        AzureClientRegistrationRepository clientRepo = context.getBean(AzureClientRegistrationRepository.class);
        assertDefaultScopes(
            clientRepo.getAzureClient(),
            "openid", "profile", "offline_access", "https://graph.microsoft.com/User.Read", "Calendars.Read"
        );
    }

    @Test
    public void customizeUri() {
        AnnotationConfigApplicationContext context = getContext();
        TestPropertySourceUtils.addInlinedPropertiesToEnvironment(context,
            "azure.activedirectory.authorization-server-uri = http://localhost/");
        context.refresh();

        AzureClientRegistrationRepository clientRepo = context.getBean(AzureClientRegistrationRepository.class);
        ClientRegistration azure = clientRepo.findByRegistrationId("azure");
        AuthorizationServerEndpoints endpoints = new AuthorizationServerEndpoints("http://localhost/");
        assertEquals(endpoints.authorizationEndpoint("fake-tenant-id"),
            azure.getProviderDetails().getAuthorizationUri());
        assertEquals(endpoints.tokenEndpoint("fake-tenant-id"), azure.getProviderDetails().getTokenUri());
        assertEquals(endpoints.jwkSetEndpoint("fake-tenant-id"), azure.getProviderDetails().getJwkSetUri());
    }

    @Test
    public void clientRequiresOnDemandPermissions() {
        AnnotationConfigApplicationContext context = getContext();
        TestPropertySourceUtils.addInlinedPropertiesToEnvironment(context,
            "azure.activedirectory.authorization.graph.scopes = Calendars.Read",
            "azure.activedirectory.authorization.graph.on-demand = true",
            "azure.activedirectory.authorization.arm.scopes = https://management.core.windows.net/user_impersonation"
        );
        context.refresh();

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
