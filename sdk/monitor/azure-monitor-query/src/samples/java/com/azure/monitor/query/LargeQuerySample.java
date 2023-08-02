// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.query;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.util.Configuration;
import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.monitor.query.models.*;

public class LargeQuerySample {


    // BEGIN: com.azure.monitor.query.LargeQuerySample-setWorkspaceId
    static final String WORKSPACE_ID = "{workspace-id}";
    // END: com.azure.monitor.query.LargeQuerySample-setWorkspaceId

    static LogsQueryClient client;

    public static void main(String[] args) {

        String queryString = "query string";

        // BEGIN: com.azure.monitor.query.LargeQuerySample-createLogsQueryClient
        LogsQueryClient client = new LogsQueryClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildClient();
        // END: com.azure.monitor.query.LargeQuerySample-createLogsQueryClient



    }


    // BEGIN: com.azure.monitor.query.LargeQuerySample-createBatchQueryFromLargeQuery
    static LogsBatchQuery createBatchQueryFromLargeQuery(String originalQuery,
                                                         List<OffsetDateTime> endpoints) {
        LogsBatchQuery batchQuery = new LogsBatchQuery();

        for (int i = 0; i < endpoints.size(); i++) {
            String queryString;
            if (i == endpoints.size() - 1) {
                queryString = String.format("%1$s | where %3$s >= datetime('%2$s')", originalQuery, endpoints.get(i), "TimeGenerated");
            }
            else {
                queryString = String.format("%1$s | where %4$s >= datetime('%2$s') and %4$s < datetime('%3$s')", originalQuery, endpoints.get(i), endpoints.get(i + 1));
            }
            batchQuery.addWorkspaceQuery(WORKSPACE_ID, queryString, null);
        }

        return batchQuery;
    }
    // END: com.azure.monitor.query.LargeQuerySample-createBatchQueryFromLargeQuery


    // BEGIN: com.azure.monitor.query.LargeQuerySample-createBatchQueryFromLargeQueryBySize
    static LogsBatchQuery createBatchQueryFromLargeQueryByByteSize(String originalQuery,
                                                                   int maxByteSizePerBatch) {
        String findBatchEndpointsQuery = String.format(
            "%1$s | sort by %2$s desc | extend batch_num = row_cumsum(estimate_data_size(*)) / %3$s | summarize endpoint=min(%2$s) by batch_num | sort by batch_num desc | project endpoint",
            originalQuery,
            "TimeGenerated",
            maxByteSizePerBatch);

        LogsQueryResult result = client.queryWorkspace(WORKSPACE_ID, findBatchEndpointsQuery, QueryTimeInterval.ALL);
        LogsTable table = result.getTable();
        List<LogsTableColumn> columns = table.getColumns();
        List<LogsTableRow> rows = table.getRows();
        List<OffsetDateTime> endpoints = new ArrayList<>();

        for (LogsTableRow row : rows) {
            row.getColumnValue("endpoint").ifPresent(rowValue -> {
                endpoints.add(rowValue.getValueAsDateTime());
            });
        }

        return createBatchQueryFromLargeQuery(originalQuery, endpoints);
    }
    // END: com.azure.monitor.query.LargeQuerySample-createBatchQueryFromLargeQueryBySize

    // BEGIN: com.azure.monitor.query.LargeQuerySample-createBatchQueryFromLargeQueryByRows
    static LogsBatchQuery createBatchQueryFromLargeQueryByRowCount(String originalQuery,
                                                                   int maxRowPerBatch) {

        String findBatchEndpointsQuery = String.format(
            "%1$s | sort by %2$s desc | extend batch_num = row_cumsum(1) / %3$s | summarize endpoint=min(%2$s) by batch_num | sort by batch_num desc | project endpoint",
            originalQuery,
            "TimeGenerated",
            maxRowPerBatch);

        LogsQueryResult result = client.queryWorkspace(WORKSPACE_ID, findBatchEndpointsQuery, QueryTimeInterval.ALL);
        LogsTable table = result.getTable();
        List<LogsTableColumn> columns = table.getColumns();
        List<LogsTableRow> rows = table.getRows();
        List<OffsetDateTime> endpoints = new ArrayList<>();

        for (LogsTableRow row : rows) {
            row.getColumnValue("endpoint").ifPresent(rowValue -> {
                endpoints.add(rowValue.getValueAsDateTime());
            });
        }

        return createBatchQueryFromLargeQuery(originalQuery, endpoints);
    }
    // END: com.azure.monitor.query.LargeQuerySample-createBatchQueryFromLargeQueryByRows
}
