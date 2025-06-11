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
import java.util.concurrent.TimeUnit;

/**
 * Sample for creating a percentile metric asynchronously
 */
public class CreatePercentileMetricSampleAsync {

    /**
     * Main method to demonstrate creating a percentile metric asynchronously.
     *
     * @param args Command-line arguments
     */
    public static void main(String[] args) {
        // BEGIN: com.azure.analytics.onlineexperimentation.createpercentilemetricasync
        String endpoint = System.getenv("AZURE_ONLINEEXPERIMENTATION_ENDPOINT");
        DefaultAzureCredential credential = new DefaultAzureCredentialBuilder().build();

        OnlineExperimentationAsyncClient client = new OnlineExperimentationClientBuilder()
                .endpoint(endpoint)
                .credential(credential)
                .buildAsyncClient();

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

        // Create the metric asynchronously
        client.createOrUpdateMetric("p95_response_time_seconds", p95ResponseTimeMetric)
                .subscribe(response -> {
                    System.out.printf("Created metric: %s%n", response.getId());
                },
                        error -> System.err.println("An error occurred while creating the metric: " + error));
        // END: com.azure.analytics.onlineexperimentation.createpercentilemetricasync

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
