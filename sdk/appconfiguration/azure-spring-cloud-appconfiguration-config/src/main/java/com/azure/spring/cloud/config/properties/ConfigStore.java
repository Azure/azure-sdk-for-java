// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.config.properties;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.util.StringUtils;

import com.azure.spring.cloud.config.ConnectionManager;

/**
 * Config Store Properties for Requests to an Azure App Configuration Store.
 */
public final class ConfigStore {

    private static final String DEFAULT_KEYS = "/application/";

    private String endpoint; // Config store endpoint

    private List<String> endpoints = new ArrayList<>();

    private String connectionString;

    private List<String> connectionStrings = new ArrayList<>();

    // Label values separated by comma in the Azure Config Service, can be empty
    private List<AppConfigurationStoreSelects> selects = new ArrayList<>();

    private boolean failFast = true;

    private FeatureFlagStore featureFlags = new FeatureFlagStore();

    private boolean enabled = true;

    private AppConfigurationStoreMonitoring monitoring = new AppConfigurationStoreMonitoring();

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
     * @param endpoints list of endpoints to connect to geo-replicated config store instances.
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
     * @param connectionStrings list of connection strings to connect to geo-replicated config store instances.
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
    public List<AppConfigurationStoreSelects> getSelects() {
        return selects;
    }

    /**
     * @param selects the selects to set
     */
    public void setSelects(List<AppConfigurationStoreSelects> selects) {
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

    /**
     * @throws IllegalStateException Connection String URL endpoint is invalid
     */
    @PostConstruct
    public void validateAndInit() {
        if (selects.isEmpty()) {
            selects.add(new AppConfigurationStoreSelects().setKeyFilter(DEFAULT_KEYS));
        }

        for (AppConfigurationStoreSelects selectedKeys : selects) {
            selectedKeys.validateAndInit();
        }

        if (StringUtils.hasText(connectionString)) {
            String endpoint = (ConnectionManager.getEndpointFromConnectionString(connectionString));
            try {
                // new URI is used to validate the endpoint as a valid URI
                new URI(endpoint);
                this.endpoint = endpoint;
            } catch (URISyntaxException e) {
                throw new IllegalStateException("Endpoint in connection string is not a valid URI.", e);
            }
        }

        monitoring.validateAndInit();
    }
}
