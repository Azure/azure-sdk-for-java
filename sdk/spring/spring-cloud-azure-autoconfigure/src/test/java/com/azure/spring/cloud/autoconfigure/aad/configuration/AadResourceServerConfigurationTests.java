// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.aad.configuration;

import com.azure.spring.cloud.autoconfigure.aad.AadAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.aad.AadResourceServerWebSecurityConfigurerAdapter;
import com.azure.spring.cloud.autoconfigure.context.AzureGlobalPropertiesAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.http.HttpMessageConvertersAutoConfiguration;
import org.springframework.boot.autoconfigure.logging.ConditionEvaluationReportLoggingListener;
import org.springframework.boot.autoconfigure.web.client.RestTemplateAutoConfiguration;
import org.springframework.boot.logging.LogLevel;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.resource.OAuth2ResourceServerConfigurer;
import org.springframework.security.web.SecurityFilterChain;

import static com.azure.spring.cloud.autoconfigure.aad.implementation.WebApplicationContextRunnerUtils.resourceServerRunner;
import static org.assertj.core.api.Assertions.assertThat;

class AadResourceServerConfigurationTests {
    @Test
    void useDefaultWebSecurityConfigurerAdapter() {
        resourceServerRunner()
            .withPropertyValues("spring.cloud.azure.active-directory.enabled=true",
                "spring.cloud.azure.active-directory.credential.client-id=fake-client-id"
            )
            .run(context -> {
                assertThat(context).hasSingleBean(AadResourceServerWebSecurityConfigurerAdapter.class);
                assertThat(context).hasSingleBean(AadResourceServerConfiguration.DefaultAadResourceServerWebSecurityConfigurerAdapter.class);
            });
    }

    @Test
    void useCustomWebSecurityConfigurerAdapter() {
        resourceServerRunner()
            .withUserConfiguration(TestAadResourceServerWebSecurityConfigurerAdapter.class)
            .withPropertyValues("spring.cloud.azure.active-directory.enabled=true",
                "spring.cloud.azure.active-directory.credential.client-id=fake-client-id"
            )
            .run(context -> {
                assertThat(context).hasSingleBean(WebSecurityConfigurerAdapter.class);
                assertThat(context).doesNotHaveBean(AadResourceServerConfiguration.DefaultAadResourceServerWebSecurityConfigurerAdapter.class);
                assertThat(context).hasSingleBean(TestAadResourceServerWebSecurityConfigurerAdapter.class);
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
            .withInitializer(new ConditionEvaluationReportLoggingListener(LogLevel.INFO))
            .withPropertyValues("spring.cloud.azure.active-directory.enabled=true",
                "spring.cloud.azure.active-directory.credential.client-id=fake-client-id"
            )
            .run(context -> {
                assertThat(context).hasSingleBean(SecurityFilterChain.class);
                assertThat(context).doesNotHaveBean(WebSecurityConfigurerAdapter.class);
                assertThat(context).doesNotHaveBean(AadResourceServerConfiguration.DefaultAadResourceServerWebSecurityConfigurerAdapter.class);
                assertThat(context).hasBean("testSecurityFilterChain");

            });
    }

    @EnableWebSecurity
    static class TestAadResourceServerWebSecurityConfigurerAdapter extends
        AadResourceServerWebSecurityConfigurerAdapter {

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            super.configure(http);
        }
    }

    @EnableWebSecurity
    static class TestSecurityFilterChain {

        @Bean
        public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
            return http.oauth2ResourceServer(OAuth2ResourceServerConfigurer::jwt).build();
        }
    }
}
