// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.actuator.implementation.appconfiguration;

import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health.Builder;

import com.azure.spring.cloud.appconfiguration.config.AppConfigurationRefresh;
import com.azure.spring.cloud.appconfiguration.config.AppConfigurationStoreHealth;

/**
 * Indicator class of App Configuration
 */
public class AppConfigurationConfigHealthIndicator extends AbstractHealthIndicator {

    private final AppConfigurationRefresh refresh;

    /**
     * Indicator for the Health endpoint for connections to App Configurations.
     * @param refresh App Configuration store refresher
     */
    public AppConfigurationConfigHealthIndicator(AppConfigurationRefresh refresh) {
        this.refresh = refresh;
    }

    @Override
    protected void doHealthCheck(Builder builder) throws Exception {
        boolean healthy = true;

        for (String store : refresh.getAppConfigurationStoresHealth().keySet()) {
            if (AppConfigurationStoreHealth.DOWN.equals(refresh.getAppConfigurationStoresHealth().get(store))) {
                healthy = false;
                builder.withDetail(store, "DOWN");
            } else if (AppConfigurationStoreHealth.NOT_LOADED
                .equals(refresh.getAppConfigurationStoresHealth().get(store))) {
                builder.withDetail(store, "NOT LOADED");
            } else {
                builder.withDetail(store, "UP");
            }
        }
        if (!healthy) {
            builder.down();
        }
        builder.up();
    }
}
