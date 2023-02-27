// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.actuator.appconfiguration;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;

import com.azure.spring.cloud.config.AppConfigurationRefresh;

/**
 * Indicator class of App Configuration 
 */
public final class AppConfigurationConfigHealthIndicator implements HealthIndicator {

    private final AppConfigurationRefresh refresh;

    /**
     * Indicator for the Health endpoint for connections to App Configurations.
     * @param refresh App Configuration store refresher
     */
    public AppConfigurationConfigHealthIndicator(AppConfigurationRefresh refresh) {
        this.refresh = refresh;
    }

    @Override
    public Health health() {
        Health.Builder healthBuilder = new Health.Builder();
        boolean healthy = true;

        for (String store : refresh.getAppConfigurationStoresHealth().keySet()) {
            if ("DOWN".equals(refresh.getAppConfigurationStoresHealth().get(store))) {
                healthy = false;
                healthBuilder.withDetail(store, "DOWN");
            } else if ("NOT_LOADED".equals(refresh.getAppConfigurationStoresHealth().get(store))) {
                healthBuilder.withDetail(store, "NOT LOADED");
            } else {
                healthBuilder.withDetail(store, "UP");
            }
        }

        if (!healthy) {
            return healthBuilder.down().build();
        }
        return healthBuilder.up().build();
    }

}
