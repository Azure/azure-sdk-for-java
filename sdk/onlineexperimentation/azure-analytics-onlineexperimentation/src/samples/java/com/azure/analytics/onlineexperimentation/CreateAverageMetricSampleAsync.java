// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.analytics.onlineexperimentation;

import com.azure.analytics.onlineexperimentation.models.AggregatedValue;
import com.azure.analytics.onlineexperimentation.models.AverageMetricDefinition;
import com.azure.analytics.onlineexperimentation.models.DesiredDirection;
import com.azure.analytics.onlineexperimentation.models.ExperimentMetric;
import com.azure.analytics.onlineexperimentation.models.LifecycleStage;
import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * Sample for creating an average metric asynchronously
 */
public class CreateAverageMetricSampleAsync {

    /**
     * Main method to demonstrate creating an average metric asynchronously.
     *
     * @param args Command-line arguments
     */
    public static void main(String[] args) {
        // BEGIN: com.azure.analytics.onlineexperimentation.createaveragemetricasync
        String endpoint = System.getenv("AZURE_ONLINEEXPERIMENTATION_ENDPOINT");
        DefaultAzureCredential credential = new DefaultAzureCredentialBuilder().build();

        OnlineExperimentationAsyncClient client = new OnlineExperimentationClientBuilder()
                .endpoint(endpoint)
                .credential(credential)
                .buildAsyncClient();

        // Define the Average metric - calculates the mean of a numeric value across events
        ExperimentMetric avgRevenueMetric = new ExperimentMetric()
                .setLifecycle(LifecycleStage.ACTIVE)
                .setDisplayName("Average revenue per purchase")
                .setDescription("The average revenue per purchase transaction in USD")
                .setCategories(Arrays.asList("Business"))
                .setDesiredDirection(DesiredDirection.INCREASE)
                .setDefinition(new AverageMetricDefinition()
                        .setValue(new AggregatedValue().setEventName("Purchase").setEventProperty("Revenue")));

        // Create the metric asynchronously
        client.createOrUpdateMetric("avg_revenue_per_purchase", avgRevenueMetric)
                .subscribe(response -> {
                    System.out.printf("Created metric: %s%n", response.getId());
                    System.out.printf("Display name: %s%n", response.getDisplayName());
                },
                        error -> System.err.println("An error occurred while creating the metric: " + error));
        // END: com.azure.analytics.onlineexperimentation.createaveragemetricasync

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
