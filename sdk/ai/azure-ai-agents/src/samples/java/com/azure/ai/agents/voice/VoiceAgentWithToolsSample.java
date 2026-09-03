// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.voice;

import com.azure.ai.agents.AgentsClient;
import com.azure.ai.agents.AgentsClientBuilder;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.ai.agents.models.AgentVersionDetails;
import com.azure.ai.agents.models.CreateAgentVersionInput;
import com.azure.ai.agents.models.RealtimeAudioFormatsAudioPcm;
import com.azure.ai.agents.models.RealtimeAudioFormatsAudioPcmRate;
import com.azure.ai.agents.models.VoiceAgentAudioConfig;
import com.azure.ai.agents.models.VoiceAgentAudioInputConfig;
import com.azure.ai.agents.models.VoiceAgentAudioOutputConfig;
import com.azure.ai.agents.models.VoiceAgentDefinition;
import com.azure.ai.agents.models.VoiceAgentFunctionTool;
import com.azure.ai.agents.models.VoiceAgentInputTranscription;
import com.azure.ai.agents.models.VoiceAgentInputTranscriptionModel;
import com.azure.ai.agents.models.VoiceAgentServerVadTurnDetection;
import com.azure.ai.agents.models.VoiceAgentSystemTool;
import com.azure.ai.agents.models.VoiceAgentSystemToolName;
import com.azure.ai.agents.models.VoiceAgentTool;
import com.azure.ai.agents.models.VoiceOutputModality;
import com.azure.ai.agents.models.VoiceType;
import com.azure.ai.agents.models.VoiceModelType;

import java.util.Arrays;
import java.util.Collections;

/**
 * Demonstrates a voice-agent definition with audio processing, transcription, and tools.
 *
 * <p>Before running the sample, set these environment variables:</p>
 * <ul>
 *   <li>{@code FOUNDRY_PROJECT_ENDPOINT} - The Azure AI Project endpoint.</li>
 *   <li>{@code FOUNDRY_VOICE_MODEL} - Optional. The voice model or deployment name. Defaults to {@code gpt-realtime}.</li>
 *   <li>{@code FOUNDRY_VOICE_MODEL_TYPE} - Optional. The voice model type. Defaults to {@code managed}.</li>
 *   <li>{@code FOUNDRY_VOICE_AGENT_NAME} - The voice agent name. Defaults to {@code voice-agent-with-tools-java}.</li>
 * </ul>
 */
public class VoiceAgentWithToolsSample {
    public static void main(String[] args) {
        Configuration configuration = Configuration.getGlobalConfiguration();
        String endpoint = configuration.get("FOUNDRY_PROJECT_ENDPOINT");
        String model = configuration.get("FOUNDRY_VOICE_MODEL", "gpt-realtime");
        VoiceModelType modelType = VoiceModelType.fromString(configuration.get(
            "FOUNDRY_VOICE_MODEL_TYPE", VoiceModelType.MANAGED.toString()));
        String agentName = configuration.get("FOUNDRY_VOICE_AGENT_NAME", "voice-agent-with-tools-java");

        AgentsClient client = new AgentsClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint(endpoint)
            .allowPreview(true)
            .buildAgentsClient();

        RealtimeAudioFormatsAudioPcm pcm = new RealtimeAudioFormatsAudioPcm()
            .setRate(RealtimeAudioFormatsAudioPcmRate.TWO_FOUR_ZERO_ZERO_ZERO);
        VoiceAgentAudioInputConfig input = new VoiceAgentAudioInputConfig()
            .setFormat(pcm)
            .setTurnDetection(new VoiceAgentServerVadTurnDetection()
                .setThreshold(0.5)
                .setPrefixPaddingMs(300L)
                .setSilenceDurationMs(500L))
            .setTranscription(new VoiceAgentInputTranscription(VoiceAgentInputTranscriptionModel.WHISPER_1));
        VoiceAgentAudioOutputConfig output = new VoiceAgentAudioOutputConfig()
            .setVoice("en-US-AvaNeural")
            .setVoiceType(VoiceType.AZURE_STANDARD);
        VoiceAgentFunctionTool weather = new VoiceAgentFunctionTool("get_weather")
            .setDescription("Get the current weather for a city.")
            .setParameters(BinaryData.fromString("{}"));
        VoiceAgentSystemTool endCall = new VoiceAgentSystemTool(VoiceAgentSystemToolName.END_CONVERSATION);
        VoiceAgentDefinition definition = new VoiceAgentDefinition()
                .setModelType(modelType)
                .setModel(model)
                .setInstructions("Use tools when they help answer the caller.")
                .setAudio(new VoiceAgentAudioConfig().setInput(input).setOutput(output))
                .setOutputModalities(Collections.singletonList(VoiceOutputModality.AUDIO))
                .setTools(Arrays.<VoiceAgentTool>asList(weather, endCall))
                .setStore(true);

        try {
            AgentVersionDetails created = client.createAgentVersion(agentName,
                new CreateAgentVersionInput(definition));
            AgentVersionDetails fetched = client.getAgentVersionDetails(agentName, created.getVersion());
            VoiceAgentDefinition fetchedDefinition = (VoiceAgentDefinition) fetched.getDefinition();
            System.out.println("Configured voice tools: " + fetchedDefinition.getTools().size());
            for (VoiceAgentTool tool : fetchedDefinition.getTools()) {
                System.out.printf("  %s%n", tool.getType());
            }
        } finally {
            client.deleteAgent(agentName);
        }
    }
}
