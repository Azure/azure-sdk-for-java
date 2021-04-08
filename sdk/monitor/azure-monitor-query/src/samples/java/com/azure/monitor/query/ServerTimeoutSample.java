// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.query;

import com.azure.core.http.rest.Response;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.monitor.query.log.implementation.models.QueryResults;

import java.time.Duration;

public class ServerTimeoutSample {
    public static void main(String[] args) {
        // create client
        AzureLogQueryClient azureLogQueryClient = new AzureLogQueryClientBuilder()
            .credential(new ClientSecretCredentialBuilder()
                .clientId("clientid")
                .clientSecret("clientsecret")
                .tenantId("tenantid").build())
            .buildClient();

        // set request options: server timeout, rendering, statistics
        QueryLogsRequestOptions options = new QueryLogsRequestOptions()
            .setServerTimeout(Duration.ofSeconds(30))
            .setIncludeRendering(true) // may not be required
            .setIncludeStatistics(true);

        // make service call with these request options set as filter header
        Response<QueryResults> response = azureLogQueryClient
            .queryLogsWithResponse("{workspace-id}", "requests | take 5", options);

        // process response
        QueryResults value = response.getValue();
    }
}
