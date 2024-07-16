// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.aad.security;

import com.azure.spring.cloud.autoconfigure.implementation.aad.configuration.properties.AadAuthenticationProperties;
import com.azure.spring.cloud.autoconfigure.implementation.aad.security.properties.AadAuthorizationServerEndpoints;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
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

import static com.azure.spring.cloud.autoconfigure.implementation.aad.WebApplicationContextRunnerUtils.oauthClientRunner;
import static com.azure.spring.cloud.autoconfigure.implementation.aad.WebApplicationContextRunnerUtils.resourceServerWithOboContextRunner;
import static com.azure.spring.cloud.autoconfigure.implementation.aad.WebApplicationContextRunnerUtils.webApplicationAndResourceServerContextRunner;
import static com.azure.spring.cloud.autoconfigure.implementation.aad.WebApplicationContextRunnerUtils.webApplicationContextRunner;
import static com.azure.spring.cloud.autoconfigure.implementation.aad.security.AadClientRegistrationRepository.AZURE_CLIENT_REGISTRATION_ID;
import static com.azure.spring.cloud.autoconfigure.implementation.aad.security.AadClientRegistrationRepository.resourceServerCount;
import static com.azure.spring.cloud.autoconfigure.implementation.aad.security.constants.Constants.AZURE_DELEGATED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.oauth2.core.AuthorizationGrantType.AUTHORIZATION_CODE;
import static org.springframework.security.oauth2.core.AuthorizationGrantType.JWT_BEARER;
import static org.springframework.security.oauth2.core.ClientAuthenticationMethod.PRIVATE_KEY_JWT;

class AadClientRegistrationRepositoryTests {

    @Test
    void noClientsConfiguredTest() {
        webApplicationContextRunner()
            .run(context -> {
                AadClientRegistrationRepository repository =
                    (AadClientRegistrationRepository) context.getBean(ClientRegistrationRepository.class);
                assertEquals(new HashSet<>(Arrays.asList("openid", "profile", "offline_access")),
                    repository.getAzureClientAccessTokenScopes());

                ClientRegistration azure = repository.findByRegistrationId(AZURE_CLIENT_REGISTRATION_ID);
                assertEquals(AUTHORIZATION_CODE, azure.getAuthorizationGrantType());
                assertEquals(new HashSet<>(Arrays.asList("openid", "profile", "offline_access")), azure.getScopes());
                List<ClientRegistration> clients = collectClients(repository);

                assertEquals(1, clients.size());
                assertEquals(azure, clients.get(0));
            });
    }

    @Test
    void azureClientConfiguredTest() {
        webApplicationContextRunner()
            .withPropertyValues(
                "spring.cloud.azure.active-directory.authorization-clients.azure.scopes = Azure.Scope",
                "spring.cloud.azure.active-directory.authorization-clients.azure.client-authentication-method = private_key_jwt"
            )
            .run(context -> {
                AadClientRegistrationRepository repository =
                    (AadClientRegistrationRepository) context.getBean(ClientRegistrationRepository.class);
                assertEquals(new HashSet<>(Arrays.asList("Azure.Scope", "openid", "profile", "offline_access")),
                    repository.getAzureClientAccessTokenScopes());
                ClientRegistration azure = repository.findByRegistrationId(AZURE_CLIENT_REGISTRATION_ID);
                assertEquals(PRIVATE_KEY_JWT, azure.getClientAuthenticationMethod());
                assertEquals(AUTHORIZATION_CODE, azure.getAuthorizationGrantType());
                assertEquals(new HashSet<>(Arrays.asList("Azure.Scope", "openid", "profile", "offline_access")),
                    azure.getScopes());

                List<ClientRegistration> clients = collectClients(repository);
                assertEquals(Collections.singletonList(azure), clients);
            });
    }

    @Test
    void azureClientInvalidedConfiguredTest() {
        webApplicationContextRunner()
            .withPropertyValues(
                "spring.cloud.azure.active-directory.authorization-clients.azure.client-authentication-method = client_secret_jwt"
            )
            .run(context ->
                assertThrows(IllegalStateException.class, () -> context.getBean(AadAuthenticationProperties.class))
            );
    }

    @Test
    void otherClientInvalidedConfiguredTest() {
        webApplicationContextRunner()
            .withPropertyValues(
                "spring.cloud.azure.active-directory.authorization-clients.other.client-authentication-method = client_secret_jwt"
            )
            .run(context ->
                assertThrows(IllegalStateException.class, () -> context.getBean(AadAuthenticationProperties.class))
            );
    }

