// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.voice;

import com.azure.ai.agents.BetaVoiceAgentsLiveWebSocketSessionAsyncClient;
import com.azure.ai.agents.AgentsClientBuilder;
import com.azure.ai.agents.models.VoiceAgentLiveWebSocketConnectionOptions;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import com.openai.models.live.ClientEvent;
import com.openai.models.live.ServerEvent;
import com.openai.models.responses.ResponseStreamEvent;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;


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
            Mono.usingWhen(agent.builder.beta().buildBetaVoiceAgentsLiveWebSocketAsyncClient()
                .openWebSocketSession(agent.name, new VoiceAgentLiveWebSocketConnectionOptions().setStoreEnabled(store)),
                GptLiveAudioConversationAsyncSample::runAudioConversationAsync,
                BetaVoiceAgentsLiveWebSocketSessionAsyncClient::closeAsync,
                (session, error) -> session.closeAsync(),
                BetaVoiceAgentsLiveWebSocketSessionAsyncClient::closeAsync).block();
        } finally {
            Schedulers.shutdownNow();
        }
    }

    static Mono<Void> runAudioConversationAsync(BetaVoiceAgentsLiveWebSocketSessionAsyncClient session) {
        return Mono.defer(() -> runAudioConversationAsync(session,
            new GptLiveSampleUtils.AudioProcessor(
                null, null, pcm -> session.sendEvent(GptLiveSampleUtils.audio(pcm.toBytes()))), System.in));
    }

    static Mono<Void> runAudioConversationAsync(BetaVoiceAgentsLiveWebSocketSessionAsyncClient session,
        GptLiveSampleUtils.AudioProcessor processor, InputStream input) {
        return Mono.usingWhen(Mono.just(processor),
            audio -> Mono.firstWithSignal(runConversationAsync(session.receiveEvents(), session::sendEvent,
                () -> "transport close code=" + session.getCloseCode() + ", reason=" + session.getCloseReason(),
                transcripts -> VoiceAgentLiveAudioConversationAsyncSample.waitForEnter(input)
                    .then(closeAudioAsync(audio)).then(Mono.<String>empty()),
                audio::queueAudio, () -> { }, true, audio), audio.failureSignalAsync()), GptLiveAudioConversationAsyncSample::closeAudioAsync,
            (audio, error) -> closeAudioAsync(audio), GptLiveAudioConversationAsyncSample::closeAudioAsync);
    }

    private static Mono<Void> closeAudioAsync(GptLiveSampleUtils.AudioProcessor audio) {
        return Mono.<Void>fromRunnable(audio::close).subscribeOn(Schedulers.boundedElastic());
    }

    private static Mono<Void> runConversationAsync(Flux<ServerEvent> events,
        Function<ClientEvent, Mono<Void>> sendEvent, Supplier<String> closeDetails,
        Function<GptLiveSampleUtils.AudioTranscriptPrinter, Mono<String>> nextPrompt,
        Consumer<byte[]> playAudio, Runnable drainAudio, boolean spoken, GptLiveSampleUtils.AudioProcessor microphone) {
        Mono<Void> opening = microphone == null ? Mono.empty()
            : Mono.defer(() -> sendEvent.apply(GptLiveSampleUtils.userMessage("Greet me briefly with: Hi, how can I help you today?"))
                .then(sendEvent.apply(GptLiveSampleUtils.responseCreate()))
                .then(Mono.fromRunnable(() -> System.out.println(
                    "Opening greeting requested. Speak into the microphone. Press Enter to end the call."))));
        return runConversationCoreAsync(events, sendEvent, closeDetails, nextPrompt, playAudio, drainAudio, spoken,
            spoken && microphone == null, microphone == null && System.console() != null,
            () -> Mono.fromRunnable(() -> {
                if (microphone != null) {
                    microphone.start();
                }
            }), opening, backend -> Mono.just(false), reason -> microphone != null);
    }

    static Mono<Void> runConversationCoreAsync(Flux<ServerEvent> events,
        Function<ClientEvent, Mono<Void>> sendEvent, Supplier<String> closeDetails,
        Function<GptLiveSampleUtils.AudioTranscriptPrinter, Mono<String>> nextPrompt,
        Consumer<byte[]> playAudio, Runnable drainAudio, boolean spoken, boolean interactive, boolean inputEchoed,
        Supplier<Mono<Void>> onStarted, Mono<Void> opening,
        Function<ResponseStreamEvent, Mono<Boolean>> handleResponse, Predicate<String> acceptClose) {
        return Mono.defer(() -> {
            GptLiveSampleUtils.AudioTranscriptPrinter transcripts = new GptLiveSampleUtils.AudioTranscriptPrinter(inputEchoed, interactive);
            GptLiveSampleUtils.Greeting greeting = new GptLiveSampleUtils.Greeting();
            Sinks.Empty<Void> ready = Sinks.empty();
            Sinks.Empty<Void> closed = Sinks.empty();
            Sinks.Empty<Void> audioStarted = Sinks.empty();
            Sinks.Empty<Void> stopAudio = Sinks.empty();
            Sinks.Empty<Void> audioStopped = Sinks.empty();
            AtomicReference<Sinks.Empty<Void>> responseCompleted = new AtomicReference<>();
            GptLiveSampleUtils.ConversationEventHandler<Mono<Void>> handler
                = new GptLiveSampleUtils.ConversationEventHandler<Mono<Void>>(spoken, closeDetails, acceptClose) {
                    @Override
                    Mono<Void> onStarted() {
                        return Mono.defer(onStarted).doOnSuccess(ignored -> ready.tryEmitEmpty());
                    }

                    @Override
                    Mono<Void> onResponse(ResponseStreamEvent event) {
                        return Mono.defer(() -> handleResponse.apply(event)).defaultIfEmpty(false)
                            .doOnNext(continuation -> {
                                if (event.isCompleted() && completeResponse(continuation)) {
                                    responseCompleted.getAndSet(null).tryEmitEmpty();
                                }
                            }).then();
                    }

                    @Override
                    Mono<Void> onClosed() {
                        return Mono.fromRunnable(() -> closed.tryEmitEmpty());
                    }

                    @Override
                    Mono<Void> ignored() {
                        return Mono.empty();
                    }
                };
            Mono<Void> receive = events
                .publishOn(Schedulers.boundedElastic(), 1)
                .takeUntil(ServerEvent::isSessionClosed)
                .concatMap(event -> Mono.defer(() -> {
                    if (spoken) {
                        transcripts.print(event);
                        if (event.isSessionOutputAudioDelta()) {
                            byte[] pcm = GptLiveSampleUtils.decodeAudio(event);
                            playAudio.accept(pcm);
                            greeting.onAudio(pcm);
                        } else if (event.isSessionOutputTranscriptDelta()
                            && !event.asSessionOutputTranscriptDelta().delta().trim().isEmpty()) {
                            greeting.onTranscript();
                        }
                    } else {
                        GptLiveSampleUtils.printEvent(event);
                    }
                    return handler.process(event);
                }), 1)
                .then(Mono.defer(() -> handler.awaitConversationAfterReceive() ? Mono.never() : Mono.empty()));
            Mono<Void> audio = ready.asMono()
                .thenMany(GptLiveSampleUtils.silenceFramesAsync().takeUntilOther(stopAudio.asMono())
                    .concatMap(frame -> sendEvent.apply(frame).doOnSuccess(ignored -> audioStarted.tryEmitEmpty()), 1))
                .then()
                .doOnSuccess(ignored -> audioStopped.tryEmitEmpty())
                .then(Mono.never());
            Mono<Void> renderTranscripts = greeting.quietPeriodsAsync()
                .doOnNext(ignored -> transcripts.endUtterance()).then();
            Mono<Void> conversation = ready.asMono().timeout(GptLiveSampleUtils.TIMEOUT)
                .then(Mono.defer(() -> interactive ? audioStarted.asMono().timeout(GptLiveSampleUtils.TIMEOUT) : Mono.empty()))
                .then(Mono.defer(() -> interactive
                    ? sendEvent.apply(GptLiveSampleUtils.commentary("Hi, how can I help you today?"))
                        .timeout(GptLiveSampleUtils.TIMEOUT)
                        .then(greeting.awaitPlaybackAsync(drainAudio))
                        .then(Mono.fromRunnable(transcripts::endUtterance))
                    : opening))
                .then(Mono.defer(() -> GptLiveSampleUtils.promptAsync(sendEvent, nextPrompt, responseCompleted, spoken, transcripts, handler)))
                .then(Mono.defer(() -> {
                    handler.requestClose();
                    stopAudio.tryEmitEmpty();
                    return (interactive ? audioStopped.asMono() : Mono.<Void>empty())
                        .then(sendEvent.apply(GptLiveSampleUtils.closeSession()))
                        .then(closed.asMono()).timeout(GptLiveSampleUtils.TIMEOUT);
                }));
            return interactive ? Mono.firstWithSignal(receive, audio, renderTranscripts, conversation)
                : Mono.firstWithSignal(receive, conversation);
        });
    }

}
