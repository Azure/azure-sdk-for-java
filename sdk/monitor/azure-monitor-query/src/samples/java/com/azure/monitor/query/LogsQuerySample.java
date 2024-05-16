// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.query;

import com.azure.core.util.Configuration;
import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.monitor.query.models.LogsQueryResult;
import com.azure.monitor.query.models.LogsTable;
import com.azure.monitor.query.models.LogsTableCell;
import com.azure.monitor.query.models.LogsTableRow;

import java.util.Optional;

/**
 * A sample to demonstrate querying for logs from Azure Monitor using a Kusto query
 */
public class LogsQuerySample {

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
                .buildClient();

        LogsQueryResult queryResults = logsQueryClient.queryWorkspace("d2d0e126-fa1e-4b0a-b647-250cdd471e68", "AppRequests",
                null);
        System.out.println("Number of tables = " + queryResults.getAllTables().size());

        // Sample to iterate over all cells in the table
        for (LogsTable table : queryResults.getAllTables()) {
            for (LogsTableCell tableCell : table.getAllTableCells()) {
                System.out.println("Column = " + tableCell.getColumnName() + "; value = " + tableCell.getValueAsString());
            }
        }

        // Sample to iterate by row
        for (LogsTable table : queryResults.getAllTables()) {
            for (LogsTableRow row : table.getRows()) {
                row.getRow()
                        .forEach(cell -> System.out.println("Column = " + cell.getColumnName() + "; value = " + cell.getValueAsString()));
            }
        }

        // Sample to get value of a column
        for (LogsTable table : queryResults.getAllTables()) {
            for (LogsTableRow row : table.getRows()) {
                Optional<LogsTableCell> resourceGroup = row.getColumnValue("DurationMs");
                if (resourceGroup.isPresent()) {
                    System.out.println(resourceGroup.get().getValueAsString());
                }
            }
        }
    }
}
