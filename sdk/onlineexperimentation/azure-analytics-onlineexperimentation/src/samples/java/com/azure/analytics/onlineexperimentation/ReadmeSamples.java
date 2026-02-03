// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.analytics.onlineexperimentation;

import java.util.Arrays;
import java.util.UUID;

import com.azure.analytics.onlineexperimentation.models.DesiredDirection;
import com.azure.analytics.onlineexperimentation.models.DiagnosticDetail;
import com.azure.analytics.onlineexperimentation.models.EventRateMetricDefinition;
import com.azure.analytics.onlineexperimentation.models.ExperimentMetric;
import com.azure.analytics.onlineexperimentation.models.ExperimentMetricValidationResult;
import com.azure.analytics.onlineexperimentation.models.LifecycleStage;
import com.azure.analytics.onlineexperimentation.models.ObservedEvent;
import com.azure.core.http.RequestConditions;
import com.azure.identity.DefaultAzureCredentialBuilder;

public final class ReadmeSamples {
    public void experimentMetricLifecycle() {
        // BEGIN: com.azure.analytics.onlineexperimentation.readme
        // [Step 1] Initialize the SDK client
        // The endpoint URL from the Microsoft.OnlineExperimentation/workspaces resource
        String endpoint = System.getenv("AZURE_ONLINEEXPERIMENTATION_ENDPOINT");

        System.out.printf("AZURE_ONLINEEXPERIMENTATION_ENDPOINT is %s%n", endpoint);

        OnlineExperimentationClient client = new OnlineExperimentationClientBuilder()
                .endpoint(endpoint)
                .credential(new DefaultAzureCredentialBuilder().build())
                .buildClient();

        // [Step 2] Define the experiment metric
        ExperimentMetric exampleMetric = new ExperimentMetric()
                .setLifecycle(LifecycleStage.ACTIVE)
                .setDisplayName("% users with LLM interaction who made a high-value purchase")
                .setDescription(
                        "Percentage of users who received a response from the LLM and then made a purchase of $100 or more")
                .setCategories(Arrays.asList("Business"))
                .setDesiredDirection(DesiredDirection.INCREASE)
                .setDefinition(new EventRateMetricDefinition()
                        .setEvent(new ObservedEvent().setEventName("ResponseReceived"))
                        .setRateCondition("Revenue > 100"));

        // [Optional][Step 2a] Validate the metric - checks for input errors without persisting anything.
        System.out.println("Checking if the experiment metric definition is valid...");

        ExperimentMetricValidationResult validationResult = client.validateMetric(exampleMetric);

        System.out.printf("Experiment metric definition valid: %s.%n", validationResult.isValid());
        if (validationResult.getDiagnostics() != null) {
            for (DiagnosticDetail detail : validationResult.getDiagnostics()) {
                // Inspect details of why the metric definition was rejected as Invalid.
                System.out.printf("- %s: %s%n", detail.getCode(), detail.getMessage());
            }
        }

        if (!validationResult.isValid()) {
            System.out.println("Metric validation failed. Exiting sample.");
            return;
        }

        // [Step 3] Create the experiment metric
        String exampleMetricId = "sample_metric_id_" + UUID.randomUUID().toString().replace("-", "");

        System.out.printf("Creating the experiment metric %s...%n", exampleMetricId);

        // Create with If-None-Match to ensure no one else created this metric in the meantime
        RequestConditions createConditions = new RequestConditions().setIfNoneMatch("*");
        ExperimentMetric createdMetric = client.createOrUpdateMetric(exampleMetricId, exampleMetric, createConditions);

        System.out.printf("Experiment metric %s created, etag: %s.%n", createdMetric.getId(), createdMetric.getETag());

        // [Step 4] Deactivate the experiment metric and update the description.
        ExperimentMetric updateRequest = new ExperimentMetric()
                .setLifecycle(LifecycleStage.INACTIVE) // pauses computation of this metric
                .setDescription("No longer need to compute this.");

        // Update with If-Match to ensure no one else updated the metric in the meantime
        RequestConditions updateConditions = new RequestConditions().setIfMatch(createdMetric.getETag());
        ExperimentMetric updatedMetric = client.createOrUpdateMetric(exampleMetricId, updateRequest, updateConditions);

        System.out.printf("Updated metric: %s, etag: %s.%n", updatedMetric.getId(), updatedMetric.getETag());

        // [Step 5] Delete the experiment metric.
        RequestConditions deleteConditions = new RequestConditions().setIfMatch(updatedMetric.getETag());
        client.deleteMetric(exampleMetricId, deleteConditions);

        System.out.printf("Deleted metric: %s.%n", exampleMetricId);
        // END: com.azure.analytics.onlineexperimentation.readme
    }
}
