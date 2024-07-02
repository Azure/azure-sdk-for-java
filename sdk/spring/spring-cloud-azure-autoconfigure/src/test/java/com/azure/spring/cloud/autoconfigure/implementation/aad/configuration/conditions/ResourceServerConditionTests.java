// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.aad.configuration.conditions;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthenticationToken;

class ResourceServerConditionTests extends AbstractCondition {

    @Test
    void testResourceServerConditionWhenApplicationTypeIsEmpty() {
        this.contextRunner
            .withPropertyValues(
                "spring.cloud.azure.active-directory.credential.client-id = fake-client-id")
            .withUserConfiguration(ResourceServerConditionConfig.class)
            .run(assertConditionMatch(true));
    }

    @Test
    void testResourceServerConditionWhenNoOAuth2ResourceDependency() {
        this.contextRunner
            .withPropertyValues("spring.cloud.azure.active-directory.credential.client-id = fake-client-id")
            .withClassLoader(new FilteredClassLoader(BearerTokenAuthenticationToken.class))
            .withUserConfiguration(ResourceServerConditionConfig.class)
            .run(assertConditionMatch(false));
    }

    @Test
    void testResourceServerConditionWhenApplicationTypeIsWebApplication() {
        this.contextRunner
            .withPropertyValues(
                "spring.cloud.azure.active-directory.credential.client-id = fake-client-id",
                "spring.cloud.azure.active-directory.application-type=web_application")
            .withUserConfiguration(ResourceServerConditionConfig.class)
            .run(assertConditionMatch(false));
    }

    @Test
    void testResourceServerConditionWhenApplicationTypeIsResourceServer() {
        this.contextRunner
            .withPropertyValues(
                "spring.cloud.azure.active-directory.credential.client-id = fake-client-id",
                "spring.cloud.azure.active-directory.application-type=resource_server")
            .withUserConfiguration(ResourceServerConditionConfig.class)
            .run(assertConditionMatch(true));
    }

    @Test
    void testResourceServerConditionWhenApplicationTypeIsResourceServerWithOBO() {
        this.contextRunner
            .withPropertyValues(
                "spring.cloud.azure.active-directory.credential.client-id = fake-client-id",
                "spring.cloud.azure.active-directory.application-type=resource_server_with_obo")
            .withUserConfiguration(ResourceServerConditionConfig.class)
            .run(assertConditionMatch(true));
    }

    @Test
    void testResourceServerConditionWhenApplicationTypeIsWebApplicationAndResourceServer() {
        this.contextRunner
            .withPropertyValues(
                "spring.cloud.azure.active-directory.credential.client-id = fake-client-id",
                "spring.cloud.azure.active-directory.application-type=web_application_and_resource_server")
            .withUserConfiguration(ResourceServerConditionConfig.class)
            .run(assertConditionMatch(true));
    }

    @Configuration
    @Conditional(ResourceServerCondition.class)
    static class ResourceServerConditionConfig extends Config { }
}
