// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.query;

import com.azure.core.http.rest.Response;
import com.azure.core.util.Configuration;
import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.monitor.query.log.implementation.models.QueryResults;

import java.time.Duration;

public class ServerTimeoutSample {
    public static void main(String[] args) {

        // Create token credential
        ClientSecretCredential tokenCredential = new ClientSecretCredentialBuilder()
            .clientId(Configuration.getGlobalConfiguration().get("AZURE_MONITOR_CLIENT_ID"))
            .clientSecret(Configuration.getGlobalConfiguration().get("AZURE_MONITOR_CLIENT_SECRET"))
            .tenantId(Configuration.getGlobalConfiguration().get("AZURE_TENANT_ID"))
            .build();

        // create client
        AzureMonitorQueryClient azureLogQueryClient = new AzureMonitorQueryClientBuilder()
            .credential(tokenCredential)
            .buildClient();

        // set request options: server timeout, rendering, statistics
        QueryLogsRequestOptions options = new QueryLogsRequestOptions()
            .setServerTimeout(Duration.ofSeconds(30))
            .setIncludeRendering(true) // may not be required
            .setIncludeStatistics(true);

        // make service call with these request options set as filter header
        Response<QueryResults> response = azureLogQueryClient
            .queryLogsWithResponse("d2d0e126-fa1e-4b0a-b647-250cdd471e68", "AppRequests | take 5", options);

        // process response
        QueryResults value = response.getValue();
    }
}
