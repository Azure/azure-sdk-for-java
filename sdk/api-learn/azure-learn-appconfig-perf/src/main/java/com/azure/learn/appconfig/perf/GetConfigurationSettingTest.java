// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.learn.appconfig.perf;

import com.azure.core.util.CoreUtils;
import com.azure.perf.test.core.PerfStressOptions;
import com.azure.perf.test.core.PerfStressTest;

import com.azure.data.appconfiguration.ConfigurationAsyncClient;
import com.azure.data.appconfiguration.ConfigurationClient;
import com.azure.data.appconfiguration.ConfigurationClientBuilder;

import reactor.core.publisher.Mono;

import java.util.UUID;

public class GetConfigurationSettingTest extends PerfStressTest<PerfStressOptions> {
    private static final String KEY = "perfstress-" + UUID.randomUUID().toString();
    
    protected final ConfigurationClient configurationClient;
    protected final ConfigurationAsyncClient configurationAsyncClient;
    
    public GetConfigurationSettingTest(PerfStressOptions options) {
        super(options);
        
        String connectionString = System.getenv("APP_CONFIG_CONNECTION_STRING");
        if (CoreUtils.isNullOrEmpty(connectionString)) {
            System.out.println("Environment variable APP_CONFIG_CONNECTION_STRING must be set");
            System.exit(1);
        }

        ConfigurationClientBuilder builder = new ConfigurationClientBuilder()
            .connectionString(connectionString);

        configurationClient = builder.buildClient();
        configurationAsyncClient = builder.buildAsyncClient();
    }

    @Override
    public Mono<Void> globalSetupAsync() {
        String value = new String(new char[(int)options.getSize()]);
        return super.globalSetupAsync().then(
            configurationAsyncClient.setConfigurationSetting(KEY, null, value).then());
    }

    @Override
    public Mono<Void> globalCleanupAsync() {
        return configurationAsyncClient.deleteConfigurationSetting(KEY, null).then(super.globalCleanupAsync());
    }

    @Override
    public void run() {
        configurationClient.getConfigurationSetting(KEY, null);
    }

    @Override
    public Mono<Void> runAsync() {
        return configurationAsyncClient.getConfigurationSetting(KEY, null).then();
    }
}
