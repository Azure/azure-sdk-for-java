// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.autoconfigure.condition.aad;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.server.resource.BearerTokenAuthenticationToken;

public class ResourceServerConditionTest extends AbstractCondition {

    @Test
    void testWebAppConditionWhenEnableWebAppClientMode() {
        this.contextRunner
            .withPropertyValues(
                "azure.activedirectory.client-id = fake-client-id")
            .withClassLoader(new FilteredClassLoader(BearerTokenAuthenticationToken.class))
            .withUserConfiguration(WebAppConditionConfig.class).run(match(true));
    }

    @Test
    void testWebAppConditionWhenEnableAllInClientMode() {
        this.contextRunner
            .withPropertyValues(
                "azure.activedirectory.client-id = fake-client-id",
                "azure.activedirectory.application-type=web_application_and_resource_server")
            .withUserConfiguration(WebAppConditionConfig.class).run(match(true));
    }

    @Configuration(proxyBeanMethods = false)
    @Conditional(WebApplicationCondition.class)
    static class WebAppConditionConfig extends Config { }
}
