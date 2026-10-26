// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.voice;

import com.azure.ai.agents.BetaVoiceAgentLiveWebSocketSessionAsyncClient;
import com.azure.ai.agents.AgentsClientBuilder;
import com.azure.core.util.Configuration;
import com.azure.ai.agents.models.VoiceAgentLiveWebSocketConnectionOptions;
import com.azure.identity.DefaultAzureCredentialBuilder;
import java.io.Console;
import java.io.InputStreamReader;
import java.util.Arrays;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * Runs a nonblocking, multi-turn GPT Live text conversation using Responses delegation.
 * Type messages interactively; a blank line, exit or quit ends the session.
 * Alternatively, pass prompts as command-line arguments for a scripted conversation.
 * Console lines are read on a worker, off the WebSocket thread.
 * After a remote disconnect, a pending console read may require Enter to release the input worker.
 * Interactive mode requests a spoken greeting, plays 24-kHz PCM16 replies, and prints the voice frontend's transcripts.
 * Before the first prompt, it waits for greeting audio and text, one second of speech inactivity, and speaker drain.
 * The quiet-period boundary is a heuristic, not a service speech-completion event.
 * Assistant transcript fragments are assembled into utterances using the speech-inactivity heuristic.
 * Completed utterances wait until Enter if input is active; incomplete text is not flushed by input submission.
 * Audio continues playing while typing on the You: line.
 * It streams real-time silent PCM input instead of a microphone; voice usage continues while you type.
 * Backend completion allows another typed prompt; it does not mean that spoken output has finished.
 * Scripted mode prints backend text without requiring a speaker.
 * Agent creation/deletion run outside the asynchronous session on the main thread.
 * The uniquely named agent is deleted unless FOUNDRY_KEEP_GPT_LIVE_AGENT=true.
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
public class GptLiveTextConversationAsyncSample {
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
        Console console = System.console();
        try (GptLiveSampleUtils.AgentResource agent
            = GptLiveSampleUtils.createAgent(builder, model, backendModel, "text-async", false, keepAgent)) {
            Mono.usingWhen(agent.builder.beta().buildBetaVoiceAgentLiveWebSocketAsyncClient()
                .openWebSocketSession(agent.name, new VoiceAgentLiveWebSocketConnectionOptions().setStoreEnabled(store)),
                session -> args.length == 0
                    ? GptLiveSampleUtils.runInteractiveConversationAsync(session,
                        console == null ? new InputStreamReader(System.in) : console.reader())
                    : GptLiveSampleUtils.runTextConversationAsync(session, Arrays.asList(args)),
                BetaVoiceAgentLiveWebSocketSessionAsyncClient::closeAsync,
                (session, error) -> session.closeAsync(),
                BetaVoiceAgentLiveWebSocketSessionAsyncClient::closeAsync).block();
        } finally {
            Schedulers.shutdownNow();
        }
    }

}
