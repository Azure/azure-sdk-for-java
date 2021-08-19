package com.azure.monitor.query.perf;

import com.azure.core.util.Configuration;
import com.azure.monitor.query.LogsQueryClient;
import com.azure.monitor.query.perf.core.ServiceTest;
import com.azure.perf.test.core.PerfStressOptions;
import reactor.core.publisher.Mono;

/**
 * This class tests the performance of a single logs query that maps the response to a custom model type using the
 * {@link LogsQueryClient}.
 */
public class LogsQueryAsModelTest extends ServiceTest<PerfStressOptions> {

    private static final String LOGS_QUERY = "AppRequests | take 100";
    private final String workspaceId;

    /**
     * Creates an instance of logs query perf test.
     * @param options the configurable options for perf testing this class
     */
    public LogsQueryAsModelTest(PerfStressOptions options) {
        super(options);
        workspaceId = Configuration.getGlobalConfiguration().get("AZURE_MONITOR_LOGS_WORKSPACE_ID");
        if (workspaceId == null) {
            throw new IllegalStateException(String.format(CONFIGURATION_ERROR, "AZURE_MONITOR_LOGS_WORKSPACE_ID"));
        }
    }

    @Override
    public void run() {
        logsQueryClient.query(workspaceId, LOGS_QUERY, null).toObject(CustomModel.class);
    }

    @Override
    public Mono<Void> runAsync() {
        return logsQueryAsyncClient.query(workspaceId, LOGS_QUERY, null)
                .map(response -> response.toObject(CustomModel.class))
                .then();
    }
}
