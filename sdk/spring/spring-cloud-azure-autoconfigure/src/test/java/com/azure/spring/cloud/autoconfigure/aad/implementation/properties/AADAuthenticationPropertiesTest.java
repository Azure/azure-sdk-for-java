// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.aad.implementation.properties;

import com.azure.spring.cloud.autoconfigure.aad.implementation.core.AADApplicationType;
import org.junit.jupiter.api.Test;

import static com.azure.spring.cloud.autoconfigure.aad.implementation.WebApplicationContextRunnerUtils.resourceServerContextRunner;
import static com.azure.spring.cloud.autoconfigure.aad.implementation.WebApplicationContextRunnerUtils.resourceServerWithOboContextRunner;
import static com.azure.spring.cloud.autoconfigure.aad.implementation.WebApplicationContextRunnerUtils.webApplicationContextRunner;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AADAuthenticationPropertiesTest {

    @Test
    void webAppWithOboWithExceptionTest() {
        webApplicationContextRunner()
            .withPropertyValues(
                "spring.cloud.azure.active-directory.authorization-clients.graph.authorizationGrantType = on_behalf_of")
            .run(context ->
                assertThrows(IllegalStateException.class, () -> context.getBean(AADAuthenticationProperties.class)));
    }

    @Test
    void graphUriConfigurationTest() {
        webApplicationContextRunner()
            .run(context -> {
                AADAuthenticationProperties properties = context.getBean(AADAuthenticationProperties.class);
                assertEquals(properties.getGraphBaseUri(), "https://graph.microsoft.com/");
                assertEquals(properties.getGraphMembershipUri(), "https://graph.microsoft.com/v1.0/me/memberOf");
            });

        webApplicationContextRunner()
            .withPropertyValues(
                "spring.cloud.azure.active-directory.graph-base-uri=https://microsoftgraph.chinacloudapi.cn"
            )
            .run(context -> {
                AADAuthenticationProperties properties = context.getBean(AADAuthenticationProperties.class);
                assertEquals(properties.getGraphBaseUri(), "https://microsoftgraph.chinacloudapi.cn/");
                assertEquals(properties.getGraphMembershipUri(),
                    "https://microsoftgraph.chinacloudapi.cn/v1.0/me/memberOf");
            });

        webApplicationContextRunner()
            .withPropertyValues(
                "spring.cloud.azure.active-directory.graph-base-uri=https://microsoftgraph.chinacloudapi.cn/"
            )
            .run(context -> {
                AADAuthenticationProperties properties = context.getBean(AADAuthenticationProperties.class);
                assertEquals(properties.getGraphBaseUri(), "https://microsoftgraph.chinacloudapi.cn/");
                assertEquals(properties.getGraphMembershipUri(),
                    "https://microsoftgraph.chinacloudapi.cn/v1.0/me/memberOf");
            });

        webApplicationContextRunner()
            .withPropertyValues(
                "spring.cloud.azure.active-directory.graph-membership-uri=https://graph.microsoft.com/v1.0/me/memberOf"
            )
            .run(context -> {
                AADAuthenticationProperties properties = context.getBean(AADAuthenticationProperties.class);
                assertEquals(properties.getGraphBaseUri(), "https://graph.microsoft.com/");
                assertEquals(properties.getGraphMembershipUri(), "https://graph.microsoft.com/v1.0/me/memberOf");
            });

        webApplicationContextRunner()
            .withPropertyValues(
                "spring.cloud.azure.active-directory.graph-base-uri=https://microsoftgraph.chinacloudapi.cn/",
                "spring.cloud.azure.active-directory.graph-membership-uri=https://microsoftgraph.chinacloudapi.cn/v1.0/me/memberOf"
            )
            .run(context -> {
                AADAuthenticationProperties properties = context.getBean(AADAuthenticationProperties.class);
                assertEquals(properties.getGraphBaseUri(), "https://microsoftgraph.chinacloudapi.cn/");
                assertEquals(properties.getGraphMembershipUri(),
                    "https://microsoftgraph.chinacloudapi.cn/v1.0/me/memberOf");
            });
    }

    @Test
    void graphUriConfigurationWithExceptionTest() {
        webApplicationContextRunner()
            .withPropertyValues(
                "spring.cloud.azure.active-directory.graph-membership-uri=https://microsoftgraph.chinacloudapi.cn/v1.0/me/memberOf"
            )
            .run(context ->
                assertThrows(IllegalStateException.class, () -> context.getBean(AADAuthenticationProperties.class))
            );
    }

    @Test
    void multiTenantWithAllowedGroupsConfiguredTest1() {
        webApplicationContextRunner()
            .withPropertyValues(
                "spring.cloud.azure.active-directory.tenant-id=",
                "spring.cloud.azure.active-directory.user-group.allowed-groups=group1,group2"
            )
            .run(context ->
                assertThrows(IllegalStateException.class, () -> context.getBean(AADAuthenticationProperties.class))
            );
    }

    @Test
    void multiTenantWithAllowedGroupsConfiguredTest2() {
        webApplicationContextRunner()
            .withPropertyValues(
                "spring.cloud.azure.active-directory.tenant-id=common",
                "spring.cloud.azure.active-directory.user-group.allowed-groups=group1,group2"
            )
            .run(context ->
                assertThrows(IllegalStateException.class, () -> context.getBean(AADAuthenticationProperties.class))
            );
    }

    @Test
    void multiTenantWithAllowedGroupsConfiguredTest3() {
        webApplicationContextRunner()
            .withPropertyValues(
                "spring.cloud.azure.active-directory.tenant-id=organizations",
                "spring.cloud.azure.active-directory.user-group.allowed-groups=group1,group2"
            )
            .run(context ->
                assertThrows(IllegalStateException.class, () -> context.getBean(AADAuthenticationProperties.class))
            );
    }

    @Test
    void multiTenantWithAllowedGroupsIdConfiguredTest1() {
        webApplicationContextRunner()
            .withPropertyValues(
                "spring.cloud.azure.active-directory.tenant-id=",
                "spring.cloud.azure.active-directory.user-group.allowed-group-ids = 7c3a5d22-9093-42d7-b2eb-e72d06bf3718,"
                    + "39087533-2593-4b5b-ad05-4a73a01ea6a9"
            )
            .run(context ->
                assertThrows(IllegalStateException.class, () -> context.getBean(AADAuthenticationProperties.class))
            );
    }

    @Test
    void multiTenantWithAllowedGroupsIdConfiguredTest2() {
        webApplicationContextRunner()
            .withPropertyValues(
                "spring.cloud.azure.active-directory.tenant-id=common",
                "spring.cloud.azure.active-directory.user-group.allowed-group-ids = 7c3a5d22-9093-42d7-b2eb-e72d06bf3718,"
                    + "39087533-2593-4b5b-ad05-4a73a01ea6a9"
            )
            .run(context ->
                assertThrows(IllegalStateException.class, () -> context.getBean(AADAuthenticationProperties.class))
            );
    }

    @Test
    void multiTenantWithAllowedGroupsIdConfiguredTest3() {
        webApplicationContextRunner()
            .withPropertyValues(
                "spring.cloud.azure.active-directory.tenant-id=organizations",
                "spring.cloud.azure.active-directory.user-group.allowed-group-ids = 7c3a5d22-9093-42d7-b2eb-e72d06bf3718,"
                    + "39087533-2593-4b5b-ad05-4a73a01ea6a9"
            )
            .run(context ->
                assertThrows(IllegalStateException.class, () -> context.getBean(AADAuthenticationProperties.class))
            );
    }

    @Test
    void multiTenantWithAllowedGroupsIdConfiguredTest4() {
        webApplicationContextRunner()
            .withPropertyValues(
                "spring.cloud.azure.active-directory.tenant-id=consumers",
                "spring.cloud.azure.active-directory.user-group.allowed-group-ids = 7c3a5d22-9093-42d7-b2eb-e72d06bf3718,"
                    + "39087533-2593-4b5b-ad05-4a73a01ea6a9"
            )
            .run(context ->
                assertThrows(IllegalStateException.class, () -> context.getBean(AADAuthenticationProperties.class))
            );
    }

    @Test
    void multiTenantWithAllowedGroupsConfiguredTest4() {
        webApplicationContextRunner()
            .withPropertyValues(
                "spring.cloud.azure.active-directory.tenant-id=consumers",
                "spring.cloud.azure.active-directory.user-group.allowed-groups=group1,group2"
            )
            .run(context ->
                assertThrows(IllegalStateException.class, () -> context.getBean(AADAuthenticationProperties.class))
            );
    }

    @Test
    void applicationTypeOfWebApplication() {
        webApplicationContextRunner()
            .run(context -> {
                AADAuthenticationProperties properties = context.getBean(AADAuthenticationProperties.class);
                assertEquals(properties.getApplicationType(), AADApplicationType.WEB_APPLICATION);
            });

        webApplicationContextRunner()
            .withPropertyValues("spring.cloud.azure.active-directory.application-type=web_application")
            .run(context -> {
                AADAuthenticationProperties properties = context.getBean(AADAuthenticationProperties.class);
                assertEquals(properties.getApplicationType(), AADApplicationType.WEB_APPLICATION);
            });

        resourceServerWithOboContextRunner()
            .withPropertyValues("spring.cloud.azure.active-directory.application-type=web_application")
            .run(context -> {
                AADAuthenticationProperties properties = context.getBean(AADAuthenticationProperties.class);
                assertEquals(properties.getApplicationType(), AADApplicationType.WEB_APPLICATION);
            });
    }

    @Test
    void applicationTypeWithResourceServer() {
        resourceServerContextRunner()
            .withPropertyValues("spring.cloud.azure.active-directory.enabled=true")
            .run(context -> {
                AADAuthenticationProperties properties = context.getBean(AADAuthenticationProperties.class);
                assertEquals(properties.getApplicationType(), AADApplicationType.RESOURCE_SERVER);
            });

        resourceServerContextRunner()
            .withPropertyValues(
                "spring.cloud.azure.active-directory.enabled=true",
                "spring.cloud.azure.active-directory.application-type=resource_server"
            )
            .run(context -> {
                AADAuthenticationProperties properties = context.getBean(AADAuthenticationProperties.class);
                assertEquals(properties.getApplicationType(), AADApplicationType.RESOURCE_SERVER);
            });

        resourceServerWithOboContextRunner()
            .withPropertyValues("spring.cloud.azure.active-directory.application-type=resource_server")
            .run(context -> {
                AADAuthenticationProperties properties = context.getBean(AADAuthenticationProperties.class);
                assertEquals(properties.getApplicationType(), AADApplicationType.RESOURCE_SERVER);
            });
    }

    @Test
    void applicationTypeOfResourceServerWithOBO() {
        resourceServerWithOboContextRunner()
            .run(context -> {
                AADAuthenticationProperties properties = context.getBean(AADAuthenticationProperties.class);
                assertEquals(properties.getApplicationType(), AADApplicationType.RESOURCE_SERVER_WITH_OBO);
            });

        resourceServerWithOboContextRunner()
            .withPropertyValues(
                "spring.cloud.azure.active-directory.application-type=resource_server_with_obo"
            )
            .run(context -> {
                AADAuthenticationProperties properties = context.getBean(AADAuthenticationProperties.class);
                assertEquals(properties.getApplicationType(), AADApplicationType.RESOURCE_SERVER_WITH_OBO);
            });
    }

    @Test
    void applicationTypeWithWebApplicationAndResourceServer() {
        resourceServerWithOboContextRunner()
            .withPropertyValues(
                "spring.cloud.azure.active-directory.application-type=web_application_and_resource_server"
            )
            .run(context -> {
                AADAuthenticationProperties properties = context.getBean(AADAuthenticationProperties.class);
                assertEquals(properties.getApplicationType(), AADApplicationType.WEB_APPLICATION_AND_RESOURCE_SERVER);
            });
    }

    @Test
    void testInvalidApplicationType() {
        resourceServerContextRunner()
            .withPropertyValues(
                "spring.cloud.azure.active-directory.enabled=true",
                "spring.cloud.azure.active-directory.application-type=web_application"
            )
            .run(context -> {
                assertThrows(IllegalStateException.class, () -> context.getBean(AADAuthenticationProperties.class));
            });

        webApplicationContextRunner()
            .withPropertyValues("spring.cloud.azure.active-directory.application-type=resource_server")
            .run(context -> {
                assertThrows(IllegalStateException.class, () -> context.getBean(AADAuthenticationProperties.class));
            });
    }

    @Test
    void invalidAuthorizationCodeWhenOnDemandIsFalse() {
        webApplicationContextRunner()
            .withPropertyValues(
                "spring.cloud.azure.active-directory.authorization-clients.graph.scopes = Graph.Scope",
                "spring.cloud.azure.active-directory.authorization-clients.graph.on-demand = true",
                "spring.cloud.azure.active-directory.authorization-clients.graph.authorizationGrantType = azure_delegated"
            )
            .run(context -> {
                assertThrows(IllegalStateException.class, () -> context.getBean(AADAuthenticationProperties.class));
            });
    }
}
