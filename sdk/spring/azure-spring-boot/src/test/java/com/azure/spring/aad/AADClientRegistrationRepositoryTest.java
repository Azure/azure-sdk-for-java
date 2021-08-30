// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.aad;

import com.azure.spring.autoconfigure.aad.AADAuthenticationProperties;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.azure.spring.aad.AADAuthorizationGrantType.AUTHORIZATION_CODE;
import static com.azure.spring.aad.AADAuthorizationGrantType.AZURE_DELEGATED;
import static com.azure.spring.aad.AADClientRegistrationRepository.AZURE_CLIENT_REGISTRATION_ID;
import static com.azure.spring.aad.AADClientRegistrationRepository.resourceServerCount;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AADClientRegistrationRepositoryTest {

    @Test
    public void noClientsConfiguredTest() {
        WebApplicationContextRunnerUtils
            .webApplicationContextRunner()
            .run(context -> {
                AADClientRegistrationRepository repository =
                    (AADClientRegistrationRepository) context.getBean(ClientRegistrationRepository.class);
                assertEquals(new HashSet<>(Arrays.asList("openid", "profile", "offline_access")),
                    repository.getAzureClientAccessTokenScopes());

                ClientRegistration azure = repository.findByRegistrationId(AZURE_CLIENT_REGISTRATION_ID);
                assertEquals(AUTHORIZATION_CODE.getValue(), azure.getAuthorizationGrantType().getValue());
                assertEquals(new HashSet<>(Arrays.asList("openid", "profile", "offline_access")), azure.getScopes());
                List<ClientRegistration> clients = collectClients(repository);

                assertEquals(1, clients.size());
                assertEquals(azure, clients.get(0));
            });
    }

    @Test
    public void azureClientConfiguredTest() {
        WebApplicationContextRunnerUtils
            .webApplicationContextRunner()
            .withPropertyValues(
                "azure.activedirectory.authorization-clients.azure.scopes = Azure.Scope"
            )
            .run(context -> {
                AADClientRegistrationRepository repository =
                    (AADClientRegistrationRepository) context.getBean(ClientRegistrationRepository.class);
                assertEquals(new HashSet<>(Arrays.asList("Azure.Scope", "openid", "profile", "offline_access")),
                    repository.getAzureClientAccessTokenScopes());

                ClientRegistration azure = repository.findByRegistrationId(AZURE_CLIENT_REGISTRATION_ID);
                assertEquals(AUTHORIZATION_CODE.getValue(), azure.getAuthorizationGrantType().getValue());
                assertEquals(new HashSet<>(Arrays.asList("Azure.Scope", "openid", "profile", "offline_access")),
                    azure.getScopes());

                List<ClientRegistration> clients = collectClients(repository);
                assertEquals(Collections.singletonList(azure), clients);
            });
    }

    @Test
    public void graphClientConfiguredTest() {
        WebApplicationContextRunnerUtils
            .webApplicationContextRunner()
            .withPropertyValues(
                "azure.activedirectory.authorization-clients.graph.scopes = Graph.Scope"
            )
            .run(context -> {
                AADClientRegistrationRepository repository =
                    (AADClientRegistrationRepository) context.getBean(ClientRegistrationRepository.class);
                assertEquals(new HashSet<>(Arrays.asList("openid", "profile", "offline_access")),
                    repository.getAzureClientAccessTokenScopes());

                ClientRegistration azure = repository.findByRegistrationId(AZURE_CLIENT_REGISTRATION_ID);
                assertEquals(AUTHORIZATION_CODE.getValue(), azure.getAuthorizationGrantType().getValue());
                assertEquals(new HashSet<>(Arrays.asList("Graph.Scope", "openid", "profile", "offline_access")),
                    azure.getScopes());

                ClientRegistration graph = repository.findByRegistrationId("graph");
                assertEquals(AZURE_DELEGATED.getValue(), graph.getAuthorizationGrantType().getValue());
                assertEquals(new HashSet<>(Collections.singletonList("Graph.Scope")), graph.getScopes());

                List<ClientRegistration> clients = collectClients(repository);
                assertEquals(Collections.singletonList(azure), clients);
            });
    }

    @Test
    public void onDemandGraphClientConfiguredTest() {
        WebApplicationContextRunnerUtils
            .webApplicationContextRunner()
            .withPropertyValues(
                "azure.activedirectory.authorization-clients.graph.scopes = Graph.Scope",
                "azure.activedirectory.authorization-clients.graph.on-demand = true"
            )
            .run(context -> {
                AADClientRegistrationRepository repository =
                    (AADClientRegistrationRepository) context.getBean(ClientRegistrationRepository.class);
                assertEquals(new HashSet<>(Arrays.asList("openid", "profile", "offline_access")),
                    repository.getAzureClientAccessTokenScopes());

                ClientRegistration azure = repository.findByRegistrationId(AZURE_CLIENT_REGISTRATION_ID);
                assertEquals(AUTHORIZATION_CODE.getValue(), azure.getAuthorizationGrantType().getValue());
                assertEquals(new HashSet<>(Arrays.asList("openid", "profile", "offline_access")),
                    azure.getScopes());

                ClientRegistration graph = repository.findByRegistrationId("graph");
                assertEquals(AUTHORIZATION_CODE.getValue(), graph.getAuthorizationGrantType().getValue());
                assertEquals(new HashSet<>(Arrays.asList("Graph.Scope", "openid", "profile", "offline_access")),
                    graph.getScopes());

                List<ClientRegistration> clients = collectClients(repository);
                assertEquals(Arrays.asList(graph, azure), clients);
            });
    }

    @Test
    public void clientWithClientCredentialsPermissions() {
        WebApplicationContextRunnerUtils
            .webApplicationContextRunner()
            .withPropertyValues(
                "azure.activedirectory.authorization-clients.graph.scopes = fakeValue:/.default",
                "azure.activedirectory.authorization-clients.graph.authorizationGrantType = client_credentials"
            )
            .run(context -> {
                ClientRegistrationRepository repository = context.getBean(ClientRegistrationRepository.class);
                assertEquals(repository.findByRegistrationId(AZURE_CLIENT_REGISTRATION_ID).getAuthorizationGrantType(),
                    AuthorizationGrantType.AUTHORIZATION_CODE);
                assertEquals(repository.findByRegistrationId("graph").getAuthorizationGrantType(),
                    AuthorizationGrantType.CLIENT_CREDENTIALS);
            });
    }

    @Test
    public void clientWhichIsNotAuthorizationCodeButOnDemandExceptionTest() {
        WebApplicationContextRunnerUtils
            .webApplicationContextRunner()
            .withPropertyValues(
                "azure.activedirectory.authorization-clients.graph.authorizationGrantType = client_credentials",
                "azure.activedirectory.authorization-clients.graph.on-demand = true"
            )
            .run(context ->
                assertThrows(IllegalStateException.class, () -> context.getBean(AADAuthenticationProperties.class))
            );
    }

    @Test
    public void azureClientEndpointTest() {
        WebApplicationContextRunnerUtils
            .webApplicationContextRunner()
            .run(context -> {
                ClientRegistrationRepository repository = context.getBean(ClientRegistrationRepository.class);
                ClientRegistration azure = repository.findByRegistrationId(AZURE_CLIENT_REGISTRATION_ID);

                assertNotNull(azure);
                assertEquals("fake-client-id", azure.getClientId());
                assertEquals("fake-client-secret", azure.getClientSecret());

                AADAuthorizationServerEndpoints endpoints = new AADAuthorizationServerEndpoints(
                    "https://login.microsoftonline.com/", "fake-tenant-id");
                assertEquals(endpoints.authorizationEndpoint(), azure.getProviderDetails().getAuthorizationUri());
                assertEquals(endpoints.tokenEndpoint(), azure.getProviderDetails().getTokenUri());
                assertEquals(endpoints.jwkSetEndpoint(), azure.getProviderDetails().getJwkSetUri());
                assertEquals("{baseUrl}/login/oauth2/code/", azure.getRedirectUri());
            });
    }

    @Test
    public void customizeUriTest() {
        WebApplicationContextRunnerUtils
            .webApplicationContextRunner()
            .withPropertyValues(
                "azure.activedirectory.base-uri = http://localhost/"
            )
            .run(context -> {
                ClientRegistrationRepository repository = context.getBean(ClientRegistrationRepository.class);
                ClientRegistration azure = repository.findByRegistrationId(AZURE_CLIENT_REGISTRATION_ID);
                AADAuthorizationServerEndpoints endpoints = new AADAuthorizationServerEndpoints(
                    "http://localhost/", "fake-tenant-id");
                assertEquals(endpoints.authorizationEndpoint(), azure.getProviderDetails().getAuthorizationUri());
                assertEquals(endpoints.tokenEndpoint(), azure.getProviderDetails().getTokenUri());
                assertEquals(endpoints.jwkSetEndpoint(), azure.getProviderDetails().getJwkSetUri());
            });
    }

    @Test
    public void testNoGroupIdAndGroupNameConfigured() {
        WebApplicationContextRunnerUtils
            .webApplicationContextRunner()
            .run(context -> {
                ClientRegistrationRepository repository = context.getBean(ClientRegistrationRepository.class);
                ClientRegistration azure = repository.findByRegistrationId(AZURE_CLIENT_REGISTRATION_ID);
                assertEquals(new HashSet<>(Arrays.asList("openid", "profile", "offline_access")), azure.getScopes());
            });
    }

    @Test
    public void testGroupNameConfigured() {
        WebApplicationContextRunnerUtils
            .webApplicationContextRunner()
            .withPropertyValues("azure.activedirectory.user-group.allowed-group-names = group1, group2")
            .run(context -> {
                ClientRegistrationRepository repository = context.getBean(ClientRegistrationRepository.class);
                ClientRegistration azure = repository.findByRegistrationId(AZURE_CLIENT_REGISTRATION_ID);
                assertEquals(
                    new HashSet<>(Arrays.asList(
                        "openid", "profile", "offline_access", "https://graph.microsoft.com/Directory.Read.All")),
                    azure.getScopes());
            });
    }

    @Test
    public void testGroupIdConfigured() {
        WebApplicationContextRunnerUtils
            .webApplicationContextRunner()
            .withPropertyValues(
                "azure.activedirectory.user-group.allowed-group-ids = 7c3a5d22-9093-42d7-b2eb-e72d06bf3718")
            .run(context -> {
                ClientRegistrationRepository repository = context.getBean(ClientRegistrationRepository.class);
                ClientRegistration azure = repository.findByRegistrationId(AZURE_CLIENT_REGISTRATION_ID);
                assertEquals(
                    new HashSet<>(Arrays.asList(
                        "openid", "profile", "offline_access", "https://graph.microsoft.com/User.Read")),
                    azure.getScopes());
            });
    }

    @Test
    public void testGroupNameAndGroupIdConfigured() {
        WebApplicationContextRunnerUtils
            .webApplicationContextRunner()
            .withPropertyValues(
                "azure.activedirectory.user-group.allowed-group-names = group1, group2",
                "azure.activedirectory.user-group.allowed-group-ids = 7c3a5d22-9093-42d7-b2eb-e72d06bf3718")
            .run(context -> {
                ClientRegistrationRepository repository = context.getBean(ClientRegistrationRepository.class);
                ClientRegistration azure = repository.findByRegistrationId(AZURE_CLIENT_REGISTRATION_ID);
                assertEquals(
                    new HashSet<>(Arrays.asList(
                        "openid", "profile", "offline_access", "https://graph.microsoft.com/Directory.Read.All")),
                    azure.getScopes());
            });
    }

    @Test
    public void haveResourceServerScopeInAccessTokenWhenThereAreMultiResourceServerScopesInAuthCode() {
        WebApplicationContextRunnerUtils
            .webApplicationContextRunner()
            .withPropertyValues(
                "azure.activedirectory.authorization-clients.office.scopes = "
                    + "https://manage.office.com/ActivityFeed.Read",
                "azure.activedirectory.authorization-clients.arm.scopes = "
                    + "https://management.core.windows.net/user_impersonation"
            )
            .run(context -> {
                AADClientRegistrationRepository repository =
                    (AADClientRegistrationRepository) context.getBean(ClientRegistrationRepository.class);
                ClientRegistration azure = repository.findByRegistrationId(AZURE_CLIENT_REGISTRATION_ID);
                assertNotNull(azure);
                int resourceServerCountInAuthCode = resourceServerCount(azure.getScopes());
                assertTrue(resourceServerCountInAuthCode > 1);
                int resourceServerCountInAccessToken =
                    resourceServerCount(repository.getAzureClientAccessTokenScopes());
                assertTrue(resourceServerCountInAccessToken != 0);
            });
    }

    // TODO (moary) Enable this test.
    // Related issue: https://github.com/Azure/azure-sdk-for-java/issues/23154
    @Disabled
    @Test
    public void noConfigurationOnMissingRequiredProperties() {
        WebApplicationContextRunnerUtils
            .getWebApplicationRunner()
            .run(context -> {
                assertThat(context).doesNotHaveBean(ClientRegistrationRepository.class);
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

    private List<ClientRegistration> collectClients(Iterable<ClientRegistration> itr) {
        List<ClientRegistration> result = new ArrayList<>();
        itr.forEach(result::add);
        return result;
    }
}
