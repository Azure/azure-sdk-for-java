// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.aad;

import com.azure.spring.aad.webapp.AzureClientRegistration;
import com.azure.spring.autoconfigure.aad.AADAuthenticationProperties;
import org.junit.Test;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.azure.spring.aad.AADClientRegistrationRepository.resourceServerCount;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AADClientRegistrationRepositoryTest {

    @Test
    public void noPropertiesTest() {
        new WebApplicationContextRunner()
            .withUserConfiguration(AADConfiguration.class)
            .run(context -> {
                assertThat(context).hasSingleBean(AADAuthenticationProperties.class);
                assertThat(context).hasSingleBean(ClientRegistrationRepository.class);
                assertThat(context).hasSingleBean(OAuth2AuthorizedClientRepository.class);
                assertThat(context).hasSingleBean(OAuth2UserService.class);
            });
    }

    @Test
    public void basicPropertiesTest() {
        new WebApplicationContextRunner()
            .withUserConfiguration(AADConfiguration.class)
            .withPropertyValues(
                "azure.activedirectory.tenant-id=fake-tenant-id",
                "azure.activedirectory.client-id=fake-client-id",
                "azure.activedirectory.client-secret=fake-client-secret")
            .run(context -> {
                assertThat(context).hasSingleBean(AADAuthenticationProperties.class);
                assertThat(context).hasSingleBean(AADClientRegistrationRepository.class);
                assertThat(context).hasSingleBean(OAuth2AuthorizedClientRepository.class);
                assertThat(context).hasSingleBean(OAuth2UserService.class);
            });
    }

    @Test
    public void resourceServerCountTest() {
        Set<String> scopes = new HashSet<>();
        assertEquals(resourceServerCount(scopes), 0);
        scopes.add("openid");
        scopes.add("profile");
        scopes.add("offline_access");
        assertEquals(resourceServerCount(scopes), 0);
        scopes.add("https://graph.microsoft.com/User.Read");
        assertEquals(resourceServerCount(scopes), 1);
        scopes.add("https://graph.microsoft.com/Directory.Read.All");
        assertEquals(resourceServerCount(scopes), 1);
        scopes.add("https://manage.office.com/ActivityFeed.Read");
        assertEquals(resourceServerCount(scopes), 2);
        scopes.add("https://manage.office.com/ActivityFeed.ReadDlp");
        assertEquals(resourceServerCount(scopes), 2);
        scopes.add("https://manage.office.com/ServiceHealth.Read");
        assertEquals(resourceServerCount(scopes), 2);
    }

    // AAD server will return error if:
    // 1. authorizationCodeScopes have more than one resource server.
    // 2. accessTokenScopes have no resource server
    @Test
    public void haveResourceServerScopeInAccessTokenWhenThereAreMultiResourceServerScopesInAuthCode() {
        new WebApplicationContextRunner()
            .withUserConfiguration(AADConfiguration.class)
            .withPropertyValues(
                "azure.activedirectory.tenant-id=fake-tenant-id",
                "azure.activedirectory.client-id=fake-client-id",
                "azure.activedirectory.client-secret=fake-client-secret",
                "azure.activedirectory.authorization-clients.office.authorization-grant-type=authorization_code",
                "azure.activedirectory.authorization-clients.office.scopes="
                    + "https://manage.office.com/ActivityFeed.Read",
                "azure.activedirectory.authorization-clients.arm.authorization-grant-type=authorization_code",
                "azure.activedirectory.authorization-clients.arm.scopes="
                    + "https://management.core.windows.net/user_impersonation")
            .run(context -> {
                AADClientRegistrationRepository repo = context.getBean(AADClientRegistrationRepository.class);
                AzureClientRegistration azure = repo.getAzureRegistration();
                assertNotNull(azure);
                int resourceServerCountInAuthCode = resourceServerCount(azure.getClient().getScopes());
                assertTrue(resourceServerCountInAuthCode > 1);
                int resourceServerCountInAccessToken = resourceServerCount(azure.getAccessTokenScopes());
                assertTrue(resourceServerCountInAccessToken != 0);
            });
    }

    @Test
    public void azureClientTest() {
        new WebApplicationContextRunner()
            .withUserConfiguration(AADConfiguration.class)
            .withPropertyValues(
                "azure.activedirectory.tenant-id=fake-tenant-id",
                "azure.activedirectory.client-id=fake-client-id",
                "azure.activedirectory.client-secret=fake-client-secret")
            .run(context -> {
                ClientRegistrationRepository repo = context.getBean(ClientRegistrationRepository.class);
                assertThat(repo).isExactlyInstanceOf(AADClientRegistrationRepository.class);
                ClientRegistration azure = repo.findByRegistrationId("azure");
                assertNotNull(azure);
                assertEquals("fake-client-id", azure.getClientId());
                assertEquals("fake-client-secret", azure.getClientSecret());

                AADAuthorizationServerEndpoints endpoints = new AADAuthorizationServerEndpoints(
                    "https://login.microsoftonline.com/", "fake-tenant-id");
                assertEquals(endpoints.authorizationEndpoint(), azure.getProviderDetails().getAuthorizationUri());
                assertEquals(endpoints.tokenEndpoint(), azure.getProviderDetails().getTokenUri());
                assertEquals(endpoints.jwkSetEndpoint(), azure.getProviderDetails().getJwkSetUri());
                assertEquals("{baseUrl}/login/oauth2/code/", azure.getRedirectUri());
                assertRegistrationScopes(azure, "openid", "profile");
            });
    }

    @Test
    public void clientRegistrationDelegateTest1() {
        new WebApplicationContextRunner()
            .withUserConfiguration(AADConfiguration.class)
            .withPropertyValues(
                "azure.activedirectory.tenant-id=fake-tenant-id",
                "azure.activedirectory.client-id=fake-client-id",
                "azure.activedirectory.client-secret=fake-client-secret",
                "azure.activedirectory.authorization-clients.graph.authorization-grant-type=authorization_code",
                "azure.activedirectory.authorization-clients.graph.scopes=Calendars.Read")
            .run(context -> {
                AADClientRegistrationRepository repo = context.getBean(AADClientRegistrationRepository.class);
                ClientRegistration azure = repo.findByRegistrationId("azure");
                ClientRegistration graph = repo.findByRegistrationId("graph");
                assertAzureRegistrationScopes(repo.getAzureRegistration(), "openid", "profile", "offline_access");
                assertEquals(repo.getAzureRegistration().getClient(), azure);
                assertFalse(repo.isAzureDelegatedClientRegistrations(azure));
                assertTrue(repo.isAzureDelegatedClientRegistrations(graph));
                assertFalse(repo.isAzureDelegatedClientRegistrations("azure"));
                assertTrue(repo.isAzureDelegatedClientRegistrations("graph"));

                List<ClientRegistration> clients = collectClients(repo);
                assertEquals(1, clients.size());
                assertEquals("azure", clients.get(0).getRegistrationId());
            });
    }

    @Test
    public void clientRegistrationDelegateTest2() {
        new WebApplicationContextRunner()
            .withUserConfiguration(AADConfiguration.class)
            .withPropertyValues(
                "azure.activedirectory.tenant-id=fake-tenant-id",
                "azure.activedirectory.client-id=fake-client-id",
                "azure.activedirectory.client-secret=fake-client-secret",
                "azure.activedirectory.authorization-clients.graph.authorization-grant-type=authorization_code",
                "azure.activedirectory.authorization-clients.graph.scopes=Calendars.Read",
                "azure.activedirectory.authorization-clients.graph.on-demand = true",
                "azure.activedirectory.authorization-clients.arm.authorization-grant-type=authorization_code",
                "azure.activedirectory.authorization-clients.arm.scopes="
                    + "https://management.core.windows.net/user_impersonation")
            .run(context -> {
                AADClientRegistrationRepository repo = context.getBean(AADClientRegistrationRepository.class);
                ClientRegistration azure = repo.findByRegistrationId("azure");
                ClientRegistration graph = repo.findByRegistrationId("graph");
                ClientRegistration arm = repo.findByRegistrationId("arm");
                assertNotNull(azure);
                assertRegistrationScopes(
                    azure,
                    "openid",
                    "profile",
                    "offline_access",
                    "https://management.core.windows.net/user_impersonation");
                assertFalse(repo.isAzureDelegatedClientRegistrations(graph));
                assertTrue(repo.isAzureDelegatedClientRegistrations(arm));
                assertFalse(repo.isAzureDelegatedClientRegistrations("graph"));
                assertTrue(repo.isAzureDelegatedClientRegistrations("arm"));
            });
    }

    @Test
    public void azureScopesTest1() {
        new WebApplicationContextRunner()
            .withUserConfiguration(AADConfiguration.class)
            .withPropertyValues(
                "azure.activedirectory.tenant-id=fake-tenant-id",
                "azure.activedirectory.client-id=fake-client-id",
                "azure.activedirectory.client-secret=fake-client-secret",
                "azure.activedirectory.authorization-clients.azure.authorization-grant-type=authorization_code",
                "azure.activedirectory.authorization-clients.azure.scopes=Calendars.Read")
            .run(context -> {
                AADClientRegistrationRepository repo = context.getBean(AADClientRegistrationRepository.class);
                assertAzureRegistrationScopes(
                    repo.getAzureRegistration(),
                    "openid", "profile", "offline_access", "Calendars.Read"
                );
            });
    }

    @Test
    public void azureScopesTest2() {
        new WebApplicationContextRunner()
            .withUserConfiguration(AADConfiguration.class)
            .withPropertyValues(
                "azure.activedirectory.tenant-id=fake-tenant-id",
                "azure.activedirectory.client-id=fake-client-id",
                "azure.activedirectory.client-secret=fake-client-secret",
                "azure.activedirectory.authorization-clients.graph.authorization-grant-type=authorization_code",
                "azure.activedirectory.authorization-clients.graph.scopes=Calendars.Read")
            .run(context -> {
                ClientRegistrationRepository repo = context.getBean(ClientRegistrationRepository.class);
                assertThat(repo).isExactlyInstanceOf(AADClientRegistrationRepository.class);
                ClientRegistration azure = repo.findByRegistrationId("azure");
                assertRegistrationScopes(azure, "openid", "profile", "offline_access", "Calendars.Read");
            });
    }

    @Test
    public void azureScopesTest3() {
        new WebApplicationContextRunner()
            .withUserConfiguration(AADConfiguration.class)
            .withPropertyValues(
                "azure.activedirectory.tenant-id=fake-tenant-id",
                "azure.activedirectory.client-id=fake-client-id",
                "azure.activedirectory.client-secret=fake-client-secret",
                "azure.activedirectory.user-group.allowed-groups=group1, group2")
            .run(context -> {
                AADClientRegistrationRepository repo = context.getBean(AADClientRegistrationRepository.class);
                assertAzureRegistrationScopes(
                    repo.getAzureRegistration(),
                    "openid",
                    "profile",
                    "https://graph.microsoft.com/User.Read",
                    "https://graph.microsoft.com/Directory.Read.All"
                );
            });
    }

    @Test
    public void clientScopesTest1() {
        new WebApplicationContextRunner()
            .withUserConfiguration(AADConfiguration.class)
            .withPropertyValues(
                "azure.activedirectory.tenant-id=fake-tenant-id",
                "azure.activedirectory.client-id=fake-client-id",
                "azure.activedirectory.client-secret=fake-client-secret",
                "azure.activedirectory.authorization-clients.graph.authorization-grant-type=authorization_code",
                "azure.activedirectory.authorization-clients.graph.scopes=Calendars.Read")
            .run(context -> {
                ClientRegistrationRepository repo = context.getBean(AADClientRegistrationRepository.class);
                ClientRegistration azure = repo.findByRegistrationId("azure");
                ClientRegistration graph = repo.findByRegistrationId("graph");
                assertNotNull(azure);
                assertNotNull(graph);
                assertRegistrationScopes(azure, "openid", "profile", "offline_access", "Calendars.Read");
                assertRegistrationScopes(graph, "Calendars.Read");
            });
    }

    @Test
    public void clientScopesTest2() {
        new WebApplicationContextRunner()
            .withUserConfiguration(AADConfiguration.class)
            .withPropertyValues(
                "azure.activedirectory.tenant-id=fake-tenant-id",
                "azure.activedirectory.client-id=fake-client-id",
                "azure.activedirectory.client-secret=fake-client-secret",
                "azure.activedirectory.authorization-clients.graph.authorization-grant-type=authorization_code",
                "azure.activedirectory.authorization-clients.graph.scopes=Calendars.Read",
                "azure.activedirectory.authorization-clients.arm.authorization-grant-type=authorization_code",
                "azure.activedirectory.authorization-clients.arm.scopes="
                    + "https://management.core.windows.net/user_impersonation",
                "azure.activedirectory.authorization-clients.custom.authorization-grant-type=authorization_code",
                "azure.activedirectory.authorization-clients.custom.scopes="
                    + "api://52261059-e515-488e-84fd-a09a3f372814/File.Read")
            .run(context -> {
                ClientRegistrationRepository repo = context.getBean(AADClientRegistrationRepository.class);
                ClientRegistration azure = repo.findByRegistrationId("azure");
                assertRegistrationScopes(
                    azure,
                    "openid",
                    "profile",
                    "offline_access",
                    "Calendars.Read",
                    "https://management.core.windows.net/user_impersonation",
                    "api://52261059-e515-488e-84fd-a09a3f372814/File.Read");
                ClientRegistration graph = repo.findByRegistrationId("graph");
                assertRegistrationScopes(graph, "Calendars.Read");
                ClientRegistration custom = repo.findByRegistrationId("custom");
                assertRegistrationScopes(custom, "api://52261059-e515-488e-84fd-a09a3f372814/File.Read");
            });
    }

    @Test
    public void grantTypeTest() {
        new WebApplicationContextRunner()
            .withUserConfiguration(AADConfiguration.class)
            .withPropertyValues(
                "azure.activedirectory.tenant-id=fake-tenant-id",
                "azure.activedirectory.client-id=fake-client-id",
                "azure.activedirectory.client-secret=fake-client-secret",
                "azure.activedirectory.authorization-clients.graph.authorization-grant-type=client_credentials",
                "azure.activedirectory.authorization-clients.graph.scopes=fakeValue:/.default")
            .run(context -> {
                AADClientRegistrationRepository repo = context.getBean(AADClientRegistrationRepository.class);
                assertThat(repo).isExactlyInstanceOf(AADClientRegistrationRepository.class);
                ClientRegistration azure = repo.findByRegistrationId("azure");
                ClientRegistration graph = repo.findByRegistrationId("graph");
                assertEquals(azure.getAuthorizationGrantType(), AuthorizationGrantType.AUTHORIZATION_CODE);
                assertEquals(graph.getAuthorizationGrantType(), AuthorizationGrantType.CLIENT_CREDENTIALS);
            });
    }

    private void assertRegistrationScopes(ClientRegistration client, String... scopes) {
        assertEquals(scopes.length, client.getScopes().size());
        for (String s : scopes) {
            assertTrue(client.getScopes().contains(s));
        }
    }

    private void assertAzureRegistrationScopes(AzureClientRegistration client, String... expected) {
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
