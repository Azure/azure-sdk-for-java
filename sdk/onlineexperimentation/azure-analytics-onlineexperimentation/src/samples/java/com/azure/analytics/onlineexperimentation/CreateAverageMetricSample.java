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

/**
 * Sample for creating an average metric
 */
public class CreateAverageMetricSample {

    /**
     * Main method to demonstrate creating an average metric.
     *
     * @param args Command-line arguments
     */
    public static void main(String[] args) {
        // BEGIN: com.azure.analytics.onlineexperimentation.createaveragemetric
        String endpoint = System.getenv("AZURE_ONLINEEXPERIMENTATION_ENDPOINT");
        DefaultAzureCredential credential = new DefaultAzureCredentialBuilder().build();

        OnlineExperimentationClient client = new OnlineExperimentationClientBuilder()
                .endpoint(endpoint)
                .credential(credential)
                .buildClient();

        // Define the Average metric - calculates the mean of a numeric value across events
        ExperimentMetric avgRevenueMetric = new ExperimentMetric()
                .setLifecycle(LifecycleStage.ACTIVE)
                .setDisplayName("Average revenue per purchase")
                .setDescription("The average revenue per purchase transaction in USD")
                .setCategories(Arrays.asList("Business"))
                .setDesiredDirection(DesiredDirection.INCREASE)
                .setDefinition(new AverageMetricDefinition()
                        .setValue(new AggregatedValue().setEventName("Purchase").setEventProperty("Revenue")));

        // Create the metric
        ExperimentMetric response = client.createOrUpdateMetric("avg_revenue_per_purchase", avgRevenueMetric);

        System.out.printf("Created metric: %s%n", response.getId());
        System.out.printf("Display name: %s%n", response.getDisplayName());
        // END: com.azure.analytics.onlineexperimentation.createaveragemetric
    }
}
