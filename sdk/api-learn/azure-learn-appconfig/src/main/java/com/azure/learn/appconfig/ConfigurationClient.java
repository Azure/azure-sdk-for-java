// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.learn.appconfig;

import com.azure.learn.appconfig.models.ConfigurationSetting;

public final class ConfigurationClient {

    ConfigurationClient() {
        // package-private constructor
    }

    public ConfigurationSetting getConfigurationSetting(String key) {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public ConfigurationSetting getConfigurationSetting(String key, String label) {
        throw new UnsupportedOperationException("not yet implemented");
    }
}