// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.config.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;

import com.azure.spring.cloud.config.AppConfigurationRefresh;

// TODO (mametcal) need to figure out what to do with this
/**
 * Indicator class of App Configuration 
 */
public final class AppConfigurationHealthIndicator implements HealthIndicator {

    private final AppConfigurationRefresh refresh;

    /**
     * Indicator for the Health endpoint for connections to App Configurations.
     * @param refresh App Configuration store refresher
     */
    public AppConfigurationHealthIndicator(AppConfigurationRefresh refresh) {
        this.refresh = refresh;
    }

    @Override
    public Health health() {
        Health.Builder healthBuilder = new Health.Builder();
        boolean healthy = true;

        for (String store : refresh.getAppConfigurationStoresHealth().keySet()) {
            if (AppConfigurationStoreHealth.DOWN.equals(refresh.getAppConfigurationStoresHealth().get(store))) {
                healthy = false;
                healthBuilder.withDetail(store, "DOWN");
            } else if (refresh.getAppConfigurationStoresHealth().get(store).equals(AppConfigurationStoreHealth.NOT_LOADED)) {
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
