// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.analytics.onlineexperimentation;

import com.azure.analytics.onlineexperimentation.models.ExperimentMetric;
import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;

/**
 * Sample for retrieving and listing experiment metrics
 */
public class ReadExperimentMetrics {

    /**
     * Main method to demonstrate retrieving and listing metrics.
     *
     * @param args Command-line arguments
     */
    public static void main(String[] args) {
        retrieveSingleMetric();
        listAllMetrics();
    }

    /**
     * Retrieves a single metric by ID
     */
    public static void retrieveSingleMetric() {
        // BEGIN: com.azure.analytics.onlineexperimentation.retrievemetric
        String endpoint = System.getenv("AZURE_ONLINEEXPERIMENTATION_ENDPOINT");
        DefaultAzureCredential credential = new DefaultAzureCredentialBuilder().build();

        OnlineExperimentationClient client = new OnlineExperimentationClientBuilder()
                .endpoint(endpoint)
                .credential(credential)
                .buildClient();

        // Get a specific metric by ID
        ExperimentMetric metric = client.getMetric("avg_revenue_per_purchase");

        // Access metric properties to view or use the metric definition
        System.out.printf("Metric ID: %s%n", metric.getId());
        System.out.printf("Display name: %s%n", metric.getDisplayName());
        System.out.printf("Description: %s%n", metric.getDescription());
        System.out.printf("Lifecycle stage: %s%n", metric.getLifecycle());
        System.out.printf("Desired direction: %s%n", metric.getDesiredDirection());
        // END: com.azure.analytics.onlineexperimentation.retrievemetric
    }

    /**
     * Lists all metrics in the workspace
     */
    public static void listAllMetrics() {
        // BEGIN: com.azure.analytics.onlineexperimentation.listmetrics
        String endpoint = System.getenv("AZURE_ONLINEEXPERIMENTATION_ENDPOINT");
        DefaultAzureCredential credential = new DefaultAzureCredentialBuilder().build();

        OnlineExperimentationClient client = new OnlineExperimentationClientBuilder()
                .endpoint(endpoint)
                .credential(credential)
                .buildClient();

        // List all metrics in the workspace
        System.out.println("Listing all metrics:");
        for (ExperimentMetric item : client.listMetrics()) {
            System.out.printf("- %s: %s%n", item.getId(), item.getDisplayName());
        }
        // END: com.azure.analytics.onlineexperimentation.listmetrics
    }
}
