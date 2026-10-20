// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.voice;

import com.azure.ai.agents.BetaVoiceAgentLiveWebSocketSessionAsyncClient;
import com.azure.ai.agents.AgentsClientBuilder;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import java.io.InputStream;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
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
                .openWebSocketSession(agent.name), GptLiveAudioConversationAsyncSample::runConversation,
                BetaVoiceAgentLiveWebSocketSessionAsyncClient::closeAsync,
                (session, error) -> session.closeAsync(),
                BetaVoiceAgentLiveWebSocketSessionAsyncClient::closeAsync).block();
        } finally {
            Schedulers.shutdownNow();
        }
    }

    static Mono<Void> runConversation(BetaVoiceAgentLiveWebSocketSessionAsyncClient session) {
        return Mono.defer(() -> runConversation(session,
            new VoiceAgentLiveAudioConversationAsyncSample.AudioProcessor(
                null, null, pcm -> session.sendEvent(GptLiveSampleUtils.audio(pcm.toBytes()))), System.in));
    }

    static Mono<Void> runConversation(BetaVoiceAgentLiveWebSocketSessionAsyncClient session,
        VoiceAgentLiveAudioConversationAsyncSample.AudioProcessor processor, InputStream input) {
        return Mono.usingWhen(Mono.just(processor),
            audio -> {
                AtomicBoolean started = new AtomicBoolean();
                AtomicBoolean finalized = new AtomicBoolean();
                GptLiveSampleUtils.AudioTranscriptPrinter transcripts = new GptLiveSampleUtils.AudioTranscriptPrinter();
                Sinks.Empty<Void> ready = Sinks.empty();
                Mono<Void> receive = session.receiveEvents().concatMap(data -> {
                    Map<?, ?> event = GptLiveSampleUtils.object(data);
                    transcripts.print(event);
                    if ("session.started".equals(GptLiveSampleUtils.type(event))) {
                        if (!started.compareAndSet(false, true)) {
                            return Mono.error(new IllegalStateException("Duplicate session.started event."));
                        }
                        return Mono.<Void>fromRunnable(() -> {
                            audio.start();
                        }).subscribeOn(Schedulers.boundedElastic())
                            .then(session.sendEvent(GptLiveSampleUtils.userMessage(
                                "Greet me briefly with: Hi, how can I help you today?")))
                            .then(session.sendEvent(GptLiveSampleUtils.event("response.create")))
                            .then(Mono.fromRunnable(() -> {
                                ready.tryEmitEmpty();
                                System.out.println("Opening greeting requested. Speak into the microphone. "
                                    + "Press Enter to end the call.");
                            })).thenReturn(data);
                    } else if ("session.output_audio.delta".equals(GptLiveSampleUtils.type(event))) {
                        audio.queueAudio(decodeAudio(event));
                    } else if ("session.closed".equals(GptLiveSampleUtils.type(event))) {
                        finalized.set(true);
                    }
                    return Mono.just(data);
                }).takeUntil(data ->
                    "session.closed".equals(GptLiveSampleUtils.type(GptLiveSampleUtils.object(data))))
                    .then(Mono.defer(() -> finalized.get() ? Mono.empty()
                        : Mono.error(new IllegalStateException("GPT Live disconnected without session.closed."))));
                Mono<Void> stop = ready.asMono().timeout(GptLiveSampleUtils.TIMEOUT)
                    .then(VoiceAgentLiveAudioConversationAsyncSample.waitForEnter(input))
                    .then(Mono.fromRunnable(audio::close).subscribeOn(Schedulers.boundedElastic()))
                    .then(session.sendEvent(GptLiveSampleUtils.event("session.close")))
                    .then(Mono.delay(GptLiveSampleUtils.TIMEOUT))
                    .then(Mono.error(new IllegalStateException("Timed out waiting for session.closed.")));
                return Mono.firstWithSignal(receive, audio.failureSignal(), stop);
            }, GptLiveAudioConversationAsyncSample::closeAudio,
            (audio, error) -> closeAudio(audio), GptLiveAudioConversationAsyncSample::closeAudio);
    }

    static byte[] decodeAudio(Map<?, ?> event) {
        byte[] pcm = Base64.getDecoder().decode(GptLiveSampleUtils.string(event, "delta"));
        if (pcm.length % 2 != 0) {
            throw new IllegalArgumentException("GPT Live PCM16 audio must contain complete two-byte samples.");
        }
        return pcm;
    }

    private static Mono<Void> closeAudio(VoiceAgentLiveAudioConversationAsyncSample.AudioProcessor audio) {
        return Mono.<Void>fromRunnable(audio::close).subscribeOn(Schedulers.boundedElastic());
    }
}
