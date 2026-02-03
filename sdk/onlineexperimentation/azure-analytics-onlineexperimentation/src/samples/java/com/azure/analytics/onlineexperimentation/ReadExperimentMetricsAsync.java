// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.analytics.onlineexperimentation;

import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;

import java.util.concurrent.TimeUnit;

/**
 * Sample for retrieving and listing experiment metrics asynchronously
 */
public class ReadExperimentMetricsAsync {

    /**
     * Main method to demonstrate retrieving and listing metrics asynchronously.
     *
     * @param args Command-line arguments
     */
    public static void main(String[] args) {
        // BEGIN: com.azure.analytics.onlineexperimentation.retrievemetricasync
        String endpoint = System.getenv("AZURE_ONLINEEXPERIMENTATION_ENDPOINT");
        DefaultAzureCredential credential = new DefaultAzureCredentialBuilder().build();

        OnlineExperimentationAsyncClient client = new OnlineExperimentationClientBuilder()
                .endpoint(endpoint)
                .credential(credential)
                .buildAsyncClient();

        // Get a specific metric by ID asynchronously
        System.out.println("Retrieving single metric:");
        client.getMetric("avg_revenue_per_purchase")
                .subscribe(metric -> {
                    // Access metric properties to view or use the metric definition
                    System.out.printf("Metric ID: %s%n", metric.getId());
                    System.out.printf("Display name: %s%n", metric.getDisplayName());
                    System.out.printf("Description: %s%n", metric.getDescription());
                    System.out.printf("Lifecycle stage: %s%n", metric.getLifecycle());
                    System.out.printf("Desired direction: %s%n", metric.getDesiredDirection());
                },
                        error -> System.err.println("An error occurred while retrieving the metric: " + error));
        // END: com.azure.analytics.onlineexperimentation.retrievemetricasync

        // BEGIN: com.azure.analytics.onlineexperimentation.listmetricsasync
        // List all metrics in the workspace asynchronously
        System.out.println("Listing all metrics:");
        client.listMetrics()
                .subscribe(item -> {
                    System.out.printf("- %s: %s%n", item.getId(), item.getDisplayName());
                },
                        error -> System.err.println("An error occurred while listing metrics: " + error));
        // END: com.azure.analytics.onlineexperimentation.listmetricsasync

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
