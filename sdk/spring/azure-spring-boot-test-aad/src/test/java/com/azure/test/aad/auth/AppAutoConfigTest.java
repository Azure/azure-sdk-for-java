package com.azure.test.aad.auth;

import com.azure.spring.autoconfigure.aad.AuthorizationServerEndpoints;
import com.azure.spring.autoconfigure.aad.AzureClientRegistrationRepository;
import com.azure.spring.autoconfigure.aad.DefaultClient;
import com.azure.test.utils.AppRunner;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
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

public class AppAutoConfigTest {

    @Test
    public void clientRegistered() {
        try (AppRunner appRunner = createApp()) {
            appRunner.start();

            ClientRegistrationRepository clientRegistrationRepository =
                appRunner.getBean(ClientRegistrationRepository.class);
            ClientRegistration azureClientRegistration = clientRegistrationRepository.findByRegistrationId("azure");

            assertNotNull(azureClientRegistration);
            assertEquals("fake-client-id", azureClientRegistration.getClientId());
            assertEquals("fake-client-secret", azureClientRegistration.getClientSecret());

            AuthorizationServerEndpoints authorizationServerEndpoints = new AuthorizationServerEndpoints();
            assertEquals(
                authorizationServerEndpoints.authorizationEndpoint("fake-tenant-id"),
                azureClientRegistration.getProviderDetails().getAuthorizationUri()
            );
            assertEquals(
                authorizationServerEndpoints.tokenEndpoint("fake-tenant-id"),
                azureClientRegistration.getProviderDetails().getTokenUri()
            );
            assertEquals(
                authorizationServerEndpoints.jwkSetEndpoint("fake-tenant-id"),
                azureClientRegistration.getProviderDetails().getJwkSetUri()
            );
            assertEquals(
                "{baseUrl}/login/oauth2/code/{registrationId}",
                azureClientRegistration.getRedirectUriTemplate()
            );
            assertDefaultScopes(azureClientRegistration, "openid", "profile");
        }
    }

    @Test
    public void clientRequiresPermissionRegistered() {
        try (AppRunner appRunner = createApp()) {
            appRunner.property(
                "azure.activedirectory.authorization.graph.scope",
                "https://graph.microsoft.com/Calendars.Read"
            );
            appRunner.start();

            ClientRegistrationRepository clientRegistrationRepository =
                appRunner.getBean(ClientRegistrationRepository.class);
            ClientRegistration azureClientRegistration = clientRegistrationRepository.findByRegistrationId("azure");
            ClientRegistration graphClientRegistration = clientRegistrationRepository.findByRegistrationId("graph");

            assertNotNull(azureClientRegistration);
            assertDefaultScopes(
                azureClientRegistration,
                "openid", "profile", "offline_access", "https://graph.microsoft.com/Calendars.Read"
            );

            assertNotNull(graphClientRegistration);
            assertDefaultScopes(graphClientRegistration, "https://graph.microsoft.com/Calendars.Read");
        }
    }

    @Test
    public void clientRequiresMultiPermissions() {
        try (AppRunner appRunner = createApp()) {
            appRunner.property(
                "azure.activedirectory.authorization.graph.scope",
                "https://graph.microsoft.com/Calendars.Read"
            );
            appRunner.property(
                "azure.activedirectory.authorization.arm.scope",
                "https://management.core.windows.net/user_impersonation"
            );
            appRunner.start();

            ClientRegistrationRepository clientRegistrationRepository =
                appRunner.getBean(ClientRegistrationRepository.class);
            ClientRegistration azureClientRegistration = clientRegistrationRepository.findByRegistrationId("azure");
            ClientRegistration graphClientRegistration = clientRegistrationRepository.findByRegistrationId("graph");

            assertNotNull(azureClientRegistration);
            assertDefaultScopes(
                azureClientRegistration,
                "openid",
                "profile",
                "offline_access",
                "https://graph.microsoft.com/Calendars.Read",
                "https://management.core.windows.net/user_impersonation"
            );

            assertNotNull(graphClientRegistration);
            assertDefaultScopes(graphClientRegistration, "https://graph.microsoft.com/Calendars.Read");
        }
    }

    @Test
    public void clientRequiresPermissionInDefaultClient() {
        try (AppRunner appRunner = createApp()) {
            appRunner.property(
                "azure.activedirectory.authorization.azure.scope",
                "https://graph.microsoft.com/Calendars.Read"
            );
            appRunner.start();

            ClientRegistrationRepository clientRegistrationRepository =
                appRunner.getBean(ClientRegistrationRepository.class);
            ClientRegistration azureClientRegistration = clientRegistrationRepository.findByRegistrationId("azure");

            assertNotNull(azureClientRegistration);
            assertDefaultScopes(
                azureClientRegistration,
                "openid", "profile", "offline_access", "https://graph.microsoft.com/Calendars.Read"
            );
        }
    }

