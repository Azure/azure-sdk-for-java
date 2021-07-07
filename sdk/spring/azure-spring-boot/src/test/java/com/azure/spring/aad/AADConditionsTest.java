// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.aad;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.assertj.AssertableApplicationContext;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.test.context.runner.ContextConsumer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.server.resource.BearerTokenAuthenticationToken;

import static org.assertj.core.api.Assertions.assertThat;

public class AADConditionsTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner();

    @Test
    void testWebAppConditionWhenEnableWebAppClientMode() {
        this.contextRunner
            .withPropertyValues("azure.activedirectory.enable-web-app-and-resource-server=false")
            .withClassLoader(new FilteredClassLoader(BearerTokenAuthenticationToken.class))
            .withUserConfiguration(WebAppConditionConfig.class).run(match(true));
    }

    @Test
    void testWebAppConditionWhenEnableAllInClientMode() {
        this.contextRunner
            .withPropertyValues("azure.activedirectory.enable-web-app-and-resource-server=true")
            .withUserConfiguration(WebAppConditionConfig.class).run(match(true));
    }


    @Test
    void testWebApiConditionWhenEnableWebApiClientMode() {
        this.contextRunner
            .withPropertyValues("azure.activedirectory.enable-web-app-and-resource-server=false")
            .withUserConfiguration(WebApiConditionConfig.class).run(match(true));
    }

    @Test
    void testWebApiConditionWhenEnableAllInClientMode() {
        this.contextRunner
            .withPropertyValues("azure.activedirectory.enable-web-app-and-resource-server=true")
            .withUserConfiguration(WebApiConditionConfig.class).run(match(true));
    }

    @Test
    void testClientConditionWhenEnableWebAppClientMode() {
        this.contextRunner
            .withPropertyValues("azure.activedirectory.enable-web-app-and-resource-server=false")
            .withClassLoader(new FilteredClassLoader(BearerTokenAuthenticationToken.class))
            .withUserConfiguration(ClientRegistrationConditionConfig.class).run(match(true));
    }

    @Test
    void testClientConditionWhenEnableWebApiClientMode() {
        this.contextRunner
            .withPropertyValues("azure.activedirectory.enable-web-app-and-resource-server=false")
            .withUserConfiguration(ClientRegistrationConditionConfig.class).run(match(true));
    }

    @Test
    void testClientConditionWhenEnableAllInClientMode() {
        this.contextRunner
            .withPropertyValues("azure.activedirectory.enable-web-app-and-resource-server=true")
            .withUserConfiguration(ClientRegistrationConditionConfig.class).run(match(true));
    }

    @Configuration(proxyBeanMethods = false)
    @Conditional(AADConditions.WebAppCondition.class)
    static class WebAppConditionConfig extends Config { }

    @Configuration(proxyBeanMethods = false)
    @Conditional(AADConditions.WebApiCondition.class)
    static class WebApiConditionConfig extends Config { }

    @Configuration(proxyBeanMethods = false)
    @Conditional(AADConditions.ClientRegistrationCondition.class)
    static class ClientRegistrationConditionConfig extends Config { }

    static class Config {

        @Bean
        String myBean() {
            return "myBean";
        }
    }

    private ContextConsumer<AssertableApplicationContext> match(boolean expected) {
        return (context) -> {
            if (expected) {
                assertThat(context).hasBean("myBean");
            } else {
                assertThat(context).doesNotHaveBean("myBean");
            }
        };
    }
}
