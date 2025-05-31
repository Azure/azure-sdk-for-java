// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.analytics.onlineexperimentation;

import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import reactor.core.publisher.Mono;

/**
 * Sample for deleting experiment metrics asynchronously
 */
public class DeleteExperimentMetricAsync {

    /**
     * Main method to demonstrate deleting a metric asynchronously.
     * @param args Command-line arguments
     */
    public static void main(String[] args) {
        deleteMetricAsync()
            .block(); // Wait for the operation to complete
    }

    /**
     * Deletes a metric asynchronously
     * @return A Mono representing the completion of the delete operation
     */
    public static Mono<Void> deleteMetricAsync() {
        // BEGIN: com.azure.analytics.onlineexperimentation.deletemetricasync
        String endpoint = System.getenv("AZURE_ONLINEEXPERIMENTATION_ENDPOINT");
        DefaultAzureCredential credential = new DefaultAzureCredentialBuilder().build();

        OnlineExperimentationAsyncClient client = new OnlineExperimentationClientBuilder()
            .endpoint(endpoint)
            .credential(credential)
            .buildAsyncClient();

        // Delete a metric by ID - removes it from the workspace
        return client.deleteMetric("test_metric_id")
            .doOnSuccess(unused -> {
                System.out.println("Metric deleted successfully");
            });
        // END: com.azure.analytics.onlineexperimentation.deletemetricasync
    }
}
