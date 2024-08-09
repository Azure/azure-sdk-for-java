// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.appconfiguration.config.implementation;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.config.ConfigDataResource;
import org.springframework.boot.context.config.Profiles;

import com.azure.spring.cloud.appconfiguration.config.implementation.properties.AppConfigurationProviderProperties;
import com.azure.spring.cloud.appconfiguration.config.implementation.properties.AppConfigurationStoreMonitoring;
import com.azure.spring.cloud.appconfiguration.config.implementation.properties.ConfigStore;
import com.azure.spring.cloud.appconfiguration.config.implementation.properties.FeatureFlagKeyValueSelector;

public class FeatureManagementDataResource extends ConfigDataResource {

    private final boolean featureFlagsEnabled;

    private final String endpoint;

    private final Profiles profiles;

    private List<FeatureFlagKeyValueSelector> featureFlagSelects = new ArrayList<>();

    private final AppConfigurationStoreMonitoring monitoring;

    private final AppConfigurationProviderProperties appProperties;

    public FeatureManagementDataResource(ConfigStore configStore, Profiles profiles,
        AppConfigurationProviderProperties appProperties) {
        this.featureFlagsEnabled = configStore.getFeatureFlags().getEnabled();
        this.endpoint = configStore.getEndpoint();
        this.featureFlagSelects = configStore.getFeatureFlags().getSelects();;
        this.monitoring = configStore.getMonitoring();
        this.profiles = profiles;
        this.appProperties = appProperties;
    }

    /**
     * @return the featureFlagSelects
     */
    public List<FeatureFlagKeyValueSelector> getFeatureFlagSelects() {
        return featureFlagSelects;
    }

    /**
     * @return the featureFlagsEnabled
     */
    public boolean isEnabled() {
        return featureFlagsEnabled;
    }

    /**
     * @return the endpoint
     */
    public String getEndpoint() {
        return endpoint;
    }

    /**
     * @return the monitoring
     */
    public AppConfigurationStoreMonitoring getMonitoring() {
        return monitoring;
    }
    
    /**
     * @return the profiles
     */
    public Profiles getProfiles() {
        return profiles;
    }

    /**
     * @return the appProperties
     */
    public AppConfigurationProviderProperties getAppProperties() {
        return appProperties;
    }

}