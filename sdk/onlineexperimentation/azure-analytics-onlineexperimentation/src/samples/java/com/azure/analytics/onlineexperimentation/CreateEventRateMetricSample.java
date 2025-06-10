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

/**
 * Sample for creating an event rate metric
 */
public class CreateEventRateMetricSample {

    /**
     * Main method to demonstrate creating an event rate metric.
     *
     * @param args Command-line arguments
     */
    public static void main(String[] args) {
        // BEGIN: com.azure.analytics.onlineexperimentation.createeventratemetric
        String endpoint = System.getenv("AZURE_ONLINEEXPERIMENTATION_ENDPOINT");
        DefaultAzureCredential credential = new DefaultAzureCredentialBuilder().build();

        OnlineExperimentationClient client = new OnlineExperimentationClientBuilder()
                .endpoint(endpoint)
                .credential(credential)
                .buildClient();

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

        // Create the metric
        ExperimentMetric response = client.createOrUpdateMetric("momo_pct_relevance_good", relevanceMetric);

        System.out.printf("Created metric: %s%n", response.getId());
        // END: com.azure.analytics.onlineexperimentation.createeventratemetric
    }
}
