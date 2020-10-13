// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.learn.appconfig;

import com.azure.learn.appconfig.models.ConfigurationSetting;

public class ConfigurationClient {
    private ConfigurationAsyncClient asyncClient;

    ConfigurationClient(ConfigurationAsyncClient asyncClient) {
        this.asyncClient = asyncClient;
    }

    public ConfigurationSetting getConfigurationSetting(String key) {
        return asyncClient.getConfigurationSetting(key).block();
    }

    public ConfigurationSetting putConfigurationSetting(ConfigurationSetting setting) {
        return asyncClient.putConfigurationSetting(setting).block();
    }
}
