// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.projects;

import com.azure.ai.projects.models.RoutineAction;
import com.azure.ai.projects.models.RoutineRun;
import com.azure.ai.projects.models.RoutineTrigger;
import com.azure.ai.projects.models.ScheduleRoutineTrigger;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Sample demonstrating a routine that fires on a recurring cron schedule using the synchronous
 * {@link BetaRoutinesClient}.
 *
 * <p>The routine is configured with a {@link ScheduleRoutineTrigger} that fires every five minutes. The sample
 * then polls the routine runs until one completes. Routines are a preview feature. Before running, set:</p>
 * <ul>
 *   <li>{@code FOUNDRY_PROJECT_ENDPOINT} - the Azure AI Foundry project endpoint.</li>
 *   <li>{@code HOSTED_AGENT_NAME} - the name of a deployed hosted agent.</li>
 * </ul>
 */
public class RoutinesScheduleTriggerSample {
    private static final String ROUTINE_NAME = "sample-routine";
    private static final Duration RUN_TIMEOUT = Duration.ofMinutes(6);

    public static void main(String[] args) {
        Configuration configuration = Configuration.getGlobalConfiguration();
        String endpoint = configuration.get("FOUNDRY_PROJECT_ENDPOINT");
        String agentName = configuration.get("HOSTED_AGENT_NAME");

        BetaRoutinesClient routinesClient = new AIProjectClientBuilder()
            .endpoint(endpoint)
            .credential(new DefaultAzureCredentialBuilder().build())
            .beta()
            .buildBetaRoutinesClient();

        // Clean up any pre-existing routine with the same name.
        try {
            routinesClient.deleteRoutine(ROUTINE_NAME);
        } catch (RuntimeException ignored) {
            // The sample routine does not already exist.
        }

        try {
            // BEGIN:com.azure.ai.projects.RoutinesScheduleTriggerSample.createRoutine
            RoutineAction action = RoutinesSampleUtils.agentAction(agentName);

            ScheduleRoutineTrigger trigger = new ScheduleRoutineTrigger("*/5 * * * *", "UTC");
            Map<String, RoutineTrigger> triggers = new HashMap<>();
            triggers.put("every_five_minutes", trigger);

            com.azure.ai.projects.models.Routine created = routinesClient.createOrUpdateRoutine(ROUTINE_NAME,
                "Routine used by the schedule-trigger sample.", true, triggers, action);
            System.out.printf("Created routine: %s enabled=%s%n", created.getName(), created.isEnabled());
            System.out.printf("cron expression: %s; time zone: %s%n",
                trigger.getCronExpression(), trigger.getTimeZone());
            // END:com.azure.ai.projects.RoutinesScheduleTriggerSample.createRoutine

            System.out.printf("Waiting up to %d minutes for a scheduled run...%n", RUN_TIMEOUT.toMinutes());
            RoutineRun completedRun = RoutinesSampleUtils.waitForCompletedRun(routinesClient, ROUTINE_NAME, RUN_TIMEOUT);
            RoutinesSampleUtils.reportRun(completedRun, RUN_TIMEOUT);
        } finally {
            routinesClient.deleteRoutine(ROUTINE_NAME);
            System.out.println("Routine deleted");
        }
    }
}
