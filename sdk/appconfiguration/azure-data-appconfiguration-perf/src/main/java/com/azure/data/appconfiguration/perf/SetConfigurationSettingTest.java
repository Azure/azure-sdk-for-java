// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.appconfiguration.perf;

import com.azure.data.appconfiguration.perf.core.ConfigurationClientTestBase;
import com.azure.perf.test.core.PerfStressOptions;
import reactor.core.publisher.Mono;

public class SetConfigurationSettingTest extends ConfigurationClientTestBase<PerfStressOptions> {

    public SetConfigurationSettingTest(PerfStressOptions options) {
        super(options);
    }

    // Perform the API call to be tested here
    @Override
    public void run() {
        configurationClient.setConfigurationSetting("some_key", "some_label",
            "some_value").getLabel();
    }

    @Override
    public Mono<Void> runAsync() {
        return configurationAsyncClient.setConfigurationSetting("some_key", "some_label", "some_value")
            .map(configurationSetting -> {
                configurationSetting.getLabel();
                return 1;
            }).then();
    }
}
