// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.active.directory.implementation.autoconfigure.aad;

import com.azure.spring.cloud.autoconfigure.active.directory.implementation.aad.AADApplicationType;
import org.junit.jupiter.api.Test;

import static com.azure.spring.cloud.autoconfigure.active.directory.implementation.aad.WebApplicationContextRunnerUtils.resourceServerContextRunner;
import static com.azure.spring.cloud.autoconfigure.active.directory.implementation.aad.WebApplicationContextRunnerUtils.resourceServerWithOboContextRunner;
import static com.azure.spring.cloud.autoconfigure.active.directory.implementation.aad.WebApplicationContextRunnerUtils.webApplicationContextRunner;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class AADAuthenticationPropertiesTest {

    @Test
    public void webAppWithOboWithExceptionTest() {
        webApplicationContextRunner()
            .withPropertyValues(
                "spring.cloud.azure.active-directory.authorization-clients.graph.authorizationGrantType = on_behalf_of")
            .run(context ->
                assertThrows(IllegalStateException.class, () -> context.getBean(AADAuthenticationProperties.class)));
    }

    @Test
    public void graphUriConfigurationTest() {
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
    public void graphUriConfigurationWithExceptionTest() {
        webApplicationContextRunner()
            .withPropertyValues(
                "spring.cloud.azure.active-directory.graph-membership-uri=https://microsoftgraph.chinacloudapi.cn/v1.0/me/memberOf"
            )
            .run(context ->
                assertThrows(IllegalStateException.class, () -> context.getBean(AADAuthenticationProperties.class))
            );
    }

    @Test
    public void multiTenantWithAllowedGroupsConfiguredTest1() {
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
    public void multiTenantWithAllowedGroupsConfiguredTest2() {
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
    public void multiTenantWithAllowedGroupsConfiguredTest3() {
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
    public void multiTenantWithAllowedGroupsIdConfiguredTest1() {
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
    public void multiTenantWithAllowedGroupsIdConfiguredTest2() {
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
    public void multiTenantWithAllowedGroupsIdConfiguredTest3() {
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
    public void multiTenantWithAllowedGroupsIdConfiguredTest4() {
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
    public void multiTenantWithAllowedGroupsConfiguredTest4() {
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
    public void applicationTypeOfWebApplication() {
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
    public void applicationTypeWithResourceServer() {
        resourceServerContextRunner()
            .run(context -> {
                AADAuthenticationProperties properties = context.getBean(AADAuthenticationProperties.class);
                assertEquals(properties.getApplicationType(), AADApplicationType.RESOURCE_SERVER);
            });

        resourceServerContextRunner()
            .withPropertyValues("spring.cloud.azure.active-directory.application-type=resource_server")
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
    public void applicationTypeOfResourceServerWithOBO() {
        resourceServerWithOboContextRunner()
            .run(context -> {
                AADAuthenticationProperties properties = context.getBean(AADAuthenticationProperties.class);
                assertEquals(properties.getApplicationType(), AADApplicationType.RESOURCE_SERVER_WITH_OBO);
            });

        resourceServerWithOboContextRunner()
            .withPropertyValues("spring.cloud.azure.active-directory.application-type=resource_server_with_obo")
            .run(context -> {
                AADAuthenticationProperties properties = context.getBean(AADAuthenticationProperties.class);
                assertEquals(properties.getApplicationType(), AADApplicationType.RESOURCE_SERVER_WITH_OBO);
            });
    }

    @Test
    public void applicationTypeWithWebApplicationAndResourceServer() {
        resourceServerWithOboContextRunner()
            .withPropertyValues("spring.cloud.azure.active-directory.application-type=web_application_and_resource_server")
            .run(context -> {
                AADAuthenticationProperties properties = context.getBean(AADAuthenticationProperties.class);
                assertEquals(properties.getApplicationType(), AADApplicationType.WEB_APPLICATION_AND_RESOURCE_SERVER);
            });
    }

    @Test
    public void testInvalidApplicationType() {
        resourceServerContextRunner()
            .withPropertyValues("spring.cloud.azure.active-directory.application-type=web_application")
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
    public void invalidAuthorizationCodeWhenOnDemandIsFalse() {
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
