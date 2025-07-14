// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.analytics.onlineexperimentation;

import com.azure.analytics.onlineexperimentation.models.AggregatedValue;
import com.azure.analytics.onlineexperimentation.models.DesiredDirection;
import com.azure.analytics.onlineexperimentation.models.ExperimentMetric;
import com.azure.analytics.onlineexperimentation.models.LifecycleStage;
import com.azure.analytics.onlineexperimentation.models.SumMetricDefinition;
import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * Sample for creating a sum metric asynchronously
 */
public class CreateSumMetricSampleAsync {

    /**
     * Main method to demonstrate creating a sum metric asynchronously.
     *
     * @param args Command-line arguments
     */
    public static void main(String[] args) {
        // BEGIN: com.azure.analytics.onlineexperimentation.createsummetricasync
        String endpoint = System.getenv("AZURE_ONLINEEXPERIMENTATION_ENDPOINT");
        DefaultAzureCredential credential = new DefaultAzureCredentialBuilder().build();

        OnlineExperimentationAsyncClient client = new OnlineExperimentationClientBuilder()
                .endpoint(endpoint)
                .credential(credential)
                .buildAsyncClient();

        // Define the Sum metric - sums a numeric value across all events of a type
        SumMetricDefinition sumDefinition = new SumMetricDefinition()
                .setValue(new AggregatedValue().setEventName("Purchase").setEventProperty("Revenue"));
        // Add an optional filter
        sumDefinition.getValue().setFilter("Revenue > 0");

        ExperimentMetric revenueMetric = new ExperimentMetric()
                .setLifecycle(LifecycleStage.ACTIVE)
                .setDisplayName("Total revenue")
                .setDescription("Sum of revenue from all purchase transactions")
                .setCategories(Arrays.asList("Business"))
                .setDesiredDirection(DesiredDirection.INCREASE)
                .setDefinition(sumDefinition);

        // Create the metric asynchronously
        client.createOrUpdateMetric("total_revenue", revenueMetric)
                .subscribe(response -> {
                    System.out.printf("Created metric: %s%n", response.getId());
                },
                        error -> System.err.println("An error occurred while creating the metric: " + error));
        // END: com.azure.analytics.onlineexperimentation.createsummetricasync

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