    @Test
    public void aadAwareClientRepository() {
        try (AppRunner appRunner = createApp()) {
            appRunner.property(
                "azure.activedirectory.authorization.graph.scope",
                "https://graph.microsoft.com/Calendars.Read")
            ;
            appRunner.start();

            AzureClientRegistrationRepository azureClientRegistrationRepository =
                (AzureClientRegistrationRepository) appRunner.getBean(ClientRegistrationRepository.class);
            ClientRegistration azureClientRegistration =
                azureClientRegistrationRepository.findByRegistrationId("azure");
            ClientRegistration graphClientRegistration =
                azureClientRegistrationRepository.findByRegistrationId("graph");

            assertDefaultScopes(
                azureClientRegistrationRepository.defaultClient(),
                "openid", "profile", "offline_access"
            );
            assertEquals(azureClientRegistrationRepository.defaultClient().getClientRegistration(), azureClientRegistration);

            assertFalse(azureClientRegistrationRepository.isAuthorizedClient(azureClientRegistration));
            assertTrue(azureClientRegistrationRepository.isAuthorizedClient(graphClientRegistration));
            assertFalse(azureClientRegistrationRepository.isAuthorizedClient("azure"));
            assertTrue(azureClientRegistrationRepository.isAuthorizedClient("graph"));

            List<ClientRegistration> clientRegistrations = collectClients(azureClientRegistrationRepository);
            assertEquals(1, clientRegistrations.size());
            assertEquals("azure", clientRegistrations.get(0).getRegistrationId());
        }
    }

    @Test
    public void defaultClientWithAuthzScope() {
        try (AppRunner appRunner = createApp()) {
            appRunner.property(
                "azure.activedirectory.authorization.azure.scope",
                "https://graph.microsoft.com/Calendars.Read"
            );
            appRunner.start();

            AzureClientRegistrationRepository azureClientRegistrationRepository =
                appRunner.getBean(AzureClientRegistrationRepository.class);
            assertDefaultScopes(
                azureClientRegistrationRepository.defaultClient(),
                "openid", "profile", "offline_access", "https://graph.microsoft.com/Calendars.Read"
            );
        }
    }

    @Test
    public void customizeUri() {
        try (AppRunner appRunner = createApp()) {
            appRunner.property("azure.activedirectory.authorization-server-uri", "http://localhost/");
            appRunner.start();

            AzureClientRegistrationRepository azureClientRegistrationRepository =
                appRunner.getBean(AzureClientRegistrationRepository.class);
            ClientRegistration azureClientRegistration =
                azureClientRegistrationRepository.findByRegistrationId("azure");

            AuthorizationServerEndpoints endpoints = new AuthorizationServerEndpoints("http://localhost/");
            assertEquals(
                endpoints.authorizationEndpoint("fake-tenant-id"),
                azureClientRegistration.getProviderDetails().getAuthorizationUri()
            );
            assertEquals(
                endpoints.tokenEndpoint("fake-tenant-id"),
                azureClientRegistration.getProviderDetails().getTokenUri()
            );
            assertEquals(
                endpoints.jwkSetEndpoint("fake-tenant-id"),
                azureClientRegistration.getProviderDetails().getJwkSetUri()
            );
        }
    }

    private AppRunner createApp() {
        AppRunner result = new AppRunner(DumbApp.class);
        result.property("azure.activedirectory.tenant-id", "fake-tenant-id");
        result.property("azure.activedirectory.client-id", "fake-client-id");
        result.property("azure.activedirectory.client-secret", "fake-client-secret");
        result.property("azure.activedirectory.user-group.allowed-groups", "group1");
        return result;
    }

    private void assertDefaultScopes(ClientRegistration client, String ... scopes) {
        assertEquals(scopes.length, client.getScopes().size());
        for (String s : scopes) {
            assertTrue(client.getScopes().contains(s));
        }
    }

    private void assertDefaultScopes(DefaultClient client, String ... expected) {
        assertEquals(expected.length, client.getScopeList().size());
        for (String e : expected) {
            assertTrue(client.getScopeList().contains(e));
        }
    }

    private List<ClientRegistration> collectClients(Iterable<ClientRegistration> iterable) {
        List<ClientRegistration> result = new ArrayList<>();
        iterable.forEach(result::add);
        return result;
    }

    @Configuration
    @EnableAutoConfiguration
    @EnableWebSecurity
    public static class DumbApp {}
}
