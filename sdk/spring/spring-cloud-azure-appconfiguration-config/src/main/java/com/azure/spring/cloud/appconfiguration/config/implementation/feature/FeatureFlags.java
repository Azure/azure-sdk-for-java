// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.appconfiguration.config.implementation.feature;

import java.util.List;

import com.azure.data.appconfiguration.models.ConfigurationSetting;
import com.azure.data.appconfiguration.models.SettingSelector;
import com.azure.spring.cloud.appconfiguration.config.implementation.properties.ConfigStore;

public class FeatureFlags {

    private SettingSelector settingSelector;

    private List<ConfigurationSetting> featureFlags;

    private ConfigStore configStore;

    public FeatureFlags(SettingSelector settingSelector, List<ConfigurationSetting> featureFlags) {
        this.settingSelector = settingSelector;
        this.featureFlags = featureFlags;
    }

    /**
     * @return the configStore
     */
    public ConfigStore getConfigStore() {
        return configStore;
    }

    /**
     * @param configStore the configStore to set
     */
    public void setConfigStore(ConfigStore configStore) {
        this.configStore = configStore;
    }

    /**
     * @return the settingSelector
     */
    public SettingSelector getSettingSelector() {
        return settingSelector;
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
