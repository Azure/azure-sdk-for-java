// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.ingestion.perf.core;

import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.monitor.ingestion.LogsIngestionAsyncClient;
import com.azure.monitor.ingestion.LogsIngestionClient;
import com.azure.monitor.ingestion.LogsIngestionClientBuilder;
import com.azure.perf.test.core.PerfStressOptions;
import com.azure.perf.test.core.PerfStressTest;

/**
 * Base class for Azure Monitor Ingestion performance tests.
 */
public abstract class ServiceTest<TOptions extends PerfStressOptions> extends PerfStressTest<TOptions> {
    public static final String CONFIGURATION_ERROR = "Configuration %s must be set in either environment variables "
            + "or system properties.%n";

    protected final LogsIngestionClient logsIngestionClient;
    protected final LogsIngestionAsyncClient logsIngestionAsyncClient;

    /**
     * The base class for Azure Monitor Ingestion performance tests.
     * @param options the configurable options for performing perf testing on this class.
     */
    public ServiceTest(TOptions options) {
        super(options);

        String dataCollectionEndpoint = Configuration.getGlobalConfiguration().get("AZURE_MONITOR_DCE");
        if (CoreUtils.isNullOrEmpty(dataCollectionEndpoint)) {
            throw new IllegalStateException(String.format(CONFIGURATION_ERROR, "AZURE_MONITOR_DCE"));
        }

        LogsIngestionClientBuilder logsIngestionClientBuilder = new LogsIngestionClientBuilder()
                .credential(new DefaultAzureCredentialBuilder().build())
                .endpoint(dataCollectionEndpoint);

        this.logsIngestionClient = logsIngestionClientBuilder.buildClient();
        this.logsIngestionAsyncClient = logsIngestionClientBuilder.buildAsyncClient();

    }
}
