// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.learn.appconfig;

import com.azure.learn.appconfig.models.ConfigurationSetting;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;

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

    public Response<ConfigurationSetting> getConfigurationSettingWithResponse(ConfigurationSetting setting, boolean ifChanged, Context context) {
        throw new UnsupportedOperationException("not yet implemented");
    }
}