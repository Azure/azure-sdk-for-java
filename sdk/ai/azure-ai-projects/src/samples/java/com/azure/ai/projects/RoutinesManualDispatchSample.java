// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.projects;

import com.azure.ai.projects.models.CustomRoutineTrigger;
import com.azure.ai.projects.models.DispatchRoutineResult;
import com.azure.ai.projects.models.InvokeAgentResponsesApiDispatchPayload;
import com.azure.ai.projects.models.Routine;
import com.azure.ai.projects.models.RoutineAction;
import com.azure.ai.projects.models.RoutineRun;
import com.azure.ai.projects.models.RoutineTrigger;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Sample demonstrating manual dispatch of a routine using the synchronous {@link BetaRoutinesClient}.
 *
 * <p>The routine is created with a manual {@link CustomRoutineTrigger}, dispatched on demand with an input
 * payload, and the resulting run is polled until completion. Routines are a preview feature. Before running, set:</p>
 * <ul>
 *   <li>{@code FOUNDRY_PROJECT_ENDPOINT} - the Azure AI Foundry project endpoint.</li>
 *   <li>{@code HOSTED_AGENT_NAME} - the name of a deployed hosted agent.</li>
 * </ul>
 */
public class RoutinesManualDispatchSample {
    private static final String ROUTINE_NAME = "sample-routine";
    private static final Duration RUN_TIMEOUT = Duration.ofMinutes(5);

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
            RoutineAction action = RoutinesSampleUtils.agentAction(agentName);
            CustomRoutineTrigger trigger = new CustomRoutineTrigger("manual",
                Collections.<String, BinaryData>emptyMap());
            Map<String, RoutineTrigger> triggers = new HashMap<>();
            triggers.put("manual", trigger);

            Routine created = routinesClient.createOrUpdateRoutine(ROUTINE_NAME,
                "Routine used by manual dispatch sample.", true, triggers, action);
            System.out.printf("Created routine: %s enabled=%s%n", created.getName(), created.isEnabled());

            // BEGIN:com.azure.ai.projects.RoutinesManualDispatchSample.dispatch
            DispatchRoutineResult dispatch = routinesClient.dispatchRoutine(created.getName(),
                new InvokeAgentResponsesApiDispatchPayload(BinaryData.fromObject("Hello, Tell me a joke.")));
            System.out.printf("Dispatched the routine. Dispatch ID %s, task ID: %s%n",
                dispatch.getDispatchId(), dispatch.getTaskId());
            // END:com.azure.ai.projects.RoutinesManualDispatchSample.dispatch

            System.out.printf("Waiting up to %d minutes for the dispatched run...%n", RUN_TIMEOUT.toMinutes());
            RoutineRun completedRun = RoutinesSampleUtils.waitForCompletedRun(routinesClient, created.getName(),
                RUN_TIMEOUT);
            RoutinesSampleUtils.reportRun(completedRun, RUN_TIMEOUT);
        } finally {
            routinesClient.deleteRoutine(ROUTINE_NAME);
            System.out.println("Routine deleted");
        }
    }
}
