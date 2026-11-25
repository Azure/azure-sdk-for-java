// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.aad.configuration;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class AadDefaultWebSecurityConditionTests {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withUserConfiguration(DefaultSecurityConfiguration.class);

    @Test
    void matchesWhenSecurityClassesArePresentAndNoFilterChainExists() {
        contextRunner.run(context -> {
            assertThat(context).hasNotFailed();
            assertThat(context).hasBean("defaultSecurity");
        });
    }

    @Test
    void backsOffWhenUserFilterChainExists() {
        assertConditionDoesNotMatch(contextRunner.withBean(SecurityFilterChain.class,
            () -> mock(SecurityFilterChain.class)));
    }

    @Test
    void backsOffWhenParentFilterChainExists() {
        try (AnnotationConfigApplicationContext parent = new AnnotationConfigApplicationContext()) {
            parent.registerBean(SecurityFilterChain.class, () -> mock(SecurityFilterChain.class));
            parent.refresh();
            assertConditionDoesNotMatch(contextRunner.withParent(parent));
        }
    }

    @Test
    void backsOffWhenHttpSecurityIsMissing() {
        assertConditionDoesNotMatch(contextRunner.withClassLoader(new FilteredClassLoader(HttpSecurity.class)));
    }

    @Test
    void backsOffWhenSecurityFilterChainIsMissing() {
        assertConditionDoesNotMatch(contextRunner.withClassLoader(new FilteredClassLoader(SecurityFilterChain.class)));
    }

    private void assertConditionDoesNotMatch(ApplicationContextRunner runner) {
        runner.run(context -> {
            assertThat(context).hasNotFailed();
            assertThat(context).doesNotHaveBean("defaultSecurity");
        });
    }

    @Configuration(proxyBeanMethods = false)
    @Conditional(AadDefaultWebSecurityCondition.class)
    static class DefaultSecurityConfiguration {

        @Bean
        String defaultSecurity() {
            return "default";
        }
    }
}
