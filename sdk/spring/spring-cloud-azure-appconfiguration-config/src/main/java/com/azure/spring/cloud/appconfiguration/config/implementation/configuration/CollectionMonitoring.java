// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.appconfiguration.config.implementation.configuration;

import java.util.List;

import com.azure.data.appconfiguration.models.ConfigurationSetting;
import com.azure.data.appconfiguration.models.SettingSelector;

public class CollectionMonitoring {

    private SettingSelector settingSelector;

    private List<ConfigurationSetting> configurations;

    public CollectionMonitoring(SettingSelector settingSelector, List<ConfigurationSetting> configurations) {
        this.settingSelector = settingSelector;
        this.configurations = configurations;
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
    public List<ConfigurationSetting> getConfigurations() {
        return configurations;
    }

    /**
     * @param configurations the configurations to set
     */
    public void setConfigurations(List<ConfigurationSetting> configurations) {
        this.configurations = configurations;
    }

}
