// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.autoconfigure.aad;

import com.azure.spring.aad.AADApplicationType;
import com.azure.spring.aad.webapp.WebApplicationContextRunnerUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.server.resource.BearerTokenAuthenticationToken;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class AADAuthenticationPropertiesTest {

    private final WebApplicationContextRunner contextRunner = new WebApplicationContextRunner();

    @Test
    public void webAppWithOboWithExceptionTest() {
        WebApplicationContextRunnerUtils
            .getContextRunnerWithRequiredProperties()
            .withPropertyValues(
                "azure.activedirectory.authorization-clients.graph.authorizationGrantType = on_behalf_of")
            .run(context ->
                assertThrows(IllegalStateException.class, () -> context.getBean(AADAuthenticationProperties.class)));
    }

    @Test
    public void graphUriConfigurationTest() {
        WebApplicationContextRunnerUtils
            .getContextRunnerWithRequiredProperties()
            .run(context -> {
                AADAuthenticationProperties properties = context.getBean(AADAuthenticationProperties.class);
                assertEquals(properties.getGraphBaseUri(), "https://graph.microsoft.com/");
                assertEquals(properties.getGraphMembershipUri(), "https://graph.microsoft.com/v1.0/me/memberOf");
            });

        WebApplicationContextRunnerUtils
            .getContextRunnerWithRequiredProperties()
            .withPropertyValues(
                "azure.activedirectory.graph-base-uri=https://microsoftgraph.chinacloudapi.cn"
            )
            .run(context -> {
                AADAuthenticationProperties properties = context.getBean(AADAuthenticationProperties.class);
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
                AADAuthenticationProperties properties = context.getBean(AADAuthenticationProperties.class);
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
                AADAuthenticationProperties properties = context.getBean(AADAuthenticationProperties.class);
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
                AADAuthenticationProperties properties = context.getBean(AADAuthenticationProperties.class);
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
            .run(context ->
                assertThrows(IllegalStateException.class, () -> context.getBean(AADAuthenticationProperties.class))
            );
    }

    @Test
    public void multiTenantWithAllowedGroupsConfiguredTest1() {
        WebApplicationContextRunnerUtils
            .getContextRunnerWithRequiredProperties()
            .withPropertyValues(
                "azure.activedirectory.tenant-id=",
                "azure.activedirectory.user-group.allowed-groups=group1,group2"
            )
            .run(context ->
                assertThrows(IllegalStateException.class, () -> context.getBean(AADAuthenticationProperties.class))
            );
    }

    @Test
    public void multiTenantWithAllowedGroupsConfiguredTest2() {
        WebApplicationContextRunnerUtils
            .getContextRunnerWithRequiredProperties()
            .withPropertyValues(
                "azure.activedirectory.tenant-id=common",
                "azure.activedirectory.user-group.allowed-groups=group1,group2"
            )
            .run(context ->
                assertThrows(IllegalStateException.class, () -> context.getBean(AADAuthenticationProperties.class))
            );
    }

    @Test
    public void multiTenantWithAllowedGroupsConfiguredTest3() {
        WebApplicationContextRunnerUtils
            .getContextRunnerWithRequiredProperties()
            .withPropertyValues(
                "azure.activedirectory.tenant-id=organizations",
                "azure.activedirectory.user-group.allowed-groups=group1,group2"
            )
            .run(context ->
                assertThrows(IllegalStateException.class, () -> context.getBean(AADAuthenticationProperties.class))
            );
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
            .run(context ->
                assertThrows(IllegalStateException.class, () -> context.getBean(AADAuthenticationProperties.class))
            );
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
            .run(context ->
                assertThrows(IllegalStateException.class, () -> context.getBean(AADAuthenticationProperties.class))
            );
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
            .run(context ->
                assertThrows(IllegalStateException.class, () -> context.getBean(AADAuthenticationProperties.class))
            );
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
            .run(context ->
                assertThrows(IllegalStateException.class, () -> context.getBean(AADAuthenticationProperties.class))
            );
    }

    @Test
    public void multiTenantWithAllowedGroupsConfiguredTest4() {
        WebApplicationContextRunnerUtils
            .getContextRunnerWithRequiredProperties()
            .withPropertyValues(
                "azure.activedirectory.tenant-id=consumers",
                "azure.activedirectory.user-group.allowed-groups=group1,group2"
            )
            .run(context ->
                assertThrows(IllegalStateException.class, () -> context.getBean(AADAuthenticationProperties.class))
            );
    }

    private WebApplicationContextRunner contextRunnerWithConfiguredApplicationType(String applicationType) {
        WebApplicationContextRunner runner = contextRunner
            .withUserConfiguration(AADAutoConfiguration.class)
            .withPropertyValues(
                "azure.activedirectory.client-id = fake-client-id",
                "azure.activedirectory.client-secret = fake-client-secret",
                "azure.activedirectory.tenant-id = fake-tenant-id",
                "azure.activedirectory.app-id-uri=fake-app-id-uri");
        if (applicationType == null) {
            return runner;
        }
        return runner
            .withPropertyValues("azure.activedirectory.application-type=" + applicationType);
    }

    @Test
    public void applicationTypeOfWebApplication() {
        WebApplicationContextRunnerUtils
            .getContextRunnerWithRequiredProperties()
            .run(context -> {
                AADAuthenticationProperties properties = context.getBean(AADAuthenticationProperties.class);
                assertEquals(properties.getApplicationType(), AADApplicationType.WEB_APPLICATION);
            });

        WebApplicationContextRunnerUtils
            .getContextRunnerWithRequiredProperties()
            .withPropertyValues(
                "azure.activedirectory.application-type=web_application")
            .run(context -> {
                AADAuthenticationProperties properties = context.getBean(AADAuthenticationProperties.class);
                assertEquals(properties.getApplicationType(), AADApplicationType.WEB_APPLICATION);
            });

        this.contextRunnerWithConfiguredApplicationType("web_application")
            .run(context -> {
                AADAuthenticationProperties properties = context.getBean(AADAuthenticationProperties.class);
                assertEquals(properties.getApplicationType(), AADApplicationType.WEB_APPLICATION);
            });
    }

    @Test
    public void applicationTypeOfResourceServerWithOBO() {
        this.contextRunnerWithConfiguredApplicationType(null)
            .run(context -> {
                AADAuthenticationProperties properties = context.getBean(AADAuthenticationProperties.class);
                assertEquals(properties.getApplicationType(), AADApplicationType.RESOURCE_SERVER_WITH_OBO);
            });

        this.contextRunnerWithConfiguredApplicationType("resource_server_with_obo")
            .withPropertyValues(
                "azure.activedirectory.application-type=resource_server_with_obo")
            .run(context -> {
                AADAuthenticationProperties properties = context.getBean(AADAuthenticationProperties.class);
                assertEquals(properties.getApplicationType(), AADApplicationType.RESOURCE_SERVER_WITH_OBO);
            });
    }

    @Test
    public void applicationTypeWithWebApplicationAndResourceServer() {
        this.contextRunnerWithConfiguredApplicationType("web_application_and_resource_server")
            .run(context -> {
                AADAuthenticationProperties properties = context.getBean(AADAuthenticationProperties.class);
                assertEquals(properties.getApplicationType(), AADApplicationType.WEB_APPLICATION_AND_RESOURCE_SERVER);
            });
    }


    @Test
    public void applicationTypeWithResourceServer() {
        this.contextRunnerWithConfiguredApplicationType(null)
            .withClassLoader(new FilteredClassLoader(ClientRegistration.class))
            .run(context -> {
                AADAuthenticationProperties properties = context.getBean(AADAuthenticationProperties.class);
                assertEquals(properties.getApplicationType(), AADApplicationType.RESOURCE_SERVER);
            });

        this.contextRunnerWithConfiguredApplicationType(null)
            .withClassLoader(new FilteredClassLoader(ClientRegistration.class))
            .withPropertyValues(
                "azure.activedirectory.application-type=resource_server")
            .run(context -> {
                AADAuthenticationProperties properties = context.getBean(AADAuthenticationProperties.class);
                assertEquals(properties.getApplicationType(), AADApplicationType.RESOURCE_SERVER);
            });

        this.contextRunnerWithConfiguredApplicationType("resource_server")
            .run(context -> {
                AADAuthenticationProperties properties = context.getBean(AADAuthenticationProperties.class);
                assertEquals(properties.getApplicationType(), AADApplicationType.RESOURCE_SERVER);
            });
    }

    @Test
    public void applicationTypeOfInvalidWebApplication() {
        this.contextRunnerWithConfiguredApplicationType("web_application")
            .withClassLoader(new FilteredClassLoader(ClientRegistration.class))
            .run(context -> {
                assertThrows(IllegalStateException.class, () -> context.getBean(AADAuthenticationProperties.class));
            });
    }

    @Test
    public void applicationTypeOfInvalidResourceServer() {
        this.contextRunnerWithConfiguredApplicationType("resource_server")
            .withClassLoader(new FilteredClassLoader(BearerTokenAuthenticationToken.class))
            .run(context -> {
                assertThrows(IllegalStateException.class, () -> context.getBean(AADAuthenticationProperties.class));
            });
    }
}
