// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.actuate.appconfiguration;

import com.azure.core.exception.ResourceNotFoundException;
import com.azure.data.appconfiguration.ConfigurationClient;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;

/**
 * Indicator class of App Configuration
 */
public class AppConfigurationHealthIndicator extends AbstractHealthIndicator {

    private final ConfigurationClient configurationClient;

    public AppConfigurationHealthIndicator(ConfigurationClient configurationClient) {
        this.configurationClient = configurationClient;
    }

    @Override
    protected void doHealthCheck(Health.Builder builder) throws Exception {
        try {
            this.configurationClient.getConfigurationSetting("azure-spring-none-existing-setting", null);
            builder.up();
        } catch (Exception e) {
            if (e instanceof ResourceNotFoundException) {
                builder.up();
            } else {
                builder.down(e);
            }
        }
    }
}
