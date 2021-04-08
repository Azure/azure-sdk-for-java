// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.query;

import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.monitor.query.log.implementation.models.QueryResults;

public class AzureLogQueryClientSample {

    public static void main(String[] args) {
        AzureLogQueryClient azureLogQueryClient = new AzureLogQueryClientBuilder()
            .credential(new ClientSecretCredentialBuilder()
                .clientId("clientid")
                .clientSecret("clientsecret")
                .tenantId("tenantid").build())
            .buildClient();

        QueryResults queryResults = azureLogQueryClient.queryLogs("d2d0e126-fa1e-4b0a-b647-250cdd471e68", "AppRequests");
        System.out.println(queryResults.getTables().size());
        System.out.println(queryResults.getTables().get(0).getRows().size());
        System.out.println(queryResults.getTables().get(0).getColumns().size());
        queryResults.getTables().get(0).getColumns().stream().map(column -> column.getName()).sorted().forEach(System.out::println);
    }
}
