// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.voice;

import com.azure.ai.agents.AgentsClientBuilder;
import com.azure.ai.agents.BetaVoiceAgentsLiveWebSocketSessionAsyncClient;
import com.azure.ai.agents.models.VoiceAgentLiveWebSocketConnectionOptions;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.openai.models.live.ClientEvent;
import com.openai.models.live.ServerEvent;
import com.openai.models.responses.ResponseStreamEvent;
import java.io.BufferedReader;
import java.io.Console;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Schedulers;

/**
 * Advanced asynchronous console conversation with speaker playback and frontend transcripts.
 * Uses the same settings as {@link GptLiveTextConversationAsyncSample}. Blocking device and console work runs
 * off the WebSocket thread. Silent PCM input while typing consumes voice-session usage.
 * Greeting completion uses a one-second speech-inactivity heuristic and speaker drain.
 * Blank input, EOF, exit, or quit requests graceful closure. A noninterruptible console read may require Enter
 * after remote closure. Standard input is not closed. The standalone main shuts down shared Reactor schedulers.
 */
public class GptLiveInteractiveTextConversationAsyncSample {
    public static void main(String[] args) {
        Configuration configuration = Configuration.getGlobalConfiguration();
        String endpoint = GptLiveSampleUtils.requireSetting(configuration, "FOUNDRY_PROJECT_ENDPOINT");
        String model = configuration.get("FOUNDRY_GPT_LIVE_MODEL");
        String backendModel = configuration.get("FOUNDRY_GPT_LIVE_BACKEND_MODEL");
        if (model == null) {
            model = "gpt-live-1";
        } else if (model.trim().isEmpty()) {
            throw new IllegalArgumentException("FOUNDRY_GPT_LIVE_MODEL must not be blank.");
        }
        if (backendModel == null) {
            backendModel = "gpt-5.6-terra";
        } else if (backendModel.trim().isEmpty()) {
            throw new IllegalArgumentException("FOUNDRY_GPT_LIVE_BACKEND_MODEL must not be blank.");
        }
        boolean keepAgent = Boolean.parseBoolean(configuration.get("FOUNDRY_KEEP_GPT_LIVE_AGENT", "false"));
        boolean store = Boolean.parseBoolean(configuration.get("FOUNDRY_GPT_LIVE_STORE", "false"));
        AgentsClientBuilder builder = new AgentsClientBuilder().endpoint(endpoint)
            .credential(new DefaultAzureCredentialBuilder().build()).allowPreview(true);
        Console console = System.console();
        try (GptLiveSampleUtils.AgentResource agent
            = GptLiveSampleUtils.createAgent(builder, model, backendModel, "interactive-async", false, keepAgent)) {
            Mono.usingWhen(builder.beta().buildBetaVoiceAgentsLiveWebSocketAsyncClient()
                .openWebSocketSession(agent.name, new VoiceAgentLiveWebSocketConnectionOptions().setStoreEnabled(store)),
                session -> Mono.usingWhen(
                    Mono.fromCallable(VoiceAgentRealtimeSampleUtils.SpeakerPlayer::new)
                        .subscribeOn(Schedulers.boundedElastic()),
                    player -> runInteractiveConversationAsync(session,
                        console == null ? new InputStreamReader(System.in) : console.reader(), player::play, player::drain),
                    player -> Mono.<Void>fromRunnable(player::close).subscribeOn(Schedulers.boundedElastic())),
                BetaVoiceAgentsLiveWebSocketSessionAsyncClient::closeAsync,
                (session, error) -> session.closeAsync(),
                BetaVoiceAgentsLiveWebSocketSessionAsyncClient::closeAsync).block();
        } finally {
            Schedulers.shutdownNow();
        }
    }

    static Mono<Void> runInteractiveConversationAsync(BetaVoiceAgentsLiveWebSocketSessionAsyncClient session, Reader input,
        Consumer<byte[]> playAudio, Runnable drainAudio) {
        return Mono.defer(() -> runInteractiveConversationAsync(session.receiveEvents(), session::sendEvent,
            () -> "transport close code=" + session.getCloseCode() + ", reason=" + session.getCloseReason(),
            input, playAudio, drainAudio));
    }

