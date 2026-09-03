// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.voice;

import com.azure.ai.agents.AgentsClient;
import com.azure.ai.agents.AgentsClientBuilder;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.ai.agents.models.AgentDetails;
import com.azure.ai.agents.models.AgentKind;
import com.azure.ai.agents.models.AgentVersionDetails;
import com.azure.ai.agents.models.CreateAgentVersionInput;
import com.azure.ai.agents.models.VoiceModelType;

/**
 * Demonstrates the synchronous voice-agent lifecycle.
 *
 * <p>Before running the sample, set these environment variables:</p>
 * <ul>
 *   <li>{@code FOUNDRY_PROJECT_ENDPOINT} - The Azure AI Project endpoint.</li>
 *   <li>{@code FOUNDRY_VOICE_MODEL} - Optional. The voice model or deployment name. Defaults to {@code gpt-realtime}.</li>
 *   <li>{@code FOUNDRY_VOICE_MODEL_TYPE} - Optional. The voice model type. Defaults to {@code managed}.</li>
 *   <li>{@code FOUNDRY_VOICE_AGENT_NAME} - The voice agent name. Defaults to {@code voice-agent-java}.</li>
 * </ul>
 */
public class VoiceAgentBasicSample {
    public static void main(String[] args) {
        Configuration configuration = Configuration.getGlobalConfiguration();
        String endpoint = configuration.get("FOUNDRY_PROJECT_ENDPOINT");
        String model = configuration.get("FOUNDRY_VOICE_MODEL", "gpt-realtime");
        VoiceModelType modelType = VoiceModelType.fromString(configuration.get(
            "FOUNDRY_VOICE_MODEL_TYPE", VoiceModelType.MANAGED.toString()));
        String agentName = configuration.get("FOUNDRY_VOICE_AGENT_NAME", "voice-agent-java");

        AgentsClient client = new AgentsClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint(endpoint)
            .allowPreview(true)
            .buildAgentsClient();
        try {
            AgentVersionDetails created = client.createAgentVersion(agentName,
                new CreateAgentVersionInput(VoiceAgentSampleUtils.createDefinition(modelType, model,
                    "You are a friendly voice assistant. Keep replies short and natural.")));
            System.out.printf("Created voice agent %s, version %s%n", created.getName(), created.getVersion());

            AgentDetails agent = client.getAgent(agentName);
            System.out.printf("Retrieved voice agent %s, state %s%n", agent.getName(), agent.getState());
            for (AgentDetails item : client.listAgents(AgentKind.VOICE, null, null, null, null)) {
                System.out.println("Voice agent: " + item.getName());
            }

            AgentVersionDetails updated = client.createAgentVersion(agentName,
                new CreateAgentVersionInput(VoiceAgentSampleUtils.createDefinition(modelType, model,
                    "You are a friendly voice assistant. Always greet the caller warmly."))
                    .setDescription("Updated voice-agent instructions."));
            System.out.println("Created updated version: " + updated.getVersion());
            client.disableAgent(agentName);
            System.out.println("Disabled voice agent");
            client.enableAgent(agentName);
            System.out.println("Enabled voice agent");
        } finally {
            client.deleteAgent(agentName);
            System.out.println("Deleted voice agent: " + agentName);
        }
    }
}
