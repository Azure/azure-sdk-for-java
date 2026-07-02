// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.projects;

import com.azure.ai.projects.models.CustomRoutineTrigger;
import com.azure.ai.projects.models.Routine;
import com.azure.ai.projects.models.RoutineAction;
import com.azure.ai.projects.models.RoutineTrigger;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Sample demonstrating CRUD operations on routines (create, retrieve, enable, disable, list, delete) using the
 * synchronous {@link BetaRoutinesClient}.
 *
 * <p>A routine binds to a hosted agent. A {@link CustomRoutineTrigger} is used to keep the sample self-contained
 * (no GitHub or schedule resources required). Routines are a preview feature. Before running, set:</p>
 * <ul>
 *   <li>{@code FOUNDRY_PROJECT_ENDPOINT} - the Azure AI Foundry project endpoint.</li>
 *   <li>{@code HOSTED_AGENT_NAME} - the name of a deployed hosted agent.</li>
 * </ul>
 */
public class RoutinesSample {
    private static final String ROUTINE_NAME = "sample-routine";

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
            // BEGIN:com.azure.ai.projects.RoutinesSample.createRoutine
            RoutineAction action = RoutinesSampleUtils.agentAction(agentName);

            CustomRoutineTrigger trigger = new CustomRoutineTrigger("sample-provider",
                Collections.singletonMap("source", BinaryData.fromString("\"sample_routines_crud\"")))
                .setEventName("sample-event");
            Map<String, RoutineTrigger> triggers = new HashMap<>();
            triggers.put("manual", trigger);

            Routine created = routinesClient.createOrUpdateRoutine(ROUTINE_NAME,
                "Routine created by the azure-ai-projects sample.", true, triggers, action);
            System.out.printf("Created routine: %s enabled=%s%n", created.getName(), created.isEnabled());
            // END:com.azure.ai.projects.RoutinesSample.createRoutine

            // Disable the routine.
            Routine disabled = routinesClient.disableRoutine(ROUTINE_NAME);
            System.out.printf("Disabled routine: %s enabled=%s%n", disabled.getName(), disabled.isEnabled());

            // Retrieve the routine to verify its state.
            Routine fetched = routinesClient.getRoutine(ROUTINE_NAME);
            System.out.printf("Retrieved routine: %s enabled=%s description=%s%n",
                fetched.getName(), fetched.isEnabled(), fetched.getDescription());

            // Re-enable the routine.
            Routine enabled = routinesClient.enableRoutine(ROUTINE_NAME);
            System.out.printf("Enabled routine: %s enabled=%s%n", enabled.getName(), enabled.isEnabled());

            // List all routines.
            for (Routine routine : routinesClient.listRoutines()) {
                System.out.printf("  - %s enabled=%s%n", routine.getName(), routine.isEnabled());
            }
        } finally {
            // Delete the routine.
            routinesClient.deleteRoutine(ROUTINE_NAME);
            System.out.println("Routine deleted");
        }
    }
}
