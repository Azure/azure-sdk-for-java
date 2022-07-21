// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.aad.implementation.conditions;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.server.resource.BearerTokenAuthenticationToken;

class ClientRegistrationConditionTests extends AbstractCondition {

    @Test
    void testClientConditionWhenApplicationTypeIsEmpty() {
        this.contextRunner
            .withPropertyValues(
                "spring.cloud.azure.active-directory.credential.client-id = fake-client-id")
            .withUserConfiguration(ClientRegistrationConditionConfig.class)
            .run(assertConditionMatch(true));
    }

    @Test
    void testClientConditionWhenNoOAuth2ClientDependency() {
        this.contextRunner
            .withPropertyValues("spring.cloud.azure.active-directory.credential.client-id = fake-client-id")
            .withClassLoader(new FilteredClassLoader(ClientRegistration.class))
            .withUserConfiguration(ClientRegistrationConditionConfig.class)
            .run(assertConditionMatch(false));
    }

    @Test
    void testClientConditionWhenApplicationTypeIsWebApplication() {
        this.contextRunner
            .withPropertyValues(
                "spring.cloud.azure.active-directory.credential.client-id = fake-client-id",
                "spring.cloud.azure.active-directory.application-type=web_application")
            .withClassLoader(new FilteredClassLoader(BearerTokenAuthenticationToken.class))
            .withUserConfiguration(ClientRegistrationConditionConfig.class)
            .run(assertConditionMatch(true));
    }

    @Test
    void testClientConditionWhenApplicationTypeIsResourceServer() {
        this.contextRunner
            .withPropertyValues(
                "spring.cloud.azure.active-directory.credential.client-id = fake-client-id",
                "spring.cloud.azure.active-directory.application-type=resource_server")
            .withUserConfiguration(ClientRegistrationConditionConfig.class)
            .run(assertConditionMatch(false));
    }

    @Test
    void testClientConditionWhenApplicationTypeIsResourceServerWithOBO() {
        this.contextRunner
            .withPropertyValues(
                "spring.cloud.azure.active-directory.credential.client-id = fake-client-id",
                "spring.cloud.azure.active-directory.application-type=resource_server_with_obo")
            .withUserConfiguration(ClientRegistrationConditionConfig.class)
            .run(assertConditionMatch(true));
    }

    @Test
    void testClientConditionWhenApplicationTypeIsWebApplicationAndResourceServer() {
        this.contextRunner
            .withPropertyValues(
                "spring.cloud.azure.active-directory.credential.client-id = fake-client-id",
                "spring.cloud.azure.active-directory.application-type=web_application_and_resource_server")
            .withUserConfiguration(ClientRegistrationConditionConfig.class)
            .run(assertConditionMatch(true));
    }

    @Configuration
    @Conditional(ClientRegistrationCondition.class)
    static class ClientRegistrationConditionConfig extends Config { }
}
