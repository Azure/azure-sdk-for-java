// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.aad.properties;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Map;

import static com.azure.spring.cloud.autoconfigure.aad.implementation.WebApplicationContextRunnerUtils.oauthClientRunner;
import static com.azure.spring.cloud.autoconfigure.aad.implementation.WebApplicationContextRunnerUtils.resourceServerContextRunner;
import static com.azure.spring.cloud.autoconfigure.aad.implementation.WebApplicationContextRunnerUtils.resourceServerWithOboContextRunner;
import static com.azure.spring.cloud.autoconfigure.aad.implementation.WebApplicationContextRunnerUtils.webApplicationContextRunner;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AadAuthenticationPropertiesTests {

    @Test
    void mapPropertiesSetting() {
        webApplicationContextRunner()
            .withPropertyValues(
                "spring.cloud.azure.active-directory.authorization-clients.test.authorizationGrantType = authorization_code",
                "spring.cloud.azure.active-directory.authorization-clients.test.scopes = test1,test2",
                "spring.cloud.azure.active-directory.authenticate-additional-parameters.prompt = login"
            )
            .run(context -> {
                AadAuthenticationProperties properties = context.getBean(AadAuthenticationProperties.class);

                Map<String, AuthorizationClientProperties> authorizationClients = properties.getAuthorizationClients();
                assertTrue(authorizationClients.containsKey("test"));
                assertTrue(authorizationClients.get("test").getScopes().containsAll(Arrays.asList("test1", "test2")));
                assertEquals(authorizationClients.get("test").getAuthorizationGrantType(), AadAuthorizationGrantType.AUTHORIZATION_CODE);

                Map<String, Object> authenticateAdditionalParameters = properties.getAuthenticateAdditionalParameters();
                assertEquals(authenticateAdditionalParameters.size(), 1);
                assertTrue(authenticateAdditionalParameters.containsKey("prompt"));
                assertEquals(authenticateAdditionalParameters.get("prompt"), "login");
            });
    }

    @Test
    void webAppWithOboWithExceptionTest() {
        webApplicationContextRunner()
            .withPropertyValues(
                "spring.cloud.azure.active-directory.authorization-clients.graph.authorizationGrantType = on_behalf_of")
            .run(context ->
                assertThrows(IllegalStateException.class, () -> context.getBean(AadAuthenticationProperties.class)));
    }

    @Test
    void graphUriConfigurationTest() {
        webApplicationContextRunner()
            .run(context -> {
                AadAuthenticationProperties properties = context.getBean(AadAuthenticationProperties.class);
                assertEquals(properties.getProfile().getEnvironment().getMicrosoftGraphEndpoint(), "https://graph.microsoft.com/");
                assertEquals(properties.getGraphMembershipUri(), "https://graph.microsoft.com/v1.0/me/memberOf");
            });

        webApplicationContextRunner()
            .withPropertyValues(
                "spring.cloud.azure.active-directory.profile.environment.microsoft-graph-endpoint=https://microsoftgraph.chinacloudapi.cn"
            )
            .run(context -> {
                AadAuthenticationProperties properties = context.getBean(AadAuthenticationProperties.class);
                assertEquals(properties.getProfile().getEnvironment().getMicrosoftGraphEndpoint(), "https://microsoftgraph.chinacloudapi.cn/");
                assertEquals(properties.getGraphMembershipUri(),
                    "https://microsoftgraph.chinacloudapi.cn/v1.0/me/memberOf");
            });

        webApplicationContextRunner()
            .withPropertyValues(
                "spring.cloud.azure.active-directory..profile.environment.microsoft-graph-endpoint=https://microsoftgraph.chinacloudapi.cn/"
            )
            .run(context -> {
                AadAuthenticationProperties properties = context.getBean(AadAuthenticationProperties.class);
                assertEquals(properties.getProfile().getEnvironment().getMicrosoftGraphEndpoint(), "https://microsoftgraph.chinacloudapi.cn/");
                assertEquals(properties.getGraphMembershipUri(),
                    "https://microsoftgraph.chinacloudapi.cn/v1.0/me/memberOf");
            });

        webApplicationContextRunner()
            .run(context -> {
                AadAuthenticationProperties properties = context.getBean(AadAuthenticationProperties.class);
                assertEquals(properties.getProfile().getEnvironment().getMicrosoftGraphEndpoint(), "https://graph.microsoft.com/");
                assertEquals(properties.getGraphMembershipUri(), "https://graph.microsoft.com/v1.0/me/memberOf");
            });

        webApplicationContextRunner()
            .withPropertyValues(
                "spring.cloud.azure.active-directory.profile.environment.microsoft-graph-endpoint=https://microsoftgraph.chinacloudapi.cn/"
            )
            .run(context -> {
                AadAuthenticationProperties properties = context.getBean(AadAuthenticationProperties.class);
                assertEquals(properties.getProfile().getEnvironment().getMicrosoftGraphEndpoint(), "https://microsoftgraph.chinacloudapi.cn/");
                assertEquals(properties.getGraphMembershipUri(),
                    "https://microsoftgraph.chinacloudapi.cn/v1.0/me/memberOf");
            });
    }

    @Test
    void multiTenantWithAllowedGroupsConfiguredTest1() {
        webApplicationContextRunner()
            .withPropertyValues(
                "spring.cloud.azure.active-directory.profile.tenant-id=",
                "spring.cloud.azure.active-directory.user-group.allowed-group-names=group1,group2"
            )
            .run(context ->
                assertThrows(IllegalStateException.class, () -> context.getBean(AadAuthenticationProperties.class))
            );
    }

    @Test
    void multiTenantWithAllowedGroupsConfiguredTest2() {
        webApplicationContextRunner()
            .withPropertyValues(
                "spring.cloud.azure.active-directory.profile.tenant-id=common",
                "spring.cloud.azure.active-directory.user-group.allowed-group-names=group1,group2"
            )
            .run(context ->
                assertThrows(IllegalStateException.class, () -> context.getBean(AadAuthenticationProperties.class))
            );
    }

    @Test
    void multiTenantWithAllowedGroupsConfiguredTest3() {
        webApplicationContextRunner()
            .withPropertyValues(
                "spring.cloud.azure.active-directory.profile.tenant-id=organizations",
                "spring.cloud.azure.active-directory.user-group.allowed-group-names=group1,group2"
            )
            .run(context ->
                assertThrows(IllegalStateException.class, () -> context.getBean(AadAuthenticationProperties.class))
            );
    }

    @Test
    void multiTenantWithAllowedGroupsIdConfiguredTest1() {
        webApplicationContextRunner()
            .withPropertyValues(
                "spring.cloud.azure.active-directory.profile.tenant-id=",
                "spring.cloud.azure.active-directory.user-group.allowed-group-ids = 7c3a5d22-9093-42d7-b2eb-e72d06bf3718,"
                    + "39087533-2593-4b5b-ad05-4a73a01ea6a9"
            )
            .run(context ->
                assertThrows(IllegalStateException.class, () -> context.getBean(AadAuthenticationProperties.class))
            );
    }

    @Test
    void multiTenantWithAllowedGroupsIdConfiguredTest2() {
        webApplicationContextRunner()
            .withPropertyValues(
                "spring.cloud.azure.active-directory.profile.tenant-id=common",
                "spring.cloud.azure.active-directory.user-group.allowed-group-ids = 7c3a5d22-9093-42d7-b2eb-e72d06bf3718,"
                    + "39087533-2593-4b5b-ad05-4a73a01ea6a9"
            )
            .run(context ->
                assertThrows(IllegalStateException.class, () -> context.getBean(AadAuthenticationProperties.class))
            );
    }

    @Test
    void multiTenantWithAllowedGroupsIdConfiguredTest3() {
        webApplicationContextRunner()
            .withPropertyValues(
                "spring.cloud.azure.active-directory.profile.tenant-id=organizations",
                "spring.cloud.azure.active-directory.user-group.allowed-group-ids = 7c3a5d22-9093-42d7-b2eb-e72d06bf3718,"
                    + "39087533-2593-4b5b-ad05-4a73a01ea6a9"
            )
            .run(context ->
                assertThrows(IllegalStateException.class, () -> context.getBean(AadAuthenticationProperties.class))
            );
    }

    @Test
    void multiTenantWithAllowedGroupsIdConfiguredTest4() {
        webApplicationContextRunner()
            .withPropertyValues(
                "spring.cloud.azure.active-directory.profile.tenant-id=consumers",
                "spring.cloud.azure.active-directory.user-group.allowed-group-ids = 7c3a5d22-9093-42d7-b2eb-e72d06bf3718,"
                    + "39087533-2593-4b5b-ad05-4a73a01ea6a9"
            )
            .run(context ->
                assertThrows(IllegalStateException.class, () -> context.getBean(AadAuthenticationProperties.class))
            );
    }

    @Test
    void multiTenantWithAllowedGroupsConfiguredTest4() {
        webApplicationContextRunner()
            .withPropertyValues(
                "spring.cloud.azure.active-directory.profile.tenant-id=consumers",
                "spring.cloud.azure.active-directory.user-group.allowed-group-names=group1,group2"
            )
            .run(context ->
                assertThrows(IllegalStateException.class, () -> context.getBean(AadAuthenticationProperties.class))
            );
    }

    @Test
    void applicationTypeOfWebApplication() {
        webApplicationContextRunner()
            .run(context -> {
                AadAuthenticationProperties properties = context.getBean(AadAuthenticationProperties.class);
                assertEquals(properties.getApplicationType(), AadApplicationType.WEB_APPLICATION);
            });

        webApplicationContextRunner()
            .withPropertyValues("spring.cloud.azure.active-directory.application-type=web_application")
            .run(context -> {
                AadAuthenticationProperties properties = context.getBean(AadAuthenticationProperties.class);
                assertEquals(properties.getApplicationType(), AadApplicationType.WEB_APPLICATION);
            });

        resourceServerWithOboContextRunner()
            .withPropertyValues("spring.cloud.azure.active-directory.application-type=web_application")
            .run(context -> {
                AadAuthenticationProperties properties = context.getBean(AadAuthenticationProperties.class);
                assertEquals(properties.getApplicationType(), AadApplicationType.WEB_APPLICATION);
            });
    }

    @Test
    void applicationTypeWithResourceServer() {
        resourceServerContextRunner()
            .withPropertyValues("spring.cloud.azure.active-directory.enabled=true")
            .run(context -> {
                AadAuthenticationProperties properties = context.getBean(AadAuthenticationProperties.class);
                assertEquals(properties.getApplicationType(), AadApplicationType.RESOURCE_SERVER);
            });

        resourceServerContextRunner()
            .withPropertyValues(
                "spring.cloud.azure.active-directory.enabled=true",
                "spring.cloud.azure.active-directory.application-type=resource_server"
            )
            .run(context -> {
                AadAuthenticationProperties properties = context.getBean(AadAuthenticationProperties.class);
                assertEquals(properties.getApplicationType(), AadApplicationType.RESOURCE_SERVER);
            });

        resourceServerWithOboContextRunner()
            .withPropertyValues("spring.cloud.azure.active-directory.application-type=resource_server")
            .run(context -> {
                AadAuthenticationProperties properties = context.getBean(AadAuthenticationProperties.class);
                assertEquals(properties.getApplicationType(), AadApplicationType.RESOURCE_SERVER);
            });
    }

    @Test
    void applicationTypeOfResourceServerWithOBO() {
        resourceServerWithOboContextRunner()
            .run(context -> {
                AadAuthenticationProperties properties = context.getBean(AadAuthenticationProperties.class);
                assertEquals(properties.getApplicationType(), AadApplicationType.RESOURCE_SERVER_WITH_OBO);
            });

        resourceServerWithOboContextRunner()
            .withPropertyValues(
                "spring.cloud.azure.active-directory.application-type=resource_server_with_obo"
            )
            .run(context -> {
                AadAuthenticationProperties properties = context.getBean(AadAuthenticationProperties.class);
                assertEquals(properties.getApplicationType(), AadApplicationType.RESOURCE_SERVER_WITH_OBO);
            });
    }

    @Test
    void applicationTypeWithWebApplicationAndResourceServer() {
        resourceServerWithOboContextRunner()
            .withPropertyValues(
                "spring.cloud.azure.active-directory.application-type=web_application_and_resource_server"
            )
            .run(context -> {
                AadAuthenticationProperties properties = context.getBean(AadAuthenticationProperties.class);
                assertEquals(properties.getApplicationType(), AadApplicationType.WEB_APPLICATION_AND_RESOURCE_SERVER);
            });
    }

    @Test
    void testInvalidApplicationType() {
        resourceServerContextRunner()
            .withPropertyValues(
                "spring.cloud.azure.active-directory.enabled=true",
                "spring.cloud.azure.active-directory.application-type=web_application"
            )
            .run(context -> assertThrows(IllegalStateException.class, () -> context.getBean(AadAuthenticationProperties.class)));

        webApplicationContextRunner()
            .withPropertyValues("spring.cloud.azure.active-directory.application-type=resource_server")
            .run(context -> assertThrows(IllegalStateException.class, () -> context.getBean(AadAuthenticationProperties.class)));
    }

    @Test
    void setDefaultValueFromAzureGlobalPropertiesTest() {
        oauthClientRunner()
            .withPropertyValues(
                "spring.cloud.azure.active-directory.enabled = true",
                "spring.cloud.azure.credential.client-id = global-client-id",
                "spring.cloud.azure.credential.client-secret = global-client-secret",
                "spring.cloud.azure.profile.tenant-id = global-tenant-id",
                "spring.cloud.azure.active-directory.credential.client-id = aad-client-id",
                "spring.cloud.azure.active-directory.credential.client-secret = aad-client-secret",
                "spring.cloud.azure.active-directory.profile.tenant-id = aad-tenant-id"
            )
            .run(context -> {
                AadAuthenticationProperties properties = context.getBean(AadAuthenticationProperties.class);
                assertEquals("aad-client-id", properties.getCredential().getClientId());
                assertEquals("aad-client-secret", properties.getCredential().getClientSecret());
                assertEquals("aad-tenant-id", properties.getProfile().getTenantId());
            });
        oauthClientRunner()
            .withPropertyValues(
                "spring.cloud.azure.active-directory.enabled = true",
                "spring.cloud.azure.credential.client-id = global-client-id",
                "spring.cloud.azure.credential.client-secret = global-client-secret",
                "spring.cloud.azure.profile.tenant-id = global-tenant-id"
            )
            .run(context -> {
                AadAuthenticationProperties properties = context.getBean(AadAuthenticationProperties.class);
                assertEquals("global-client-id", properties.getCredential().getClientId());
                assertEquals("global-client-secret", properties.getCredential().getClientSecret());
                assertEquals("global-tenant-id", properties.getProfile().getTenantId());
            });
    }
}
