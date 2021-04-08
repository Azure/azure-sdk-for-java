// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.query;

import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.monitor.query.log.implementation.models.BatchResponse;
import com.azure.monitor.query.log.implementation.models.LogQueryResponse;

import java.util.Arrays;
import java.util.List;

public class BatchQuerySample {
    public static void main(String[] args) {
        AzureLogQueryClient azureLogQueryClient = new AzureLogQueryClientBuilder()
            .credential(new ClientSecretCredentialBuilder()
                .clientId("clientid")
                .clientSecret("clientsecret")
                .tenantId("tenantid").build())
            .buildClient();

        BatchResponse batchResponse = azureLogQueryClient.queryLogsBatch(Arrays.asList(
            "requests | count",
            "Requests | summarize count() by bin(TimeGenerated, 1h) | render barchart title='24H requests'",
            "requests | where timestamp > ago(30d) and client_City == \"Redmond\""
        ));

        List<LogQueryResponse> responses = batchResponse.getResponses();
    }
}
