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
     * Prefix for client configurations for connecting to configuration stores.
     */
    public static final String CONFIG_PREFIX = "spring.cloud.azure.appconfiguration";

    private boolean enabled = true;

    /**
     * List of Azure App Configuration stores to connect to.
     */
    private List<ConfigStore> stores = new ArrayList<>();

    private Duration refreshInterval;

    /**
     * @return the enabled
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * @param enabled the enabled to set
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * @return the stores
     */
    public List<ConfigStore> getStores() {
        return stores;
    }

    /**
     * @param stores the stores to set
     */
    public void setStores(List<ConfigStore> stores) {
        this.stores = stores;
    }

    /**
     * @return the refreshInterval
     */
    public Duration getRefreshInterval() {
        return refreshInterval;
    }

    /**
     * @param refreshInterval the refreshInterval to set
     */
    public void setRefreshInterval(Duration refreshInterval) {
        this.refreshInterval = refreshInterval;
    }

    /**
     * Validates at least one store is configured for use, and that they are valid.
     * @throws IllegalArgumentException when duplicate endpoints are configured
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
