// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.analytics.onlineexperimentation;

import com.azure.analytics.onlineexperimentation.models.DesiredDirection;
import com.azure.analytics.onlineexperimentation.models.DiagnosticDetail;
import com.azure.analytics.onlineexperimentation.models.EventCountMetricDefinition;
import com.azure.analytics.onlineexperimentation.models.ExperimentMetric;
import com.azure.analytics.onlineexperimentation.models.ExperimentMetricValidationResult;
import com.azure.analytics.onlineexperimentation.models.LifecycleStage;
import com.azure.analytics.onlineexperimentation.models.ObservedEvent;
import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;

import java.util.Arrays;

/**
 * Sample for validating experiment metrics
 */
public class ValidateExperimentMetrics {

    /**
     * Main method to demonstrate validating a metric definition.
     *
     * @param args Command-line arguments
     */
    public static void main(String[] args) {
        validateMetric();
    }

    /**
     * Validates a metric definition before creation
     */
    public static void validateMetric() {
        // BEGIN: com.azure.analytics.onlineexperimentation.validatemetric
        String endpoint = System.getenv("AZURE_ONLINEEXPERIMENTATION_ENDPOINT");
        DefaultAzureCredential credential = new DefaultAzureCredentialBuilder().build();

        OnlineExperimentationClient client = new OnlineExperimentationClientBuilder()
                .endpoint(endpoint)
                .credential(credential)
                .buildClient();

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
        ExperimentMetricValidationResult validationResult = client.validateMetric(metricToValidate);

        // Check if the metric definition is valid
        if (validationResult.isValid()) {
            System.out.println("Metric definition is valid");

            // Now create the validated metric
            ExperimentMetric createdMetric = client.createOrUpdateMetric("test_metric_id", metricToValidate);
            System.out.printf("Created metric: %s%n", createdMetric.getId());
        } else {
            // Handle validation errors
            System.out.println("Metric definition has errors:");
            for (DiagnosticDetail error : validationResult.getDiagnostics()) {
                System.out.printf("- [%s] %s%n", error.getCode(), error.getMessage());
            }
        }
        // END: com.azure.analytics.onlineexperimentation.validatemetric
    }
}
