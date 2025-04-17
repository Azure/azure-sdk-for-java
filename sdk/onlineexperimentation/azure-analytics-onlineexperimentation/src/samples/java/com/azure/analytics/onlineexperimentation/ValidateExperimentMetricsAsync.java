// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.analytics.onlineexperimentation;

import com.azure.analytics.onlineexperimentation.models.*;
import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import reactor.core.publisher.Mono;

import java.util.Arrays;

/**
 * Sample for validating experiment metrics asynchronously
 */
public class ValidateExperimentMetricsAsync {

    /**
     * Main method to demonstrate validating a metric definition asynchronously.
     * @param args Command-line arguments
     */
    public static void main(String[] args) {
        validateMetricAsync()
            .block(); // Wait for the operation to complete
    }

    /**
     * Validates a metric definition asynchronously before creation
     * @return A Mono that completes when the validation process finishes
     */
    public static Mono<Void> validateMetricAsync() {
        // BEGIN: com.azure.analytics.onlineexperimentation.validatemetricasync
        String endpoint = System.getenv("AZURE_ONLINEEXPERIMENTATION_ENDPOINT");
        DefaultAzureCredential credential = new DefaultAzureCredentialBuilder().build();

        OnlineExperimentationAsyncClient client = new OnlineExperimentationClientBuilder()
            .endpoint(endpoint)
            .credential(credential)
            .buildAsyncClient();

        // Define a metric to validate
        ExperimentMetric metricToValidate = new ExperimentMetric(
            LifecycleStage.ACTIVE,
            "Test metric for validation",
            "This metric definition will be validated before creation",
            Arrays.asList("Test"),
            DesiredDirection.INCREASE,
            new EventCountMetricDefinition("TestEvent")
        );

        // Validate the metric - checks for errors in the definition
        return client.validateMetric(metricToValidate)
            .flatMap(validationResult -> {
                // Check if the metric definition is valid
                if (validationResult.getResult().equals(ValidationResult.VALID)) {
                    System.out.println("Metric definition is valid");

                    // Now create the validated metric
                    return client.createOrUpdateMetric("test_metric_id", metricToValidate)
                        .map(createdMetric -> {
                            System.out.printf("Created metric: %s%n", createdMetric.getId());
                            return createdMetric;
                        })
                        .then();
                } else {
                    // Handle validation errors
                    System.out.println("Metric definition has errors:");
                    for (DiagnosticDetail error : validationResult.getDiagnostics()) {
                        System.out.printf("- [%s] %s%n", error.getCode(), error.getMessage());
                    }
                    return Mono.empty();
                }
            });
        // END: com.azure.analytics.onlineexperimentation.validatemetricasync
    }
}
