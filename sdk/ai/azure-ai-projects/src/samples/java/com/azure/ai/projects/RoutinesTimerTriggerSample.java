// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.projects;

import com.azure.ai.projects.models.Routine;
import com.azure.ai.projects.models.RoutineAction;
import com.azure.ai.projects.models.RoutineRun;
import com.azure.ai.projects.models.RoutineTrigger;
import com.azure.ai.projects.models.TimerRoutineTrigger;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;

/**
 * Sample demonstrating a one-shot routine that fires at a specific time using the synchronous
 * {@link BetaRoutinesClient}.
 *
 * <p>The routine is configured with a {@link TimerRoutineTrigger} that fires roughly twenty seconds after
 * creation. The sample then polls the routine runs until one completes. Routines are a preview feature.
 * Before running, set:</p>
 * <ul>
 *   <li>{@code FOUNDRY_PROJECT_ENDPOINT} - the Azure AI Foundry project endpoint.</li>
 *   <li>{@code HOSTED_AGENT_NAME} - the name of a deployed hosted agent.</li>
 * </ul>
 */
public class RoutinesTimerTriggerSample {
    private static final String ROUTINE_NAME = "sample-routine";
    private static final Duration RUN_TIMEOUT = Duration.ofMinutes(3);

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
            // BEGIN:com.azure.ai.projects.RoutinesTimerTriggerSample.createRoutine
            RoutineAction action = RoutinesSampleUtils.agentAction(agentName);

            OffsetDateTime fireAt = OffsetDateTime.now(ZoneOffset.UTC).plusSeconds(20);
            TimerRoutineTrigger trigger = new TimerRoutineTrigger().setAt(fireAt);
            Map<String, RoutineTrigger> triggers = new HashMap<>();
            triggers.put("once", trigger);

            Routine created = routinesClient.createOrUpdateRoutine(ROUTINE_NAME,
                "Routine used by the timer-trigger sample.", true, triggers, action);
            System.out.printf("Created routine: %s enabled=%s%n", created.getName(), created.isEnabled());
            System.out.printf("Fire at: %s%n", trigger.getAt());
            // END:com.azure.ai.projects.RoutinesTimerTriggerSample.createRoutine

            System.out.printf("Waiting up to %d minutes for the timer run...%n", RUN_TIMEOUT.toMinutes());
            RoutineRun completedRun = RoutinesSampleUtils.waitForCompletedRun(routinesClient, ROUTINE_NAME, RUN_TIMEOUT);
            RoutinesSampleUtils.reportRun(completedRun, RUN_TIMEOUT);
        } finally {
            routinesClient.deleteRoutine(ROUTINE_NAME);
            System.out.println("Routine deleted");
        }
    }
}
