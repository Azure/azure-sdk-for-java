// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.voice;

import com.azure.ai.agents.BetaVoiceAgentLiveWebSocketSessionAsyncClient;
import com.azure.ai.agents.AgentsClientBuilder;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Configuration;
import com.azure.ai.agents.models.VoiceAgentLiveWebSocketConnectionOptions;
import com.azure.identity.DefaultAzureCredentialBuilder;
import java.io.BufferedReader;
import java.io.Console;
import java.io.InputStreamReader;
import java.io.Reader;
import java.time.Duration;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Consumer;
import java.util.function.Supplier;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
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
                    ? runConversation(session, console == null ? new InputStreamReader(System.in) : console.reader())
                    : runConversation(session, Arrays.asList(args)),
                BetaVoiceAgentLiveWebSocketSessionAsyncClient::closeAsync,
                (session, error) -> session.closeAsync(),
                BetaVoiceAgentLiveWebSocketSessionAsyncClient::closeAsync).block();
        } finally {
            Schedulers.shutdownNow();
        }
    }

    static Mono<Void> runConversation(BetaVoiceAgentLiveWebSocketSessionAsyncClient session, List<String> prompts) {
        return Mono.defer(() -> {
            Iterator<String> pending = prompts.iterator();
            return runConversation(session, transcripts -> Mono.defer(() -> {
                if (!pending.hasNext()) {
                    return Mono.empty();
                }
                String prompt = pending.next();
                System.out.println("You: " + prompt);
                return Mono.just(prompt);
            }), pcm -> { }, () -> { }, false);
        });
    }

    static Mono<Void> runConversation(BetaVoiceAgentLiveWebSocketSessionAsyncClient session, Reader input) {
        return Mono.usingWhen(
            Mono.fromCallable(VoiceAgentRealtimeSampleUtils.SpeakerPlayer::new)
                .subscribeOn(Schedulers.boundedElastic()),
            player -> runConversation(session, input, player::play, player::drain),
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

    static Mono<Void> runConversation(BetaVoiceAgentLiveWebSocketSessionAsyncClient session, Reader input,
        Consumer<byte[]> playAudio) {
        return runConversation(session, input, playAudio, () -> { });
    }

    static Mono<Void> runConversation(BetaVoiceAgentLiveWebSocketSessionAsyncClient session, Reader input,
        Consumer<byte[]> playAudio, Runnable drainAudio) {
        return Mono.defer(() -> runInteractiveConversation(session.receiveEvents(), session::sendEvent,
            () -> "transport close code=" + session.getCloseCode() + ", reason=" + session.getCloseReason(),
            input, playAudio, drainAudio));
    }

    static Mono<Void> runInteractiveConversation(Flux<BinaryData> events,
        Function<BinaryData, Mono<Void>> sendEvent, Supplier<String> closeDetails, Reader input,
        Consumer<byte[]> playAudio, Runnable drainAudio) {
        return Mono.defer(() -> {
            System.out.println("Type a message and press Enter. Blank line, 'exit' or 'quit' ends the session.");
            System.out.println("Interactive voice streams silent audio while you type; voice usage is billable.");
            BufferedReader console = input instanceof BufferedReader ? (BufferedReader) input : new BufferedReader(input);
            return runConversation(events, sendEvent, closeDetails, transcripts -> readPrompt(console, transcripts),
                playAudio, drainAudio, true);
        });
    }

    private static Mono<Void> runConversation(BetaVoiceAgentLiveWebSocketSessionAsyncClient session,
        Function<GptLiveSampleUtils.AudioTranscriptPrinter, Mono<String>> nextPrompt,
        Consumer<byte[]> playAudio, Runnable drainAudio, boolean spoken) {
        return Mono.defer(() -> runConversation(session.receiveEvents(), session::sendEvent,
            () -> "transport close code=" + session.getCloseCode() + ", reason=" + session.getCloseReason(),
            nextPrompt, playAudio, drainAudio, spoken));
    }

    private static Mono<Void> runConversation(Flux<BinaryData> events,
        Function<BinaryData, Mono<Void>> sendEvent, Supplier<String> closeDetails,
        Function<GptLiveSampleUtils.AudioTranscriptPrinter, Mono<String>> nextPrompt,
        Consumer<byte[]> playAudio, Runnable drainAudio, boolean spoken) {
        return Mono.defer(() -> {
            AtomicBoolean finalized = new AtomicBoolean();
            AtomicBoolean started = new AtomicBoolean();
            AtomicBoolean closeRequested = new AtomicBoolean();
            AtomicBoolean remoteHangup = new AtomicBoolean();
            GptLiveSampleUtils.AudioTranscriptPrinter transcripts
                = new GptLiveSampleUtils.AudioTranscriptPrinter(System.console() != null, spoken);
            Greeting greeting = new Greeting();
            Sinks.Empty<Void> ready = Sinks.empty();
            Sinks.Empty<Void> closed = Sinks.empty();
            Sinks.Empty<Void> audioStarted = Sinks.empty();
            Sinks.Empty<Void> stopAudio = Sinks.empty();
            Sinks.Empty<Void> audioStopped = Sinks.empty();
            AtomicReference<Sinks.Empty<Void>> responseCompleted = new AtomicReference<>();
            Mono<Void> receive = events
                .publishOn(Schedulers.boundedElastic(), 1)
                .doOnNext(data -> {
                    Map<?, ?> event = GptLiveSampleUtils.object(data);
                    String eventType = GptLiveSampleUtils.type(event);
                    if (spoken) {
                        transcripts.print(event);
                        if ("session.output_audio.delta".equals(eventType)) {
                            byte[] pcm = GptLiveAudioConversationAsyncSample.decodeAudio(event);
                            playAudio.accept(pcm);
                            greeting.onAudio(pcm);
                        } else if ("session.output_transcript.delta".equals(eventType)
                            && !GptLiveSampleUtils.string(event, "delta").trim().isEmpty()) {
                            greeting.onTranscript();
                        }
                    } else {
                        GptLiveSampleUtils.printEvent(event);
                    }
                    String type = GptLiveSampleUtils.type(GptLiveSampleUtils.backendEvent(event));
                    if ("session.started".equals(type)) {
                        if (!started.compareAndSet(false, true)) {
                            throw new IllegalStateException("Duplicate session.started event.");
                        }
                        ready.tryEmitEmpty();
                    } else if ("response.completed".equals(type)) {
                        Sinks.Empty<Void> completion = responseCompleted.getAndSet(null);
                        if (spoken && started.get() && completion == null) {
                            return;
                        }
                        if (!started.get() || closeRequested.get() || completion == null) {
                            throw new IllegalStateException("Unexpected GPT Live response lifecycle event.");
                        }
                        if (!spoken) {
                            System.out.println();
                        }
                        completion.tryEmitEmpty();
                    } else if ("session.closed".equals(type)) {
                        boolean remote = started.get() && "remote_hangup".equals(event.get("reason"));
                        boolean requested = closeRequested.get() && "close_requested".equals(event.get("reason"));
                        if (!requested && !remote) {
                            throw new IllegalStateException("GPT Live closed before all turns completed. Event: "
                                + data + "; " + closeDetails.get());
                        }
                        remoteHangup.set(remote);
                        System.out.println("Live session closed (" + event.get("reason") + ").");
                        finalized.set(true);
                        closed.tryEmitEmpty();
                    }
                })
                .takeUntil(data -> "session.closed".equals(GptLiveSampleUtils.type(GptLiveSampleUtils.object(data))))
                .then(Mono.defer(() -> remoteHangup.get() ? Mono.empty() : finalized.get() ? Mono.never()
                    : Mono.error(new IllegalStateException("GPT Live disconnected without session.closed."))));
            Mono<Void> audio = ready.asMono()
                .thenMany(silenceFrames().takeUntilOther(stopAudio.asMono())
                    .concatMap(frame -> sendEvent.apply(frame).doOnSuccess(ignored -> audioStarted.tryEmitEmpty()), 1))
                .then()
                .doOnSuccess(ignored -> audioStopped.tryEmitEmpty())
                .then(Mono.never());
            Mono<Void> renderTranscripts = greeting.quietPeriods()
                .doOnNext(ignored -> transcripts.endUtterance()).then();
            Mono<Void> conversation = ready.asMono().timeout(GptLiveSampleUtils.TIMEOUT)
                .then(Mono.defer(() -> spoken ? audioStarted.asMono().timeout(GptLiveSampleUtils.TIMEOUT) : Mono.empty()))
                .then(Mono.defer(() -> spoken
                    ? sendEvent.apply(GptLiveSampleUtils.commentary("Hi, how can I help you today?"))
                        .timeout(GptLiveSampleUtils.TIMEOUT)
                        .then(greeting.awaitPlayback(drainAudio))
                        .then(Mono.fromRunnable(transcripts::endUtterance))
                    : Mono.empty()))
                .then(Mono.defer(() -> prompt(sendEvent, nextPrompt, responseCompleted, spoken, transcripts)))
                .then(Mono.defer(() -> {
                    closeRequested.set(true);
                    stopAudio.tryEmitEmpty();
                    return (spoken ? audioStopped.asMono() : Mono.<Void>empty())
                        .then(sendEvent.apply(GptLiveSampleUtils.event("session.close")))
                        .then(closed.asMono()).timeout(GptLiveSampleUtils.TIMEOUT);
                }));
            return spoken ? Mono.firstWithSignal(receive, audio, renderTranscripts, conversation)
                : Mono.firstWithSignal(receive, conversation);
        });
    }

    private static Mono<Void> prompt(Function<BinaryData, Mono<Void>> sendEvent,
        Function<GptLiveSampleUtils.AudioTranscriptPrinter, Mono<String>> nextPrompt,
        AtomicReference<Sinks.Empty<Void>> responseCompleted, boolean spoken,
        GptLiveSampleUtils.AudioTranscriptPrinter transcripts) {
        return nextPrompt.apply(transcripts).flatMap(text -> {
            Sinks.Empty<Void> completion = Sinks.empty();
            responseCompleted.set(completion);
            if (!spoken) {
                System.out.print("Assistant: ");
                System.out.flush();
            }
            return sendEvent.apply(GptLiveSampleUtils.userMessage(text))
                .then(sendEvent.apply(GptLiveSampleUtils.event("response.create")))
                .then(completion.asMono()).timeout(GptLiveSampleUtils.TIMEOUT)
                .thenReturn(true);
        }).defaultIfEmpty(false)
            .flatMap(keepGoing -> keepGoing
                ? Mono.defer(() -> prompt(sendEvent, nextPrompt, responseCompleted, spoken, transcripts)) : Mono.empty());
    }

    static Flux<BinaryData> silenceFrames() {
        // Twenty milliseconds of 24-kHz mono PCM16LE.
        BinaryData silence = GptLiveSampleUtils.audio(new byte[960]);
        return Mono.just(silence)
            .concatWith(Mono.just(silence).delayElement(Duration.ofMillis(20)).repeat());
    }

    static final class Greeting {
        // PCM16 peak levels up to 256 (about -42 dBFS) are treated as silence for this heuristic.
        static final int SILENCE_PEAK_THRESHOLD = 256;
        private final Sinks.Many<Integer> progress = Sinks.many().replay().latest();
        private int received;

        void onAudio(byte[] pcm) {
            for (int offset = 0; offset < pcm.length; offset += 2) {
                int sample = (short) ((pcm[offset] & 0xff) | (pcm[offset + 1] << 8));
                if (sample < -SILENCE_PEAK_THRESHOLD || sample > SILENCE_PEAK_THRESHOLD) {
                    received |= 1;
                    progress.tryEmitNext(received);
                    return;
                }
            }
        }

        void onTranscript() {
            received |= 2;
            progress.tryEmitNext(received);
        }

        Mono<Void> awaitPlayback(Runnable drainAudio) {
            // Output can continue with silent PCM after speaking; only speech activity restarts this timer.
            return quietPeriods().next()
                .timeout(GptLiveSampleUtils.TIMEOUT)
                .then(Mono.<Void>fromRunnable(drainAudio).subscribeOn(Schedulers.boundedElastic()));
        }

        Flux<Long> quietPeriods() {
            return progress.asFlux().filter(parts -> parts == 3)
                .switchMap(parts -> Mono.delay(Duration.ofSeconds(1)));
        }
    }

    static Mono<String> readPrompt(Reader input) {
        return readPrompt(input, new GptLiveSampleUtils.AudioTranscriptPrinter());
    }

    private static Mono<String> readPrompt(Reader input, GptLiveSampleUtils.AudioTranscriptPrinter transcripts) {
        return Mono.defer(() -> {
            BufferedReader reader = input instanceof BufferedReader ? (BufferedReader) input : new BufferedReader(input);
            return Mono.fromCallable(() -> {
                transcripts.beginInput();
                String line;
                try {
                    line = reader.readLine();
                } finally {
                    transcripts.endInput();
                }
                if (line == null) {
                    return null;
                }
                String text = line.trim();
                return text.isEmpty() || "exit".equalsIgnoreCase(text) || "quit".equalsIgnoreCase(text) ? null : text;
            }).subscribeOn(Schedulers.boundedElastic())
                .doOnCancel(transcripts::endInput);
        });
    }
}
