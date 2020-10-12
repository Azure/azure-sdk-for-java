// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.learn.appconfig;

import com.azure.learn.appconfig.models.ConfigurationSetting;
import reactor.core.publisher.Mono;

public class ConfigurationAsyncClient {
    ConfigurationAsyncClient() {
        // package-private constructor
    }    

    public Mono<ConfigurationSetting> getConfigurationSetting(String key) {
        return Mono.error(new UnsupportedOperationException("not yet implemented"));
    }

    public Mono<ConfigurationSetting> getConfigurationSetting(String key, String label) {
        return Mono.error(new UnsupportedOperationException("not yet implemented"));
    }
}
