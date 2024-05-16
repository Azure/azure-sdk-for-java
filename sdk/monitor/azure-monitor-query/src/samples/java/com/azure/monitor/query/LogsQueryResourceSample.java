// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.query;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.monitor.query.models.LogsQueryResult;
import com.azure.monitor.query.models.LogsTable;
import com.azure.monitor.query.models.LogsTableCell;
import com.azure.monitor.query.models.LogsTableRow;
import com.azure.monitor.query.models.QueryTimeInterval;

import java.util.Optional;

/**
 * A sample to demonstrate querying for logs of an Azure resource from Azure Monitor using a Kusto query
 */
public class LogsQueryResourceSample {

    /**
     * The main method to execute the sample.
     * @param args Ignored args.
     */
    public static void main(String[] args) {
        TokenCredential tokenCredential = new DefaultAzureCredentialBuilder().build();

        LogsQueryClient logsQueryClient = new LogsQueryClientBuilder()
            .credential(tokenCredential)
            .buildClient();

        LogsQueryResult queryResults = logsQueryClient
            .queryResource("/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/{resourceProviderNamespace}/{resourceType}/{resourceName}", "AppRequests",
            QueryTimeInterval.ALL);
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
