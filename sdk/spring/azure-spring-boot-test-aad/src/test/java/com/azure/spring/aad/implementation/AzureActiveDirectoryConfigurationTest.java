package com.azure.spring.aad.implementation;

import com.azure.test.utils.AppRunner;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;
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

    @Test
    public void clientRegistered() {
        try (AppRunner runner = createApp()) {
            runner.start();

            ClientRegistrationRepository repo = runner.getBean(ClientRegistrationRepository.class);
            ClientRegistration azure = repo.findByRegistrationId("azure");

            assertNotNull(azure);
            assertEquals("fake-client-id", azure.getClientId());
            assertEquals("fake-client-secret", azure.getClientSecret());

            IdentityEndpoints endpoints = new IdentityEndpoints();
            assertEquals(endpoints.authorizationEndpoint("fake-tenant-id"), azure.getProviderDetails().getAuthorizationUri());
            assertEquals(endpoints.tokenEndpoint("fake-tenant-id"), azure.getProviderDetails().getTokenUri());
            assertEquals(endpoints.jwkSetEndpoint("fake-tenant-id"), azure.getProviderDetails().getJwkSetUri());
            assertEquals("{baseUrl}/login/oauth2/code/{registrationId}", azure.getRedirectUriTemplate());
            assertDefaultScopes(azure, "openid", "profile");
        }
    }

    @Test
    public void clientRequiresPermissionRegistered() {
        try (AppRunner runner = createApp()) {
            runner.property("azure.active.directory.authorization.graph.scopes", "Calendars.Read");
            runner.start();

            ClientRegistrationRepository repo = runner.getBean(ClientRegistrationRepository.class);
            ClientRegistration azure = repo.findByRegistrationId("azure");
            ClientRegistration graph = repo.findByRegistrationId("graph");

            assertNotNull(azure);
            assertDefaultScopes(azure, "openid", "profile", "offline_access", "Calendars.Read");

            assertNotNull(graph);
            assertDefaultScopes(graph, "Calendars.Read");
        }
    }

    @Test
    public void clientRequiresMultiPermissions() {
        try (AppRunner runner = createApp()) {
            runner.property("azure.active.directory.authorization.graph.scopes", "Calendars.Read");
            runner.property("azure.active.directory.authorization.arm.scopes", "https://management.core.windows.net/user_impersonation");
            runner.start();

            ClientRegistrationRepository repo = runner.getBean(ClientRegistrationRepository.class);
            ClientRegistration azure = repo.findByRegistrationId("azure");
            ClientRegistration graph = repo.findByRegistrationId("graph");

            assertNotNull(azure);
            assertDefaultScopes(
                azure,
                "openid",
                "profile",
                "offline_access",
                "Calendars.Read",
                "https://management.core.windows.net/user_impersonation");

            assertNotNull(graph);
            assertDefaultScopes(graph, "Calendars.Read");
        }
    }

    @Test
    public void clientRequiresPermissionInDefaultClient() {
        try (AppRunner runner = createApp()) {
            runner.property("azure.active.directory.authorization.azure.scopes", "Calendars.Read");
            runner.start();

            ClientRegistrationRepository repo = runner.getBean(ClientRegistrationRepository.class);
            ClientRegistration azure = repo.findByRegistrationId("azure");

            assertNotNull(azure);
            assertDefaultScopes(azure, "openid", "profile", "offline_access", "Calendars.Read");
        }
    }

    @Test
    public void aadAwareClientRepository() {
        try (AppRunner runner = createApp()) {
            runner.property("azure.active.directory.authorization.graph.scopes", "Calendars.Read");
            runner.start();

            AzureClientRegistrationRepository repo = (AzureClientRegistrationRepository) runner.getBean(ClientRegistrationRepository.class);
            ClientRegistration azure = repo.findByRegistrationId("azure");
            ClientRegistration graph = repo.findByRegistrationId("graph");

            assertDefaultScopes(repo.defaultClient(), "openid", "profile", "offline_access");
            assertEquals(repo.defaultClient().client(), azure);

            assertFalse(repo.isAuthzClient(azure));
            assertTrue(repo.isAuthzClient(graph));
            assertFalse(repo.isAuthzClient("azure"));
            assertTrue(repo.isAuthzClient("graph"));

            List<ClientRegistration> clients = collectClients((Iterable<ClientRegistration>) repo);
            assertEquals(1, clients.size());
            assertEquals("azure", clients.get(0).getRegistrationId());
        }
    }

    @Test
    public void defaultClientWithAuthzScope() {
        try (AppRunner runner = createApp()) {
            runner.property("azure.active.directory.authorization.azure.scopes", "Calendars.Read");
            runner.start();

            AzureClientRegistrationRepository repo = runner.getBean(AzureClientRegistrationRepository.class);
            assertDefaultScopes(repo.defaultClient(), "openid", "profile", "offline_access", "Calendars.Read");
        }
    }

    @Test
    public void customizeUri() {
        try (AppRunner runner = createApp()) {
            runner.property("azure.active.directory.uri", "http://localhost/");
            runner.start();

            AzureClientRegistrationRepository repo = runner.getBean(AzureClientRegistrationRepository.class);
            ClientRegistration azure = repo.findByRegistrationId("azure");

            IdentityEndpoints endpoints = new IdentityEndpoints("http://localhost/");
            assertEquals(endpoints.authorizationEndpoint("fake-tenant-id"), azure.getProviderDetails().getAuthorizationUri());
            assertEquals(endpoints.tokenEndpoint("fake-tenant-id"), azure.getProviderDetails().getTokenUri());
            assertEquals(endpoints.jwkSetEndpoint("fake-tenant-id"), azure.getProviderDetails().getJwkSetUri());
        }
    }

    private AppRunner createApp() {
        AppRunner result = new AppRunner(DumbApp.class);
        result.property("azure.active.directory.uri", "https://login.microsoftonline.com");
        result.property("azure.active.directory.tenant-id", "fake-tenant-id");
        result.property("azure.active.directory.client-id", "fake-client-id");
        result.property("azure.active.directory.client-secret", "fake-client-secret");
        result.property("azure.active.directory.user-group.allowed-groups", "groupA, groupB");
        return result;
    }

    private void assertDefaultScopes(ClientRegistration client, String ... scopes) {
        assertEquals(scopes.length, client.getScopes().size());
        for (String s : scopes) {
            assertTrue(client.getScopes().contains(s));
        }
    }

    private void assertDefaultScopes(DefaultClient client, String ... expected) {
        assertEquals(expected.length, client.scopes().size());
        for (String e : expected) {
            assertTrue(client.scopes().contains(e));
        }
    }

    private List<ClientRegistration> collectClients(Iterable<ClientRegistration> itr) {
        List<ClientRegistration> result = new ArrayList<>();
        itr.forEach(c -> result.add(c));
        return result;
    }

    @Configuration
    @EnableWebSecurity
    @SpringBootApplication
    public static class DumbApp {}
}
