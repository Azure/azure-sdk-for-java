// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.actuator.implementation.appconfiguration;

import com.azure.core.exception.ResourceNotFoundException;
import com.azure.data.appconfiguration.ConfigurationAsyncClient;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;

import java.time.Duration;

import static com.azure.spring.cloud.actuator.implementation.util.ActuateConstants.DEFAULT_HEALTH_CHECK_TIMEOUT;

/**
 * Indicator class of App Configuration
 */
public class AppConfigurationHealthIndicator extends AbstractHealthIndicator {

    private Duration timeout = DEFAULT_HEALTH_CHECK_TIMEOUT;
    private final ConfigurationAsyncClient configurationAsyncClient;

    /**
     * Creates a new instance of {@link AppConfigurationHealthIndicator}.
     * @param configurationAsyncClient the configuration client
     */
    public AppConfigurationHealthIndicator(ConfigurationAsyncClient configurationAsyncClient) {
        this.configurationAsyncClient = configurationAsyncClient;
    }

    @Override
    protected void doHealthCheck(Health.Builder builder) {
        try {
            this.configurationAsyncClient.getConfigurationSetting("spring-cloud-azure-not-existing-setting", null)
                .block(timeout);
            builder.up();
        } catch (Exception e) {
            if (e instanceof ResourceNotFoundException) {
                builder.up();
            } else {
                throw e;
            }
        }
    }

    /**
     * Set health check request timeout.
     * @param timeout the duration value.
     */
    public void setTimeout(Duration timeout) {
        this.timeout = timeout;
    }
}
