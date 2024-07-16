// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.aad.configuration;

import com.azure.spring.cloud.autoconfigure.implementation.context.AzureGlobalPropertiesAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.http.HttpMessageConvertersAutoConfiguration;
import org.springframework.boot.autoconfigure.logging.ConditionEvaluationReportLoggingListener;
import org.springframework.boot.autoconfigure.web.client.RestTemplateAutoConfiguration;
import org.springframework.boot.logging.LogLevel;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

import static com.azure.spring.cloud.autoconfigure.implementation.aad.WebApplicationContextRunnerUtils.webApplicationContextRunner;
import static org.assertj.core.api.Assertions.assertThat;

class AadWebApplicationConfigurationTests {
    @Test
    void useDefaultSecurityFilterChain() {
        webApplicationContextRunner()
            .withPropertyValues("spring.cloud.azure.active-directory.enabled=true",
                "spring.cloud.azure.active-directory.credential.client-id=fake-client-id"
            )
            .run(context -> {
                assertThat(context).hasSingleBean(SecurityFilterChain.class);
                assertThat(context).hasBean("defaultAadWebApplicationFilterChain");
            });
    }

    @Test
    void useCustomSecurityFilterChain() {
        new WebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                HttpMessageConvertersAutoConfiguration.class,
                RestTemplateAutoConfiguration.class))
            .withUserConfiguration(AzureGlobalPropertiesAutoConfiguration.class,
                TestSecurityFilterChain.class,
                AadAutoConfiguration.class)
            .withInitializer(ConditionEvaluationReportLoggingListener.forLogLevel(LogLevel.INFO))
            .withPropertyValues("spring.cloud.azure.active-directory.enabled=true",
                "spring.cloud.azure.active-directory.credential.client-id=fake-client-id"
            )
            .run(context -> {
                assertThat(context).hasSingleBean(SecurityFilterChain.class);
                assertThat(context).hasBean("testSecurityFilterChain");
            });
    }

    @EnableWebSecurity
    static class TestSecurityFilterChain {

        @SuppressWarnings({"deprecation", "removal"})
        @Bean
        public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
            return http.oauth2Login(Customizer.withDefaults()).authorizeRequests(request -> request.anyRequest().authenticated()).build();
        }
    }
}
