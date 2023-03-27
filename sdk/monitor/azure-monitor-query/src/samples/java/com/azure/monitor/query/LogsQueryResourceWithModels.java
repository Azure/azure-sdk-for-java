// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.query;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.monitor.query.models.QueryTimeInterval;

import java.util.List;

/**
 * Sample to demonstrate using a custom model to read the results of a logs query.
 */
public class LogsQueryResourceWithModels {
    /**
     * The main method to run the sample.
     * @param args ignored args
     */
    public static void main(String[] args) {
        TokenCredential tokenCredential = new DefaultAzureCredentialBuilder().build();

        LogsQueryClient logsQueryClient = new LogsQueryClientBuilder()
            .credential(tokenCredential)
            .buildClient();

        // Sample to use a model type to read the results
        List<CustomModel> customModels  = logsQueryClient
            .queryResource("/subscriptions/{subscriptionId}/resourceGroups/{resourceGroupName}/providers/{resourceProviderNamespace}/{resourceType}/{resourceName}",
                "AppRequests", QueryTimeInterval.ALL, CustomModel.class);

        customModels.forEach(model -> System.out.println("Time generated " + model.getTimeGenerated()
            + "; success = " + model.getSuccess() + "; operation name = " + model.getOperationName()));
    }
}
