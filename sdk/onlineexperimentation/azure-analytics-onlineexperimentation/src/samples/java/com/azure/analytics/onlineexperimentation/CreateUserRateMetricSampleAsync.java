// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.analytics.onlineexperimentation;

import com.azure.analytics.onlineexperimentation.models.DesiredDirection;
import com.azure.analytics.onlineexperimentation.models.ExperimentMetric;
import com.azure.analytics.onlineexperimentation.models.LifecycleStage;
import com.azure.analytics.onlineexperimentation.models.UserRateMetricDefinition;
import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import reactor.core.publisher.Mono;

import java.util.Arrays;

/**
 * Sample for creating a user rate metric asynchronously
 */
public class CreateUserRateMetricSampleAsync {

    /**
     * Main method to demonstrate creating a user rate metric asynchronously.
     * @param args Command-line arguments
     */
    public static void main(String[] args) {
        createUserRateMetricAsync()
            .block(); // Wait for the operation to complete
    }

    /**
     * Creates a user rate metric asynchronously
     * @return A Mono containing the created metric
     */
    public static Mono<ExperimentMetric> createUserRateMetricAsync() {
        // BEGIN: com.azure.analytics.onlineexperimentation.createuserratemetricasync
        String endpoint = System.getenv("AZURE_ONLINEEXPERIMENTATION_ENDPOINT");
        DefaultAzureCredential credential = new DefaultAzureCredentialBuilder().build();

        OnlineExperimentationAsyncClient client = new OnlineExperimentationClientBuilder()
            .endpoint(endpoint)
            .credential(credential)
            .buildAsyncClient();

        // Define the User Rate metric - measures percentage of users who performed action B after action A
        UserRateMetricDefinition userRateDefinition = new UserRateMetricDefinition("ResponseReceived", "Purchase");
        // Add an optional filter to the end event
        userRateDefinition.getEndEvent().setFilter("Revenue > 100");

        ExperimentMetric conversionMetric = new ExperimentMetric(
            LifecycleStage.ACTIVE,
            "% users with LLM interaction who made a high-value purchase",
            "Percentage of users who received a response from the LLM and then made a purchase of $100 or more",
            Arrays.asList("Business"),
            DesiredDirection.INCREASE,
            userRateDefinition
        );

        // Create the metric asynchronously
        return client.createOrUpdateMetric("pct_chat_to_high_value_purchase_conversion", conversionMetric)
            .doOnNext(response -> {
                System.out.printf("Created metric: %s%n", response.getId());
            });
        // END: com.azure.analytics.onlineexperimentation.createuserratemetricasync
    }
}
