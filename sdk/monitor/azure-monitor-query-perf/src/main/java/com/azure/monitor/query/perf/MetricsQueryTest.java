// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.query.perf;

import com.azure.core.util.Configuration;
import com.azure.core.util.Context;
import com.azure.monitor.query.MetricsQueryClient;
import com.azure.monitor.query.models.MetricsQueryOptions;
import com.azure.monitor.query.perf.core.ServiceTest;
import com.azure.perf.test.core.PerfStressOptions;
import reactor.core.publisher.Mono;

import java.util.Arrays;

/**
 * This class tests the performance of a basic metrics query using the {@link MetricsQueryClient}.
 */
public class MetricsQueryTest extends ServiceTest<PerfStressOptions> {
    private final String resourceId;

    /**
     * Creates an instance of metrics query perf test.
     * @param options the configurable options for perf testing this class
     */
    public MetricsQueryTest(PerfStressOptions options) {
        super(options);
        resourceId = Configuration.getGlobalConfiguration().get("AZURE_MONITOR_METRICS_RESOURCE_ID");
        if (resourceId == null) {
            throw new IllegalStateException(String.format(CONFIGURATION_ERROR, "AZURE_MONITOR_METRICS_RESOURCE_ID"));
        }
    }

    @Override
    public void run() {
        metricsQueryClient.queryWithResponse(resourceId, Arrays.asList("SuccessfulCalls"),
                new MetricsQueryOptions().setTop(100), Context.NONE);
    }

    @Override
    public Mono<Void> runAsync() {
        return metricsQueryAsyncClient.queryWithResponse(resourceId, Arrays.asList("SuccessfulCalls"),
                new MetricsQueryOptions().setTop(100)).then();
    }
}
