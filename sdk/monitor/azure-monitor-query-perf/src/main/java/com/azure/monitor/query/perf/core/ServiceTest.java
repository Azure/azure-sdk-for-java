// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.query.perf.core;

import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.monitor.query.LogsQueryAsyncClient;
import com.azure.monitor.query.LogsQueryClient;
import com.azure.monitor.query.LogsQueryClientBuilder;
import com.azure.monitor.query.MetricsQueryAsyncClient;
import com.azure.monitor.query.MetricsQueryClient;
import com.azure.monitor.query.MetricsQueryClientBuilder;
import com.azure.perf.test.core.PerfStressOptions;
import com.azure.perf.test.core.PerfStressTest;

/**
 * Base class for Azure Monitor Query performance tests.
 */
public abstract class ServiceTest<TOptions extends PerfStressOptions> extends PerfStressTest<TOptions> {
    public static final String CONFIGURATION_ERROR = "Configuration %s must be set in either environment variables "
            + "or system properties.%n";

    protected final LogsQueryClient logsQueryClient;
    protected final LogsQueryAsyncClient logsQueryAsyncClient;
    protected final MetricsQueryClient metricsQueryClient;
    protected final MetricsQueryAsyncClient metricsQueryAsyncClient;

    /**
     * The base class for Azure Monitor Query performance tests.
     * @param options the configurable options for performing perf testing on this class.
     */
    public ServiceTest(TOptions options) {
        super(options);

        LogsQueryClientBuilder logsQueryClientBuilder = new LogsQueryClientBuilder()
                .credential(new DefaultAzureCredentialBuilder().build());

        MetricsQueryClientBuilder metricsQueryClientBuilder = new MetricsQueryClientBuilder()
                .credential(new DefaultAzureCredentialBuilder().build());
        this.logsQueryClient = logsQueryClientBuilder.buildClient();
        this.logsQueryAsyncClient = logsQueryClientBuilder.buildAsyncClient();

        this.metricsQueryClient = metricsQueryClientBuilder.buildClient();
        this.metricsQueryAsyncClient = metricsQueryClientBuilder.buildAsyncClient();
    }
}
