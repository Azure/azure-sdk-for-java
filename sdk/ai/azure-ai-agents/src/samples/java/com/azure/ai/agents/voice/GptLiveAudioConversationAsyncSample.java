// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.voice;

import com.azure.ai.agents.BetaVoiceAgentLiveWebSocketSessionAsyncClient;
import com.azure.ai.agents.AgentsClientBuilder;
import com.azure.ai.agents.models.VoiceAgentLiveWebSocketConnectionOptions;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * Streams microphone and speaker audio over a native GPT Live WebSocket.
 * Requires Java Sound devices supporting 24-kHz mono PCM16LE. Press Enter to finish.
 * Audio capture and playback run on dedicated threads, not the WebSocket receive thread.
 * Playback is bounded to 60 seconds; overflow and device failures terminate the call explicitly.
 * The uniquely named agent is deleted unless FOUNDRY_KEEP_GPT_LIVE_AGENT=true.
 * Requests an opening greeting and labels spoken transcripts by speaker.
 * The standalone main method shuts down shared Reactor schedulers on exit; do not invoke it inside a shared application.
 *
 * <p>Before running the sample, set these environment variables:</p>
 * <ul>
 *   <li>{@code FOUNDRY_PROJECT_ENDPOINT} - The Azure AI Project endpoint.</li>
 *   <li>{@code FOUNDRY_GPT_LIVE_MODEL} - Optional. The GPT Live deployment name. Defaults to {@code gpt-live-1}.</li>
 *   <li>{@code FOUNDRY_GPT_LIVE_BACKEND_MODEL} - Optional. The Responses backend deployment name in the Live
 *   resource. Defaults to {@code gpt-5.6-terra}.</li>
 *   <li>{@code FOUNDRY_GPT_LIVE_STORE} - Optional. Set to {@code true} to persist conversation content and audio
 *   and request a Foundry conversation ID. Defaults to {@code false}. Also keep the agent for later REST reads.</li>
 *   <li>{@code FOUNDRY_KEEP_GPT_LIVE_AGENT} - Optional. Set to {@code true} to keep the agent after the sample.
 *   Defaults to {@code false}.</li>
 * </ul>
 */
public class GptLiveAudioConversationAsyncSample {
    public static void main(String[] args) {
        Configuration configuration = Configuration.getGlobalConfiguration();
        String endpoint = configuration.get("FOUNDRY_PROJECT_ENDPOINT");
        String model = configuration.get("FOUNDRY_GPT_LIVE_MODEL");
        String backendModel = configuration.get("FOUNDRY_GPT_LIVE_BACKEND_MODEL");
        boolean keepAgent = Boolean.parseBoolean(configuration.get("FOUNDRY_KEEP_GPT_LIVE_AGENT", "false"));
        boolean store = Boolean.parseBoolean(configuration.get("FOUNDRY_GPT_LIVE_STORE", "false"));

        if (endpoint == null || endpoint.trim().isEmpty()) {
            throw new IllegalArgumentException("Set environment variable FOUNDRY_PROJECT_ENDPOINT before running this sample.");
        }
        if (model == null) {
            model = "gpt-live-1";
        } else if (model.trim().isEmpty()) {
            throw new IllegalArgumentException("Set environment variable FOUNDRY_GPT_LIVE_MODEL before running this sample.");
        }
        if (backendModel == null) {
            backendModel = "gpt-5.6-terra";
        } else if (backendModel.trim().isEmpty()) {
            throw new IllegalArgumentException("Set environment variable FOUNDRY_GPT_LIVE_BACKEND_MODEL before running this sample.");
        }

        AgentsClientBuilder builder = new AgentsClientBuilder().endpoint(endpoint)
            .credential(new DefaultAzureCredentialBuilder().build())
            .allowPreview(true);
        try (GptLiveSampleUtils.AgentResource agent
            = GptLiveSampleUtils.createAgent(builder, model, backendModel, "audio-async", false, keepAgent)) {
            Mono.usingWhen(agent.builder.beta().buildBetaVoiceAgentLiveWebSocketAsyncClient()
                .openWebSocketSession(agent.name, new VoiceAgentLiveWebSocketConnectionOptions().setStoreEnabled(store)),
                GptLiveSampleUtils::runConversation,
                BetaVoiceAgentLiveWebSocketSessionAsyncClient::closeAsync,
                (session, error) -> session.closeAsync(),
                BetaVoiceAgentLiveWebSocketSessionAsyncClient::closeAsync).block();
        } finally {
            Schedulers.shutdownNow();
        }
    }

}
