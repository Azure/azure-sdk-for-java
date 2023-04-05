// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.actuator.appconfiguration;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;

import com.azure.spring.cloud.appconfiguration.config.AppConfigurationRefresh;
import com.azure.spring.cloud.appconfiguration.config.AppConfigurationStoreHealth;

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
            if (AppConfigurationStoreHealth.DOWN.equals(refresh.getAppConfigurationStoresHealth().get(store))) {
                healthy = false;
                healthBuilder.withDetail(store, AppConfigurationStoreHealth.DOWN.toString());
            } else if (AppConfigurationStoreHealth.NOT_LOADED
                .equals(refresh.getAppConfigurationStoresHealth().get(store))) {
                healthBuilder.withDetail(store, AppConfigurationStoreHealth.NOT_LOADED.toString());
            } else {
                healthBuilder.withDetail(store, AppConfigurationStoreHealth.UP.toString());
            }
        }

        if (!healthy) {
            return healthBuilder.down().build();
        }
        return healthBuilder.up().build();
    }

}
