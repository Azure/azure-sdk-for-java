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

/**
 *
 */
public class LogsQuerySample {
    /**
     * @param args
     */
    public static void main(String[] args) {
        ClientSecretCredential tokenCredential = new ClientSecretCredentialBuilder()
            .clientId(Configuration.getGlobalConfiguration().get("AZURE_MONITOR_CLIENT_ID"))
            .clientSecret(Configuration.getGlobalConfiguration().get("AZURE_MONITOR_CLIENT_SECRET"))
            .tenantId(Configuration.getGlobalConfiguration().get("AZURE_TENANT_ID"))
            .build();

        AzureMonitorQueryClient queryClient = new AzureMonitorQueryClientBuilder()
            .credential(tokenCredential)
            .buildClient();

        LogsQueryResult queryResults = queryClient.queryLogs("d2d0e126-fa1e-4b0a-b647-250cdd471e68", "AppRequests",
            null);
        System.out.println("Number of tables = " + queryResults.getLogsTables().size());

        // Sample to iterate over all cells in the table
        for (LogsTable table : queryResults.getLogsTables()) {
            for (LogsTableCell tableCell : table.getAllTableCells()) {
                System.out.println("Column = " + tableCell.getColumnName()+ "; value = " + tableCell.getRowValue());
            }
        }

        // Sample to iterate by row
        for (LogsTable table : queryResults.getLogsTables()) {
            for (LogsTableRow row : table.getTableRows()) {
                System.out.println("Row index " + row.getRowIndex());
                row.getTableRow()
                    .forEach(cell -> System.out.println("Column = " + cell.getColumnName() + "; value = " + cell.getRowValue()));
            }
        }
    }
}
