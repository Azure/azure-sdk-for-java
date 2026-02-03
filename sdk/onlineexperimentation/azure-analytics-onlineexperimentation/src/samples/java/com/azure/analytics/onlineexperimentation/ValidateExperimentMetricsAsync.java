// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.analytics.onlineexperimentation;

import com.azure.analytics.onlineexperimentation.models.DesiredDirection;
import com.azure.analytics.onlineexperimentation.models.DiagnosticDetail;
import com.azure.analytics.onlineexperimentation.models.EventCountMetricDefinition;
import com.azure.analytics.onlineexperimentation.models.ExperimentMetric;
import com.azure.analytics.onlineexperimentation.models.LifecycleStage;
import com.azure.analytics.onlineexperimentation.models.ObservedEvent;
import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * Sample for validating experiment metrics asynchronously
 */
public class ValidateExperimentMetricsAsync {

    /**
     * Main method to demonstrate validating a metric definition asynchronously.
     *
     * @param args Command-line arguments
     */
    public static void main(String[] args) {
        // BEGIN: com.azure.analytics.onlineexperimentation.validatemetricasync
        String endpoint = System.getenv("AZURE_ONLINEEXPERIMENTATION_ENDPOINT");
        DefaultAzureCredential credential = new DefaultAzureCredentialBuilder().build();

        OnlineExperimentationAsyncClient client = new OnlineExperimentationClientBuilder()
                .endpoint(endpoint)
                .credential(credential)
                .buildAsyncClient();

        // Define a metric to validate
        ExperimentMetric metricToValidate = new ExperimentMetric()
                .setLifecycle(LifecycleStage.ACTIVE)
                .setDisplayName("Test metric for validation")
                .setDescription("This metric definition will be validated before creation")
                .setCategories(Arrays.asList("Test"))
                .setDesiredDirection(DesiredDirection.INCREASE)
                .setDefinition(new EventCountMetricDefinition()
                        .setEvent(new ObservedEvent().setEventName("TestEvent")));

        // Validate the metric - checks for errors in the definition
        client.validateMetric(metricToValidate)
                .flatMap(validationResult -> {
                    // Check if the metric definition is valid
                    if (validationResult.isValid()) {
                        System.out.println("Metric definition is valid");

                        // Now create the validated metric
                        return client.createOrUpdateMetric("test_metric_id", metricToValidate)
                                .map(createdMetric -> {
                                    System.out.printf("Created metric: %s%n", createdMetric.getId());
                                    return createdMetric;
                                });
                    } else {
                        // Handle validation errors
                        System.out.println("Metric definition has errors:");
                        for (DiagnosticDetail error : validationResult.getDiagnostics()) {
                            System.out.printf("- [%s] %s%n", error.getCode(), error.getMessage());
                        }
                        return null;
                    }
                })
                .subscribe();
        // END: com.azure.analytics.onlineexperimentation.validatemetricasync

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
