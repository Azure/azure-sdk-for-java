// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.projects;

import com.azure.ai.projects.models.RoutineAction;
import com.azure.ai.projects.models.RoutineTrigger;
import com.azure.ai.projects.models.TimerRoutineTrigger;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;

/**
 * Sample demonstrating a one-shot routine that fires at a specific time using the asynchronous
 * {@link BetaRoutinesAsyncClient}.
 *
 * <p>The routine is configured with a {@link TimerRoutineTrigger} that fires roughly twenty seconds after
 * creation. The sample then polls the routine runs until one completes. Routines are a preview feature.
 * Before running, set:</p>
 * <ul>
 *   <li>{@code FOUNDRY_PROJECT_ENDPOINT} - the Azure AI Foundry project endpoint.</li>
 *   <li>{@code HOSTED_AGENT_NAME} - the name of a deployed hosted agent.</li>
 * </ul>
 */
public class RoutinesTimerTriggerAsyncSample {
    private static final String ROUTINE_NAME = "sample-routine";
    private static final Duration RUN_TIMEOUT = Duration.ofMinutes(3);

    public static void main(String[] args) {
        Configuration configuration = Configuration.getGlobalConfiguration();
        String endpoint = configuration.get("FOUNDRY_PROJECT_ENDPOINT");
        String agentName = configuration.get("HOSTED_AGENT_NAME");

        BetaRoutinesAsyncClient routinesAsyncClient = new AIProjectClientBuilder()
            .endpoint(endpoint)
            .credential(new DefaultAzureCredentialBuilder().build())
            .beta()
            .buildBetaRoutinesAsyncClient();

        RoutineAction action = RoutinesSampleUtils.agentAction(agentName);
        OffsetDateTime fireAt = OffsetDateTime.now(ZoneOffset.UTC).plusSeconds(20);
        TimerRoutineTrigger trigger = new TimerRoutineTrigger().setAt(fireAt);
        Map<String, RoutineTrigger> triggers = new HashMap<>();
        triggers.put("once", trigger);

        routinesAsyncClient.deleteRoutine(ROUTINE_NAME)
            .onErrorResume(ignored -> Mono.empty())
            .then(routinesAsyncClient.createOrUpdateRoutine(ROUTINE_NAME,
                "Routine used by the timer-trigger sample.", true, triggers, action))
            .flatMap(created -> {
                System.out.printf("Created routine: %s enabled=%s%n", created.getName(), created.isEnabled());
                System.out.printf("Fire at: %s%n", trigger.getAt());
                System.out.printf("Waiting up to %d minutes for the timer run...%n", RUN_TIMEOUT.toMinutes());
                return RoutinesSampleUtils.waitForCompletedRunAsync(routinesAsyncClient, ROUTINE_NAME, RUN_TIMEOUT)
                    .doOnNext(completedRun -> RoutinesSampleUtils.reportRun(completedRun, RUN_TIMEOUT))
                    .then();
            })
            .then(routinesAsyncClient.deleteRoutine(ROUTINE_NAME))
            .doOnSuccess(unused -> System.out.println("Routine deleted"))
            .block();
    }
}
