// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.appconfiguration.config.implementation.configuration;

import java.util.List;

import com.azure.data.appconfiguration.models.ConfigurationSetting;
import com.azure.data.appconfiguration.models.SettingSelector;

public class WatchedConfigurationSettings {

    private final SettingSelector settingSelector;

    private final List<ConfigurationSetting> configurationSettings;

    public WatchedConfigurationSettings(SettingSelector settingSelector,
        List<ConfigurationSetting> configurationSettings) {
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
     * @return the configurationSettings
     */
    public List<ConfigurationSetting> getConfigurationSettings() {
        return configurationSettings;
    }

}
