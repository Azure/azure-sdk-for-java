// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.aad;

import com.azure.spring.autoconfigure.aad.AADAuthenticationProperties;
import org.junit.Test;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.security.oauth2.client.registration.ClientRegistration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class AADAuthenticationPropertiesTest {

    @Test
    public void customizeUri() {
        new WebApplicationContextRunner()
            .withUserConfiguration(AADConfiguration.class)
            .withPropertyValues(
                "azure.activedirectory.tenant-id=fake-tenant-id",
                "azure.activedirectory.client-id=fake-client-id",
                "azure.activedirectory.client-secret=fake-client-secret",
                "azure.activedirectory.base-uri = http://localhost/")
            .run(context -> {
                AADClientRegistrationRepository repo = context.getBean(AADClientRegistrationRepository.class);
                ClientRegistration azure = repo.findByRegistrationId("azure");
                assertNotNull(azure);
                AADAuthorizationServerEndpoints endpoints =
                    new AADAuthorizationServerEndpoints("http://localhost/", "fake-tenant-id");
                assertEquals(endpoints.authorizationEndpoint(), azure.getProviderDetails().getAuthorizationUri());
                assertEquals(endpoints.tokenEndpoint(), azure.getProviderDetails().getTokenUri());
                assertEquals(endpoints.jwkSetEndpoint(), azure.getProviderDetails().getJwkSetUri());
            });
    }

    @Test(expected = IllegalStateException.class)
    public void testGrantTypeNotConfigured() {
        new WebApplicationContextRunner()
            .withUserConfiguration(AADConfiguration.class)
            .withPropertyValues(
                "azure.activedirectory.tenant-id=fake-tenant-id",
                "azure.activedirectory.client-id=fake-client-id",
                "azure.activedirectory.client-secret=fake-client-secret",
                "azure.activedirectory.authorization-clients.graph.scopes=https://graph.microsoft.com/User.Read")
            .run(context -> {
                AADAuthenticationProperties properties = context.getBean(AADAuthenticationProperties.class);
                assertNotNull(properties);
            });
    }

    @Test(expected = IllegalStateException.class)
    public void testScopesNotConfigured() {
        new WebApplicationContextRunner()
            .withUserConfiguration(AADConfiguration.class)
            .withPropertyValues(
                "azure.activedirectory.tenant-id=fake-tenant-id",
                "azure.activedirectory.client-id=fake-client-id",
                "azure.activedirectory.client-secret=fake-client-secret",
                "azure.activedirectory.authorization-clients.graph.authorization-grant-type=authorization_code")
            .run(context -> {
                AADAuthenticationProperties properties = context.getBean(AADAuthenticationProperties.class);
                assertNotNull(properties);
            });
    }

    @Test
    public void testGrantTypeAndScopesConfigured() {
        new WebApplicationContextRunner()
            .withUserConfiguration(AADConfiguration.class)
            .withPropertyValues(
                "azure.activedirectory.tenant-id=fake-tenant-id",
                "azure.activedirectory.client-id=fake-client-id",
                "azure.activedirectory.client-secret=fake-client-secret",
                "azure.activedirectory.authorization-clients.graph.authorization-grant-type=authorization_code",
                "azure.activedirectory.authorization-clients.graph.scopes=https://graph.microsoft.com/User.Read")
            .run(context -> {
                AADAuthenticationProperties properties = context.getBean(AADAuthenticationProperties.class);
                assertNotNull(properties);
            });
    }

    @Test(expected = IllegalStateException.class)
    public void onBehalfOfWithOnDemand() {
        new WebApplicationContextRunner()
            .withUserConfiguration(AADConfiguration.class)
            .withPropertyValues(
                "azure.activedirectory.tenant-id=fake-tenant-id",
                "azure.activedirectory.client-id=fake-client-id",
                "azure.activedirectory.client-secret=fake-client-secret",
                "azure.activedirectory.authorization-clients.graph.authorization-grant-type=on-behalf-of",
                "azure.activedirectory.authorization-clients.graph.on-demand=true")
            .run(context -> {
                AADAuthenticationProperties properties = context.getBean(AADAuthenticationProperties.class);
                assertNotNull(properties);
            });
    }

    @Test(expected = IllegalStateException.class)
    public void clientCredentialWithOnDemandTest() {
        new WebApplicationContextRunner()
            .withUserConfiguration(AADConfiguration.class)
            .withPropertyValues(
                "azure.activedirectory.tenant-id=fake-tenant-id",
                "azure.activedirectory.client-id=fake-client-id",
                "azure.activedirectory.client-secret=fake-client-secret",
                "azure.activedirectory.authorization-clients.graph.authorization-grant-type=client_credentials",
                "azure.activedirectory.authorization-clients.graph.on-demand = true")
            .run(context -> {
                AADAuthenticationProperties properties = context.getBean(AADAuthenticationProperties.class);
                assertNotNull(properties);
            });
    }

    @Test
    public void graphBaseUriTest() {
        new WebApplicationContextRunner()
            .withUserConfiguration(AADConfiguration.class)
            .withPropertyValues(
                "azure.activedirectory.tenant-id=fake-tenant-id",
                "azure.activedirectory.client-id=fake-client-id",
                "azure.activedirectory.client-secret=fake-client-secret")
            .run(context -> {
                AADAuthenticationProperties properties = context.getBean(AADAuthenticationProperties.class);
                assertNotNull(properties);
                assertEquals(properties.getGraphBaseUri(), "https://graph.microsoft.com/");
                assertEquals(properties.getGraphMembershipUri(), "https://graph.microsoft.com/v1.0/me/memberOf");
            });

        new WebApplicationContextRunner()
            .withUserConfiguration(AADConfiguration.class)
            .withPropertyValues(
                "azure.activedirectory.tenant-id=fake-tenant-id",
                "azure.activedirectory.client-id=fake-client-id",
                "azure.activedirectory.client-secret=fake-client-secret",
                "azure.activedirectory.graph-base-uri=https://microsoftgraph.chinacloudapi.cn")
            .run(context -> {
                AADAuthenticationProperties properties = context.getBean(AADAuthenticationProperties.class);
                assertNotNull(properties);
                assertEquals(properties.getGraphBaseUri(), "https://microsoftgraph.chinacloudapi.cn/");
                assertEquals(properties.getGraphMembershipUri(),
                    "https://microsoftgraph.chinacloudapi.cn/v1.0/me/memberOf");
            });

        new WebApplicationContextRunner()
            .withUserConfiguration(AADConfiguration.class)
            .withPropertyValues(
                "azure.activedirectory.tenant-id=fake-tenant-id",
                "azure.activedirectory.client-id=fake-client-id",
                "azure.activedirectory.client-secret=fake-client-secret",
                "azure.activedirectory.graph-base-uri=https://microsoftgraph.chinacloudapi.cn/")
            .run(context -> {
                AADAuthenticationProperties properties = context.getBean(AADAuthenticationProperties.class);
                assertNotNull(properties);
                assertEquals(properties.getGraphBaseUri(), "https://microsoftgraph.chinacloudapi.cn/");
                assertEquals(properties.getGraphMembershipUri(),
                    "https://microsoftgraph.chinacloudapi.cn/v1.0/me/memberOf");
            });

        new WebApplicationContextRunner()
            .withUserConfiguration(AADConfiguration.class)
            .withPropertyValues(
                "azure.activedirectory.tenant-id=fake-tenant-id",
                "azure.activedirectory.client-id=fake-client-id",
                "azure.activedirectory.client-secret=fake-client-secret",
                "azure.activedirectory.graph-membership-uri=https://graph.microsoft.com/v1.0/me/memberOf")
            .run(context -> {
                AADAuthenticationProperties properties = context.getBean(AADAuthenticationProperties.class);
                assertNotNull(properties);
                assertEquals(properties.getGraphBaseUri(), "https://graph.microsoft.com/");
                assertEquals(properties.getGraphMembershipUri(), "https://graph.microsoft.com/v1.0/me/memberOf");
            });

        new WebApplicationContextRunner()
            .withUserConfiguration(AADConfiguration.class)
            .withPropertyValues(
                "azure.activedirectory.tenant-id=fake-tenant-id",
                "azure.activedirectory.client-id=fake-client-id",
                "azure.activedirectory.client-secret=fake-client-secret",
                "azure.activedirectory.graph-base-uri=https://microsoftgraph.chinacloudapi.cn/",
                "azure.activedirectory.graph-membership-uri=https://microsoftgraph.chinacloudapi.cn/v1.0/me/memberOf")
            .run(context -> {
                AADAuthenticationProperties properties = context.getBean(AADAuthenticationProperties.class);
                assertNotNull(properties);
                assertEquals(properties.getGraphBaseUri(), "https://microsoftgraph.chinacloudapi.cn/");
                assertEquals(properties.getGraphMembershipUri(), "https://microsoftgraph.chinacloudapi.cn/v1"
                    + ".0/me/memberOf");
            });
    }

    @Test(expected = IllegalStateException.class)
    public void graphUriConfigurationWithExceptionTest() {
        new WebApplicationContextRunner()
            .withUserConfiguration(AADConfiguration.class)
            .withPropertyValues(
                "azure.activedirectory.tenant-id=fake-tenant-id",
                "azure.activedirectory.client-id=fake-client-id",
                "azure.activedirectory.client-secret=fake-client-secret",
                "azure.activedirectory.graph-membership-uri=https://microsoftgraph.chinacloudapi.cn/v1.0/me/memberOf")
            .run(context -> {
                AADAuthenticationProperties properties = context.getBean(AADAuthenticationProperties.class);
                assertNotNull(properties);
            });
    }

    @Test(expected = IllegalStateException.class)
    public void multiTenantWithAllowedGroupsConfiguredTest1() {
        new WebApplicationContextRunner()
            .withUserConfiguration(AADConfiguration.class)
            .withPropertyValues(
                "azure.activedirectory.tenant-id=",
                "azure.activedirectory.client-id=fake-client-id",
                "azure.activedirectory.client-secret=fake-client-secret",
                "azure.activedirectory.user-group.allowed-groups=group1,group2")
            .run(context -> {
                AADAuthenticationProperties properties = context.getBean(AADAuthenticationProperties.class);
                assertNotNull(properties);
            });
    }

    @Test(expected = IllegalStateException.class)
    public void multiTenantWithAllowedGroupsConfiguredTest2() {
        new WebApplicationContextRunner()
            .withUserConfiguration(AADConfiguration.class)
            .withPropertyValues(
                "azure.activedirectory.tenant-id=common",
                "azure.activedirectory.client-id=fake-client-id",
                "azure.activedirectory.client-secret=fake-client-secret",
                "azure.activedirectory.user-group.allowed-groups=group1,group2")
            .run(context -> {
                AADAuthenticationProperties properties = context.getBean(AADAuthenticationProperties.class);
                assertNotNull(properties);
            });
    }

    @Test(expected = IllegalStateException.class)
    public void multiTenantWithAllowedGroupsConfiguredTest3() {
        new WebApplicationContextRunner()
            .withUserConfiguration(AADConfiguration.class)
            .withPropertyValues(
                "azure.activedirectory.tenant-id=organizations",
                "azure.activedirectory.client-id=fake-client-id",
                "azure.activedirectory.client-secret=fake-client-secret",
                "azure.activedirectory.user-group.allowed-groups=group1,group2")
            .run(context -> {
                AADAuthenticationProperties properties = context.getBean(AADAuthenticationProperties.class);
                assertNotNull(properties);
            });
    }

    @Test(expected = IllegalStateException.class)
    public void multiTenantWithAllowedGroupsConfiguredTest4() {
        new WebApplicationContextRunner()
            .withUserConfiguration(AADConfiguration.class)
            .withPropertyValues(
                "azure.activedirectory.tenant-id=consumers",
                "azure.activedirectory.client-id=fake-client-id",
                "azure.activedirectory.client-secret=fake-client-secret",
                "azure.activedirectory.user-group.allowed-groups=group1,group2")
            .run(context -> {
                AADAuthenticationProperties properties = context.getBean(AADAuthenticationProperties.class);
                assertNotNull(properties);
            });
    }

}
