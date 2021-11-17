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
                "azure.activedirectory.authorization-clients.graph.authorizationGrantType = on_behalf_of")
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
                "azure.activedirectory.graph-base-uri=https://microsoftgraph.chinacloudapi.cn"
            )
            .run(context -> {
                AADAuthenticationProperties properties = context.getBean(AADAuthenticationProperties.class);
                assertEquals(properties.getGraphBaseUri(), "https://microsoftgraph.chinacloudapi.cn/");
                assertEquals(properties.getGraphMembershipUri(),
                    "https://microsoftgraph.chinacloudapi.cn/v1.0/me/memberOf");
            });

        webApplicationContextRunner()
            .withPropertyValues(
                "azure.activedirectory.graph-base-uri=https://microsoftgraph.chinacloudapi.cn/"
            )
            .run(context -> {
                AADAuthenticationProperties properties = context.getBean(AADAuthenticationProperties.class);
                assertEquals(properties.getGraphBaseUri(), "https://microsoftgraph.chinacloudapi.cn/");
                assertEquals(properties.getGraphMembershipUri(),
                    "https://microsoftgraph.chinacloudapi.cn/v1.0/me/memberOf");
            });

        webApplicationContextRunner()
            .withPropertyValues(
                "azure.activedirectory.graph-membership-uri=https://graph.microsoft.com/v1.0/me/memberOf"
            )
            .run(context -> {
                AADAuthenticationProperties properties = context.getBean(AADAuthenticationProperties.class);
                assertEquals(properties.getGraphBaseUri(), "https://graph.microsoft.com/");
                assertEquals(properties.getGraphMembershipUri(), "https://graph.microsoft.com/v1.0/me/memberOf");
            });

        webApplicationContextRunner()
            .withPropertyValues(
                "azure.activedirectory.graph-base-uri=https://microsoftgraph.chinacloudapi.cn/",
                "azure.activedirectory.graph-membership-uri=https://microsoftgraph.chinacloudapi.cn/v1.0/me/memberOf"
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
                "azure.activedirectory.graph-membership-uri=https://microsoftgraph.chinacloudapi.cn/v1.0/me/memberOf"
            )
            .run(context ->
                assertThrows(IllegalStateException.class, () -> context.getBean(AADAuthenticationProperties.class))
            );
    }

    @Test
    void multiTenantWithAllowedGroupsConfiguredTest1() {
        webApplicationContextRunner()
            .withPropertyValues(
                "azure.activedirectory.tenant-id=",
                "azure.activedirectory.user-group.allowed-groups=group1,group2"
            )
            .run(context ->
                assertThrows(IllegalStateException.class, () -> context.getBean(AADAuthenticationProperties.class))
            );
    }

    @Test
    void multiTenantWithAllowedGroupsConfiguredTest2() {
        webApplicationContextRunner()
            .withPropertyValues(
                "azure.activedirectory.tenant-id=common",
                "azure.activedirectory.user-group.allowed-groups=group1,group2"
            )
            .run(context ->
                assertThrows(IllegalStateException.class, () -> context.getBean(AADAuthenticationProperties.class))
            );
    }

    @Test
    void multiTenantWithAllowedGroupsConfiguredTest3() {
        webApplicationContextRunner()
            .withPropertyValues(
                "azure.activedirectory.tenant-id=organizations",
                "azure.activedirectory.user-group.allowed-groups=group1,group2"
            )
            .run(context ->
                assertThrows(IllegalStateException.class, () -> context.getBean(AADAuthenticationProperties.class))
            );
    }

    @Test
    void multiTenantWithAllowedGroupsIdConfiguredTest1() {
        webApplicationContextRunner()
            .withPropertyValues(
                "azure.activedirectory.tenant-id=",
                "azure.activedirectory.user-group.allowed-group-ids = 7c3a5d22-9093-42d7-b2eb-e72d06bf3718,"
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
                "azure.activedirectory.tenant-id=common",
                "azure.activedirectory.user-group.allowed-group-ids = 7c3a5d22-9093-42d7-b2eb-e72d06bf3718,"
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
                "azure.activedirectory.tenant-id=organizations",
                "azure.activedirectory.user-group.allowed-group-ids = 7c3a5d22-9093-42d7-b2eb-e72d06bf3718,"
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
                "azure.activedirectory.tenant-id=consumers",
                "azure.activedirectory.user-group.allowed-group-ids = 7c3a5d22-9093-42d7-b2eb-e72d06bf3718,"
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
                "azure.activedirectory.tenant-id=consumers",
                "azure.activedirectory.user-group.allowed-groups=group1,group2"
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
            .withPropertyValues("azure.activedirectory.application-type=web_application")
            .run(context -> {
                AADAuthenticationProperties properties = context.getBean(AADAuthenticationProperties.class);
                assertEquals(properties.getApplicationType(), AADApplicationType.WEB_APPLICATION);
            });

        resourceServerWithOboContextRunner()
            .withPropertyValues("azure.activedirectory.application-type=web_application")
            .run(context -> {
                AADAuthenticationProperties properties = context.getBean(AADAuthenticationProperties.class);
                assertEquals(properties.getApplicationType(), AADApplicationType.WEB_APPLICATION);
            });
    }

    @Test
    void applicationTypeWithResourceServer() {
        resourceServerContextRunner()
            .run(context -> {
                AADAuthenticationProperties properties = context.getBean(AADAuthenticationProperties.class);
                assertEquals(properties.getApplicationType(), AADApplicationType.RESOURCE_SERVER);
            });

        resourceServerContextRunner()
            .withPropertyValues("azure.activedirectory.application-type=resource_server")
            .run(context -> {
                AADAuthenticationProperties properties = context.getBean(AADAuthenticationProperties.class);
                assertEquals(properties.getApplicationType(), AADApplicationType.RESOURCE_SERVER);
            });

        resourceServerWithOboContextRunner()
            .withPropertyValues("azure.activedirectory.application-type=resource_server")
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
            .withPropertyValues("azure.activedirectory.application-type=resource_server_with_obo")
            .run(context -> {
                AADAuthenticationProperties properties = context.getBean(AADAuthenticationProperties.class);
                assertEquals(properties.getApplicationType(), AADApplicationType.RESOURCE_SERVER_WITH_OBO);
            });
    }

    @Test
    void applicationTypeWithWebApplicationAndResourceServer() {
        resourceServerWithOboContextRunner()
            .withPropertyValues("azure.activedirectory.application-type=web_application_and_resource_server")
            .run(context -> {
                AADAuthenticationProperties properties = context.getBean(AADAuthenticationProperties.class);
                assertEquals(properties.getApplicationType(), AADApplicationType.WEB_APPLICATION_AND_RESOURCE_SERVER);
            });
    }

    @Test
    void testInvalidApplicationType() {
        resourceServerContextRunner()
            .withPropertyValues("azure.activedirectory.application-type=web_application")
            .run(context -> {
                assertThrows(IllegalStateException.class, () -> context.getBean(AADAuthenticationProperties.class));
            });

        webApplicationContextRunner()
            .withPropertyValues("azure.activedirectory.application-type=resource_server")
            .run(context -> {
                assertThrows(IllegalStateException.class, () -> context.getBean(AADAuthenticationProperties.class));
            });
    }

    @Test
    void invalidAuthorizationCodeWhenOnDemandIsFalse() {
        webApplicationContextRunner()
            .withPropertyValues(
                "azure.activedirectory.authorization-clients.graph.scopes = Graph.Scope",
                "azure.activedirectory.authorization-clients.graph.on-demand = true",
                "azure.activedirectory.authorization-clients.graph.authorizationGrantType = azure_delegated"
            )
            .run(context -> {
                assertThrows(IllegalStateException.class, () -> context.getBean(AADAuthenticationProperties.class));
            });
    }
}
