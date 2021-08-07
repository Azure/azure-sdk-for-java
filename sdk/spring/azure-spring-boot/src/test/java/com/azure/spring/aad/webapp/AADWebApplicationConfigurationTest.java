// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.aad.webapp;

import com.azure.spring.aad.AADAuthorizationServerEndpoints;
import com.azure.spring.aad.AADClientRegistrationRepository;
import com.azure.spring.autoconfigure.aad.AADAuthenticationProperties;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AADWebApplicationConfigurationTest {

    @Test
    public void aadAwareClientRepository() {
        WebApplicationContextRunnerUtils
            .getContextRunnerWithRequiredProperties()
            .withPropertyValues(
                "azure.activedirectory.authorization-clients.graph.scopes = Calendars.Read"
            )
            .run(context -> {
                AADClientRegistrationRepository clientRepo =
                    context.getBean(AADClientRegistrationRepository.class);
                ClientRegistration azure = clientRepo.findByRegistrationId("azure");
                ClientRegistration graph = clientRepo.findByRegistrationId("graph");
                assertDefaultScopes(
                    clientRepo.getAzureClient(),
                    "openid", "profile", "offline_access"
                );
                assertEquals(clientRepo.getAzureClient().getClient(), azure);

                assertFalse(clientRepo.isAzureDelegatedClientRegistration(azure));
                assertTrue(clientRepo.isAzureDelegatedClientRegistration(graph));
                assertFalse(clientRepo.isAzureDelegatedClientRegistration("azure"));
                assertTrue(clientRepo.isAzureDelegatedClientRegistration("graph"));

                List<ClientRegistration> clients = collectClients(clientRepo);
                assertEquals(1, clients.size());
                assertEquals("azure", clients.get(0).getRegistrationId());
            });
    }

    @Test
    public void clientRegistered() {
        WebApplicationContextRunnerUtils
            .getContextRunnerWithRequiredProperties()
            .run(context -> {
                ClientRegistrationRepository clientRepo = context.getBean(AADClientRegistrationRepository.class);
                ClientRegistration azure = clientRepo.findByRegistrationId("azure");

                assertNotNull(azure);
                assertEquals("fake-client-id", azure.getClientId());
                assertEquals("fake-client-secret", azure.getClientSecret());

                AADAuthorizationServerEndpoints endpoints = new AADAuthorizationServerEndpoints(
                    "https://login.microsoftonline.com/", "fake-tenant-id");
                assertEquals(endpoints.authorizationEndpoint(),
                    azure.getProviderDetails().getAuthorizationUri());
                assertEquals(endpoints.tokenEndpoint(), azure.getProviderDetails().getTokenUri());
                assertEquals(endpoints.jwkSetEndpoint(), azure.getProviderDetails().getJwkSetUri());
                assertEquals("{baseUrl}/login/oauth2/code/", azure.getRedirectUri());
                assertDefaultScopes(azure, "openid", "profile");
            });
    }

    @Test
    public void clientRequiresMultiPermissions() {
        WebApplicationContextRunnerUtils
            .getContextRunnerWithRequiredProperties()
            .withPropertyValues(
                "azure.activedirectory.authorization-clients.graph.scopes = Calendars.Read",
                "azure.activedirectory.authorization-clients.arm.scopes = "
                    + "https://management.core.windows.net/user_impersonation"
            )
            .run(context -> {
                ClientRegistrationRepository clientRepo = context.getBean(AADClientRegistrationRepository.class);
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
            });
    }

    @Test
    public void clientRequiresOnDemandPermissions() {
        WebApplicationContextRunnerUtils
            .getContextRunnerWithRequiredProperties()
            .withPropertyValues(
                "azure.activedirectory.authorization-clients.graph.scopes = Calendars.Read",
                "azure.activedirectory.authorization-clients.graph.on-demand = true",
                "azure.activedirectory.authorization-clients.arm.scopes = "
                    + "https://management.core.windows.net/user_impersonation"
            )
            .run(context -> {
                AADClientRegistrationRepository repo =
                    context.getBean(AADClientRegistrationRepository.class);
                ClientRegistration azure = repo.findByRegistrationId("azure");
                ClientRegistration graph = repo.findByRegistrationId("graph");
                ClientRegistration arm = repo.findByRegistrationId("arm");

                assertNotNull(azure);
                assertDefaultScopes(
                    azure,
                    "openid",
                    "profile",
                    "offline_access",
                    "https://management.core.windows.net/user_impersonation");

                assertFalse(repo.isAzureDelegatedClientRegistration(graph));
                assertTrue(repo.isAzureDelegatedClientRegistration(arm));
                assertFalse(repo.isAzureDelegatedClientRegistration("graph"));
                assertTrue(repo.isAzureDelegatedClientRegistration("arm"));
            });
    }

    @Test
    public void clientWithClientCredentialsPermissions() {
        WebApplicationContextRunnerUtils
            .getContextRunnerWithRequiredProperties()
            .withPropertyValues(
                "azure.activedirectory.authorization-clients.graph.scopes = fakeValue:/.default",
                "azure.activedirectory.authorization-clients.graph.authorizationGrantType = client_credentials"
            )
            .run(context -> {
                AADClientRegistrationRepository repo =
                    context.getBean(AADClientRegistrationRepository.class);

                assertEquals(repo.findByRegistrationId("azure").getAuthorizationGrantType(),
                    AuthorizationGrantType.AUTHORIZATION_CODE);
                assertEquals(repo.findByRegistrationId("graph").getAuthorizationGrantType(),
                    AuthorizationGrantType.CLIENT_CREDENTIALS);
            });
    }

    @Test
    public void webAppWithOboWithExceptionTest() {
        WebApplicationContextRunnerUtils
            .getContextRunnerWithRequiredProperties()
            .withPropertyValues(
                "azure.activedirectory.authorization-clients.graph.authorizationGrantType = on_behalf_of"
            )
            .run(context -> {
                assertThrows(IllegalStateException.class, () -> context.getBean(AADAuthenticationProperties.class));
            });
    }

    @Test
    public void clientWhichIsNotAuthorizationCodeButOnDemandExceptionTest() {
        WebApplicationContextRunnerUtils
            .getContextRunnerWithRequiredProperties()
            .withPropertyValues(
                "azure.activedirectory.authorization-clients.graph.authorizationGrantType = client_credentials",
                "azure.activedirectory.authorization-clients.graph.on-demand = true"
            )
            .run(context -> {
                assertThrows(IllegalStateException.class, () -> context.getBean(AADAuthenticationProperties.class));
            });
    }

    @Test
    public void clientRequiresPermissionInDefaultClient() {
        WebApplicationContextRunnerUtils
            .getContextRunnerWithRequiredProperties()
            .withPropertyValues(
                "azure.activedirectory.authorization-clients.graph.scopes = Calendars.Read"
            )
            .run(context -> {
                ClientRegistrationRepository clientRepo = context.getBean(AADClientRegistrationRepository.class);
                ClientRegistration azure = clientRepo.findByRegistrationId("azure");
                assertDefaultScopes(azure,
                    "openid", "profile", "offline_access", "Calendars.Read");
            });
    }

    @Test
    public void clientRequiresPermissionRegistered() {
        WebApplicationContextRunnerUtils
            .getContextRunnerWithRequiredProperties()
            .withPropertyValues(
                "azure.activedirectory.authorization-clients.graph.scopes = Calendars.Read"
            )
            .run(context -> {
                ClientRegistrationRepository clientRepo = context.getBean(AADClientRegistrationRepository.class);
                ClientRegistration azure = clientRepo.findByRegistrationId("azure");
                ClientRegistration graph = clientRepo.findByRegistrationId("graph");

                assertNotNull(azure);
                assertNotNull(graph);
                assertDefaultScopes(azure, "openid", "profile", "offline_access", "Calendars.Read");
                assertDefaultScopes(graph, "Calendars.Read");
            });
    }

    @Test
    public void configurationOnRequiredProperties() {
        WebApplicationContextRunnerUtils
            .getContextRunnerWithRequiredProperties()
            .run(context -> {
                assertThat(context).hasSingleBean(AADClientRegistrationRepository.class);
                assertThat(context).hasSingleBean(OAuth2AuthorizedClientRepository.class);
                assertThat(context).hasSingleBean(OAuth2UserService.class);
            });
    }

    @Test
    public void customizeUri() {
        WebApplicationContextRunnerUtils
            .getContextRunnerWithRequiredProperties()
            .withPropertyValues(
                "azure.activedirectory.base-uri = http://localhost/"
            )
            .run(context -> {
                AADClientRegistrationRepository clientRepo =
                    context.getBean(AADClientRegistrationRepository.class);
                ClientRegistration azure = clientRepo.findByRegistrationId("azure");
                AADAuthorizationServerEndpoints endpoints = new AADAuthorizationServerEndpoints(
                    "http://localhost/", "fake-tenant-id");
                assertEquals(endpoints.authorizationEndpoint(),
                    azure.getProviderDetails().getAuthorizationUri());
                assertEquals(endpoints.tokenEndpoint(), azure.getProviderDetails().getTokenUri());
                assertEquals(endpoints.jwkSetEndpoint(), azure.getProviderDetails().getJwkSetUri());
            });
    }

    @Test
    public void defaultClientWithAuthzScope() {
        WebApplicationContextRunnerUtils
            .getContextRunnerWithRequiredProperties().withPropertyValues(
            "azure.activedirectory.authorization-clients.azure.scopes = Calendars.Read")
            .run(context -> {
                AADClientRegistrationRepository clientRepo =
                    context.getBean(AADClientRegistrationRepository.class);
                assertDefaultScopes(
                    clientRepo.getAzureClient(),
                    "openid", "profile", "offline_access", "Calendars.Read"
                );
            });
    }

    @Test
    public void graphUriConfigurationTest() {
        WebApplicationContextRunnerUtils
            .getContextRunnerWithRequiredProperties()
            .run(context -> {
                AADAuthenticationProperties properties =
                    context.getBean(AADAuthenticationProperties.class);
                assertEquals(properties.getGraphBaseUri(), "https://graph.microsoft.com/");
                assertEquals(properties.getGraphMembershipUri(), "https://graph.microsoft.com/v1.0/me/memberOf");
            });

        WebApplicationContextRunnerUtils
            .getContextRunnerWithRequiredProperties()
            .withPropertyValues(
                "azure.activedirectory.graph-base-uri=https://microsoftgraph.chinacloudapi.cn"
            )
            .run(context -> {
                AADAuthenticationProperties properties =
                    context.getBean(AADAuthenticationProperties.class);
                assertEquals(properties.getGraphBaseUri(), "https://microsoftgraph.chinacloudapi.cn/");
                assertEquals(properties.getGraphMembershipUri(),
                    "https://microsoftgraph.chinacloudapi.cn/v1.0/me/memberOf");
            });

        WebApplicationContextRunnerUtils
            .getContextRunnerWithRequiredProperties()
            .withPropertyValues(
                "azure.activedirectory.graph-base-uri=https://microsoftgraph.chinacloudapi.cn/"
            )
            .run(context -> {
                AADAuthenticationProperties properties =
                    context.getBean(AADAuthenticationProperties.class);
                assertEquals(properties.getGraphBaseUri(), "https://microsoftgraph.chinacloudapi.cn/");
                assertEquals(properties.getGraphMembershipUri(),
                    "https://microsoftgraph.chinacloudapi.cn/v1.0/me/memberOf");
            });

        WebApplicationContextRunnerUtils
            .getContextRunnerWithRequiredProperties()
            .withPropertyValues(
                "azure.activedirectory.graph-membership-uri=https://graph.microsoft.com/v1.0/me/memberOf"
            )
            .run(context -> {
                AADAuthenticationProperties properties =
                    context.getBean(AADAuthenticationProperties.class);
                assertEquals(properties.getGraphBaseUri(), "https://graph.microsoft.com/");
                assertEquals(properties.getGraphMembershipUri(), "https://graph.microsoft.com/v1.0/me/memberOf");
            });

        WebApplicationContextRunnerUtils
            .getContextRunnerWithRequiredProperties()
            .withPropertyValues(
                "azure.activedirectory.graph-base-uri=https://microsoftgraph.chinacloudapi.cn/",
                "azure.activedirectory.graph-membership-uri=https://microsoftgraph.chinacloudapi.cn/v1.0/me/memberOf"
            )
            .run(context -> {
                AADAuthenticationProperties properties =
                    context.getBean(AADAuthenticationProperties.class);
                assertEquals(properties.getGraphBaseUri(), "https://microsoftgraph.chinacloudapi.cn/");
                assertEquals(properties.getGraphMembershipUri(),
                    "https://microsoftgraph.chinacloudapi.cn/v1.0/me/memberOf");
            });
    }

    @Test
    public void graphUriConfigurationWithExceptionTest() {
        WebApplicationContextRunnerUtils
            .getContextRunnerWithRequiredProperties()
            .withPropertyValues(
                "azure.activedirectory.graph-membership-uri=https://microsoftgraph.chinacloudapi.cn/v1.0/me/memberOf"
            )
            .run(context -> {
                assertThrows(IllegalStateException.class, () -> context.getBean(AADAuthenticationProperties.class));
            });
    }

    @Test
    public void multiTenantWithAllowedGroupsConfiguredTest1() {
        WebApplicationContextRunnerUtils
            .getContextRunnerWithRequiredProperties()
            .withPropertyValues(
                "azure.activedirectory.tenant-id=",
                "azure.activedirectory.user-group.allowed-groups=group1,group2"
            )
            .run(context -> {
                assertThrows(IllegalStateException.class, () -> context.getBean(AADAuthenticationProperties.class));
            });
    }

    @Test
    public void multiTenantWithAllowedGroupsConfiguredTest2() {
        WebApplicationContextRunnerUtils
            .getContextRunnerWithRequiredProperties()
            .withPropertyValues(
                "azure.activedirectory.tenant-id=common",
                "azure.activedirectory.user-group.allowed-groups=group1,group2"
            )
            .run(context -> {
                assertThrows(IllegalStateException.class, () -> context.getBean(AADAuthenticationProperties.class));
            });
    }

    @Test
    public void multiTenantWithAllowedGroupsConfiguredTest3() {
        WebApplicationContextRunnerUtils
            .getContextRunnerWithRequiredProperties()
            .withPropertyValues(
                "azure.activedirectory.tenant-id=organizations",
                "azure.activedirectory.user-group.allowed-groups=group1,group2"
            )
            .run(context -> {
                assertThrows(IllegalStateException.class, () -> context.getBean(AADAuthenticationProperties.class));
            });
    }

    @Test
    public void multiTenantWithAllowedGroupsIdConfiguredTest1() {
        WebApplicationContextRunnerUtils
            .getContextRunnerWithRequiredProperties()
            .withPropertyValues(
                "azure.activedirectory.tenant-id=",
                "azure.activedirectory.user-group.allowed-group-ids = 7c3a5d22-9093-42d7-b2eb-e72d06bf3718,"
                    + "39087533-2593-4b5b-ad05-4a73a01ea6a9"
            )
            .run(context -> {
                assertThrows(IllegalStateException.class, () -> context.getBean(AADAuthenticationProperties.class));
            });
    }

    @Test
    public void multiTenantWithAllowedGroupsIdConfiguredTest2() {
        WebApplicationContextRunnerUtils
            .getContextRunnerWithRequiredProperties()
            .withPropertyValues(
                "azure.activedirectory.tenant-id=common",
                "azure.activedirectory.user-group.allowed-group-ids = 7c3a5d22-9093-42d7-b2eb-e72d06bf3718,"
                    + "39087533-2593-4b5b-ad05-4a73a01ea6a9"
            )
            .run(context -> {
                assertThrows(IllegalStateException.class, () -> context.getBean(AADAuthenticationProperties.class));
            });
    }

    @Test
    public void multiTenantWithAllowedGroupsIdConfiguredTest3() {
        WebApplicationContextRunnerUtils
            .getContextRunnerWithRequiredProperties()
            .withPropertyValues(
                "azure.activedirectory.tenant-id=organizations",
                "azure.activedirectory.user-group.allowed-group-ids = 7c3a5d22-9093-42d7-b2eb-e72d06bf3718,"
                    + "39087533-2593-4b5b-ad05-4a73a01ea6a9"
            )
            .run(context -> {
                assertThrows(IllegalStateException.class, () -> context.getBean(AADAuthenticationProperties.class));
            });
    }

    @Test
    public void multiTenantWithAllowedGroupsIdConfiguredTest4() {
        WebApplicationContextRunnerUtils
            .getContextRunnerWithRequiredProperties()
            .withPropertyValues(
                "azure.activedirectory.tenant-id=consumers",
                "azure.activedirectory.user-group.allowed-group-ids = 7c3a5d22-9093-42d7-b2eb-e72d06bf3718,"
                    + "39087533-2593-4b5b-ad05-4a73a01ea6a9"
            )
            .run(context -> {
                assertThrows(IllegalStateException.class, () -> context.getBean(AADAuthenticationProperties.class));
            });
    }

    @Test
    public void multiTenantWithAllowedGroupsConfiguredTest4() {
        WebApplicationContextRunnerUtils
            .getContextRunnerWithRequiredProperties()
            .withPropertyValues(
                "azure.activedirectory.tenant-id=consumers",
                "azure.activedirectory.user-group.allowed-groups=group1,group2"
            )
            .run(context -> {
                assertThrows(IllegalStateException.class, () -> context.getBean(AADAuthenticationProperties.class));
            });
    }

    @Test
    public void testNoGroupIdAndGroupNameConfigured() {
        WebApplicationContextRunnerUtils
            .getContextRunnerWithRequiredProperties()
            .run(context -> {
                AADClientRegistrationRepository clientRepo =
                    context.getBean(AADClientRegistrationRepository.class);
                assertDefaultScopes(
                    clientRepo.getAzureClient(),
                    "openid", "profile"
                );
            });
    }

    @Test
    public void testGroupNameConfigured() {
        WebApplicationContextRunnerUtils
            .getContextRunnerWithRequiredProperties()
            .withPropertyValues("azure.activedirectory.user-group.allowed-group-names = group1, group2")
            .run(context -> {
                AADClientRegistrationRepository clientRepo =
                    context.getBean(AADClientRegistrationRepository.class);
                assertDefaultScopes(
                    clientRepo.getAzureClient(),
                    "openid", "profile", "https://graph.microsoft.com/Directory.Read.All"
                );
            });
    }

    @Test
    public void testGroupIdConfigured() {
        WebApplicationContextRunnerUtils
            .getContextRunnerWithRequiredProperties()
            .withPropertyValues(
                "azure.activedirectory.user-group.allowed-group-ids = 7c3a5d22-9093-42d7-b2eb-e72d06bf3718")
            .run(context -> {
                AADClientRegistrationRepository clientRepo =
                    context.getBean(AADClientRegistrationRepository.class);
                assertDefaultScopes(
                    clientRepo.getAzureClient(),
                    "openid", "profile", "https://graph.microsoft.com/User.Read"
                );
            });
    }

    @Test
    public void testGroupNameAndGroupIdConfigured() {
        WebApplicationContextRunnerUtils
            .getContextRunnerWithRequiredProperties()
            .withPropertyValues(
                "azure.activedirectory.user-group.allowed-group-names = group1, group2",
                "azure.activedirectory.user-group.allowed-group-ids = 7c3a5d22-9093-42d7-b2eb-e72d06bf3718")
            .run(context -> {
                AADClientRegistrationRepository clientRepo =
                    context.getBean(AADClientRegistrationRepository.class);
                assertDefaultScopes(
                    clientRepo.getAzureClient(),
                    "openid", "profile", "https://graph.microsoft.com/Directory.Read.All"
                );
            });
    }

    @Test
    public void haveResourceServerScopeInAccessTokenWhenThereAreMultiResourceServerScopesInAuthCode() {
        WebApplicationContextRunnerUtils
            .getContextRunnerWithRequiredProperties()
            .withPropertyValues(
                "azure.activedirectory.authorization-clients.office.scopes = "
                    + "https://manage.office.com/ActivityFeed.Read",
                "azure.activedirectory.authorization-clients.arm.scopes = "
                    + "https://management.core.windows.net/user_impersonation"
            )
            .run(context -> {
                AADClientRegistrationRepository repo =
                    context.getBean(AADClientRegistrationRepository.class);
                AzureClientRegistration azure = repo.getAzureClient();
                assertNotNull(azure);
                int resourceServerCountInAuthCode = resourceServerCount(azure.getClient().getScopes());
                assertTrue(resourceServerCountInAuthCode > 1);
                int resourceServerCountInAccessToken = resourceServerCount(azure.getAccessTokenScopes());
                assertTrue(resourceServerCountInAccessToken != 0);
            });
    }

    @Disabled
    @Test
    public void noConfigurationOnMissingRequiredProperties() {
        WebApplicationContextRunnerUtils
            .getContextRunner()
            .run(context -> {
                assertThat(context).doesNotHaveBean(AADClientRegistrationRepository.class);
                assertThat(context).doesNotHaveBean(OAuth2AuthorizedClientRepository.class);
                assertThat(context).doesNotHaveBean(OAuth2UserService.class);
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
