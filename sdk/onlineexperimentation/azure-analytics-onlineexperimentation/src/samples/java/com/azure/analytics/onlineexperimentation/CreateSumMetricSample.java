// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.analytics.onlineexperimentation;

import com.azure.analytics.onlineexperimentation.models.*;
import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;

import java.util.Arrays;

/**
 * Sample for creating a sum metric
 */
public class CreateSumMetricSample {

    /**
     * Main method to demonstrate creating a sum metric.
     * @param args Command-line arguments
     */
    public static void main(String[] args) {
        // BEGIN: com.azure.analytics.onlineexperimentation.createsummetric
        String endpoint = System.getenv("AZURE_ONLINEEXPERIMENTATION_ENDPOINT");
        DefaultAzureCredential credential = new DefaultAzureCredentialBuilder().build();

        OnlineExperimentationClient client = new OnlineExperimentationClientBuilder()
            .endpoint(endpoint)
            .credential(credential)
            .buildClient();

        // Define the Sum metric - sums a numeric value across all events of a type
        SumMetricDefinition sumDefinition = new SumMetricDefinition("Purchase", "Revenue");
        // Add an optional filter
        sumDefinition.getValue().setFilter("Revenue > 0");

        ExperimentMetric revenueMetric = new ExperimentMetric(
            LifecycleStage.ACTIVE,
            "Total revenue",
            "Sum of revenue from all purchase transactions",
            Arrays.asList("Business"),
            DesiredDirection.INCREASE,
            sumDefinition
        );

        // Create the metric
        ExperimentMetric response = client.createOrUpdateMetric("total_revenue", revenueMetric);

        System.out.printf("Created metric: %s%n", response.getId());
        // END: com.azure.analytics.onlineexperimentation.createsummetric
    }
}
