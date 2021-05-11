// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.appconfiguration.perf.core;

import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.data.appconfiguration.ConfigurationAsyncClient;
import com.azure.data.appconfiguration.ConfigurationClient;
import com.azure.data.appconfiguration.ConfigurationClientBuilder;
import com.azure.perf.test.core.PerfStressOptions;
import com.azure.perf.test.core.PerfStressTest;

/**
 * Base class for Azure App Configuration performance tests.
 */
public abstract class ServiceTest<TOptions extends PerfStressOptions> extends PerfStressTest<TOptions> {
    private static final String CONFIGURATION_ERROR = "Configuration %s must be set in either environment variables "
                                                          + "or system properties.%n";

    protected final ConfigurationClient configurationClient;
    protected final ConfigurationAsyncClient configurationAsyncClient;

    /**
     * The base class for Azure App Configuration tests.
     *
     * @param options the configurable options for performing perf testing on this class.
     *
     * @throws RuntimeException if "AZURE_APPCONFIG_CONNECTION_STRING" is null or empty.
     */
    public ServiceTest(TOptions options) {
        super(options);

        String connectionString = Configuration.getGlobalConfiguration().get("AZURE_APPCONFIG_CONNECTION_STRING");
        if (CoreUtils.isNullOrEmpty(connectionString)) {
            throw new RuntimeException(String.format(CONFIGURATION_ERROR, "AZURE_APPCONFIG_CONNECTION_STRING"));
        }

        ConfigurationClientBuilder builder = new ConfigurationClientBuilder().connectionString(connectionString);

        this.configurationClient = builder.buildClient();
        this.configurationAsyncClient  = builder.buildAsyncClient();
    }
}
