// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.analytics.onlineexperimentation;

import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import reactor.core.publisher.Mono;

/**
 * Sample for retrieving and listing experiment metrics asynchronously
 */
public class ReadExperimentMetricsAsync {

    /**
     * Main method to demonstrate retrieving and listing metrics asynchronously.
     * @param args Command-line arguments
     */
    public static void main(String[] args) {
        retrieveSingleMetricAsync()
            .block();

        listAllMetricsAsync()
            .block();
    }

    /**
     * Retrieves a single metric by ID asynchronously
     * @return A Mono that completes when the operation is finished
     */
    public static Mono<Void> retrieveSingleMetricAsync() {
        // BEGIN: com.azure.analytics.onlineexperimentation.retrievemetricasync
        String endpoint = System.getenv("AZURE_ONLINEEXPERIMENTATION_ENDPOINT");
        DefaultAzureCredential credential = new DefaultAzureCredentialBuilder().build();

        OnlineExperimentationAsyncClient client = new OnlineExperimentationClientBuilder()
            .endpoint(endpoint)
            .credential(credential)
            .buildAsyncClient();

        // Get a specific metric by ID asynchronously
        return client.getMetric("avg_revenue_per_purchase")
            .doOnNext(metric -> {
                // Access metric properties to view or use the metric definition
                System.out.printf("Metric ID: %s%n", metric.getId());
                System.out.printf("Display name: %s%n", metric.getDisplayName());
                System.out.printf("Description: %s%n", metric.getDescription());
                System.out.printf("Lifecycle stage: %s%n", metric.getLifecycle());
                System.out.printf("Desired direction: %s%n", metric.getDesiredDirection());
            })
            .then();
        // END: com.azure.analytics.onlineexperimentation.retrievemetricasync
    }

    /**
     * Lists all metrics in the workspace asynchronously
     * @return A Mono that completes when the operation is finished
     */
    public static Mono<Void> listAllMetricsAsync() {
        // BEGIN: com.azure.analytics.onlineexperimentation.listmetricsasync
        String endpoint = System.getenv("AZURE_ONLINEEXPERIMENTATION_ENDPOINT");
        DefaultAzureCredential credential = new DefaultAzureCredentialBuilder().build();

        OnlineExperimentationAsyncClient client = new OnlineExperimentationClientBuilder()
            .endpoint(endpoint)
            .credential(credential)
            .buildAsyncClient();

        // List all metrics in the workspace asynchronously
        System.out.println("Listing all metrics:");
        return client.listMetrics()
            .doOnNext(item -> {
                System.out.printf("- %s: %s%n", item.getId(), item.getDisplayName());
            })
            .then();
        // END: com.azure.analytics.onlineexperimentation.listmetricsasync
    }
}
