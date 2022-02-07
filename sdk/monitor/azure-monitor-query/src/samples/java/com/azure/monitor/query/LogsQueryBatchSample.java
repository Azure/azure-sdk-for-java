// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.query;

import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.util.Configuration;
import com.azure.core.util.Context;
import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.monitor.query.models.LogsBatchQuery;
import com.azure.monitor.query.models.LogsBatchQueryResult;
import com.azure.monitor.query.models.LogsBatchQueryResultCollection;
import com.azure.monitor.query.models.LogsQueryOptions;
import com.azure.monitor.query.models.LogsTable;
import com.azure.monitor.query.models.LogsTableRow;

import java.util.List;

/**
 * A sample to demonstrate querying for logs from Azure Monitor using a batch of Kusto queries.
 */
public class LogsQueryBatchSample {
    /**
     * The main method to execute the sample.
     * @param args Ignored args.
     */
    public static void main(String[] args) {
        ClientSecretCredential tokenCredential = new ClientSecretCredentialBuilder()
                .clientId(Configuration.getGlobalConfiguration().get("AZURE_MONITOR_CLIENT_ID"))
                .clientSecret(Configuration.getGlobalConfiguration().get("AZURE_MONITOR_CLIENT_SECRET"))
                .tenantId(Configuration.getGlobalConfiguration().get("AZURE_TENANT_ID"))
                .build();

        LogsQueryClient logsQueryClient = new LogsQueryClientBuilder()
                .credential(tokenCredential)
                .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
                .buildClient();

        LogsBatchQuery logsBatchQuery = new LogsBatchQuery();
        logsBatchQuery.addWorkspaceQuery("d2d0e126-fa1e-4b0a-b647-250cdd471e68", "AppRequests | take 2", null);
        logsBatchQuery.addWorkspaceQuery("d2d0e126-fa1e-4b0a-b647-250cdd471e68", "AppRequests | take 3", null);
        logsBatchQuery.addWorkspaceQuery("d2d0e126-fa1e-4b0a-b647-250cdd471e68", "AppRequests | take 4", null,
                        new LogsQueryOptions().setIncludeStatistics(true));

        LogsBatchQueryResultCollection batchResultCollection = logsQueryClient
                .queryBatchWithResponse(logsBatchQuery, Context.NONE).getValue();

        List<LogsBatchQueryResult> responses = batchResultCollection.getBatchResults();

        for (LogsBatchQueryResult response : responses) {

            // Sample to iterate by row
            for (LogsTable table : response.getAllTables()) {
                for (LogsTableRow row : table.getRows()) {
                    System.out.println("Row index " + row.getRowIndex());
                    row.getRow()
                            .forEach(cell -> System.out.println("Column = " + cell.getColumnName() + "; value = " + cell.getValueAsString()));
                }
            }
        }
    }
}
