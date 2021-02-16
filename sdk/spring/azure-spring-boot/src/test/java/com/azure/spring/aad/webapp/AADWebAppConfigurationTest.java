// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.aad.webapp;

import com.azure.spring.aad.AADAuthorizationServerEndpoints;
import com.azure.spring.autoconfigure.aad.AADAuthenticationProperties;
import org.junit.Test;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.azure.spring.aad.webapp.AADWebAppConfiguration.resourceServerCount;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AADWebAppConfigurationTest {

    @Test
    public void aadAwareClientRepository() {
        WebApplicationContextRunnerUtils
            .getContextRunnerWithRequiredProperties()
            .withPropertyValues(
                "azure.activedirectory.authorization-clients.graph.scopes = Calendars.Read"
            )
            .run(context -> {
                AADWebAppClientRegistrationRepository clientRepo =
                    context.getBean(AADWebAppClientRegistrationRepository.class);
                ClientRegistration azure = clientRepo.findByRegistrationId("azure");
                ClientRegistration graph = clientRepo.findByRegistrationId("graph");
                assertDefaultScopes(
                    clientRepo.getAzureClient(),
                    "openid", "profile", "offline_access"
                );
                assertEquals(clientRepo.getAzureClient().getClient(), azure);

                assertFalse(clientRepo.isClientNeedConsentWhenLogin(azure));
                assertTrue(clientRepo.isClientNeedConsentWhenLogin(graph));
                assertFalse(clientRepo.isClientNeedConsentWhenLogin("azure"));
                assertTrue(clientRepo.isClientNeedConsentWhenLogin("graph"));

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
                ClientRegistrationRepository clientRepo = context.getBean(AADWebAppClientRegistrationRepository.class);
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
                assertEquals("{baseUrl}/login/oauth2/code/", azure.getRedirectUriTemplate());
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
                ClientRegistrationRepository clientRepo = context.getBean(AADWebAppClientRegistrationRepository.class);
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
                AADWebAppClientRegistrationRepository repo =
                    context.getBean(AADWebAppClientRegistrationRepository.class);
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

                assertFalse(repo.isClientNeedConsentWhenLogin(graph));
                assertTrue(repo.isClientNeedConsentWhenLogin(arm));
                assertFalse(repo.isClientNeedConsentWhenLogin("graph"));
                assertTrue(repo.isClientNeedConsentWhenLogin("arm"));
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
                ClientRegistrationRepository clientRepo = context.getBean(AADWebAppClientRegistrationRepository.class);
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
                ClientRegistrationRepository clientRepo = context.getBean(AADWebAppClientRegistrationRepository.class);
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
                assertThat(context).hasSingleBean(AADWebAppClientRegistrationRepository.class);
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
                AADWebAppClientRegistrationRepository clientRepo =
                    context.getBean(AADWebAppClientRegistrationRepository.class);
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
            "azure.activedirectory.authorization-clients.azure.scopes = Calendars.Read"
        )
            .run(context -> {
                AADWebAppClientRegistrationRepository clientRepo =
                    context.getBean(AADWebAppClientRegistrationRepository.class);
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

    @Test(expected = IllegalStateException.class)
    public void graphUriConfigurationWithExceptionTest() {
        WebApplicationContextRunnerUtils
            .getContextRunnerWithRequiredProperties()
            .withPropertyValues(
                "azure.activedirectory.graph-membership-uri=https://microsoftgraph.chinacloudapi.cn/v1.0/me/memberOf"
            )
            .run(context -> {
                AADAuthenticationProperties properties = context.getBean(AADAuthenticationProperties.class);
            });
    }

    @Test
    public void groupConfiguration() {
        WebApplicationContextRunnerUtils
            .getContextRunnerWithRequiredProperties()
            .withPropertyValues("azure.activedirectory.user-group.allowed-groups = group1, group2")
            .run(context -> {
                AADWebAppClientRegistrationRepository clientRepo =
                    context.getBean(AADWebAppClientRegistrationRepository.class);
                assertDefaultScopes(
                    clientRepo.getAzureClient(),
                    "openid", "profile", "https://graph.microsoft.com/User.Read",
                    "https://graph.microsoft.com/Directory.Read.All"
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
                AADWebAppClientRegistrationRepository repo =
                    context.getBean(AADWebAppClientRegistrationRepository.class);
                AzureClientRegistration azure = repo.getAzureClient();
                assertNotNull(azure);
                int resourceServerCountInAuthCode = resourceServerCount(azure.getClient().getScopes());
                assertTrue(resourceServerCountInAuthCode > 1);
                int resourceServerCountInAccessToken = resourceServerCount(azure.getAccessTokenScopes());
                assertTrue(resourceServerCountInAccessToken != 0);
            });
    }

    @Test
    public void noConfigurationOnMissingRequiredProperties() {
        WebApplicationContextRunnerUtils
            .getContextRunner()
            .run(context -> {
                assertThat(context).doesNotHaveBean(AADWebAppClientRegistrationRepository.class);
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
