// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.actuator.implementation.appconfiguration;

import com.azure.data.appconfiguration.ConfigurationAsyncClient;
import com.azure.data.appconfiguration.models.ConfigurationSetting;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AppConfigurationHealthIndicatorTests {

    @Test
    void appconfigurationIsUp() {
        ConfigurationAsyncClient mockAsyncClient = mock(ConfigurationAsyncClient.class);
        Mono<ConfigurationSetting> response = Mono.just(new ConfigurationSetting());
        when(mockAsyncClient.getConfigurationSetting("spring-cloud-azure-not-existing-setting", null))
            .thenReturn(response);
        AppConfigurationHealthIndicator indicator = new AppConfigurationHealthIndicator(mockAsyncClient);
        Health health = indicator.health();
        assertThat(health.getStatus()).isEqualTo(Status.UP);
    }

    @Test
    void appconfigurationIsDown() {
        ConfigurationAsyncClient mockAsyncClient = mock(ConfigurationAsyncClient.class);
        when(mockAsyncClient.getConfigurationSetting("spring-cloud-azure-not-existing-setting", null))
            .thenReturn(Mono.error(new IllegalStateException("The gremlins have cut the cable.")));
        AppConfigurationHealthIndicator indicator = new AppConfigurationHealthIndicator(mockAsyncClient);
        Health health = indicator.health();
        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
    }
}