    static Mono<Void> runInteractiveConversationAsync(Flux<ServerEvent> events,
        Function<ClientEvent, Mono<Void>> sendEvent, Supplier<String> closeDetails, Reader input,
        Consumer<byte[]> playAudio, Runnable drainAudio) {
        return Mono.defer(() -> {
            System.out.println("Type a message and press Enter. Blank line, 'exit' or 'quit' ends the session.");
            System.out.println("Interactive voice streams silent audio while you type; voice usage is billable.");
            BufferedReader console = input instanceof BufferedReader ? (BufferedReader) input : new BufferedReader(input);
            GptLiveSampleUtils.AudioTranscriptPrinter transcripts
                = new GptLiveSampleUtils.AudioTranscriptPrinter(System.console() != null, true);
            GptLiveSampleUtils.Greeting greeting = new GptLiveSampleUtils.Greeting();
            Sinks.Empty<Void> ready = Sinks.empty();
            Sinks.Empty<Void> closed = Sinks.empty();
            Sinks.Empty<Void> audioStarted = Sinks.empty();
            Sinks.Empty<Void> stopAudio = Sinks.empty();
            Sinks.Empty<Void> audioStopped = Sinks.empty();
            AtomicBoolean started = new AtomicBoolean();
            AtomicBoolean closing = new AtomicBoolean();
            AtomicBoolean finalized = new AtomicBoolean();
            AtomicBoolean remoteHangup = new AtomicBoolean();
            AtomicReference<Sinks.Empty<Void>> responseCompleted = new AtomicReference<>();
            Mono<Void> receive = events.publishOn(Schedulers.boundedElastic(), 1)
                .takeUntil(ServerEvent::isSessionClosed)
                .doOnNext(event -> {
                    transcripts.print(event);
                    if (event.isSessionOutputAudioDelta()) {
                        byte[] pcm = GptLiveSampleUtils.decodeAudio(event);
                        playAudio.accept(pcm);
                        greeting.onAudio(pcm);
                    } else if (event.isSessionOutputTranscriptDelta()
                        && !event.asSessionOutputTranscriptDelta().delta().trim().isEmpty()) {
                        greeting.onTranscript();
                    }
                    if (event.isSessionStarted()) {
                        if (!started.compareAndSet(false, true)) {
                            throw new IllegalStateException("Duplicate session.started event.");
                        }
                        ready.tryEmitEmpty();
                    } else if (event.isResponseEvent()) {
                        ResponseStreamEvent response = GptLiveSampleUtils.backendEvent(event);
                        if (response.isCompleted()) {
                            Sinks.Empty<Void> completion = responseCompleted.getAndSet(null);
                            if (!started.get() || (completion != null && closing.get())) {
                                throw new IllegalStateException("Unexpected GPT Live response lifecycle event.");
                            }
                            if (completion != null) {
                                completion.tryEmitEmpty();
                            }
                        }
                    } else if (event.isSessionClosed()) {
                        String reason = event.asSessionClosed().reason().toString();
                        remoteHangup.set(started.get() && "remote_hangup".equals(reason));
                        if (!remoteHangup.get() && !(closing.get() && "close_requested".equals(reason))) {
                            throw new IllegalStateException("GPT Live closed before all turns completed. Event: "
                                + event + "; " + closeDetails.get());
                        }
                        finalized.set(true);
                        System.out.println("Live session closed (" + reason + ").");
                        closed.tryEmitEmpty();
                    }
                })
                .then(Mono.defer(() -> {
                    if (!finalized.get()) {
                        return Mono.error(new IllegalStateException("GPT Live disconnected without session.closed."));
                    }
                    return remoteHangup.get() ? Mono.empty() : Mono.<Void>never();
                }));
            Mono<Void> audio = ready.asMono()
                .thenMany(GptLiveSampleUtils.silenceFramesAsync().takeUntilOther(stopAudio.asMono())
                    .concatMap(frame -> sendEvent.apply(frame).doOnSuccess(ignored -> audioStarted.tryEmitEmpty()), 1))
                .then().doOnSuccess(ignored -> audioStopped.tryEmitEmpty()).then(Mono.never());
            Mono<Void> renderTranscripts = greeting.quietPeriodsAsync()
                .doOnNext(ignored -> transcripts.endUtterance()).then();
            Mono<Void> conversation = ready.asMono().timeout(GptLiveSampleUtils.TIMEOUT)
                .then(audioStarted.asMono().timeout(GptLiveSampleUtils.TIMEOUT))
                .then(Mono.defer(() -> sendEvent.apply(GptLiveSampleUtils.commentary("Hi, how can I help you today?"))))
                .timeout(GptLiveSampleUtils.TIMEOUT)
                .then(greeting.awaitPlaybackAsync(drainAudio))
                .then(Mono.fromRunnable(transcripts::endUtterance))
                .then(Mono.defer(() -> readTurns(console, transcripts, sendEvent, responseCompleted)))
                .then(Mono.defer(() -> {
                    closing.set(true);
                    stopAudio.tryEmitEmpty();
                    return audioStopped.asMono().then(sendEvent.apply(GptLiveSampleUtils.closeSession()))
                        .then(closed.asMono()).timeout(GptLiveSampleUtils.TIMEOUT);
                }));
            return Mono.firstWithSignal(receive, audio, renderTranscripts, conversation)
                .doFinally(ignored -> transcripts.endInput());
        });
    }

    static Mono<String> readPromptAsync(Reader input) {
        return readPromptAsync(input, new GptLiveSampleUtils.AudioTranscriptPrinter());
    }

    private static Mono<String> readPromptAsync(Reader input, GptLiveSampleUtils.AudioTranscriptPrinter transcripts) {
        return Mono.defer(() -> {
            BufferedReader reader = input instanceof BufferedReader ? (BufferedReader) input : new BufferedReader(input);
            return Mono.fromCallable(() -> GptLiveSampleUtils.readPromptBlocking(reader, transcripts))
                .subscribeOn(Schedulers.boundedElastic()).doOnCancel(transcripts::endInput);
        });
    }

    private static Mono<Void> readTurns(Reader input, GptLiveSampleUtils.AudioTranscriptPrinter transcripts,
        Function<ClientEvent, Mono<Void>> sendEvent, AtomicReference<Sinks.Empty<Void>> responseCompleted) {
        return readPromptAsync(input, transcripts).flatMap(text -> {
            Sinks.Empty<Void> completion = Sinks.empty();
            responseCompleted.set(completion);
            return sendEvent.apply(GptLiveSampleUtils.userMessage(text))
                .then(sendEvent.apply(GptLiveSampleUtils.responseCreate()))
                .then(completion.asMono()).timeout(GptLiveSampleUtils.TIMEOUT)
                .then(Mono.defer(() -> readTurns(input, transcripts, sendEvent, responseCompleted)));
        });
    }
}
