package com.azure.monitor.query.perf;

import com.azure.core.util.Configuration;
import com.azure.monitor.query.LogsQueryClient;
import com.azure.monitor.query.perf.core.ServiceTest;
import com.azure.perf.test.core.PerfStressOptions;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

/**
 * This class tests the performance of a batch of logs queries using the {@link LogsQueryClient}.
 */
public class LogsBatchQueryTest extends ServiceTest<PerfStressOptions> {

    private static final List<String> LOGS_BATCH_QUERIES = Arrays
            .asList("AppRequests | take 100", "AzureActivity | take 100", "AppPerformanceCounters | take 100");
    private final String workspaceId;

    /**
     * Creates an instance of logs batch query perf test.
     * @param options the configurable options for perf testing this class
     */
    public LogsBatchQueryTest(PerfStressOptions options) {
        super(options);
        workspaceId = Configuration.getGlobalConfiguration().get("AZURE_MONITOR_LOGS_WORKSPACE_ID");
        if (workspaceId == null) {
            throw new IllegalStateException(String.format(CONFIGURATION_ERROR, "AZURE_MONITOR_LOGS_WORKSPACE_ID"));
        }
    }

    @Override
    public void run() {
        logsQueryClient.queryLogsBatch(workspaceId, LOGS_BATCH_QUERIES, null);
    }

    @Override
    public Mono<Void> runAsync() {
        return logsQueryAsyncClient.queryLogsBatch(workspaceId, LOGS_BATCH_QUERIES, null).then();
    }
}
