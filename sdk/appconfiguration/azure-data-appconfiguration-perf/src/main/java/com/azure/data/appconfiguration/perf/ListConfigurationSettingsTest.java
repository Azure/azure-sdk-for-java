// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.appconfiguration.perf;

import com.azure.data.appconfiguration.models.ConfigurationSetting;
import com.azure.data.appconfiguration.models.SettingSelector;
import com.azure.data.appconfiguration.perf.core.ServiceTest;
import com.azure.perf.test.core.PerfStressOptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

/**
 * Performs listing multiple configuration settings.
 */
public class ListConfigurationSettingsTest extends ServiceTest<PerfStressOptions> {
    private static final String KEY_PREFIX = "keyPrefix";
    private static final String SETTING_VALUE = "value";

    private final SettingSelector keyFilterSelector = new SettingSelector().setKeyFilter(KEY_PREFIX + "*");

    private final int settingCount = options.getCount();

    /**
     * The ListConfigurationSettingsTest class.
     *
     * @param options the configurable options for perf testing this class
     */
    public ListConfigurationSettingsTest(PerfStressOptions options) {
        super(options);
    }

    @Override
    public Mono<Void> globalSetupAsync() {
        List<Mono<ConfigurationSetting>> settingMonoList = new ArrayList<>();
        for (int i = 0; i < settingCount; i++) {
            settingMonoList.add(configurationAsyncClient.setConfigurationSetting(
                new ConfigurationSetting().setKey(KEY_PREFIX + i).setValue(SETTING_VALUE)));
        }

        return Flux.concat(settingMonoList).then();
    }

    @Override
    public Mono<Void> globalCleanupAsync() {
        List<Mono<ConfigurationSetting>> settingMonoList = new ArrayList<>();
        for (int i = 0; i < settingCount; i++) {
            settingMonoList.add(configurationAsyncClient.deleteConfigurationSetting(
                new ConfigurationSetting().setKey(KEY_PREFIX + i).setValue(SETTING_VALUE)));
        }

        return Flux.concat(settingMonoList).then();
    }

    @Override
    public void run() {
        configurationClient.listConfigurationSettings(keyFilterSelector);
    }

    @Override
    public Mono<Void> runAsync() {
        return configurationAsyncClient.listConfigurationSettings(keyFilterSelector).then();
    }
}
