// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.query;

import com.azure.core.util.Configuration;
import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.monitor.query.log.implementation.models.QueryResults;
import com.azure.monitor.query.log.implementation.models.Table;

import java.util.List;

public class AzureLogQueryClientSample {
    public static void main(String[] args) {
        ClientSecretCredential tokenCredential = new ClientSecretCredentialBuilder()
            .clientId(Configuration.getGlobalConfiguration().get("AZURE_MONITOR_CLIENT_ID"))
            .clientSecret(Configuration.getGlobalConfiguration().get("AZURE_MONITOR_CLIENT_SECRET"))
            .tenantId(Configuration.getGlobalConfiguration().get("AZURE_TENANT_ID"))
            .build();

        AzureMonitorQueryClient queryClient = new AzureMonitorQueryClientBuilder()
            .credential(tokenCredential)
            .buildClient();

        QueryResults queryResults = queryClient.queryLogs("d2d0e126-fa1e-4b0a-b647-250cdd471e68", "AppRequests");
        System.out.println("Number of tables = " + queryResults.getTables().size());
        System.out.println("Number of rows = " + queryResults.getTables().get(0).getRows().size());
        System.out.println("Number of columns = " + queryResults.getTables().get(0).getColumns().size());
        System.out.println("___________________________________________________________________________");

        for (Table table : queryResults.getTables()) {
            for (List<String> row : table.getRows()) {
                for (int i = 0; i < table.getColumns().size(); i++) {
                    System.out.println("Column = " + table.getColumns().get(i).getName() + "; value = " + row.get(i));
                }
                System.out.println("___________________________________________________________________________");
            }
        }
    }
}
