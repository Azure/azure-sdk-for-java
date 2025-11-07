// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.appconfiguration.config.implementation;

import java.time.Duration;
import java.util.List;

import org.springframework.boot.context.config.ConfigDataResource;
import org.springframework.boot.context.config.Profiles;

import com.azure.spring.cloud.appconfiguration.config.implementation.properties.AppConfigurationKeyValueSelector;
import com.azure.spring.cloud.appconfiguration.config.implementation.properties.AppConfigurationStoreMonitoring;
import com.azure.spring.cloud.appconfiguration.config.implementation.properties.ConfigStore;
import com.azure.spring.cloud.appconfiguration.config.implementation.properties.FeatureFlagKeyValueSelector;

/**
 * Represents an Azure App Configuration data resource that extends Spring Boot's ConfigDataResource.
 * 
 * @since 6.0.0
 */
public class AzureAppConfigDataResource extends ConfigDataResource {

    /** Indicates whether the configuration store is enabled for loading configuration data. */
    private final boolean configStoreEnabled;

    /** The endpoint URL of the Azure App Configuration store. */
    private final String endpoint;

    /** List of key prefixes to trim from configuration keys when loading. */
    private final List<String> trimKeyPrefix;

    /** Spring Boot profiles configuration for conditional property loading. */
    private final Profiles profiles;

    /** List of selectors for filtering key-value pairs from the configuration store. */
    private final List<AppConfigurationKeyValueSelector> selects;

    /** List of selectors for filtering feature flag key-value pairs from the configuration store. */
    private final List<FeatureFlagKeyValueSelector> featureFlagSelects;

    /** Monitoring configuration for the configuration store including refresh triggers. */
    private final AppConfigurationStoreMonitoring monitoring;

    /** Indicates whether this resource supports configuration refresh at runtime. */
    private final boolean isRefresh;

    /** The interval at which configuration should be refreshed from the store. */
    private final Duration refreshInterval;

    /**
     * Constructs a new AzureAppConfigDataResource with the specified configuration store settings.
     * 
     * @param configStore the configuration store settings containing endpoint, selectors, and other options
     * @param profiles the Spring Boot profiles for conditional configuration loading
     * @param startup boolean for if this is startup or refresh
     * @param refreshInterval the interval at which configuration should be refreshed
     */
    AzureAppConfigDataResource(boolean appConfigEnabled, ConfigStore configStore, Profiles profiles, boolean startup,
        Duration refreshInterval) {
        this.configStoreEnabled = appConfigEnabled && configStore.isEnabled();
        this.endpoint = configStore.getEndpoint();
        this.selects = configStore.getSelects();
        this.featureFlagSelects = configStore.getFeatureFlags().getSelects();
        this.trimKeyPrefix = configStore.getTrimKeyPrefix();
        this.monitoring = configStore.getMonitoring();
        this.profiles = profiles;
        this.isRefresh = !startup;
        this.refreshInterval = refreshInterval;
    }

    /**
     * Gets the list of key-value selectors used to filter configuration data from the store.
     * 
     * @return the list of configuration key-value selectors, may be null or empty
     */
    public List<AppConfigurationKeyValueSelector> getSelects() {
        return selects;
    }

    /**
     * Gets the list of feature flag selectors used to filter feature flag data from the store.
     * 
     * @return the list of feature flag selectors, may be null or empty
     */
    public List<FeatureFlagKeyValueSelector> getFeatureFlagSelects() {
        return featureFlagSelects;
    }

    /**
     * Checks whether the configuration store is enabled for loading configuration data.
     * 
     * @return true if the configuration store is enabled, false otherwise
     */
    public boolean isConfigStoreEnabled() {
        return configStoreEnabled;
    }

    /**
     * Gets the endpoint URL of the Azure App Configuration store.
     * 
     * @return the endpoint URL as a string, may be null if not configured
     */
    public String getEndpoint() {
        return endpoint;
    }

    /**
     * Gets the monitoring configuration for this configuration store.
     * 
     * @return the monitoring configuration, may be null if not configured
     */
    public AppConfigurationStoreMonitoring getMonitoring() {
        return monitoring;
    }

    /**
     * Gets the list of key prefixes to trim from configuration keys when loading.
     * 
     * @return the list of key prefixes to trim, may be null or empty if no trimming is configured
     */
    public List<String> getTrimKeyPrefix() {
        return trimKeyPrefix;
    }

    /**
     * Gets the Spring Boot profiles configuration for conditional property loading.
     * 
     * @return the profiles configuration, never null
     */
    public Profiles getProfiles() {
        return profiles;
    }

    /**
     * Returns if true if this resource is being refreshed. False if the resource is being loaded at startup.
     * 
     * @return true if this is a refresh operation, false if it is a startup load
     */
    public boolean isRefresh() {
        return isRefresh;
    }

    /**
     * Gets the interval at which configuration should be refreshed from the store.
     * 
     * @return the refresh interval, may be null if not configured
     */
    public Duration getRefreshInterval() {
        return refreshInterval;
    }
}
