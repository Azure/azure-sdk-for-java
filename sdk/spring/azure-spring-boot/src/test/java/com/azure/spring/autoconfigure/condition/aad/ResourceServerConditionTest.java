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
    void testResourceServerConditionWhenApplicationTypeIsEmpty() {
        this.contextRunner
            .withPropertyValues(
                "azure.activedirectory.client-id = fake-client-id")
            .withUserConfiguration(ResourceServerConditionConfig.class)
            .run(assertConditionMatch(true));
    }

    @Test
    void testResourceServerConditionWhenNoOAuth2ResourceDependency() {
        this.contextRunner
            .withPropertyValues("azure.activedirectory.client-id = fake-client-id")
            .withClassLoader(new FilteredClassLoader(BearerTokenAuthenticationToken.class))
            .withUserConfiguration(ResourceServerConditionConfig.class)
            .run(assertConditionMatch(false));
    }

    @Test
    void testResourceServerConditionWhenApplicationTypeIsWebApplication() {
        this.contextRunner
            .withPropertyValues(
                "azure.activedirectory.client-id = fake-client-id",
                "azure.activedirectory.application-type=web_application")
            .withUserConfiguration(ResourceServerConditionConfig.class)
            .run(assertConditionMatch(false));
    }

    @Test
    void testResourceServerConditionWhenApplicationTypeIsResourceServer() {
        this.contextRunner
            .withPropertyValues(
                "azure.activedirectory.client-id = fake-client-id",
                "azure.activedirectory.application-type=resource_server")
            .withUserConfiguration(ResourceServerConditionConfig.class)
            .run(assertConditionMatch(true));
    }

    @Test
    void testResourceServerConditionWhenApplicationTypeIsResourceServerWithOBO() {
        this.contextRunner
            .withPropertyValues(
                "azure.activedirectory.client-id = fake-client-id",
                "azure.activedirectory.application-type=resource_server_with_obo")
            .withUserConfiguration(ResourceServerConditionConfig.class)
            .run(assertConditionMatch(true));
    }

    @Test
    void testResourceServerConditionWhenApplicationTypeIsWebApplicationAndResourceServer() {
        this.contextRunner
            .withPropertyValues(
                "azure.activedirectory.client-id = fake-client-id",
                "azure.activedirectory.application-type=web_application_and_resource_server")
            .withUserConfiguration(ResourceServerConditionConfig.class)
            .run(assertConditionMatch(true));
    }

    @Configuration
    @Conditional(ResourceServerCondition.class)
    static class ResourceServerConditionConfig extends Config { }
}
