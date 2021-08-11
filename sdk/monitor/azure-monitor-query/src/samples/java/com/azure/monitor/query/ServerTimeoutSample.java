// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.query;

import com.azure.core.http.rest.Response;
import com.azure.core.util.Configuration;
import com.azure.core.util.Context;
import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.monitor.query.models.LogsQueryOptions;
import com.azure.monitor.query.models.LogsQueryResult;
import com.azure.monitor.query.models.LogsTable;
import com.azure.monitor.query.models.LogsTableRow;

import java.time.Duration;

/**
 * A sample to demonstrate querying for logs from Azure Monitor using a complex Kusto query that requires extended
 * time on the server to complete query execution. This sample shows how to set the timeout on the server.
 */
public class ServerTimeoutSample {

    /**
     * The main method to execute the sample.
     * @param args Ignored args.
     */
    public static void main(String[] args) {

        // Create token credential
        ClientSecretCredential tokenCredential = new ClientSecretCredentialBuilder()
            .clientId(Configuration.getGlobalConfiguration().get("AZURE_MONITOR_CLIENT_ID"))
            .clientSecret(Configuration.getGlobalConfiguration().get("AZURE_MONITOR_CLIENT_SECRET"))
            .tenantId(Configuration.getGlobalConfiguration().get("AZURE_TENANT_ID"))
            .build();

        // create client
        LogsQueryClient logsQueryClient = new LogsQueryClientBuilder()
            .credential(tokenCredential)
            .buildClient();

        // set request options: server timeout, rendering, statistics
        LogsQueryOptions options = new LogsQueryOptions()
            .setServerTimeout(Duration.ofSeconds(30))
            .setIncludeRendering(true)
            .setIncludeStatistics(true);

        // make service call with these request options set as filter header
        Response<LogsQueryResult> response = logsQueryClient
            .queryLogsWithResponse("d2d0e126-fa1e-4b0a-b647-250cdd471e68",
                    "AppRequests | take 5", null, options, Context.NONE);
        LogsQueryResult logsQueryResult = response.getValue();

        // Sample to iterate by row
        for (LogsTable table : logsQueryResult.getLogsTables()) {
            for (LogsTableRow row : table.getRows()) {
                System.out.println("Row index " + row.getRowIndex());
                row.getRow()
                    .forEach(cell -> System.out.println("Column = " + cell.getColumnName() + "; value = " + cell.getValueAsString()));
            }
        }

    }
}
