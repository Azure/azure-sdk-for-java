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
import java.util.concurrent.TimeUnit;

/**
 * Sample for creating an event count metric asynchronously
 */
public class CreateEventCountMetricSampleAsync {

    /**
     * Main method to demonstrate creating an event count metric asynchronously.
     *
     * @param args Command-line arguments
     */
    public static void main(String[] args) {
        // BEGIN: com.azure.analytics.onlineexperimentation.createeventcountmetricasync
        String endpoint = System.getenv("AZURE_ONLINEEXPERIMENTATION_ENDPOINT");
        DefaultAzureCredential credential = new DefaultAzureCredentialBuilder().build();

        OnlineExperimentationAsyncClient client = new OnlineExperimentationClientBuilder()
                .endpoint(endpoint)
                .credential(credential)
                .buildAsyncClient();

        // Define the Event Count metric - counts all occurrences of a specific event type
        ExperimentMetric promptSentMetric = new ExperimentMetric()
                .setLifecycle(LifecycleStage.ACTIVE)
                .setDisplayName("Total number of prompts sent")
                .setDescription("Counts the total number of prompts sent by users to the chatbot")
                .setCategories(Arrays.asList("Usage"))
                .setDesiredDirection(DesiredDirection.INCREASE)
                .setDefinition(new EventCountMetricDefinition().setEvent(new ObservedEvent("PromptSent")));

        // Create the metric with ID "prompt_sent_count" asynchronously
        client.createOrUpdateMetric("prompt_sent_count", promptSentMetric)
                .subscribe(result -> {
                    System.out.printf("Created metric: %s%n", result.getId());
                    System.out.printf("Display name: %s%n", result.getDisplayName());
                },
                        error -> System.err
                                .println("An error occurred while creating the experiment metric: " + error));
        // END: com.azure.analytics.onlineexperimentation.createeventcountmetricasync

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
