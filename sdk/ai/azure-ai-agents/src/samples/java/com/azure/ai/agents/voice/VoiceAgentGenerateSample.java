// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.voice;

import com.azure.ai.agents.AgentsClient;
import com.azure.ai.agents.AgentsClientBuilder;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.ai.agents.models.AgentDetails;
import com.azure.ai.agents.models.AgentVersionDetails;
import com.azure.ai.agents.models.VoiceAgentDefinition;
import com.azure.core.util.BinaryData;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Demonstrates guided authoring of a voice agent through the agent generation API.
 *
 * <p>Before running the sample, set these environment variables:</p>
 * <ul>
 *   <li>{@code FOUNDRY_PROJECT_ENDPOINT} - The Azure AI Project endpoint.</li>
 *   <li>{@code FOUNDRY_VOICE_AGENT_NAME} - The voice agent name. Defaults to {@code generated-voice-agent-java}.</li>
 * </ul>
 */
public class VoiceAgentGenerateSample {
    public static void main(String[] args) {
        Configuration configuration = Configuration.getGlobalConfiguration();
        String endpoint = configuration.get("FOUNDRY_PROJECT_ENDPOINT");
        String agentName = configuration.get("FOUNDRY_VOICE_AGENT_NAME", "generated-voice-agent-java");

        AgentsClient client = new AgentsClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint(endpoint)
            .allowPreview(true)
            .buildAgentsClient();

        Map<String, String> request = new LinkedHashMap<>();
        request.put("kind", "voice");
        request.put("name", agentName);
        AgentDetails generated = client.generateAgent(BinaryData.fromObject(request));
        try {
            System.out.println("Generated voice agent: " + generated.getName());
            AgentVersionDetails latest = generated.getVersions().getLatest();
            if (latest != null && latest.getDefinition() instanceof VoiceAgentDefinition) {
                VoiceAgentDefinition definition = (VoiceAgentDefinition) latest.getDefinition();
                System.out.println("Instructions: " + definition.getInstructions());
            }
        } finally {
            client.deleteAgent(generated.getName());
        }
    }
}