    @Test
    void graphClientConfiguredTest() {
        webApplicationContextRunner()
            .withPropertyValues(
                "spring.cloud.azure.active-directory.authorization-clients.graph.scopes = Graph.Scope"
            )
            .run(context -> {
                AadClientRegistrationRepository repository =
                    (AadClientRegistrationRepository) context.getBean(ClientRegistrationRepository.class);
                assertEquals(new HashSet<>(Arrays.asList("openid", "profile", "offline_access")),
                    repository.getAzureClientAccessTokenScopes());

                ClientRegistration azure = repository.findByRegistrationId(AZURE_CLIENT_REGISTRATION_ID);
                assertEquals(AUTHORIZATION_CODE, azure.getAuthorizationGrantType());
                assertEquals(new HashSet<>(Arrays.asList("Graph.Scope", "openid", "profile", "offline_access")),
                    azure.getScopes());

                ClientRegistration graph = repository.findByRegistrationId("graph");
                assertEquals(AZURE_DELEGATED, graph.getAuthorizationGrantType());
                assertEquals(new HashSet<>(Collections.singletonList("Graph.Scope")), graph.getScopes());

                List<ClientRegistration> clients = collectClients(repository);
                assertEquals(Collections.singletonList(azure), clients);
            });
    }

    @Test
    void authorizationCodeGraphClientConfiguredTest() {
        webApplicationContextRunner()
            .withPropertyValues(
                "spring.cloud.azure.active-directory.authorization-clients.graph.scopes = Graph.Scope",
                "spring.cloud.azure.active-directory.authorization-clients.graph.authorization-grant-type = authorization_code"
            )
            .run(context -> {
                AadClientRegistrationRepository repository =
                    (AadClientRegistrationRepository) context.getBean(ClientRegistrationRepository.class);
                assertEquals(new HashSet<>(Arrays.asList("openid", "profile", "offline_access")),
                    repository.getAzureClientAccessTokenScopes());

                ClientRegistration azure = repository.findByRegistrationId(AZURE_CLIENT_REGISTRATION_ID);
                assertEquals(AUTHORIZATION_CODE, azure.getAuthorizationGrantType());
                assertEquals(new HashSet<>(Arrays.asList("openid", "profile", "offline_access")),
                    azure.getScopes());

                ClientRegistration graph = repository.findByRegistrationId("graph");
                assertEquals(AUTHORIZATION_CODE, graph.getAuthorizationGrantType());
                assertEquals(new HashSet<>(Arrays.asList("Graph.Scope", "openid", "profile", "offline_access")),
                    graph.getScopes());

                List<ClientRegistration> clients = collectClients(repository);
                assertEquals(Arrays.asList(graph, azure), clients);
            });
    }

    @Test
    void clientWithJwtBearerAuthorizationGrantType() {
        resourceServerWithOboContextRunner()
            .withPropertyValues(
                "spring.cloud.azure.active-directory.authorization-clients.webapi1.authorization-grant-type = on_behalf_of",
                "spring.cloud.azure.active-directory.authorization-clients.webapi1.scopes = Test",
                "spring.cloud.azure.active-directory.authorization-clients.webapi2.authorization-grant-type = urn:ietf:params:oauth:grant-type:jwt-bearer",
                "spring.cloud.azure.active-directory.authorization-clients.webapi2.scopes = Test"
            )
            .run(context -> {
                ClientRegistrationRepository repository = context.getBean(ClientRegistrationRepository.class);
                ClientRegistration webapi1 = repository.findByRegistrationId("webapi1");
                assertEquals(JWT_BEARER, webapi1.getAuthorizationGrantType());

                ClientRegistration webapi2 = repository.findByRegistrationId("webapi2");
                assertEquals(JWT_BEARER, webapi2.getAuthorizationGrantType());
            });
    }

    @Test
    void clientWithClientCredentialsPermissions() {
        webApplicationContextRunner()
            .withPropertyValues(
                "spring.cloud.azure.active-directory.authorization-clients.graph.scopes = fakeValue:/.default",
                "spring.cloud.azure.active-directory.authorization-clients.graph.authorizationGrantType = client_credentials"
            )
            .run(context -> {
                ClientRegistrationRepository repository = context.getBean(ClientRegistrationRepository.class);
                assertEquals(repository.findByRegistrationId(AZURE_CLIENT_REGISTRATION_ID).getAuthorizationGrantType(),
                    AUTHORIZATION_CODE);
                assertEquals(repository.findByRegistrationId("graph").getAuthorizationGrantType(),
                    AuthorizationGrantType.CLIENT_CREDENTIALS);
            });
    }

    @Test
    void azureClientEndpointTest() {
        webApplicationContextRunner()
            .run(context -> {
                ClientRegistrationRepository repository = context.getBean(ClientRegistrationRepository.class);
                ClientRegistration azure = repository.findByRegistrationId(AZURE_CLIENT_REGISTRATION_ID);

                assertNotNull(azure);
                assertEquals("fake-client-id", azure.getClientId());
                assertEquals("fake-client-secret", azure.getClientSecret());

                AadAuthorizationServerEndpoints endpoints = new AadAuthorizationServerEndpoints(
                    "https://login.microsoftonline.com/", "fake-tenant-id");
                assertEquals(endpoints.getAuthorizationEndpoint(), azure.getProviderDetails().getAuthorizationUri());
                assertEquals(endpoints.getTokenEndpoint(), azure.getProviderDetails().getTokenUri());
                assertEquals(endpoints.getJwkSetEndpoint(), azure.getProviderDetails().getJwkSetUri());
                assertEquals("{baseUrl}/login/oauth2/code/", azure.getRedirectUri());
            });
    }

