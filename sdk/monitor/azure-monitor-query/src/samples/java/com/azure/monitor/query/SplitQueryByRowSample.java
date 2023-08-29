// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.query;

import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.monitor.query.models.LogsBatchQuery;
import com.azure.monitor.query.models.LogsBatchQueryResult;
import com.azure.monitor.query.models.LogsQueryResult;
import com.azure.monitor.query.models.LogsTable;
import com.azure.monitor.query.models.LogsTableRow;
import com.azure.monitor.query.models.QueryTimeInterval;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

public class SplitQueryByRowSample {

    static String workspaceId;
    static LogsQueryClient client;

    /**
     * The main method to execute the sample.
     * @param args Ignored args.
     */
    public static void main(String[] args) {
        String queryString = "AppRequests";
        workspaceId = Configuration.getGlobalConfiguration().get("AZURE_MONITOR_LOGS_WORKSPACE_ID");

        // These values are for demonstration purposes only. Please set to the appropriate values for your use case.
        int maxRowsPerBatch = 100; // 100 rows. The service maximum is 500,000 rows per query

        client = new LogsQueryClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildClient();

        // Running a log batch query with a row count limit on each batch
        LogsBatchQuery rowBasedBatchQuery = createBatchQueryFromTimeRanges(queryString,
            createQueryTimeIntervalsForBatchQueryByRowCount(queryString, maxRowsPerBatch));

        // The result of the row split query
        List<LogsBatchQueryResult> rowLimitedResults = client.queryBatch(rowBasedBatchQuery).getBatchResults();

        // consolidate the results from the batch query
        LogsQueryResult combineRowBasedQuery = simulateSingleQuery(rowLimitedResults);
    }

    /**
     * Helper method to create a batch query from a query string and a list of time intervals.
     *
     * @param originalQuery The original query string.
     * @param queryTimeIntervals The list of time intervals.
     * @return A {@link LogsBatchQuery} object equivalent to the original query.
     */
    static LogsBatchQuery createBatchQueryFromTimeRanges(String originalQuery,
                                                         List<QueryTimeInterval> queryTimeIntervals) {
        LogsBatchQuery batchQuery = new LogsBatchQuery();

        for (QueryTimeInterval timeInterval : queryTimeIntervals) {
            batchQuery.addWorkspaceQuery(workspaceId, originalQuery, timeInterval);
        }

        return batchQuery;
    }

    /**
     * Helper method to create a list of time intervals for a batch query based on the row count limit.
     *
     * @param originalQuery The original query string.
     * @param maxRowPerBatch The maximum row count per batch. If multiple log entries returned in the original query
     *                       have the exact same timestamp, the row count per batch may exceed this limit.
     * @return A list of {@link QueryTimeInterval} objects.
     */
    static List<QueryTimeInterval> createQueryTimeIntervalsForBatchQueryByRowCount(String originalQuery,
                                                                                   int maxRowPerBatch) {

        String findBatchEndpointsQuery = String.format(
            "%1$s | sort by TimeGenerated desc | extend batch_num = row_cumsum(1) / %2$s | summarize batchStart=min(TimeGenerated) by batch_num | sort by batch_num desc | project batchStart",
            originalQuery,
            maxRowPerBatch);

        LogsQueryResult result = client.queryWorkspace(workspaceId, findBatchEndpointsQuery, QueryTimeInterval.ALL);
        List<LogsTableRow> rows = result.getTable().getRows();
        List<OffsetDateTime> offsetDateTimes = new ArrayList<>();
        List<QueryTimeInterval> queryTimeIntervals = new ArrayList<>();

        for (LogsTableRow row : rows) {
            row.getColumnValue("batchStart").ifPresent(rowValue -> {
                offsetDateTimes.add(rowValue.getValueAsDateTime());
            });
        }


        for (int i = 0; i < offsetDateTimes.size(); i++) {
            OffsetDateTime startTime = offsetDateTimes.get(i);
            OffsetDateTime endTime = i == offsetDateTimes.size() - 1 ? OffsetDateTime.now() : offsetDateTimes.get(i + 1);
            QueryTimeInterval timeInterval = new QueryTimeInterval(startTime, endTime);
            queryTimeIntervals.add(timeInterval);
        }

        return queryTimeIntervals;
    }

    static LogsQueryResult simulateSingleQuery(List<LogsBatchQueryResult> batchQueryResults) {
        List<LogsTable> logsTables = new ArrayList<>();
        for (LogsBatchQueryResult batchQueryResult : batchQueryResults) {
            logsTables.addAll(batchQueryResult.getAllTables());
        }
        return new LogsQueryResult(logsTables, null, null, null);
    }
}
