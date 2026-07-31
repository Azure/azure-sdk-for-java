// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.developer.loadtesting;

import com.azure.core.exception.ClientAuthenticationException;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.http.rest.PagedIterable;
import com.azure.developer.loadtesting.models.DailyRecurrence;
import com.azure.developer.loadtesting.models.ScheduleTestsTrigger;
import com.azure.developer.loadtesting.models.Trigger;
import com.azure.identity.DefaultAzureCredentialBuilder;

import java.time.OffsetDateTime;
import java.util.Arrays;

/**
 * Sample demonstrates how to create, get, list and delete triggers.
 */
public final class TriggerOperations {
    /**
     * Authenticates with the load testing resource and shows how to manage
     * triggers.
     *
     * @param args Unused. Arguments to the program.
     *
     * @throws ClientAuthenticationException - when the credentials have
     *                                       insufficient permissions for load test
     *                                       resource.
     * @throws ResourceNotFoundException     - when the specified resource does not
     *                                       exist.
     */
    public static void main(String[] args) {
        createOrUpdateTrigger();
        getTrigger();
        listTriggers();
        deleteTrigger();
    }

    public static void createOrUpdateTrigger() {
        LoadTestAdministrationClient client = new LoadTestAdministrationClientBuilder()
                .credential(new DefaultAzureCredentialBuilder().build())
                .endpoint("<endpoint>")
                .buildClient();

        String triggerId = "sample-trigger-id";
        String testId = "sample-test-id";

        ScheduleTestsTrigger trigger = new ScheduleTestsTrigger()
                .setDisplayName("Sample Trigger")
                .setDescription("A sample trigger that runs daily")
                .setTestIds(Arrays.asList(testId))
                .setStartDateTime(OffsetDateTime.now().plusDays(1))
                .setRecurrence(new DailyRecurrence().setInterval(1));

        Trigger response = client.createOrUpdateTrigger(triggerId, trigger);

        System.out.println("Trigger created: " + response.getTriggerId());
        System.out.println("Display Name: " + response.getDisplayName());
    }

    public static void getTrigger() {
        LoadTestAdministrationClient client = new LoadTestAdministrationClientBuilder()
                .credential(new DefaultAzureCredentialBuilder().build())
                .endpoint("<endpoint>")
                .buildClient();

        String triggerId = "sample-trigger-id";

        Trigger trigger = client.getTrigger(triggerId);

        System.out.println("Trigger ID: " + trigger.getTriggerId());
        System.out.println("Display Name: " + trigger.getDisplayName());
        System.out.println("State: " + trigger.getState());
    }

    public static void listTriggers() {
        LoadTestAdministrationClient client = new LoadTestAdministrationClientBuilder()
                .credential(new DefaultAzureCredentialBuilder().build())
                .endpoint("<endpoint>")
                .buildClient();

        PagedIterable<Trigger> triggers = client.listTriggers();

        triggers.forEach(trigger -> {
            String triggerId = trigger.getTriggerId();
            String displayName = trigger.getDisplayName();

            System.out.println(String.format("%s\t%s", triggerId, displayName));
        });
    }

    public static void deleteTrigger() {
        LoadTestAdministrationClient client = new LoadTestAdministrationClientBuilder()
                .credential(new DefaultAzureCredentialBuilder().build())
                .endpoint("<endpoint>")
                .buildClient();

        String triggerId = "sample-trigger-id";

        client.deleteTrigger(triggerId);

        System.out.println("Trigger deleted: " + triggerId);
    }
}
