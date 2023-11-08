// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.aad.implementation.conditions;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.server.resource.BearerTokenAuthenticationToken;

class WebApplicationConditionTests extends AbstractCondition {

    @Test
    void testWebApplicationConditionWhenApplicationTypeIsEmpty() {
        this.contextRunner
            .withPropertyValues(
                "spring.cloud.azure.active-directory.credential.client-id = fake-client-id")
            .withUserConfiguration(AllWebApplicationConditionConfig.class)
            .run(assertConditionMatch(false));
    }

    @Test
    void testWebAppConditionWhenNoOAuth2ResourceDependency() {
        this.contextRunner
            .withPropertyValues("spring.cloud.azure.active-directory.credential.client-id = fake-client-id")
            .withClassLoader(new FilteredClassLoader(BearerTokenAuthenticationToken.class))
            .withUserConfiguration(AllWebApplicationConditionConfig.class)
            .run(assertConditionMatch(true));
    }

    @Test
    void testWebAppConditionWhenNoOAuth2ClientDependency() {
        this.contextRunner
            .withPropertyValues("spring.cloud.azure.active-directory.credential.client-id = fake-client-id")
            .withClassLoader(new FilteredClassLoader(ClientRegistration.class))
            .withUserConfiguration(AllWebApplicationConditionConfig.class)
            .run(assertConditionMatch(false));
    }

    @Test
    void testWebAppConditionWhenApplicationTypeIsWebApplication() {
        this.contextRunner
            .withPropertyValues(
                "spring.cloud.azure.active-directory.credential.client-id = fake-client-id",
                "spring.cloud.azure.active-directory.application-type=web_application")
            .withUserConfiguration(AllWebApplicationConditionConfig.class)
            .run(assertConditionMatch(true));
    }

    @Test
    void testWebAppConditionWhenApplicationTypeIsResourceServer() {
        this.contextRunner
            .withPropertyValues(
                "spring.cloud.azure.active-directory.credential.client-id = fake-client-id",
                "spring.cloud.azure.active-directory.application-type=resource_server")
            .withUserConfiguration(AllWebApplicationConditionConfig.class)
            .run(assertConditionMatch(false));
    }

    @Test
    void testWebAppConditionWhenApplicationTypeIsResourceServerWithOBO() {
        this.contextRunner
            .withPropertyValues(
                "spring.cloud.azure.active-directory.credential.client-id = fake-client-id",
                "spring.cloud.azure.active-directory.application-type=resource_server_with_obo")
            .withUserConfiguration(AllWebApplicationConditionConfig.class)
            .run(assertConditionMatch(false));
    }

    @Test
    void testWebAppConditionWhenApplicationTypeIsWebApplicationAndResourceServer() {
        this.contextRunner
            .withPropertyValues(
                "spring.cloud.azure.active-directory.credential.client-id = fake-client-id",
                "spring.cloud.azure.active-directory.application-type=web_application_and_resource_server")
            .withUserConfiguration(AllWebApplicationConditionConfig.class)
            .run(assertConditionMatch(true));
    }

    @Configuration
    @Conditional(WebApplicationCondition.class)
    static class AllWebApplicationConditionConfig extends Config { }
}
