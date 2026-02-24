// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.appconfiguration.config.implementation.properties;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.util.StringUtils;

import com.azure.spring.cloud.appconfiguration.config.implementation.AppConfigurationReplicaClientsBuilder;

import jakarta.annotation.PostConstruct;

/**
 * Connection and behavior properties for an Azure App Configuration store.
 */
public final class ConfigStore {

    /**
     * Primary endpoint URL for the App Configuration store.
     */
    private String endpoint = ""; // Config store endpoint

    /**
     * Endpoint URLs for geo-replicated store instances. Requests fail over to the
     * next endpoint in the list when the current one is unreachable.
     */
    private List<String> endpoints = new ArrayList<>();

    /**
     * Primary connection string for the App Configuration store.
     */
    private String connectionString;

    /**
     * Connection strings for geo-replicated store instances. Requests fail over to
     * the next connection string in the list when the current one is unreachable.
     */
    private List<String> connectionStrings = new ArrayList<>();

    /**
     * Key/label selectors that determine which configuration settings to load.
     * Defaults to a single selector matching the {@code /application/} prefix
     * with no label.
     */
    private List<AppConfigurationKeyValueSelector> selects = new ArrayList<>();

    private FeatureFlagStore featureFlags = new FeatureFlagStore();

    /**
     * Enables or disables this config store. When disabled, no settings are
     * loaded from it.
     */
    private boolean enabled = true;

    /**
     * Monitoring configuration for detecting configuration changes.
     */
    private AppConfigurationStoreMonitoring monitoring = new AppConfigurationStoreMonitoring();

    /**
     * Prefixes to strip from key names before binding to
     * {@code @ConfigurationProperties}. When set, the default {@code /application/}
     * prefix is no longer trimmed automatically.
     */
    private List<String> trimKeyPrefix;

    /**
     * Enables automatic discovery of geo-replicated store endpoints.
     */
    private boolean replicaDiscoveryEnabled = true;

    /**
     * Enables request distribution across multiple endpoints via load balancing.
     */
    private boolean loadBalancingEnabled = false;

    /**
     * Returns the primary endpoint URL.
     *
     * @return the endpoint URL
     */
    public String getEndpoint() {
        return endpoint;
    }

    /**
     * Sets the primary endpoint URL.
     *
     * @param endpoint the endpoint URL
     */
    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    /**
     * Returns the list of geo-replicated endpoint URLs.
     *
     * @return the endpoint URL list
     */
    public List<String> getEndpoints() {
        return endpoints;
    }

    /**
     * Sets the list of geo-replicated endpoint URLs.
     *
     * @param endpoints the endpoint URL list
     */
    public void setEndpoints(List<String> endpoints) {
        this.endpoints = endpoints;
    }

    /**
     * Returns the primary connection string.
     *
     * @return the connection string, or {@code null} if not set
     */
    public String getConnectionString() {
        return connectionString;
    }

    /**
     * Sets the primary connection string.
     *
     * @param connectionString the connection string
     */
    public void setConnectionString(String connectionString) {
        this.connectionString = connectionString;
    }

    /**
     * Returns the list of geo-replicated connection strings.
     *
     * @return the connection string list
     */
    public List<String> getConnectionStrings() {
        return connectionStrings;
    }

    /**
     * Sets the list of geo-replicated connection strings.
     *
     * @param connectionStrings the connection string list
     */
    public void setConnectionStrings(List<String> connectionStrings) {
        this.connectionStrings = connectionStrings;
    }

    /**
     * Returns whether this config store is enabled.
     *
     * @return {@code true} if enabled, {@code false} otherwise
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Sets whether this config store is enabled.
     *
     * @param enabled {@code true} to enable, {@code false} to disable
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Returns the key/label selectors for this store.
     *
     * @return the list of {@link AppConfigurationKeyValueSelector} instances
     */
    public List<AppConfigurationKeyValueSelector> getSelects() {
        return selects;
    }

    /**
     * Sets the key/label selectors for this store.
     *
     * @param selects the list of {@link AppConfigurationKeyValueSelector} instances
     */
    public void setSelects(List<AppConfigurationKeyValueSelector> selects) {
        this.selects = selects;
    }

    /**
     * Returns the monitoring configuration for this store.
     *
     * @return the {@link AppConfigurationStoreMonitoring} settings
     */
    public AppConfigurationStoreMonitoring getMonitoring() {
        return monitoring;
    }

