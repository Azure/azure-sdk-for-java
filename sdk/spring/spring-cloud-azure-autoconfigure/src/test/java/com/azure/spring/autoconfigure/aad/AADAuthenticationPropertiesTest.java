// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.autoconfigure.aad;

import com.azure.spring.aad.AADApplicationType;
import org.junit.jupiter.api.Test;

import static com.azure.spring.aad.WebApplicationContextRunnerUtils.resourceServerContextRunner;
import static com.azure.spring.aad.WebApplicationContextRunnerUtils.resourceServerWithOboContextRunner;
import static com.azure.spring.aad.WebApplicationContextRunnerUtils.webApplicationContextRunner;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class AADAuthenticationPropertiesTest {

    @Test
    public void webAppWithOboWithExceptionTest() {
        webApplicationContextRunner()
            .withPropertyValues(
                "azure.activedirectory.authorization-clients.graph.authorizationGrantType = on_behalf_of")
            .run(context ->
                assertThrows(IllegalStateException.class, () -> context.getBean(AADAuthenticationProperties.class)));
    }

    @Test
    public void applicationTypeOfWebApplication() {
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
    public void applicationTypeWithResourceServer() {
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
    public void applicationTypeOfResourceServerWithOBO() {
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
    public void applicationTypeWithWebApplicationAndResourceServer() {
        resourceServerWithOboContextRunner()
            .withPropertyValues("azure.activedirectory.application-type=web_application_and_resource_server")
            .run(context -> {
                AADAuthenticationProperties properties = context.getBean(AADAuthenticationProperties.class);
                assertEquals(properties.getApplicationType(), AADApplicationType.WEB_APPLICATION_AND_RESOURCE_SERVER);
            });
    }

    @Test
    public void testInvalidApplicationType() {
        resourceServerContextRunner()
            .withPropertyValues("azure.activedirectory.application-type=web_application")
            .run(context -> assertThrows(IllegalStateException.class, () -> context.getBean(AADAuthenticationProperties.class)));

        webApplicationContextRunner()
            .withPropertyValues("azure.activedirectory.application-type=resource_server")
            .run(context -> assertThrows(IllegalStateException.class, () -> context.getBean(AADAuthenticationProperties.class)));
    }

    @Test
    public void invalidAuthorizationCodeWhenOnDemandIsFalse() {
        webApplicationContextRunner()
            .withPropertyValues(
                "azure.activedirectory.authorization-clients.graph.scopes = Graph.Scope",
                "azure.activedirectory.authorization-clients.graph.on-demand = true",
                "azure.activedirectory.authorization-clients.graph.authorizationGrantType = azure_delegated"
            )
            .run(context -> assertThrows(IllegalStateException.class, () -> context.getBean(AADAuthenticationProperties.class)));
    }
}
