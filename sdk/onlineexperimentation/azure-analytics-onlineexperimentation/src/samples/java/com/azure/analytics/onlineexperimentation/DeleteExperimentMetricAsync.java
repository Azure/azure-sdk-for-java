// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.analytics.onlineexperimentation;

import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;

import java.util.concurrent.TimeUnit;

/**
 * Sample for deleting experiment metrics asynchronously
 */
public class DeleteExperimentMetricAsync {

    /**
     * Main method to demonstrate deleting a metric asynchronously.
     *
     * @param args Command-line arguments
     */
    public static void main(String[] args) {
        // BEGIN: com.azure.analytics.onlineexperimentation.deletemetricasync
        String endpoint = System.getenv("AZURE_ONLINEEXPERIMENTATION_ENDPOINT");
        DefaultAzureCredential credential = new DefaultAzureCredentialBuilder().build();

        OnlineExperimentationAsyncClient client = new OnlineExperimentationClientBuilder()
                .endpoint(endpoint)
                .credential(credential)
                .buildAsyncClient();

        // Delete a metric by ID - removes it from the workspace
        client.deleteMetric("test_metric_id")
                .subscribe(result -> System.out.println("Metric deleted successfully"),
                        error -> System.err.println("An error occurred while deleting the metric: " + error));
        // END: com.azure.analytics.onlineexperimentation.deletemetricasync

        // The .subscribe() creation and assignment is not a blocking call. For the purpose of this example, we sleep
        // the thread so the program does not end before the send operation is complete. Using .block() instead of
        // .subscribe() would turn this into a synchronous call.
        try {
            TimeUnit.MINUTES.sleep(5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
