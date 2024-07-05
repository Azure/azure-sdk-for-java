// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.actuator.autoconfigure.implementation.appconfiguration;

import com.azure.spring.cloud.actuator.implementation.appconfiguration.AppConfigurationHealthIndicator;
import com.azure.spring.cloud.autoconfigure.implementation.appconfiguration.AzureAppConfigurationAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.implementation.context.properties.AzureGlobalProperties;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class AppConfigurationHealthConfigurationTests {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withPropertyValues("spring.cloud.azure.appconfiguration.endpoint=https://test-appconfig.azconfig.io")
        .withBean(AzureGlobalProperties.class)
        .withConfiguration(AutoConfigurations.of(AzureAppConfigurationAutoConfiguration.class, AppConfigurationHealthConfiguration.class));

    @Test
    void runShouldCreateIndicator() {
        this.contextRunner.run((context) -> assertThat(context).hasSingleBean(AppConfigurationHealthIndicator.class));
    }

    @Test
    void runWhenDisabledShouldNotCreateIndicator() {
        this.contextRunner.withPropertyValues("management.health.azure-appconfiguration.enabled:false")
            .run((context) -> assertThat(context).doesNotHaveBean(AppConfigurationHealthIndicator.class));
    }
}
