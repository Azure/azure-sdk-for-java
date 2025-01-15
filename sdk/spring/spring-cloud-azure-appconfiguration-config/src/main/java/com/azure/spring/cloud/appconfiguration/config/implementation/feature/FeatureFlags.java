// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.appconfiguration.config.implementation.feature;

import java.util.List;

import com.azure.data.appconfiguration.models.ConfigurationSetting;
import com.azure.data.appconfiguration.models.SettingSelector;
import com.azure.spring.cloud.appconfiguration.config.implementation.AzureAppConfigDataResource;

public class FeatureFlags {

    private SettingSelector settingSelector;

    private List<ConfigurationSetting> featureFlags;

    private AzureAppConfigDataResource resource;

    public FeatureFlags(SettingSelector settingSelector, List<ConfigurationSetting> featureFlags) {
        this.settingSelector = settingSelector;
        this.featureFlags = featureFlags;
    }

    /**
     * @return the settingSelector
     */
    public SettingSelector getSettingSelector() {
        return settingSelector;
    }

    /**
     * @return the resource
     */
    public AzureAppConfigDataResource getResource() {
        return resource;
    }

    /**
     * @param resource the resource to set
     */
    public void setResource(AzureAppConfigDataResource resource) {
        this.resource = resource;
    }

    /**
     * @param settingSelector the settingSelector to set
     */
    public void setSettingSelector(SettingSelector settingSelector) {
        this.settingSelector = settingSelector;
    }

    /**
     * @return the featureFlags
     */
    public List<ConfigurationSetting> getFeatureFlags() {
        return featureFlags;
    }

    /**
     * @param featureFlags the featureFlags to set
     */
    public void setFeatureFlags(List<ConfigurationSetting> featureFlags) {
        this.featureFlags = featureFlags;
    }

}
