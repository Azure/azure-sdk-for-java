// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.analytics.onlineexperimentation;

import com.azure.analytics.onlineexperimentation.models.ExperimentMetric;
import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;

/**
 * Sample for updating experiment metrics
 */
public class UpdateExperimentMetric {

    /**
     * Main method to demonstrate updating a metric.
     *
     * @param args Command-line arguments
     */
    public static void main(String[] args) {
        updateMetric();
    }

    /**
     * Updates a metric
     */
    public static void updateMetric() {
        // BEGIN: com.azure.analytics.onlineexperimentation.updatemetric
        String endpoint = System.getenv("AZURE_ONLINEEXPERIMENTATION_ENDPOINT");
        DefaultAzureCredential credential = new DefaultAzureCredentialBuilder().build();

        OnlineExperimentationClient client = new OnlineExperimentationClientBuilder()
                .endpoint(endpoint)
                .credential(credential)
                .buildClient();

        // First, get the existing metric
        ExperimentMetric existingMetric = client.getMetric("avg_revenue_per_purchase");

        // Update the display name and description of the metric, other fields remain unchanged.
        ExperimentMetric updatedMetric = client.updateMetric(existingMetric.getId(),
                new ExperimentMetric()
                        .setDisplayName("Average revenue per purchase [USD]")
                        .setDescription(
                                "The average revenue per purchase transaction in USD. Refund transactions are excluded."),
                existingMetric.getETag());

        System.out.printf("Updated metric: %s%n", updatedMetric.getId());
        System.out.printf("New display name: %s%n", updatedMetric.getDisplayName());
        System.out.printf("New description: %s%n", updatedMetric.getDescription());
        // END: com.azure.analytics.onlineexperimentation.updatemetric
    }
}
