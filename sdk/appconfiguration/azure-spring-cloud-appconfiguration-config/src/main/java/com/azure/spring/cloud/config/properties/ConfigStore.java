// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.config.properties;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.util.StringUtils;

import com.azure.spring.cloud.config.resource.Connection;

/**
 * Config Store Properties for Requests to an Azure App Configuration Store.
 */
public  final class ConfigStore {

    private static final String DEFAULT_KEYS = "/application/";

    private String endpoint; // Config store endpoint

    private String connectionString;

    // Label values separated by comma in the Azure Config Service, can be empty
    private List<AppConfigurationStoreSelects> selects = new ArrayList<>();

    private boolean failFast = true;

    private FeatureFlagStore featureFlags = new FeatureFlagStore();

    private boolean enabled = true;

    private AppConfigurationStoreMonitoring monitoring = new AppConfigurationStoreMonitoring();

    public String getEndpoint() {
        return this.endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getConnectionString() {
        return connectionString;
    }

    public void setConnectionString(String connectionString) {
        this.connectionString = connectionString;
    }

    public boolean isFailFast() {
        return failFast;
    }

    public void setFailFast(boolean failFast) {
        this.failFast = failFast;
    }

    public boolean isEnabled() {
        return enabled;
    }

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

    public FeatureFlagStore getFeatureFlags() {
        return featureFlags;
    }

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
            String endpoint = (new Connection(connectionString)).getEndpoint();
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
