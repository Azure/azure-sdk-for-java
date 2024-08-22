// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.appconfiguration.config.implementation.properties;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.util.StringUtils;

import com.azure.spring.cloud.appconfiguration.config.implementation.AppConfigurationReplicaClientsBuilder;

import jakarta.annotation.PostConstruct;

/**
 * Config Store Properties for Requests to an Azure App Configuration Store.
 */
public final class ConfigStore {

    private static final String DEFAULT_KEYS = "/application/";

    /**
     * Endpoint for the Azure Config Service.
     */
    private String endpoint = ""; // Config store endpoint

    /**
     * List of endpoints for geo-replicated config store instances. When connecting
     * to Azure App Configuration, the endpoints will failover to the next endpoint
     * in the list if the current endpoint is unreachable.
     */
    private List<String> endpoints = new ArrayList<>();

    /**
     * Connection String for the Azure Config Service.
     */
    private String connectionString;

    /**
     * List of connection strings for geo-replicated config store instances. When
     * connecting to Azure App Configuration, the connection strings will failover
     * to the next connection string in the list if the current connection string is
     * unreachable.
     */
    private List<String> connectionStrings = new ArrayList<>();

    /**
     * List of key selectors to filter the keys to be retrieved from the Azure
     * Config Service. If no selectors are provided, the default selector will
     * retrieve all keys with the prefix "/application/" and no label.
     */
    private List<AppConfigurationKeyValueSelector> selects = new ArrayList<>();

    /**
     * If true, the application will fail to start if the Config Store cannot be
     * reached. If false, the application will start without the Config Store.
     */
    private boolean failFast = true;

    /**
     * Options for retrieving Feature Flags from the Azure Config Service.
     */
    private FeatureFlagStore featureFlags = new FeatureFlagStore();

    /**
     * If true, the Config Store will be enabled. If false, the Config Store will be
     * disabled and no keys will be retrieved from the Config Store.
     */
    private boolean enabled = true;

    /**
     * Options for monitoring the Config Store.
     */
    private AppConfigurationStoreMonitoring monitoring = new AppConfigurationStoreMonitoring();

    /**
     * List of values to be trimmed from key names before being set to
     * `@ConfigurationProperties`. By default the prefix "/application/" is trimmed
     * from key names. If any trimKeyPrefix values are provided, the default prefix
     * will not be trimmed.
     */
    private List<String> trimKeyPrefix;

    /**
     * If true, the Config Store will attempt to discover the replica endpoints for
     * the Config Store. If false, the Config Store will not attempt to discover the
     * replica endpoints for the Config Store.
     */
    private boolean replicaDiscoveryEnabled = true;

    /**
     * @return the endpoint
     */
    public String getEndpoint() {
        return endpoint;
    }

    /**
     * @param endpoint the endpoint to set
     */
    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    /**
     * @return list of endpoints
     */
    public List<String> getEndpoints() {
        return endpoints;
    }

    /**
     * @param endpoints list of endpoints to connect to geo-replicated config store
     *                  instances.
     */
    public void setEndpoints(List<String> endpoints) {
        this.endpoints = endpoints;
    }

    /**
     * @return the connectionString
     */
    public String getConnectionString() {
        return connectionString;
    }

    /**
     * @param connectionString the connectionString to set
     */
    public void setConnectionString(String connectionString) {
        this.connectionString = connectionString;
    }

    /**
     * @return connectionStrings
     */
    public List<String> getConnectionStrings() {
        return connectionStrings;
    }

    /**
     * @param connectionStrings the connectionStrings to set
     */
    public void setConnectionStrings(List<String> connectionStrings) {
        this.connectionStrings = connectionStrings;
    }

    /**
     * @return the failFast
     */
    public boolean isFailFast() {
        return failFast;
    }

    /**
     * @param failFast the failFast to set
     */
    public void setFailFast(boolean failFast) {
        this.failFast = failFast;
    }

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
     * @return the selects
     */
    public List<AppConfigurationKeyValueSelector> getSelects() {
        return selects;
    }

    /**
     * @param selects the selects to set
     */
    public void setSelects(List<AppConfigurationKeyValueSelector> selects) {
        this.selects = selects;
    }

    /**
     * @return the monitoring
     */
    public AppConfigurationStoreMonitoring getMonitoring() {
        return monitoring;
    }

    /**
     * @param monitoring the monitoring to set
     */
    public void setMonitoring(AppConfigurationStoreMonitoring monitoring) {
        this.monitoring = monitoring;
    }

    /**
     * @return the featureFlags
     */
    public FeatureFlagStore getFeatureFlags() {
        return featureFlags;
    }

    /**
     * @param featureFlags the featureFlags to set
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
     * @return the trimKeyPrefix
     */
    public List<String> getTrimKeyPrefix() {
        return trimKeyPrefix;
    }

    /**
     * @param trimKeyPrefix the values to be trimmed from key names before being set
     *                      to `@ConfigurationProperties`
     */
    public void setTrimKeyPrefix(List<String> trimKeyPrefix) {
        this.trimKeyPrefix = trimKeyPrefix;
    }

    /**
     * @return the replicaDiscoveryEnabled
     */
    public boolean isReplicaDiscoveryEnabled() {
        return replicaDiscoveryEnabled;
    }

    /**
     * @param replicaDiscoveryEnabled the replicaDiscoveryEnabled to set
     */
    public void setReplicaDiscoveryEnabled(boolean replicaDiscoveryEnabled) {
        this.replicaDiscoveryEnabled = replicaDiscoveryEnabled;
    }

    /**
     * @throws IllegalStateException Connection String URL endpoint is invalid
     */
    @PostConstruct
    public void validateAndInit() {
        if (selects.isEmpty()) {
            selects.add(new AppConfigurationKeyValueSelector().setKeyFilter(DEFAULT_KEYS));
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
