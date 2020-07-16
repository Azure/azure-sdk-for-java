// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.appconfiguration.perf.core;

import com.azure.core.util.CoreUtils;
import com.azure.data.appconfiguration.ConfigurationAsyncClient;
import com.azure.data.appconfiguration.ConfigurationClient;
import com.azure.data.appconfiguration.ConfigurationClientBuilder;
import com.azure.perf.test.core.PerfStressOptions;
import com.azure.perf.test.core.PerfStressTest;

public abstract class ConfigurationClientTestBase<TOptions extends PerfStressOptions> extends PerfStressTest<TOptions> {

    protected final ConfigurationAsyncClient configurationAsyncClient;
    protected final ConfigurationClient configurationClient;

    public ConfigurationClientTestBase(TOptions options) {
        super(options);
        String connectionString = System.getenv("APPCONFIG_CONNECTION_STRING");
        if (CoreUtils.isNullOrEmpty(connectionString)) {
            System.out.println("Environment variable APPCONFIG_CONNECTION_STRING must be set");
            System.exit(1);
        }
        ConfigurationClientBuilder configurationClientBuilder =  new ConfigurationClientBuilder()
                                                                     .connectionString(connectionString);
        configurationClient = configurationClientBuilder.buildClient();
        configurationAsyncClient = configurationClientBuilder.buildAsyncClient();
    }
}
