// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.analytics.onlineexperimentation;

import com.azure.analytics.onlineexperimentation.models.*;
import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import reactor.core.publisher.Mono;

import java.util.Arrays;

/**
 * Sample for creating an event count metric asynchronously
 */
public class CreateEventCountMetricSampleAsync {

    /**
     * Main method to demonstrate creating an event count metric asynchronously.
     * @param args Command-line arguments
     */
    public static void main(String[] args) {
        createEventCountMetricAsync()
            .block(); // Wait for the operation to complete
    }

    /**
     * Creates an event count metric asynchronously
     * @return A Mono containing the created metric
     */
    public static Mono<ExperimentMetric> createEventCountMetricAsync() {
        // BEGIN: com.azure.analytics.onlineexperimentation.createeventcountmetricasync
        String endpoint = System.getenv("AZURE_ONLINEEXPERIMENTATION_ENDPOINT");
        DefaultAzureCredential credential = new DefaultAzureCredentialBuilder().build();

        OnlineExperimentationAsyncClient client = new OnlineExperimentationClientBuilder()
            .endpoint(endpoint)
            .credential(credential)
            .buildAsyncClient();

        // Define the Event Count metric - counts all occurrences of a specific event type
        ExperimentMetric promptSentMetric = new ExperimentMetric(
            LifecycleStage.ACTIVE,
            "Total number of prompts sent",
            "Counts the total number of prompts sent by users to the chatbot",
            Arrays.asList("Usage"),
            DesiredDirection.INCREASE,
            new EventCountMetricDefinition("PromptSent")
        );

        // Create the metric with ID "prompt_sent_count" asynchronously
        return client.createOrUpdateMetric("prompt_sent_count", promptSentMetric)
            .doOnNext(response -> {
                System.out.printf("Created metric: %s%n", response.getId());
                System.out.printf("Display name: %s%n", response.getDisplayName());
            });
        // END: com.azure.analytics.onlineexperimentation.createeventcountmetricasync
    }
}
