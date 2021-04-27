// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.appconfiguration.perf;

import com.azure.data.appconfiguration.models.ConfigurationSetting;
import com.azure.data.appconfiguration.perf.core.ServiceTest;
import com.azure.perf.test.core.PerfStressOptions;
import reactor.core.publisher.Mono;

/**
 * Performs custom model recognition operations.
 */
public class SetConfigurationSettingTest extends ServiceTest<PerfStressOptions> {
    ConfigurationSetting setting;

    /**
     * The SetConfigurationSettingTest class.
     *
     * @param options the configurable options for perf testing this class
     */
    public SetConfigurationSettingTest(PerfStressOptions options) {
        super(options);
        setting = new ConfigurationSetting().setKey("key").setValue("value");
    }

    @Override
    public void run() {
        configurationClient.setConfigurationSetting(setting);
    }

    @Override
    public Mono<Void> runAsync() {
        return configurationAsyncClient.setConfigurationSetting(setting).then();
    }
}
