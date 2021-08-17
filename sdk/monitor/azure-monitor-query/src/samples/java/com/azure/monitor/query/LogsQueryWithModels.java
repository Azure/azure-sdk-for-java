// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.query;

import com.azure.core.util.Configuration;
import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.monitor.query.models.LogsQueryResult;

import java.util.List;

/**
 * Sample to demonstrate using a custom model to read the results of a logs query.
 */
public class LogsQueryWithModels {

    /**
     * The main method to run the sample.
     * @param args ignored args
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

        LogsQueryResult queryResults = logsQueryClient
                .query("{workspace-id}", "AppRequests", null);

        // Sample to use a model type to read the results
        List<CustomModel> customModels = queryResults.toObject(CustomModel.class);
        customModels.forEach(model -> System.out.println("Time generated " + model.getTimeGenerated()
                + "; success = " + model.getSuccess() + "; operation name = " + model.getOperationName()));
    }

}
