// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.projects;

import com.azure.ai.projects.models.CustomRoutineTrigger;
import com.azure.ai.projects.models.RoutineAction;
import com.azure.ai.projects.models.RoutineTrigger;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Sample demonstrating CRUD operations on routines (create, retrieve, enable, disable, list, delete) using the
 * asynchronous {@link BetaRoutinesAsyncClient}.
 *
 * <p>A routine binds to a hosted agent. A {@link CustomRoutineTrigger} is used to keep the sample self-contained.
 * Routines are a preview feature. Before running, set:</p>
 * <ul>
 *   <li>{@code FOUNDRY_PROJECT_ENDPOINT} - the Azure AI Foundry project endpoint.</li>
 *   <li>{@code HOSTED_AGENT_NAME} - the name of a deployed hosted agent.</li>
 * </ul>
 */
public class RoutinesAsyncSample {
    private static final String ROUTINE_NAME = "sample-routine";

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
        CustomRoutineTrigger trigger = new CustomRoutineTrigger("sample-provider",
            Collections.singletonMap("source", BinaryData.fromString("\"sample_routines_crud\"")))
            .setEventName("sample-event");
        Map<String, RoutineTrigger> triggers = new HashMap<>();
        triggers.put("manual", trigger);

        // Clean up any pre-existing routine, then run the full lifecycle reactively.
        routinesAsyncClient.deleteRoutine(ROUTINE_NAME)
            .onErrorResume(ignored -> Mono.empty())
            .then(routinesAsyncClient.createOrUpdateRoutine(ROUTINE_NAME,
                "Routine created by the azure-ai-projects sample.", true, triggers, action))
            .flatMap(created -> {
                System.out.printf("Created routine: %s enabled=%s%n", created.getName(), created.isEnabled());
                return routinesAsyncClient.disableRoutine(ROUTINE_NAME)
                    .doOnNext(disabled -> System.out.printf("Disabled routine: %s enabled=%s%n",
                        disabled.getName(), disabled.isEnabled()))
                    .then(routinesAsyncClient.getRoutine(ROUTINE_NAME))
                    .doOnNext(fetched -> System.out.printf("Retrieved routine: %s enabled=%s description=%s%n",
                        fetched.getName(), fetched.isEnabled(), fetched.getDescription()))
                    .then(routinesAsyncClient.enableRoutine(ROUTINE_NAME))
                    .doOnNext(enabled -> System.out.printf("Enabled routine: %s enabled=%s%n",
                        enabled.getName(), enabled.isEnabled()))
                    .thenMany(routinesAsyncClient.listRoutines())
                    .doOnNext(routine -> System.out.printf("  - %s enabled=%s%n",
                        routine.getName(), routine.isEnabled()))
                    .then();
            })
            .then(routinesAsyncClient.deleteRoutine(ROUTINE_NAME))
            .doOnSuccess(unused -> System.out.println("Routine deleted"))
            .block();
    }
}
