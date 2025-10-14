// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.analytics.onlineexperimentation;

import com.azure.analytics.onlineexperimentation.models.AggregatedValue;
import com.azure.analytics.onlineexperimentation.models.DesiredDirection;
import com.azure.analytics.onlineexperimentation.models.ExperimentMetric;
import com.azure.analytics.onlineexperimentation.models.LifecycleStage;
import com.azure.analytics.onlineexperimentation.models.PercentileMetricDefinition;
import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;

import java.util.Arrays;

/**
 * Sample for creating a percentile metric
 */
public class CreatePercentileMetricSample {

    /**
     * Main method to demonstrate creating a percentile metric.
     *
     * @param args Command-line arguments
     */
    public static void main(String[] args) {
        // BEGIN: com.azure.analytics.onlineexperimentation.createpercentilemetric
        String endpoint = System.getenv("AZURE_ONLINEEXPERIMENTATION_ENDPOINT");
        DefaultAzureCredential credential = new DefaultAzureCredentialBuilder().build();

        OnlineExperimentationClient client = new OnlineExperimentationClientBuilder()
                .endpoint(endpoint)
                .credential(credential)
                .buildClient();

        // Define the Percentile metric - calculates a specific percentile of a numeric value
        ExperimentMetric p95ResponseTimeMetric = new ExperimentMetric()
                .setLifecycle(LifecycleStage.ACTIVE)
                .setDisplayName("P95 LLM response time [seconds]")
                .setDescription("The 95th percentile of response time in seconds for LLM responses")
                .setCategories(Arrays.asList("Performance"))
                .setDesiredDirection(DesiredDirection.DECREASE)
                .setDefinition(new PercentileMetricDefinition()
                        .setValue(new AggregatedValue().setEventName("ResponseReceived")
                                .setEventProperty("ResponseTimeSeconds"))
                        .setPercentile(95));

        // Create the metric
        ExperimentMetric response = client.createOrUpdateMetric("p95_response_time_seconds", p95ResponseTimeMetric);

        System.out.printf("Created metric: %s%n", response.getId());
        // END: com.azure.analytics.onlineexperimentation.createpercentilemetric
    }
}
