// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.analytics.onlineexperimentation;

import com.azure.analytics.onlineexperimentation.models.ExperimentMetric;
import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;

import java.util.concurrent.TimeUnit;

/**
 * Sample for updating experiment metrics asynchronously
 */
public class UpdateExperimentMetricAsync {

    /**
     * Main method to demonstrate updating a metric asynchronously.
     *
     * @param args Command-line arguments
     */
    public static void main(String[] args) {
        // BEGIN: com.azure.analytics.onlineexperimentation.updatemetricasync
        String endpoint = System.getenv("AZURE_ONLINEEXPERIMENTATION_ENDPOINT");
        DefaultAzureCredential credential = new DefaultAzureCredentialBuilder().build();

        OnlineExperimentationAsyncClient client = new OnlineExperimentationClientBuilder()
                .endpoint(endpoint)
                .credential(credential)
                .buildAsyncClient();

        // First, get the existing metric
        client.getMetric("avg_revenue_per_purchase")
                .flatMap(existingMetric -> {
                    // Update the metric - the createOrUpdate method is used for both creating and updating
                    return client.updateMetric(
                            existingMetric.getId(),
                            new ExperimentMetric()
                                    .setDisplayName("Average revenue per purchase [USD]")
                                    .setDescription(
                                            "The average revenue per purchase transaction in USD. Refund transactions are excluded."),
                            existingMetric.getETag());
                })
                .subscribe(updatedMetric -> {
                    System.out.printf("Updated metric: %s%n", updatedMetric.getId());
                    System.out.printf("New display name: %s%n", updatedMetric.getDisplayName());
                    System.out.printf("New description: %s%n", updatedMetric.getDescription());
                },
                        error -> System.err.println("An error occurred while updating the metric: " + error));
        // END: com.azure.analytics.onlineexperimentation.updatemetricasync

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
