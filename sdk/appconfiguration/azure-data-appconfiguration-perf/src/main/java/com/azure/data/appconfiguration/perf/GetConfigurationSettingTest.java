// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.appconfiguration.perf;

import com.azure.data.appconfiguration.perf.core.ConfigurationClientTestBase;
import com.azure.perf.test.core.PerfStressOptions;
import reactor.core.publisher.Mono;

public class GetConfigurationSettingTest extends ConfigurationClientTestBase<PerfStressOptions> {

    public GetConfigurationSettingTest(PerfStressOptions options) {
        super(options);
    }

    // Required resource setup goes here, upload the file to be downloaded during tests.
    public Mono<Void> globalSetupAsync() {
        return super.globalSetupAsync()
                   .then(configurationAsyncClient.setConfigurationSetting("some_key", "some_label",
                       "some_value"))
                   .then();
    }

    // Perform the API call to be tested here
    @Override
    public void run() {
        configurationClient.getConfigurationSetting("some_key", "some_label").getLabel();
    }

    @Override
    public Mono<Void> runAsync() {
        return configurationAsyncClient.getConfigurationSetting("some_key", "some_label")
            .map(configurationSetting -> {
                configurationSetting.getLabel();
                return 1;
            }).then();
    }
}
