// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.voice;

import com.azure.ai.agents.BetaVoiceAgentLiveWebSocketSessionClient;
import com.azure.ai.agents.AgentsClientBuilder;
import com.azure.core.util.Configuration;
import com.azure.ai.agents.models.VoiceAgentLiveWebSocketConnectionOptions;
import com.azure.identity.DefaultAzureCredentialBuilder;
import java.io.Console;
import java.io.InputStreamReader;
import java.util.Arrays;
import reactor.core.scheduler.Schedulers;

/**
 * Runs a synchronous, multi-turn GPT Live text conversation using Responses delegation.
 * Type messages interactively; a blank line, exit or quit ends the session.
 * Alternatively, pass prompts as command-line arguments for a scripted conversation.
 * Interactive mode plays a spoken greeting and replies, and prints frontend transcripts instead of backend notes.
 * It streams silent PCM while typing; voice usage is billable. Greeting completion uses the same speech-inactivity
 * heuristic and speaker drain as the asynchronous sample. Assistant transcript fragments are assembled into
 * utterances independently of typed turns; completed text waits until Enter when a You: line is active.
 * Synchronous session I/O runs on workers using the shared voice-loop coordination in GptLiveSampleUtils.
 * After a remote disconnect, a pending console read may require Enter to release the input worker.
 * Scripted command-line prompts print backend text without speaker playback.
 * A uniquely named agent is deleted on exit unless FOUNDRY_KEEP_GPT_LIVE_AGENT=true.
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
public class GptLiveTextConversationSample {
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
            = GptLiveSampleUtils.createAgent(builder, model, backendModel, "text", false, keepAgent);
            BetaVoiceAgentLiveWebSocketSessionClient session = agent.builder.beta()
                .buildBetaVoiceAgentLiveWebSocketClient()
                .openWebSocketSession(agent.name, new VoiceAgentLiveWebSocketConnectionOptions().setStoreEnabled(store))) {
            if (args.length == 0) {
                Console console = System.console();
                GptLiveSampleUtils.runConversation(session,
                    console == null ? new InputStreamReader(System.in) : console.reader());
            } else {
                GptLiveSampleUtils.runConversation(session, Arrays.asList(args));
            }
        } finally {
            Schedulers.shutdownNow();
        }
    }

}
