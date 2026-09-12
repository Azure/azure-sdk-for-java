// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.voice;

import com.azure.ai.agents.AgentsAsyncClient;
import com.azure.ai.agents.AgentsClientBuilder;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.ai.agents.models.AgentKind;
import com.azure.ai.agents.models.CreateAgentVersionInput;
import com.azure.ai.agents.models.VoiceModelType;
import reactor.core.publisher.Mono;

/**
 * Demonstrates the asynchronous voice-agent lifecycle.
 *
 * <p>Before running the sample, set these environment variables:</p>
 * <ul>
 *   <li>{@code FOUNDRY_PROJECT_ENDPOINT} - The Azure AI Project endpoint.</li>
 *   <li>{@code FOUNDRY_VOICE_MODEL} - Optional. The voice model or deployment name. Defaults to {@code gpt-realtime}.</li>
 *   <li>{@code FOUNDRY_VOICE_MODEL_TYPE} - Optional. The voice model type. Defaults to {@code managed}.</li>
 *   <li>{@code FOUNDRY_VOICE_AGENT_NAME} - The voice agent name. Defaults to {@code voice-agent-async-java}.</li>
 * </ul>
 */
public class VoiceAgentBasicAsyncSample {
    public static void main(String[] args) {
        Configuration configuration = Configuration.getGlobalConfiguration();
        String endpoint = configuration.get("FOUNDRY_PROJECT_ENDPOINT");
        String model = configuration.get("FOUNDRY_VOICE_MODEL", "gpt-realtime");
        VoiceModelType modelType = VoiceModelType.fromString(configuration.get(
            "FOUNDRY_VOICE_MODEL_TYPE", VoiceModelType.MANAGED.toString()));
        String agentName = configuration.get("FOUNDRY_VOICE_AGENT_NAME", "voice-agent-async-java");

        AgentsAsyncClient client = new AgentsClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint(endpoint)
            .allowPreview(true)
            .buildAgentsAsyncClient();

        client.createAgentVersion(agentName,
                new CreateAgentVersionInput(VoiceAgentSampleUtils.createDefinition(modelType, model,
                    "You are a friendly voice assistant. Keep replies short and natural.")))
            .doOnNext(created -> System.out.printf("Created voice agent %s, version %s%n",
                created.getName(), created.getVersion()))
            .then(client.getAgent(agentName))
            .doOnNext(agent -> System.out.printf("Retrieved voice agent %s, state %s%n",
                agent.getName(), agent.getState()))
            .thenMany(client.listAgents(AgentKind.VOICE, null, null, null, null))
            .doOnNext(agent -> System.out.println("Voice agent: " + agent.getName()))
            .then(client.createAgentVersion(agentName,
                new CreateAgentVersionInput(VoiceAgentSampleUtils.createDefinition(modelType, model,
                    "You are a friendly voice assistant. Always greet the caller warmly."))
                    .setDescription("Updated voice-agent instructions.")))
            .doOnNext(updated -> System.out.println("Created updated version: " + updated.getVersion()))
            .then(client.disableAgent(agentName))
            .then(client.enableAgent(agentName))
            .then(client.deleteAgent(agentName))
            .onErrorResume(error -> client.deleteAgent(agentName)
                .onErrorResume(cleanupError -> Mono.empty())
                .then(Mono.error(error)))
            .block();
    }
}