    /**
     * Sets the monitoring configuration for this store.
     *
     * @param monitoring the {@link AppConfigurationStoreMonitoring} settings
     */
    public void setMonitoring(AppConfigurationStoreMonitoring monitoring) {
        this.monitoring = monitoring;
    }

    /**
     * Returns the feature flag store configuration.
     *
     * @return the {@link FeatureFlagStore} settings
     */
    public FeatureFlagStore getFeatureFlags() {
        return featureFlags;
    }

    /**
     * Sets the feature flag store configuration.
     *
     * @param featureFlags the {@link FeatureFlagStore} settings
     */
    public void setFeatureFlags(FeatureFlagStore featureFlags) {
        this.featureFlags = featureFlags;
    }

    public boolean containsEndpoint(String endpoint) {
        if (this.endpoint.startsWith(endpoint)) {
            return true;
        }
        return endpoints.stream().anyMatch(storeEndpoint -> storeEndpoint.startsWith(endpoint));
    }

    /**
     * Returns the key-name prefixes to strip before property binding.
     *
     * @return the prefix list, or {@code null} if not set
     */
    public List<String> getTrimKeyPrefix() {
        return trimKeyPrefix;
    }

    /**
     * Sets the key-name prefixes to strip before property binding.
     *
     * @param trimKeyPrefix the prefix list
     */
    public void setTrimKeyPrefix(List<String> trimKeyPrefix) {
        this.trimKeyPrefix = trimKeyPrefix;
    }

    /**
     * Returns whether automatic replica endpoint discovery is enabled.
     *
     * @return {@code true} if replica discovery is enabled
     */
    public boolean isReplicaDiscoveryEnabled() {
        return replicaDiscoveryEnabled;
    }

    /**
     * Sets whether automatic replica endpoint discovery is enabled.
     *
     * @param replicaDiscoveryEnabled {@code true} to enable replica discovery
     */
    public void setReplicaDiscoveryEnabled(boolean replicaDiscoveryEnabled) {
        this.replicaDiscoveryEnabled = replicaDiscoveryEnabled;
    }

    /**
     * Returns whether load balancing across endpoints is enabled.
     *
     * @return {@code true} if load balancing is enabled
     */
    public boolean isLoadBalancingEnabled() {
        return loadBalancingEnabled;
    }

    /**
     * Sets whether load balancing across endpoints is enabled.
     *
     * @param loadBalancingEnabled {@code true} to enable load balancing
     */
    public void setLoadBalancingEnabled(boolean loadBalancingEnabled) {
        this.loadBalancingEnabled = loadBalancingEnabled;
    }

    /**
     * Initializes default selectors, validates connection settings, extracts
     * the endpoint from connection strings, and delegates to monitoring and
     * feature-flag validation.
     *
     * @throws IllegalStateException if a connection string contains an invalid endpoint URI
     */
    @PostConstruct
    public void validateAndInit() {
        if (selects.isEmpty()) {
            selects.add(new AppConfigurationKeyValueSelector());
        }

        for (AppConfigurationKeyValueSelector selectedKeys : selects) {
            selectedKeys.validateAndInit();
        }

        if (StringUtils.hasText(connectionString)) {
            String endpoint = (AppConfigurationReplicaClientsBuilder.getEndpointFromConnectionString(connectionString));
            try {
                // new URI is used to validate the endpoint as a valid URI
                new URI(endpoint);
                this.endpoint = endpoint;
            } catch (URISyntaxException e) {
                throw new IllegalStateException("Endpoint in connection string is not a valid URI.", e);
            }
        } else if (connectionStrings.size() > 0) {
            for (String connection : connectionStrings) {

                String endpoint = (AppConfigurationReplicaClientsBuilder.getEndpointFromConnectionString(connection));
                try {
                    // new URI is used to validate the endpoint as a valid URI
                    new URI(endpoint).toURL();
                    if (!StringUtils.hasText(this.endpoint)) {
                        this.endpoint = endpoint;
                    }
                } catch (MalformedURLException | URISyntaxException | IllegalArgumentException e) {
                    throw new IllegalStateException("Endpoint in connection string is not a valid URI.", e);
                }
            }
        } else if (endpoints.size() > 0) {
            endpoint = endpoints.get(0);
        }

        monitoring.validateAndInit();
        featureFlags.validateAndInit();
    }
}
