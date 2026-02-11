// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.appconfiguration.config.implementation.properties;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import jakarta.annotation.PostConstruct;

/**
 * Properties for all Azure App Configuration stores that are loaded.
 */
@Configuration
@ConfigurationProperties(prefix = AppConfigurationProperties.CONFIG_PREFIX)
public class AppConfigurationProperties {

    /**
     * Configuration property prefix for Azure App Configuration client settings.
     */
    public static final String CONFIG_PREFIX = "spring.cloud.azure.appconfiguration";

    private boolean enabled = true;

    /**
     * Azure App Configuration store connections. At least one store must be configured.
     */
    private List<ConfigStore> stores = new ArrayList<>();

    private Duration refreshInterval;

    /**
     * Returns whether Azure App Configuration is enabled.
     *
     * @return {@code true} if enabled, {@code false} otherwise
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Sets whether Azure App Configuration is enabled.
     *
     * @param enabled {@code true} to enable, {@code false} to disable
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Returns the list of configured App Configuration stores.
     *
     * @return the list of {@link ConfigStore} instances
     */
    public List<ConfigStore> getStores() {
        return stores;
    }

    /**
     * Sets the list of App Configuration stores to connect to.
     *
     * @param stores the list of {@link ConfigStore} instances
     */
    public void setStores(List<ConfigStore> stores) {
        this.stores = stores;
    }

    /**
     * Returns the interval between configuration refreshes.
     *
     * @return the refresh interval, or {@code null} if not set
     */
    public Duration getRefreshInterval() {
        return refreshInterval;
    }

    /**
     * Sets the interval between configuration refreshes. Must be at least 1 second.
     *
     * @param refreshInterval the refresh interval duration
     */
    public void setRefreshInterval(Duration refreshInterval) {
        this.refreshInterval = refreshInterval;
    }

    /**
     * Validates that at least one store is configured with a valid endpoint or
     * connection string, and that no duplicate endpoints exist.
     *
     * @throws IllegalArgumentException if validation fails or duplicate endpoints are found
     */
    @PostConstruct
    public void validateAndInit() {
        Assert.notEmpty(this.stores, "At least one config store has to be configured.");

        this.stores.forEach(store -> {
            Assert.isTrue(
                StringUtils.hasText(store.getEndpoint()) || StringUtils.hasText(store.getConnectionString())
                    || store.getEndpoints().size() > 0 || store.getConnectionStrings().size() > 0,
                "Either configuration store name or connection string should be configured.");
            store.validateAndInit();
        });

        Map<String, Boolean> existingEndpoints = new HashMap<>();

        for (ConfigStore store : this.stores) {

            if (store.getEndpoints().size() > 0) {
                for (String endpoint : store.getEndpoints()) {
                    if (existingEndpoints.containsKey(endpoint)) {
                        throw new IllegalArgumentException("Duplicate store name exists.");
                    }
                    existingEndpoints.put(endpoint, true);
                }
            } else if (StringUtils.hasText(store.getEndpoint())) {
                if (existingEndpoints.containsKey(store.getEndpoint())) {
                    throw new IllegalArgumentException("Duplicate store name exists.");
                }
                existingEndpoints.put(store.getEndpoint(), true);
            }
        }
        if (refreshInterval != null) {
            Assert.isTrue(refreshInterval.getSeconds() >= 1, "Minimum refresh interval time is 1 Second.");
        }
    }
}
