// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.query;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.util.Context;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.monitor.query.models.LogsQueryOptions;
import com.azure.monitor.query.models.LogsQueryResult;
import com.azure.monitor.query.models.LogsTable;
import com.azure.monitor.query.models.LogsTableRow;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A sample to demonstrate querying for logs from multiple workspaces.
 */
public class MultiWorkspaceSample {

    /**
     * The main method to execute the sample.
     * @param args ignored args.
     */
    public static void main(String[] args) {
        TokenCredential tokenCredential = new DefaultAzureCredentialBuilder().build();

        LogsQueryClient logsQueryClient = new LogsQueryClientBuilder()
                .credential(tokenCredential)
                .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
                .buildClient();

        LogsQueryResult queryResults = logsQueryClient.queryWorkspaceWithResponse("d2d0e126-fa1e-4b0a-b647-250cdd471e68",
                "union * | where TimeGenerated > ago(10d) | project TenantId", null, new LogsQueryOptions()
                        .setAdditionalWorkspaces(Arrays.asList("srnagar-log-analytics-ws-2", "srnagar-log-analytics-ws")),
                Context.NONE).getValue();

        System.out.println("Number of tables = " + queryResults.getAllTables().size());

        // Sample to iterate by row
        Set<String> results = new HashSet<>();
        for (LogsTable table : queryResults.getAllTables()) {
            for (LogsTableRow row : table.getRows()) {
                Set<String> collect =
                        row.getRow().stream().map(cell -> cell.getValueAsString())
                                .collect(Collectors.toSet());

                row.getRow().forEach(cell -> System.out.println(cell.getValueAsString()));
                results.addAll(collect);
            }
        }
        System.out.println("Collected tenants " + results);
    }
}
