// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.analytics.onlineexperimentation;

import com.azure.analytics.onlineexperimentation.models.DesiredDirection;
import com.azure.analytics.onlineexperimentation.models.EventRateMetricDefinition;
import com.azure.analytics.onlineexperimentation.models.ExperimentMetric;
import com.azure.analytics.onlineexperimentation.models.LifecycleStage;
import com.azure.analytics.onlineexperimentation.models.ObservedEvent;
import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * Sample for creating an event rate metric asynchronously
 */
public class CreateEventRateMetricSampleAsync {

    /**
     * Main method to demonstrate creating an event rate metric asynchronously.
     *
     * @param args Command-line arguments
     */
    public static void main(String[] args) {
        // BEGIN: com.azure.analytics.onlineexperimentation.createeventratemetricasync
        String endpoint = System.getenv("AZURE_ONLINEEXPERIMENTATION_ENDPOINT");
        DefaultAzureCredential credential = new DefaultAzureCredentialBuilder().build();

        OnlineExperimentationAsyncClient client = new OnlineExperimentationClientBuilder()
                .endpoint(endpoint)
                .credential(credential)
                .buildAsyncClient();

        // Define the Event Rate metric - measures a percentage of events meeting a condition
        ExperimentMetric relevanceMetric = new ExperimentMetric()
                .setLifecycle(LifecycleStage.ACTIVE)
                .setDisplayName("% evaluated conversations with good relevance")
                .setDescription(
                        "Percentage of evaluated conversations where the LLM response has good relevance (score >= 4)")
                .setCategories(Arrays.asList("Quality"))
                .setDesiredDirection(DesiredDirection.INCREASE)
                .setDefinition(new EventRateMetricDefinition().setEvent(new ObservedEvent("EvaluateLLM"))
                        .setRateCondition("Relevance > 4"));

        // Create the metric asynchronously
        client.createOrUpdateMetric("momo_pct_relevance_good", relevanceMetric)
                .subscribe(response -> {
                    System.out.printf("Created metric: %s%n", response.getId());
                },
                        error -> System.err.println("An error occurred while creating the metric: " + error));
        // END: com.azure.analytics.onlineexperimentation.createeventratemetricasync

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