    @Test
    void customizeUriTest() {
        webApplicationContextRunner()
            .withPropertyValues(
                "spring.cloud.azure.active-directory.profile.environment.active-directory-endpoint = http://localhost/"
            )
            .run(context -> {
                ClientRegistrationRepository repository = context.getBean(ClientRegistrationRepository.class);
                ClientRegistration azure = repository.findByRegistrationId(AZURE_CLIENT_REGISTRATION_ID);
                AadAuthorizationServerEndpoints endpoints = new AadAuthorizationServerEndpoints(
                    "http://localhost/", "fake-tenant-id");
                assertEquals(endpoints.getAuthorizationEndpoint(), azure.getProviderDetails().getAuthorizationUri());
                assertEquals(endpoints.getTokenEndpoint(), azure.getProviderDetails().getTokenUri());
                assertEquals(endpoints.getJwkSetEndpoint(), azure.getProviderDetails().getJwkSetUri());
            });
    }

    @Test
    void testNoGroupIdAndGroupNameConfigured() {
        webApplicationContextRunner()
            .run(context -> {
                ClientRegistrationRepository repository = context.getBean(ClientRegistrationRepository.class);
                ClientRegistration azure = repository.findByRegistrationId(AZURE_CLIENT_REGISTRATION_ID);
                assertEquals(new HashSet<>(Arrays.asList("openid", "profile", "offline_access")), azure.getScopes());
            });
    }

    @Test
    void testGroupNameConfigured() {
        webApplicationContextRunner()
            .withPropertyValues("spring.cloud.azure.active-directory.user-group.allowed-group-names = group1, group2")
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
    void testGroupIdConfigured() {
        webApplicationContextRunner()
            .withPropertyValues(
                "spring.cloud.azure.active-directory.user-group.allowed-group-ids = 7c3a5d22-9093-42d7-b2eb-e72d06bf3718")
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
    void testGroupNameAndGroupIdConfigured() {
        webApplicationContextRunner()
            .withPropertyValues(
                "spring.cloud.azure.active-directory.user-group.allowed-group-names = group1, group2",
                "spring.cloud.azure.active-directory.user-group.allowed-group-ids = 7c3a5d22-9093-42d7-b2eb-e72d06bf3718")
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
    void haveResourceServerScopeInAccessTokenWhenThereAreMultiResourceServerScopesInAuthCode() {
        webApplicationContextRunner()
            .withPropertyValues(
                "spring.cloud.azure.active-directory.authorization-clients.office.scopes = "
                    + "https://manage.office.com/ActivityFeed.Read",
                "spring.cloud.azure.active-directory.authorization-clients.arm.scopes = "
                    + "https://management.core.windows.net/user_impersonation"
            )
            .run(context -> {
                AadClientRegistrationRepository repository =
                    (AadClientRegistrationRepository) context.getBean(ClientRegistrationRepository.class);
                ClientRegistration azure = repository.findByRegistrationId(AZURE_CLIENT_REGISTRATION_ID);
                assertNotNull(azure);
                int resourceServerCountInAuthCode = resourceServerCount(azure.getScopes());
                assertTrue(resourceServerCountInAuthCode > 1);
                int resourceServerCountInAccessToken =
                    resourceServerCount(repository.getAzureClientAccessTokenScopes());
                assertTrue(resourceServerCountInAccessToken != 0);
            });
    }

    @Test
    void rewriteAuthorizationGrantTypeWhenIsOnBehalfOf() {
        rewriteAuthorizationGrantTypeWhenIsOnBehalfOfByRunner(resourceServerWithOboContextRunner());
        rewriteAuthorizationGrantTypeWhenIsOnBehalfOfByRunner(webApplicationAndResourceServerContextRunner());
    }

    private void rewriteAuthorizationGrantTypeWhenIsOnBehalfOfByRunner(WebApplicationContextRunner runner) {
        runner
            .withPropertyValues(
                "spring.cloud.azure.active-directory.authorization-clients.graph.scopes = fakeValue:/.default",
                "spring.cloud.azure.active-directory.authorization-clients.graph.authorizationGrantType = on_behalf_of"
            )
            .run(context -> {
                AadClientRegistrationRepository repository = context.getBean(AadClientRegistrationRepository.class);
                assertNotNull(repository);
                ClientRegistration graph = repository.findByRegistrationId("graph");
                assertNotNull(graph);
                assertTrue(JWT_BEARER.equals(graph.getAuthorizationGrantType()));
            });
    }

    // TODO (moary) Enable this test.
    // Related issue: https://github.com/Azure/azure-sdk-for-java/issues/23154
    @Disabled
    @Test
    void noConfigurationOnMissingRequiredProperties() {
        oauthClientRunner()
            .run(context -> {
                assertThat(context).doesNotHaveBean(ClientRegistrationRepository.class);
                assertThat(context).doesNotHaveBean(OAuth2AuthorizedClientRepository.class);
                assertThat(context).doesNotHaveBean(OAuth2UserService.class);
            });
    }

    @Test
    void resourceServerCountTest() {
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
