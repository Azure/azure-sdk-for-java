// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.actuate.appconfiguration;

import com.azure.core.exception.ResourceNotFoundException;
import com.azure.data.appconfiguration.ConfigurationAsyncClient;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;

import java.time.Duration;

import static com.azure.spring.cloud.actuate.util.Constants.DEFAULT_TIMEOUT_SECONDS;

/**
 * Indicator class of App Configuration
 */
public class AppConfigurationHealthIndicator extends AbstractHealthIndicator {

    private int timeout = DEFAULT_TIMEOUT_SECONDS;
    private final ConfigurationAsyncClient configurationAsyncClient;

    public AppConfigurationHealthIndicator(ConfigurationAsyncClient configurationAsyncClient) {
        this.configurationAsyncClient = configurationAsyncClient;
    }

    @Override
    protected void doHealthCheck(Health.Builder builder) throws Exception {
        try {
            this.configurationAsyncClient.getConfigurationSetting("azure-spring-none-existing-setting", null)
                .block(Duration.ofSeconds(timeout));
            builder.up();
        } catch (Exception e) {
            if (e instanceof ResourceNotFoundException) {
                builder.up();
            } else {
                throw e;
            }
        }
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }
}
