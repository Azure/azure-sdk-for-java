// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.actuate.appconfiguration;

import com.azure.data.appconfiguration.ConfigurationAsyncClient;
import com.azure.data.appconfiguration.models.ConfigurationSetting;
import com.azure.spring.cloud.actuate.autoconfigure.appconfiguration.AppConfigurationHealthConfiguration;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;

public class AppConfigurationHealthConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withPropertyValues("spring.cloud.azure.appconfiguration.endpoint=https://moaryc-appconfig.azconfig.io")
        .withConfiguration(AutoConfigurations.of(AppConfigurationHealthConfiguration.class));

    @Test
    void configureWithNoConfigurationAsyncClient() {
        this.contextRunner.run(context -> Assertions.assertThat(context).doesNotHaveBean(AppConfigurationHealthIndicator.class));
    }

    @Test
    void configureWithConfigurationAsyncClientUp() {
        this.contextRunner
            .withUserConfiguration(AppConfigurationHealthConfigurationTest.TestConfigurationConnectionUp.class)
            .run(context -> {
                Assertions.assertThat(context).hasSingleBean(AppConfigurationHealthIndicator.class);
                final AppConfigurationHealthIndicator healthIndicator = context.getBean(AppConfigurationHealthIndicator.class);
                Health health = healthIndicator.getHealth(true);
                assertEquals(Status.UP, health.getStatus());
            });
    }

    @Test
    void configureWithConfigurationAsyncClientDown() {
        this.contextRunner
            .withUserConfiguration(AppConfigurationHealthConfigurationTest.TestConfigurationConnectionDown.class)
            .run(context -> {
                Assertions.assertThat(context).hasSingleBean(AppConfigurationHealthIndicator.class);
                final AppConfigurationHealthIndicator healthIndicator = context.getBean(AppConfigurationHealthIndicator.class);
                Health health = healthIndicator.getHealth(true);
                assertEquals(Status.DOWN, health.getStatus());
            });
    }

    @Configuration(proxyBeanMethods = false)
    static class TestConfigurationConnectionUp {

        @Bean
        ConfigurationAsyncClient configurationAsyncClient() {
            ConfigurationAsyncClient mockConfigurationAsyncClient = mock(ConfigurationAsyncClient.class);
            ConfigurationSetting mockSetting = mock(ConfigurationSetting.class);
            Mockito.when(mockConfigurationAsyncClient.getConfigurationSetting(any(String.class), isNull()))
                .thenReturn(Mono.just(mockSetting));
            return mockConfigurationAsyncClient;
        }
    }

    @Configuration(proxyBeanMethods = false)
    static class TestConfigurationConnectionDown {

        @Bean
        ConfigurationAsyncClient configurationAsyncClient() {
            ConfigurationAsyncClient mockConfigurationAsyncClient = mock(ConfigurationAsyncClient.class);
            Mockito.when(mockConfigurationAsyncClient.getConfigurationSetting(any(String.class), isNull()))
                .thenReturn(Mono.error(new IllegalArgumentException("The gremlins have cut the cable.")));
            return mockConfigurationAsyncClient;
        }
    }
}
