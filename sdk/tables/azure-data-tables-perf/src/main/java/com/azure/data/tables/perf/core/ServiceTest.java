// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.tables.perf.core;

import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.data.tables.TableServiceAsyncClient;
import com.azure.data.tables.TableServiceClient;
import com.azure.data.tables.TableServiceClientBuilder;
import com.azure.perf.test.core.PerfStressOptions;
import com.azure.perf.test.core.PerfStressTest;

public abstract class ServiceTest<TOptions extends PerfStressOptions> extends PerfStressTest<TOptions> {
    protected final TableServiceClient tableServiceClient;
    protected final TableServiceAsyncClient tableServiceAsyncClient;
    private final Configuration configuration;

    /**
     * Creates an instance of the performance test.
     *
     * @param options The options configured for the test.
     */
    public ServiceTest(TOptions options) {
        super(options);

        configuration = Configuration.getGlobalConfiguration().clone();
        String connectionString = configuration.get("AZURE_TABLES_CONNECTION_STRING");

        if (CoreUtils.isNullOrEmpty(connectionString)) {
            throw new IllegalStateException("Environment variable AZURE_TABLES_CONNECTION_STRING must be set");
        }

        // Setup the service client
        TableServiceClientBuilder builder = new TableServiceClientBuilder()
            .connectionString(connectionString);

        tableServiceClient = builder.buildClient();
        tableServiceAsyncClient = builder.buildAsyncClient();
    }
}
