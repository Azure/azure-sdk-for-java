// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.autoconfigure.condition.aad;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.server.resource.BearerTokenAuthenticationToken;

public class ClientRegistrationConditionTest extends AbstractCondition {

    @Test
    void testClientConditionWhenEnableWebAppClientMode() {
        this.contextRunner
            .withPropertyValues(
                "azure.activedirectory.client-id = fake-client-id",
                "azure.activedirectory.enable-web-app-and-resource-server=false")
            .withClassLoader(new FilteredClassLoader(BearerTokenAuthenticationToken.class))
            .withUserConfiguration(ClientRegistrationConditionConfig.class).run(match(true));
    }

    @Test
    void testClientConditionWhenEnableWebApiClientMode() {
        this.contextRunner
            .withPropertyValues(
                "azure.activedirectory.client-id = fake-client-id",
                "azure.activedirectory.enable-web-app-and-resource-server=false")
            .withUserConfiguration(ClientRegistrationConditionConfig.class).run(match(true));
    }

    @Test
    void testClientConditionWhenEnableAllInClientMode() {
        this.contextRunner
            .withPropertyValues(
                "azure.activedirectory.client-id = fake-client-id",
                "azure.activedirectory.application-type=web_application_and_resource_server")
            .withUserConfiguration(ClientRegistrationConditionConfig.class).run(match(true));
    }

    @Configuration(proxyBeanMethods = false)
    @Conditional(ClientRegistrationCondition.class)
    static class ClientRegistrationConditionConfig extends Config { }
}
