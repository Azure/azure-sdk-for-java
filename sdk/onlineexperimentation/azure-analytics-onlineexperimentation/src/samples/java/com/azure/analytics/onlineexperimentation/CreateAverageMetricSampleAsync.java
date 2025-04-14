// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.analytics.onlineexperimentation;

import com.azure.analytics.onlineexperimentation.models.AverageMetricDefinition;
import com.azure.analytics.onlineexperimentation.models.DesiredDirection;
import com.azure.analytics.onlineexperimentation.models.ExperimentMetric;
import com.azure.analytics.onlineexperimentation.models.LifecycleStage;
import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import reactor.core.publisher.Mono;

import java.util.Arrays;

/**
 * Sample for creating an average metric asynchronously
 */
public class CreateAverageMetricSampleAsync {

    /**
     * Main method to demonstrate creating an average metric asynchronously.
     * @param args Command-line arguments
     */
    public static void main(String[] args) {
        createAverageMetricAsync()
            .block(); // Wait for the operation to complete
    }

    /**
     * Creates an average metric asynchronously
     * @return A Mono containing the created metric
     */
    public static Mono<ExperimentMetric> createAverageMetricAsync() {
        // BEGIN: com.azure.analytics.onlineexperimentation.createaveragemetricasync
        String endpoint = System.getenv("AZURE_ONLINEEXPERIMENTATION_ENDPOINT");
        DefaultAzureCredential credential = new DefaultAzureCredentialBuilder().build();

        OnlineExperimentationAsyncClient client = new OnlineExperimentationClientBuilder()
            .endpoint(endpoint)
            .credential(credential)
            .buildAsyncClient();

        // Define the Average metric - calculates the mean of a numeric value across events
        ExperimentMetric avgRevenueMetric = new ExperimentMetric(
            LifecycleStage.ACTIVE,
            "Average revenue per purchase",
            "The average revenue per purchase transaction in USD",
            Arrays.asList("Business"),
            DesiredDirection.INCREASE,
            new AverageMetricDefinition("Purchase", "Revenue")
        );

        // Create the metric asynchronously
        return client.createOrUpdateMetric("avg_revenue_per_purchase", avgRevenueMetric)
            .doOnNext(response -> {
                System.out.printf("Created metric: %s%n", response.getId());
                System.out.printf("Display name: %s%n", response.getDisplayName());
            });
        // END: com.azure.analytics.onlineexperimentation.createaveragemetricasync
    }
}
