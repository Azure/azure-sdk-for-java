// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.analytics.onlineexperimentation;

import com.azure.analytics.onlineexperimentation.models.DesiredDirection;
import com.azure.analytics.onlineexperimentation.models.ExperimentMetric;
import com.azure.analytics.onlineexperimentation.models.LifecycleStage;
import com.azure.analytics.onlineexperimentation.models.ObservedEvent;
import com.azure.analytics.onlineexperimentation.models.UserCountMetricDefinition;
import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;

import java.util.Arrays;

/**
 * Sample for creating a user count metric
 */
public class CreateUserCountMetricSample {

    /**
     * Main method to demonstrate creating a user count metric.
     *
     * @param args Command-line arguments
     */
    public static void main(String[] args) {
        // BEGIN: com.azure.analytics.onlineexperimentation.createusercountmetric
        String endpoint = System.getenv("AZURE_ONLINEEXPERIMENTATION_ENDPOINT");
        DefaultAzureCredential credential = new DefaultAzureCredentialBuilder().build();

        OnlineExperimentationClient client = new OnlineExperimentationClientBuilder()
                .endpoint(endpoint)
                .credential(credential)
                .buildClient();

        // Define the User Count metric with a filter - counts unique users who performed a specific action
        UserCountMetricDefinition userCountDefinition = new UserCountMetricDefinition()
                .setEvent(new ObservedEvent().setEventName("PromptSent"));
        // Add an optional filter
        userCountDefinition.getEvent().setFilter("Page == 'checkout.html'");

        ExperimentMetric usersPromptSentMetric = new ExperimentMetric()
                .setLifecycle(LifecycleStage.ACTIVE)
                .setDisplayName("Users with at least one prompt sent on checkout page")
                .setDescription("Counts unique users who sent at least one prompt while on the checkout page")
                .setCategories(Arrays.asList("Usage"))
                .setDesiredDirection(DesiredDirection.INCREASE)
                .setDefinition(userCountDefinition);

        // Create the metric with ID "users_prompt_sent"
        ExperimentMetric response = client.createOrUpdateMetric("users_prompt_sent", usersPromptSentMetric);

        System.out.printf("Created metric: %s%n", response.getId());
        // END: com.azure.analytics.onlineexperimentation.createusercountmetric
    }
}
