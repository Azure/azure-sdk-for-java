// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.analytics.onlineexperimentation;

import com.azure.analytics.onlineexperimentation.models.DesiredDirection;
import com.azure.analytics.onlineexperimentation.models.ExperimentMetric;
import com.azure.analytics.onlineexperimentation.models.LifecycleStage;
import com.azure.analytics.onlineexperimentation.models.ObservedEvent;
import com.azure.analytics.onlineexperimentation.models.UserRateMetricDefinition;
import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * Sample for creating a user rate metric asynchronously
 */
public class CreateUserRateMetricSampleAsync {

    /**
     * Main method to demonstrate creating a user rate metric asynchronously.
     *
     * @param args Command-line arguments
     */
    public static void main(String[] args) {
        // BEGIN: com.azure.analytics.onlineexperimentation.createuserratemetricasync
        String endpoint = System.getenv("AZURE_ONLINEEXPERIMENTATION_ENDPOINT");
        DefaultAzureCredential credential = new DefaultAzureCredentialBuilder().build();

        OnlineExperimentationAsyncClient client = new OnlineExperimentationClientBuilder()
                .endpoint(endpoint)
                .credential(credential)
                .buildAsyncClient();

        // Define the User Rate metric - measures percentage of users who performed action B after action A
        UserRateMetricDefinition userRateDefinition = new UserRateMetricDefinition()
                .setStartEvent(new ObservedEvent().setEventName("ResponseReceived"))
                .setEndEvent(new ObservedEvent().setEventName("Purchase"));
        // Add an optional filter to the end event
        userRateDefinition.getEndEvent().setFilter("Revenue > 100");

        ExperimentMetric conversionMetric = new ExperimentMetric()
                .setLifecycle(LifecycleStage.ACTIVE)
                .setDisplayName("% users with LLM interaction who made a high-value purchase")
                .setDescription(
                        "Percentage of users who received a response from the LLM and then made a purchase of $100 or more")
                .setCategories(Arrays.asList("Business"))
                .setDesiredDirection(DesiredDirection.INCREASE)
                .setDefinition(userRateDefinition);

        // Create the metric asynchronously
        client.createOrUpdateMetric("pct_chat_to_high_value_purchase_conversion", conversionMetric)
                .subscribe(response -> {
                    System.out.printf("Created metric: %s%n", response.getId());
                },
                        error -> System.err.println("An error occurred while creating the metric: " + error));
        // END: com.azure.analytics.onlineexperimentation.createuserratemetricasync

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
