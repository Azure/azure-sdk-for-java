// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.query;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.monitor.query.models.LogsBatchQuery;
import com.azure.monitor.query.models.LogsBatchQueryResult;
import com.azure.monitor.query.models.LogsQueryResult;
import com.azure.monitor.query.models.LogsTable;
import com.azure.monitor.query.models.LogsTableRow;
import com.azure.monitor.query.models.QueryTimeInterval;

/**
 * A sample to demonstrate using a batch logs query to overcome the service limits for a large query.
 */
public class SplitQueryByByteSample {


    static String workspaceId;
    static LogsQueryClient client;

    /**
     * The main method to execute the sample.
     * @param args Ignored args.
     */
    public static void main(String[] args) {

        String queryString = "AppRequests";
        workspaceId = Configuration.getGlobalConfiguration().get("AZURE_MONITOR_LOGS_WORKSPACE_ID");

        int maxByteSizePerBatch = 1024 * 1024 * 10; // 10 MB

        client = new LogsQueryClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildClient();


        // Running a log batch query with a byte size limit on each batch
        LogsBatchQuery byteBasedBatchQuery = createBatchQueryFromTimeRanges(queryString,
            createQueryTimeIntervalsForBatchQueryByByteSize(queryString, maxByteSizePerBatch));

        // The result of the byte split query
        List<LogsBatchQueryResult> byteLimitedResults = client.queryBatch(byteBasedBatchQuery).getBatchResults();

        // consolidate the results from the batch query
        LogsQueryResult combineByteBasedQuery = simulateSingleQuery(byteLimitedResults);
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
     * Helper method to create a list of time intervals for a batch query based on the byte size limit.
     *
     * @param originalQuery The original query string.
     * @param maxByteSizePerBatch The maximum byte size per batch. If multiple log entries returned in the original
     *                            query have the exact same timestamp, the byte size per batch may exceed this limit.
     * @return A list of {@link QueryTimeInterval} objects.
     */
    static List<QueryTimeInterval> createQueryTimeIntervalsForBatchQueryByByteSize(String originalQuery,
                                                                                   int maxByteSizePerBatch) {
        String findBatchEndpointsQuery = String.format(
            "%1$s | sort by TimeGenerated desc | extend batch_num = row_cumsum(estimate_data_size(*)) / %2$s | summarize batchStart=min(TimeGenerated) by batch_num | sort by batch_num desc | project batchStart",
            originalQuery,
            maxByteSizePerBatch);

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


    /**
     * This method simulates the result of a single query from the results of a batch query by combining lists of
     * log tables from each batch query result. It is intended only for batch queries resulting from a split single
     * query. It is not intended to be used on queries containing statistics, visualization data, or errors.
     * @param batchQueryResults The results (lists of log tables) from the split single query.
     * @return The result (log tables) in the form of a single query.
     */
    static LogsQueryResult simulateSingleQuery(List<LogsBatchQueryResult> batchQueryResults) {
        List<LogsTable> logsTables = new ArrayList<>();
        for (LogsBatchQueryResult batchQueryResult : batchQueryResults) {
            logsTables.addAll(batchQueryResult.getAllTables());
        }
        return new LogsQueryResult(logsTables, null, null, null);
    }
}
