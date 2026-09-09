// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.projects;

import com.azure.ai.projects.models.InvokeAgentResponsesApiDispatchPayload;
import com.azure.ai.projects.models.RoutineAction;
import com.azure.ai.projects.models.RoutineTrigger;
import com.azure.ai.projects.models.TimerRoutineTrigger;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;

/**
 * Sample demonstrating manual dispatch of a routine using the asynchronous {@link BetaRoutinesAsyncClient}.
 *
 * <p>The routine is created with a timer trigger scheduled in the future, dispatched early with an input payload,
 * and the resulting run is polled until completion. Routines are a preview feature. Before running, set:</p>
 * <ul>
 *   <li>{@code FOUNDRY_PROJECT_ENDPOINT} - the Azure AI Foundry project endpoint.</li>
 *   <li>{@code HOSTED_AGENT_NAME} - the name of a deployed hosted agent.</li>
 * </ul>
 */
public class RoutinesManualDispatchAsyncSample {
    private static final String ROUTINE_NAME = "sample-routine";
    private static final Duration RUN_TIMEOUT = Duration.ofMinutes(5);

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
        TimerRoutineTrigger trigger = new TimerRoutineTrigger()
            .setAt(OffsetDateTime.now(ZoneOffset.UTC).plusHours(1));
        Map<String, RoutineTrigger> triggers = new HashMap<>();
        triggers.put("once", trigger);

        Mono<Void> workflow = routinesAsyncClient.deleteRoutine(ROUTINE_NAME)
            .onErrorResume(ignored -> Mono.empty())
            .then(routinesAsyncClient.createOrUpdateRoutine(ROUTINE_NAME,
                "Timer routine dispatched before its scheduled fire time.", true, triggers, action))
            .flatMap(created -> {
                System.out.printf("Created routine: %s enabled=%s%n", created.getName(), created.isEnabled());
                return routinesAsyncClient.dispatchRoutine(created.getName(),
                        new InvokeAgentResponsesApiDispatchPayload(BinaryData.fromObject("Hello, Tell me a joke.")))
                    .flatMap(dispatch -> {
                        System.out.printf("Dispatched the routine. Dispatch ID %s, task ID: %s%n",
                            dispatch.getDispatchId(), dispatch.getTaskId());
                        System.out.printf("Waiting up to %d minutes for the dispatched run...%n",
                            RUN_TIMEOUT.toMinutes());
                        return RoutinesSampleUtils.waitForCompletedRunAsync(routinesAsyncClient, created.getName(),
                                dispatch.getDispatchId(), RUN_TIMEOUT)
                            .switchIfEmpty(Mono.error(new IllegalStateException(
                                "The dispatched routine did not complete within the timeout.")))
                            .doOnNext(completedRun -> RoutinesSampleUtils.reportRun(completedRun, RUN_TIMEOUT))
                            .then();
                    });
            });

        workflow
            .onErrorResume(error -> routinesAsyncClient.deleteRoutine(ROUTINE_NAME)
                .onErrorResume(ignored -> Mono.empty())
                .then(Mono.<Void>error(error)))
            .then(routinesAsyncClient.deleteRoutine(ROUTINE_NAME))
            .doOnSuccess(unused -> System.out.println("Routine deleted"))
            .block();
    }
}
