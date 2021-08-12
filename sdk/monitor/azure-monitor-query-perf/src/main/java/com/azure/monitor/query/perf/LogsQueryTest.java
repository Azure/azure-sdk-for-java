// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.query.perf;

import com.azure.core.util.Configuration;
import com.azure.monitor.query.LogsQueryClient;
import com.azure.monitor.query.perf.core.ServiceTest;
import com.azure.perf.test.core.PerfStressOptions;
import reactor.core.publisher.Mono;

/**
 * This class tests the performance of a single logs query using the {@link LogsQueryClient}.
 */
public class LogsQueryTest extends ServiceTest<PerfStressOptions> {
    private static final String LOGS_QUERY = "AppRequests | take 100";
    private final String workspaceId;

    /**
     * Creates an instance of logs query perf test.
     * @param options the configurable options for perf testing this class
     */
    public LogsQueryTest(PerfStressOptions options) {
        super(options);
        workspaceId = Configuration.getGlobalConfiguration().get("AZURE_MONITOR_LOGS_WORKSPACE_ID");
        if (workspaceId == null) {
            throw new IllegalStateException(String.format(CONFIGURATION_ERROR, "AZURE_MONITOR_LOGS_WORKSPACE_ID"));
        }
    }

    @Override
    public void run() {
        logsQueryClient.query(workspaceId, LOGS_QUERY, null);
    }

    @Override
    public Mono<Void> runAsync() {
        return logsQueryAsyncClient.query(workspaceId, LOGS_QUERY, null).then();
    }
}
