// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.learn.appconfig;

import com.azure.core.http.HttpPipeline;
import com.azure.learn.appconfig.implementation.AzureAppConfigurationImpl;
import com.azure.learn.appconfig.implementation.AzureAppConfigurationImplBuilder;
import com.azure.learn.appconfig.implementation.models.GetKeyValueResponse;
import com.azure.learn.appconfig.implementation.models.PutKeyValueResponse;
import com.azure.learn.appconfig.models.ConfigurationSetting;
import reactor.core.publisher.Mono;

public class ConfigurationAsyncClient {
    private AzureAppConfigurationImpl clientImpl;

    ConfigurationAsyncClient(AzureAppConfigurationImpl clientImpl) {
        this.clientImpl = clientImpl;
    }

    public Mono<ConfigurationSetting> getConfigurationSetting(String key) {
        return clientImpl.getKeyValueWithResponseAsync(key, null, null, null, null, null, null)
            .map(GetKeyValueResponse::getValue);
    }

    public Mono<ConfigurationSetting> putConfigurationSetting(ConfigurationSetting setting) {
        return clientImpl.putKeyValueWithResponseAsync(setting.getKey(), null, null, null, setting, null)
            .map(PutKeyValueResponse::getValue);
    }
}
