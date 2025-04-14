// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.analytics.onlineexperimentation;

import com.azure.analytics.onlineexperimentation.models.ExperimentMetric;
import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import reactor.core.publisher.Mono;

/**
 * Sample for updating experiment metrics asynchronously
 */
public class UpdateExperimentMetricAsync {

    /**
     * Main method to demonstrate updating a metric asynchronously.
     * @param args Command-line arguments
     */
    public static void main(String[] args) {
        updateMetricAsync()
            .block(); // Wait for the operation to complete
    }

    /**
     * Updates a metric asynchronously
     * @return A Mono containing the updated metric
     */
    public static Mono<ExperimentMetric> updateMetricAsync() {
        // BEGIN: com.azure.analytics.onlineexperimentation.updatemetricasync
        String endpoint = System.getenv("AZURE_ONLINEEXPERIMENTATION_ENDPOINT");
        DefaultAzureCredential credential = new DefaultAzureCredentialBuilder().build();

        OnlineExperimentationAsyncClient client = new OnlineExperimentationClientBuilder()
            .endpoint(endpoint)
            .credential(credential)
            .buildAsyncClient();

        // First, get the existing metric
        return client.getMetric("avg_revenue_per_purchase")
            .flatMap(existingMetric -> {
                // Update the metric - the createOrUpdate method is used for both creating and updating
                return client.updateMetric(
                    existingMetric.getId(),
                    new ExperimentMetric()
                        .setDisplayName("Average revenue per purchase [USD]")
                        .setDescription("The average revenue per purchase transaction in USD. Refund transactions are excluded."),
                    existingMetric.getETag());
            })
            .doOnNext(updatedMetric -> {
                System.out.printf("Updated metric: %s%n", updatedMetric.getId());
                System.out.printf("New display name: %s%n", updatedMetric.getDisplayName());
                System.out.printf("New description: %s%n", updatedMetric.getDescription());
            });
        // END: com.azure.analytics.onlineexperimentation.updatemetricasync
    }
}
