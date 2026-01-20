// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.appconfiguration.config.implementation.configuration;

import java.util.List;

import com.azure.data.appconfiguration.models.ConfigurationSetting;
import com.azure.data.appconfiguration.models.SettingSelector;

public class WatchedConfigurationSettings {

    private SettingSelector settingSelector;

    private List<ConfigurationSetting> configurationSettings;

    public WatchedConfigurationSettings(SettingSelector settingSelector, List<ConfigurationSetting> configurationSettings) {
        this.settingSelector = settingSelector;
        this.configurationSettings = configurationSettings;
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
     * @return the configurationSettings
     */
    public List<ConfigurationSetting> getConfigurationSettings() {
        return configurationSettings;
    }

    /**
     * @param configurations the configurations to set
     */
    public void setConfigurationSettings(List<ConfigurationSetting> configurations) {
        this.configurationSettings = configurations;
    }

}
