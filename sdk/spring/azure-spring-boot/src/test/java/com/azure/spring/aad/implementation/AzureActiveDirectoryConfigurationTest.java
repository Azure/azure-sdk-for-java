package com.azure.spring.aad.implementation;

import org.junit.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AzureActiveDirectoryConfigurationTest {

    private void setCommonProperties() {
        System.setProperty("azure.activedirectory.client-id", "fake-client-id");
        System.setProperty("azure.activedirectory.client-secret", "fake-client-secret");
        System.setProperty("azure.activedirectory.tenant-id", "fake-tenant-id");
        System.setProperty("azure.activedirectory.user-group.allowed-groups", "group1, group2");
        System.clearProperty("azure.activedirectory.authorization.azure.scopes");
        System.clearProperty("azure.activedirectory.authorization.graph.scopes");
        System.clearProperty("azure.activedirectory.authorization.arm.scopes");
    }

    @Test
    public void clientRegistered() {
        setCommonProperties();
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        context.register(AuthorizedClientRepoTest.DumbApp.class);
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
        assertDefaultScopes(azure, "openid", "profile");
    }

    @Test
    public void clientRequiresPermissionRegistered() {
        setCommonProperties();
        System.setProperty("azure.activedirectory.authorization.graph.scopes", "Calendars.Read");
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        context.register(AuthorizedClientRepoTest.DumbApp.class);
        context.refresh();

        ClientRegistrationRepository clientRepo = context.getBean(AzureClientRegistrationRepository.class);
        ClientRegistration azure = clientRepo.findByRegistrationId("azure");
        ClientRegistration graph = clientRepo.findByRegistrationId("graph");

        assertNotNull(azure);
        assertNotNull(graph);
        assertDefaultScopes(azure, "openid", "profile", "offline_access", "Calendars.Read");
        assertDefaultScopes(graph, "Calendars.Read");
    }

    @Test
    public void clientRequiresMultiPermissions() {
        setCommonProperties();
        System.setProperty("azure.activedirectory.authorization.graph.scopes", "Calendars.Read");
        System.setProperty("azure.activedirectory.authorization.arm.scopes",
            "https://management.core.windows.net/user_impersonation");
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        context.register(AuthorizedClientRepoTest.DumbApp.class);
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
            "https://management.core.windows.net/user_impersonation");
        assertDefaultScopes(graph, "Calendars.Read");
    }

    @Test
    public void clientRequiresPermissionInDefaultClient() {
        setCommonProperties();
        System.setProperty("azure.activedirectory.authorization.graph.scopes", "Calendars.Read");
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        context.register(AuthorizedClientRepoTest.DumbApp.class);
        context.refresh();

        ClientRegistrationRepository clientRepo = context.getBean(AzureClientRegistrationRepository.class);
        ClientRegistration azure = clientRepo.findByRegistrationId("azure");
        assertDefaultScopes(azure, "openid", "profile", "offline_access", "Calendars.Read");
    }

    @Test
    public void aadAwareClientRepository() {
        setCommonProperties();
        System.setProperty("azure.activedirectory.authorization.graph.scopes", "Calendars.Read");
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        context.register(AuthorizedClientRepoTest.DumbApp.class);
        context.refresh();

        AzureClientRegistrationRepository clientRepo = context.getBean(AzureClientRegistrationRepository.class);
        ClientRegistration azure = clientRepo.findByRegistrationId("azure");
        ClientRegistration graph = clientRepo.findByRegistrationId("graph");
        assertDefaultScopes(
            clientRepo.getAzureClient(),
            "openid", "profile", "offline_access", "https://graph.microsoft.com/User.Read",
            "https://graph.microsoft.com/Directory.AccessAsUser.All"
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
        setCommonProperties();
        System.setProperty("azure.activedirectory.authorization.azure.scopes", "Calendars.Read");
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        context.register(AuthorizedClientRepoTest.DumbApp.class);
        context.refresh();

        AzureClientRegistrationRepository clientRepo = context.getBean(AzureClientRegistrationRepository.class);
        assertDefaultScopes(
            clientRepo.getAzureClient(),
            "openid", "profile", "offline_access", "https://graph.microsoft.com/User.Read",
            "https://graph.microsoft.com/Directory.AccessAsUser.All", "Calendars.Read"
        );
    }

    @Test
    public void customizeUri() {
        setCommonProperties();
        System.setProperty("azure.activedirectory.authorization-server-uri", "http://localhost/");
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        context.register(AuthorizedClientRepoTest.DumbApp.class);
        context.refresh();

        AzureClientRegistrationRepository clientRepo = context.getBean(AzureClientRegistrationRepository.class);
        ClientRegistration azure = clientRepo.findByRegistrationId("azure");
        AuthorizationServerEndpoints endpoints = new AuthorizationServerEndpoints("http://localhost/");
        assertEquals(endpoints.authorizationEndpoint("fake-tenant-id"),
            azure.getProviderDetails().getAuthorizationUri());
        assertEquals(endpoints.tokenEndpoint("fake-tenant-id"), azure.getProviderDetails().getTokenUri());
        assertEquals(endpoints.jwkSetEndpoint("fake-tenant-id"), azure.getProviderDetails().getJwkSetUri());
    }

    private void assertDefaultScopes(ClientRegistration client, String ... scopes) {
        assertEquals(scopes.length, client.getScopes().size());
        for (String s : scopes) {
            assertTrue(client.getScopes().contains(s));
        }
    }

    private void assertDefaultScopes(AzureClientRegistration client, String ... expected) {
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

    @Configuration
    @EnableWebSecurity
    @SpringBootApplication
    public static class DumbApp {
    }
}
