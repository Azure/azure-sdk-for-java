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
import java.util.concurrent.TimeUnit;

/**
 * Sample for creating a user count metric asynchronously
 */
public class CreateUserCountMetricSampleAsync {

    /**
     * Main method to demonstrate creating a user count metric asynchronously.
     *
     * @param args Command-line arguments
     */
    public static void main(String[] args) {
        // BEGIN: com.azure.analytics.onlineexperimentation.createusercountmetricasync
        String endpoint = System.getenv("AZURE_ONLINEEXPERIMENTATION_ENDPOINT");
        DefaultAzureCredential credential = new DefaultAzureCredentialBuilder().build();

        OnlineExperimentationAsyncClient client = new OnlineExperimentationClientBuilder()
                .endpoint(endpoint)
                .credential(credential)
                .buildAsyncClient();

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

        // Create the metric with ID "users_prompt_sent" asynchronously
        client.createOrUpdateMetric("users_prompt_sent", usersPromptSentMetric)
                .subscribe(response -> {
                    System.out.printf("Created metric: %s%n", response.getId());
                },
                        error -> System.err.println("An error occurred while creating the metric: " + error));
        // END: com.azure.analytics.onlineexperimentation.createusercountmetricasync

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
