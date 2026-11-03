// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.voice;

import com.azure.ai.agents.BetaVoiceAgentsLiveWebSocketSessionAsyncClient;
import com.azure.ai.agents.AgentsClientBuilder;
import com.azure.core.util.Configuration;
import com.azure.ai.agents.models.VoiceAgentLiveWebSocketConnectionOptions;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.openai.models.live.ServerEvent;
import com.openai.models.responses.ResponseStreamEvent;
import java.io.Console;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Schedulers;
import com.openai.models.live.ClientEvent;
import java.io.BufferedReader;
import java.io.Reader;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;


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
            Mono.usingWhen(agent.builder.beta().buildBetaVoiceAgentsLiveWebSocketAsyncClient()
                .openWebSocketSession(agent.name, new VoiceAgentLiveWebSocketConnectionOptions().setStoreEnabled(store)),
                session -> args.length == 0
                    ? runInteractiveConversationAsync(session,
                        console == null ? new InputStreamReader(System.in) : console.reader())
                    : runConversation(session, Arrays.asList(args)),
                BetaVoiceAgentsLiveWebSocketSessionAsyncClient::closeAsync,
                (session, error) -> session.closeAsync(),
                BetaVoiceAgentsLiveWebSocketSessionAsyncClient::closeAsync).block();
        } finally {
            Schedulers.shutdownNow();
        }
    }

    static Mono<Void> runConversation(BetaVoiceAgentsLiveWebSocketSessionAsyncClient session, List<String> prompts) {
        return Mono.defer(() -> {
            Sinks.Empty<Void> ready = Sinks.empty();
            Sinks.Empty<Void> closed = Sinks.empty();
            AtomicBoolean started = new AtomicBoolean();
            AtomicBoolean closing = new AtomicBoolean();
            AtomicBoolean finalized = new AtomicBoolean();
            AtomicBoolean remoteHangup = new AtomicBoolean();
            AtomicReference<Sinks.Empty<Void>> responseCompleted = new AtomicReference<>();
            Mono<Void> receive = session.receiveEvents()
                .publishOn(Schedulers.boundedElastic(), 1)
                .takeUntil(ServerEvent::isSessionClosed)
                .doOnNext(event -> {
                    GptLiveSampleUtils.printEvent(event);
                    if (event.isSessionStarted()) {
                        if (!started.compareAndSet(false, true)) {
                            throw new IllegalStateException("Duplicate session.started event.");
                        }
                        ready.tryEmitEmpty();
                    } else if (event.isResponseEvent()) {
                        ResponseStreamEvent response = GptLiveSampleUtils.backendEvent(event);
                        if (response.isCompleted()) {
                            Sinks.Empty<Void> completion = responseCompleted.getAndSet(null);
                            if (!started.get() || completion == null || closing.get()) {
                                throw new IllegalStateException("Unexpected GPT Live response lifecycle event.");
                            }
                            System.out.println();
                            completion.tryEmitEmpty();
                        }
                    } else if (event.isSessionClosed()) {
                        String reason = event.asSessionClosed().reason().toString();
                        remoteHangup.set(started.get() && "remote_hangup".equals(reason));
                        if (!remoteHangup.get() && !(closing.get() && "close_requested".equals(reason))) {
                            throw new IllegalStateException("GPT Live closed before all turns completed. Event: "
                                + event + "; transport close code=" + session.getCloseCode()
                                + ", reason=" + session.getCloseReason());
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
            Mono<Void> conversation = ready.asMono().timeout(GptLiveSampleUtils.TIMEOUT)
                .thenMany(Flux.fromIterable(prompts).concatMap(prompt -> Mono.defer(() -> {
                    Sinks.Empty<Void> completion = Sinks.empty();
                    responseCompleted.set(completion);
                    System.out.println("You: " + prompt);
                    System.out.print("Assistant: ");
                    return session.sendEvent(GptLiveSampleUtils.userMessage(prompt))
                        .then(session.sendEvent(GptLiveSampleUtils.responseCreate()))
                        .then(completion.asMono()).timeout(GptLiveSampleUtils.TIMEOUT);
                }), 1))
                .then(Mono.defer(() -> {
                    closing.set(true);
                    return session.sendEvent(GptLiveSampleUtils.closeSession())
                        .then(closed.asMono()).timeout(GptLiveSampleUtils.TIMEOUT);
                }));
            return Mono.firstWithSignal(receive, conversation);
        });
    }

    static Mono<Void> runInteractiveConversationAsync(BetaVoiceAgentsLiveWebSocketSessionAsyncClient session, Reader input) {
        return Mono.usingWhen(
            Mono.fromCallable(VoiceAgentRealtimeSampleUtils.SpeakerPlayer::new)
                .subscribeOn(Schedulers.boundedElastic()),
            player -> runInteractiveConversationAsync(session, input, player::play, player::drain),
            player -> Mono.<Void>fromRunnable(() -> {
                boolean speakerEnabled = player.isEnabled();
                try {
                    player.close();
                } finally {
                    System.out.printf("(received %.2fs of reply audio%s)%n",
                        player.getSecondsReceived(), speakerEnabled ? " and played it" : "");
                }
            }).subscribeOn(Schedulers.boundedElastic()));
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
            return runConversationAsync(events, sendEvent, closeDetails, transcripts -> readPromptAsync(console, transcripts),
                playAudio, drainAudio, true, null);
        });
    }

    static Mono<String> readPromptAsync(Reader input) {
        return readPromptAsync(input, new GptLiveSampleUtils.AudioTranscriptPrinter());
    }

    private static Mono<String> readPromptAsync(Reader input, GptLiveSampleUtils.AudioTranscriptPrinter transcripts) {
        return Mono.defer(() -> {
            BufferedReader reader = input instanceof BufferedReader ? (BufferedReader) input : new BufferedReader(input);
            return Mono.fromCallable(() -> GptLiveSampleUtils.readPromptBlocking(reader, transcripts))
                .subscribeOn(Schedulers.boundedElastic())
                .doOnCancel(transcripts::endInput);
        });
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
