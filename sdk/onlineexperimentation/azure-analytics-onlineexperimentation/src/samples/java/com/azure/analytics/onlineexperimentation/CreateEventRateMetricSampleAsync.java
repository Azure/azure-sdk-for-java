// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.analytics.onlineexperimentation;

import com.azure.analytics.onlineexperimentation.models.*;
import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import reactor.core.publisher.Mono;

import java.util.Arrays;

/**
 * Sample for creating an event rate metric asynchronously
 */
public class CreateEventRateMetricSampleAsync {

    /**
     * Main method to demonstrate creating an event rate metric asynchronously.
     * @param args Command-line arguments
     */
    public static void main(String[] args) {
        createEventRateMetricAsync()
            .block(); // Wait for the operation to complete
    }

    /**
     * Creates an event rate metric asynchronously
     * @return A Mono containing the created metric
     */
    public static Mono<ExperimentMetric> createEventRateMetricAsync() {
        // BEGIN: com.azure.analytics.onlineexperimentation.createeventratemetricasync
        String endpoint = System.getenv("AZURE_ONLINEEXPERIMENTATION_ENDPOINT");
        DefaultAzureCredential credential = new DefaultAzureCredentialBuilder().build();

        OnlineExperimentationAsyncClient client = new OnlineExperimentationClientBuilder()
            .endpoint(endpoint)
            .credential(credential)
            .buildAsyncClient();

        // Define the Event Rate metric - measures a percentage of events meeting a condition
        ExperimentMetric relevanceMetric = new ExperimentMetric(
            LifecycleStage.ACTIVE,
            "% evaluated conversations with good relevance",
            "Percentage of evaluated conversations where the LLM response has good relevance (score >= 4)",
            Arrays.asList("Quality"),
            DesiredDirection.INCREASE,
            new EventRateMetricDefinition("EvaluateLLM", "Relevance > 4")
        );

        // Create the metric asynchronously
        return client.createOrUpdateMetric("momo_pct_relevance_good", relevanceMetric)
            .doOnNext(response -> {
                System.out.printf("Created metric: %s%n", response.getId());
            });
        // END: com.azure.analytics.onlineexperimentation.createeventratemetricasync
    }
}
