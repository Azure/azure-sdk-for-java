// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.aad.configuration;

import com.azure.spring.cloud.autoconfigure.aad.AadAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.aad.AadWebSecurityConfigurerAdapter;
import com.azure.spring.cloud.autoconfigure.context.AzureGlobalPropertiesAutoConfiguration;
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
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.SecurityFilterChain;

import static com.azure.spring.cloud.autoconfigure.aad.implementation.WebApplicationContextRunnerUtils.webApplicationContextRunner;
import static org.assertj.core.api.Assertions.assertThat;

class AadWebApplicationConfigurationTests {
    @Test
    void useDefaultWebSecurityConfigurerAdapter() {
        webApplicationContextRunner()
            .withPropertyValues("spring.cloud.azure.active-directory.enabled=true",
                "spring.cloud.azure.active-directory.credential.client-id=fake-client-id"
            )
            .run(context -> {
                assertThat(context).hasSingleBean(AadWebSecurityConfigurerAdapter.class);
                assertThat(context).hasSingleBean(AadWebApplicationConfiguration.DefaultAadWebSecurityConfigurerAdapter.class);
            });
    }

    @Test
    void useCustomWebSecurityConfigurerAdapter() {
        webApplicationContextRunner()
            .withUserConfiguration(TestAadWebSecurityConfigurerAdapter.class)
            .withPropertyValues("spring.cloud.azure.active-directory.enabled=true",
                "spring.cloud.azure.active-directory.credential.client-id=fake-client-id"
            )
            .run(context -> {
                assertThat(context).hasSingleBean(WebSecurityConfigurerAdapter.class);
                assertThat(context).doesNotHaveBean(AadWebApplicationConfiguration.DefaultAadWebSecurityConfigurerAdapter.class);
                assertThat(context).hasSingleBean(TestAadWebSecurityConfigurerAdapter.class);
            });
    }

    @Test
    void useCustomSecurityFilterChain() {
        new WebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                HttpMessageConvertersAutoConfiguration.class,
                RestTemplateAutoConfiguration.class))
            .withUserConfiguration(AzureGlobalPropertiesAutoConfiguration.class,
                AadResourceServerConfigurationTests.TestSecurityFilterChain.class,
                AadAutoConfiguration.class)
            .withInitializer(new ConditionEvaluationReportLoggingListener(LogLevel.INFO))
            .withPropertyValues("spring.cloud.azure.active-directory.enabled=true",
                "spring.cloud.azure.active-directory.credential.client-id=fake-client-id"
            )
            .run(context -> {
                assertThat(context).hasSingleBean(SecurityFilterChain.class);
                assertThat(context).doesNotHaveBean(WebSecurityConfigurerAdapter.class);
                assertThat(context).doesNotHaveBean(AadWebApplicationConfiguration.DefaultAadWebSecurityConfigurerAdapter.class);
                assertThat(context).hasBean("testSecurityFilterChain");
            });
    }

    @EnableWebSecurity
    static class TestAadWebSecurityConfigurerAdapter extends
        AadWebSecurityConfigurerAdapter {

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            super.configure(http);
        }
    }

    @EnableWebSecurity
    static class TestSecurityFilterChain {

        @Bean
        public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
            return http.oauth2Login(Customizer.withDefaults()).authorizeRequests(request -> request.anyRequest().authenticated()).build();
        }
    }
}
