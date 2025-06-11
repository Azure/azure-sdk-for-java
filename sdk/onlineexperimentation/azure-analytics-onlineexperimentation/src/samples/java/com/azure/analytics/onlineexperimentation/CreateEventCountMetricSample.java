// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.analytics.onlineexperimentation;

import com.azure.analytics.onlineexperimentation.models.DesiredDirection;
import com.azure.analytics.onlineexperimentation.models.EventCountMetricDefinition;
import com.azure.analytics.onlineexperimentation.models.ExperimentMetric;
import com.azure.analytics.onlineexperimentation.models.LifecycleStage;
import com.azure.analytics.onlineexperimentation.models.ObservedEvent;
import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;

import java.util.Arrays;

/**
 * Sample for creating an event count metric
 */
public class CreateEventCountMetricSample {

    /**
     * Main method to demonstrate creating an event count metric.
     *
     * @param args Command-line arguments
     */
    public static void main(String[] args) {
        // BEGIN: com.azure.analytics.onlineexperimentation.createeventcountmetric
        String endpoint = System.getenv("AZURE_ONLINEEXPERIMENTATION_ENDPOINT");
        DefaultAzureCredential credential = new DefaultAzureCredentialBuilder().build();

        OnlineExperimentationClient client = new OnlineExperimentationClientBuilder()
                .endpoint(endpoint)
                .credential(credential)
                .buildClient();

        // Define the Event Count metric - counts all occurrences of a specific event type
        ExperimentMetric promptSentMetric = new ExperimentMetric()
                .setLifecycle(LifecycleStage.ACTIVE)
                .setDisplayName("Total number of prompts sent")
                .setDescription("Counts the total number of prompts sent by users to the chatbot")
                .setCategories(Arrays.asList("Usage"))
                .setDesiredDirection(DesiredDirection.INCREASE)
                .setDefinition(new EventCountMetricDefinition().setEvent(new ObservedEvent("PromptSent")));

        // Create the metric with ID "prompt_sent_count"
        ExperimentMetric response = client.createOrUpdateMetric("prompt_sent_count", promptSentMetric);

        System.out.printf("Created metric: %s%n", response.getId());
        System.out.printf("Display name: %s%n", response.getDisplayName());
        // END: com.azure.analytics.onlineexperimentation.createeventcountmetric
    }
}
