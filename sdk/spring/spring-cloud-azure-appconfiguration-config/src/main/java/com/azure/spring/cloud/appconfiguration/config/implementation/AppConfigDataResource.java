// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.appconfiguration.config.implementation;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.config.ConfigDataResource;
import org.springframework.boot.context.config.Profiles;

import com.azure.spring.cloud.appconfiguration.config.implementation.properties.AppConfigurationKeyValueSelector;
import com.azure.spring.cloud.appconfiguration.config.implementation.properties.AppConfigurationProviderProperties;
import com.azure.spring.cloud.appconfiguration.config.implementation.properties.AppConfigurationStoreMonitoring;
import com.azure.spring.cloud.appconfiguration.config.implementation.properties.ConfigStore;
import com.azure.spring.cloud.appconfiguration.config.implementation.properties.FeatureFlagKeyValueSelector;

public class AppConfigDataResource extends ConfigDataResource {

    private final boolean configStoreEnabled;

    private final boolean featureFlagsEnabled;

    private final String endpoint;

    private List<String> trimKeyPrefix;

    private final Profiles profiles;

    private List<AppConfigurationKeyValueSelector> selects = new ArrayList<>();

    private List<FeatureFlagKeyValueSelector> featureFlagSelects = new ArrayList<>();

    private AppConfigurationStoreMonitoring monitoring;

    private final AppConfigurationProviderProperties appProperties;

    public AppConfigDataResource(ConfigStore configStore, Profiles profiles,
        AppConfigurationProviderProperties appProperties) {
        this.configStoreEnabled = configStore.isEnabled();
        this.featureFlagsEnabled = configStore.getFeatureFlags().getEnabled();
        this.endpoint = configStore.getEndpoint();
        this.selects = configStore.getSelects();
        this.featureFlagSelects = configStore.getFeatureFlags().getSelects();
        this.trimKeyPrefix = configStore.getTrimKeyPrefix();
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
     * @param featureFlagSelects the featureFlagSelects to set
     */
    public void setFeatureFlagSelects(List<FeatureFlagKeyValueSelector> featureFlagSelects) {
        this.featureFlagSelects = featureFlagSelects;
    }

    /**
     * @return the configStoreEnabled
     */
    public boolean isConfigStoreEnabled() {
        return configStoreEnabled;
    }

    /**
     * @return the featureFlagsEnabled
     */
    public boolean isFeatureFlagsEnabled() {
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
     * @param monitoring the monitoring to set
     */
    public void setMonitoring(AppConfigurationStoreMonitoring monitoring) {
        this.monitoring = monitoring;
    }

    /**
     * @return the trimKeyPrefix
     */
    public List<String> getTrimKeyPrefix() {
        return trimKeyPrefix;
    }

    /**
     * @param trimKeyPrefix the trimKeyPrefix to set
     */
    public void setTrimKeyPrefix(List<String> trimKeyPrefix) {
        this.trimKeyPrefix = trimKeyPrefix;
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
