// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.voice;

import com.azure.ai.agents.AgentsClient;
import com.azure.ai.agents.AgentsClientBuilder;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.ai.agents.models.AgentVersionDetails;
import com.azure.ai.agents.models.CreateAgentVersionInput;
import com.azure.ai.agents.models.VoiceModelType;

/**
 * Demonstrates released and draft voice-agent versions.
 *
 * <p>Before running the sample, set these environment variables:</p>
 * <ul>
 *   <li>{@code FOUNDRY_PROJECT_ENDPOINT} - The Azure AI Project endpoint.</li>
 *   <li>{@code FOUNDRY_VOICE_MODEL} - Optional. The voice model or deployment name. Defaults to {@code gpt-realtime}.</li>
 *   <li>{@code FOUNDRY_VOICE_MODEL_TYPE} - Optional. The voice model type. Defaults to {@code managed}.</li>
 *   <li>{@code FOUNDRY_VOICE_AGENT_NAME} - The voice agent name. Defaults to {@code versioned-voice-agent-java}.</li>
 * </ul>
 */
public class VoiceAgentVersionsSample {
    public static void main(String[] args) {
        Configuration configuration = Configuration.getGlobalConfiguration();
        String endpoint = configuration.get("FOUNDRY_PROJECT_ENDPOINT");
        String model = configuration.get("FOUNDRY_VOICE_MODEL", "gpt-realtime");
        VoiceModelType modelType = VoiceModelType.fromString(configuration.get(
            "FOUNDRY_VOICE_MODEL_TYPE", VoiceModelType.MANAGED.toString()));
        String agentName = configuration.get("FOUNDRY_VOICE_AGENT_NAME", "versioned-voice-agent-java");

        AgentsClient client = new AgentsClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint(endpoint)
            .allowPreview(true)
            .buildAgentsClient();
        try {
            AgentVersionDetails first = client.createAgentVersion(agentName,
                new CreateAgentVersionInput(VoiceAgentSampleUtils.createDefinition(modelType, model,
                    "You are a helpful voice assistant.")));
            AgentVersionDetails released = client.createAgentVersion(agentName,
                new CreateAgentVersionInput(VoiceAgentSampleUtils.createDefinition(modelType, model,
                    "You are a helpful voice assistant. Greet the caller by name."))
                    .setDescription("Added a personalized greeting."));
            AgentVersionDetails draft = client.createAgentVersion(agentName,
                new CreateAgentVersionInput(VoiceAgentSampleUtils.createDefinition(modelType, model,
                    "You are an experimental voice assistant."))
                    .setDescription("Candidate persona under review.")
                    .setDraft(true));
            System.out.printf("Created versions %s, %s and draft %s%n",
                first.getVersion(), released.getVersion(), draft.getVersion());

            System.out.println("Released versions:");
            for (AgentVersionDetails version : client.listAgentVersions(agentName)) {
                System.out.printf("  %s (draft=%s)%n", version.getVersion(), version.isDraft());
            }
            System.out.println("All versions including drafts:");
            for (AgentVersionDetails version : client.listAgentVersions(agentName, null, null, null, null, true)) {
                System.out.printf("  %s (draft=%s)%n", version.getVersion(), version.isDraft());
            }
            AgentVersionDetails fetched = client.getAgentVersionDetails(agentName, released.getVersion());
            System.out.println("Fetched version: " + fetched.getVersion());
        } finally {
            client.deleteAgent(agentName);
        }
    }
}
