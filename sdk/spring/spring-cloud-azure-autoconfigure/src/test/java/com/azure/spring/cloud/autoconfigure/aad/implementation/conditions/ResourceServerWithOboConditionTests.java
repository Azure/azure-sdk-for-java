// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.aad.implementation.conditions;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.server.resource.BearerTokenAuthenticationToken;

class ResourceServerWithOboConditionTests extends AbstractCondition {

    @Test
    void testConditionWhenApplicationTypeInferenceIsWebApplication() {
        this.contextRunner
            .withPropertyValues("spring.cloud.azure.active-directory.credential.client-id = fake-client-id")
            .withClassLoader(new FilteredClassLoader(BearerTokenAuthenticationToken.class))
            .withUserConfiguration(ResourceServerWithOBOConditionConfig.class)
            .run(assertConditionMatch(false));
    }

    @Test
    void testConditionWhenApplicationTypeIsWebApplication() {
        this.contextRunner
            .withPropertyValues(
                "spring.cloud.azure.active-directory.credential.client-id = fake-client-id",
                "spring.cloud.azure.active-directory.application-type=web_application")
            .withUserConfiguration(ResourceServerWithOBOConditionConfig.class)
            .run(assertConditionMatch(false));
    }

    @Test
    void testConditionWhenApplicationTypeIsResourceServer() {
        this.contextRunner
            .withPropertyValues(
                "spring.cloud.azure.active-directory.credential.client-id = fake-client-id",
                "spring.cloud.azure.active-directory.application-type=resource_server")
            .withUserConfiguration(ResourceServerWithOBOConditionConfig.class)
            .run(assertConditionMatch(false));
    }

    @Test
    void testConditionWhenApplicationTypeIsResourceServerWithOBO() {
        this.contextRunner
            .withPropertyValues(
                "spring.cloud.azure.active-directory.credential.client-id = fake-client-id",
                "spring.cloud.azure.active-directory.application-type=resource_server_with_obo")
            .withUserConfiguration(ResourceServerWithOBOConditionConfig.class)
            .run(assertConditionMatch(true));
    }

    @Test
    void testConditionWhenApplicationTypeIsWebApplicationAndResourceServer() {
        this.contextRunner
            .withPropertyValues(
                "spring.cloud.azure.active-directory.credential.client-id = fake-client-id",
                "spring.cloud.azure.active-directory.application-type=web_application_and_resource_server")
            .withUserConfiguration(ResourceServerWithOBOConditionConfig.class)
            .run(assertConditionMatch(false));
    }

    @Configuration
    @Conditional(ResourceServerWithOBOCondition.class)
    static class ResourceServerWithOBOConditionConfig extends Config { }
}
