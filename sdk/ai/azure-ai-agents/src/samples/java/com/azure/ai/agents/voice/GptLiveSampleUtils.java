// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.voice;

import com.azure.ai.agents.AgentsClient;
import com.azure.ai.agents.AgentsClientBuilder;
import com.azure.ai.agents.BetaVoiceAgentLiveWebSocketSessionAsyncClient;
import com.azure.ai.agents.BetaVoiceAgentLiveWebSocketSessionClient;
import com.azure.ai.agents.BetaVoiceAgentWebSocketSessionAsyncClient;
import com.azure.ai.agents.models.CreateAgentVersionInput;
import com.azure.ai.agents.models.LiveInitialSessionAudioParam;
import com.azure.ai.agents.models.LiveResponsesDelegationParam;
import com.azure.ai.agents.models.LiveResponsesDelegationSettingsInputParam;
import com.azure.ai.agents.models.LiveSessionAudioFormatPCMParam;
import com.azure.ai.agents.models.LiveSessionAudioFormatPCMParamRate;
import com.azure.ai.agents.models.VoiceAgentLiveDefinition;
import com.azure.ai.agents.models.VoiceAgentSubProtocol;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Configuration;
import com.azure.json.JsonProviders;
import com.azure.json.JsonWriter;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Schedulers;

final class GptLiveSampleUtils {
    static final Duration TIMEOUT = Duration.ofSeconds(60);

    private GptLiveSampleUtils() {
    }

    static String requireSetting(Configuration configuration, String name) {
        String value = configuration.get(name);
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Set environment variable " + name + " before running this sample.");
        }
        return value;
    }

    /**
     * Microphone entry point called by {@link GptLiveAudioConversationAsyncSample}.
     */
    static Mono<Void> runConversation(BetaVoiceAgentLiveWebSocketSessionAsyncClient session) {
        return Mono.defer(() -> runConversation(session,
            new AudioProcessor(
                null, null, pcm -> session.sendEvent(audio(pcm.toBytes()))), System.in));
    }

    static Mono<Void> runConversation(BetaVoiceAgentLiveWebSocketSessionAsyncClient session,
        AudioProcessor processor, InputStream input) {
        return Mono.usingWhen(Mono.just(processor),
            audio -> Mono.firstWithSignal(runConversation(session.receiveEvents(), session::sendEvent,
                () -> "transport close code=" + session.getCloseCode() + ", reason=" + session.getCloseReason(),
                transcripts -> VoiceAgentLiveAudioConversationAsyncSample.waitForEnter(input)
                    .then(closeAudio(audio)).then(Mono.<String>empty()),
                audio::queueAudio, () -> { }, true, audio), audio.failureSignal()), GptLiveSampleUtils::closeAudio,
            (audio, error) -> closeAudio(audio), GptLiveSampleUtils::closeAudio);
    }

    static byte[] decodeAudio(Map<?, ?> event) {
        byte[] pcm = Base64.getDecoder().decode(string(event, "delta"));
        if (pcm.length % 2 != 0) {
            throw new IllegalArgumentException("GPT Live PCM16 audio must contain complete two-byte samples.");
        }
        return pcm;
    }

    private static Mono<Void> closeAudio(AudioProcessor audio) {
        return Mono.<Void>fromRunnable(audio::close).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Scripted-text entry point called by {@link GptLiveTextConversationSample} when command-line prompts are supplied.
     */
    static void runConversation(BetaVoiceAgentLiveWebSocketSessionClient session, List<String> prompts) {
        runConversation(session, prompts, null);
    }

    /**
     * Called by {@link GptLiveFunctionToolSample}; also serves scripted {@link GptLiveTextConversationSample} calls
     * without a tool handler.
     */
    static void runConversation(BetaVoiceAgentLiveWebSocketSessionClient session, List<String> prompts,
        Function<Map<?, ?>, BinaryData> toolHandler) {
        Iterator<String> pending = prompts.iterator();
        runConversation(session, () -> {
            if (!pending.hasNext()) {
                return null;
            }
            String prompt = pending.next();
            System.out.println("You: " + prompt);
            return prompt;
        }, toolHandler);
    }

    /**
     * Interactive entry point called by {@link GptLiveTextConversationSample} when no command-line prompts are supplied.
     */
    static void runConversation(BetaVoiceAgentLiveWebSocketSessionClient session, Reader input) {
        try (VoiceAgentRealtimeSampleUtils.SpeakerPlayer player = new VoiceAgentRealtimeSampleUtils.SpeakerPlayer()) {
            runConversation(session, input, player::play, player::drain);
            player.drain();
            System.out.printf("(received %.2fs of reply audio%s)%n", player.getSecondsReceived(),
                player.isEnabled() ? " and played it" : "");
        }
    }

    static void runConversation(BetaVoiceAgentLiveWebSocketSessionClient session, Reader input,
        Consumer<byte[]> playAudio, Runnable drainAudio) {
        System.out.println("Type a message and press Enter. Blank line, 'exit' or 'quit' ends the session.");
        System.out.println("Interactive voice streams silent audio while you type; voice usage is billable.");
        BufferedReader console = input instanceof BufferedReader ? (BufferedReader) input : new BufferedReader(input);
        runConversation(session, transcripts -> {
            try {
                return readPromptBlocking(console, transcripts);
            } catch (IOException error) {
                throw new IllegalStateException("Unable to read console input.", error);
            }
        }, playAudio, drainAudio, true, null);
    }

    /**
     * Scripted-text entry point called by {@link GptLiveTextConversationAsyncSample} with command-line prompts.
     */
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

    private static void runConversation(BetaVoiceAgentLiveWebSocketSessionClient session, Supplier<String> nextPrompt,
        Function<Map<?, ?>, BinaryData> toolHandler) {
        runConversation(session, transcripts -> nextPrompt.get(), pcm -> { }, () -> { }, false, toolHandler);
    }

    private static void runConversation(BetaVoiceAgentLiveWebSocketSessionClient session,
        Function<AudioTranscriptPrinter, String> nextPrompt, Consumer<byte[]> playAudio, Runnable drainAudio,
        boolean spoken,
        Function<Map<?, ?>, BinaryData> toolHandler) {
        ExecutorService workers = Executors.newFixedThreadPool(2, task -> {
            Thread thread = new Thread(task, "gpt-live-sync-conversation");
            thread.setDaemon(true);
            return thread;
        });
        BlockingQueue<BinaryData> pending = new LinkedBlockingQueue<>(256);
        AtomicReference<RuntimeException> receiveFailure = new AtomicReference<>();
        AtomicBoolean receiveEnded = new AtomicBoolean();
        AudioTranscriptPrinter transcripts = new AudioTranscriptPrinter(System.console() != null, spoken);
        Future<?> receiver = workers.submit(() -> {
            try {
                for (BinaryData data : session.receiveEvents(TIMEOUT)) {
                    pending.put(data);
                    if ("session.closed".equals(type(object(data)))) {
                        break;
                    }
                }
            } catch (InterruptedException error) {
                Thread.currentThread().interrupt();
            } catch (RuntimeException error) {
                receiveFailure.set(error);
            } finally {
                receiveEnded.set(true);
            }
        });
        List<BinaryData> results = new ArrayList<>();
        Future<String> prompt = null;
        boolean started = false;
        boolean greetingComplete = !spoken;
        boolean greetingAudio = false;
        boolean greetingTranscript = false;
        boolean awaitingResponse = false;
        boolean closing = false;
        int continuations = 0;
        long deadline = System.nanoTime() + TIMEOUT.toNanos();
        long nextAudio = 0;
        long quietSince = 0;
        BinaryData silence = audio(new byte[960]);
        try {
            while (true) {
                BinaryData data = pending.poll(20, TimeUnit.MILLISECONDS);
                long now = System.nanoTime();
                if (data != null) {
                    Map<?, ?> event = object(data);
                    if (spoken) {
                        transcripts.print(event);
                        if ("session.output_audio.delta".equals(type(event))) {
                            byte[] pcm = decodeAudio(event);
                            playAudio.accept(pcm);
                            if (hasSpeech(pcm)) {
                                greetingAudio = true;
                                quietSince = now;
                            }
                        } else if ("session.output_transcript.delta".equals(type(event))
                            && !string(event, "delta").trim().isEmpty()) {
                            greetingTranscript = true;
                            quietSince = now;
                        }
                    } else {
                        printEvent(event);
                    }
                    Map<?, ?> backend = backendEvent(event);
                    String type = type(backend);
                    if ("session.started".equals(type)) {
                        if (started) {
                            throw new IllegalStateException("Duplicate session.started event.");
                        }
                        started = true;
                        if (spoken) {
                            session.sendEvent(silence);
                            nextAudio = now + TimeUnit.MILLISECONDS.toNanos(20);
                            session.sendEvent(commentary("Hi, how can I help you today?"));
                        }
                    } else if ("response.completed".equals(type)) {
                        if (!awaitingResponse && spoken && started) {
                            continue;
                        }
                        if (!awaitingResponse || closing) {
                            throw new IllegalStateException("Unexpected GPT Live response lifecycle event.");
                        }
                        if (results.isEmpty()) {
                            awaitingResponse = false;
                            continuations = 0;
                            if (!spoken) {
                                System.out.println();
                            }
                        } else {
                            if (++continuations > 8) {
                                throw new IllegalStateException("Sample tool continuation limit exceeded.");
                            }
                            for (BinaryData result : results) {
                                session.sendEvent(result);
                            }
                            results.clear();
                            session.sendEvent(event("response.create"));
                        }
                    } else if (toolHandler != null && "response.output_item.done".equals(type)) {
                        Map<?, ?> item = object(backend.get("item"));
                        if ("function_call".equals(type(item))) {
                            results.add(toolHandler.apply(item));
                        }
                    } else if ("session.closed".equals(type)) {
                        if (started && ("remote_hangup".equals(event.get("reason"))
                            || closing && "close_requested".equals(event.get("reason")))) {
                            System.out.println("Live session closed (" + event.get("reason") + ").");
                            return;
                        }
                        throw new IllegalStateException("GPT Live closed before all turns completed. Event: " + data
                            + "; transport close code=" + session.getCloseCode() + ", reason=" + session.getCloseReason());
                    }
                } else if (receiveEnded.get()) {
                    if (receiveFailure.get() != null) {
                        throw receiveFailure.get();
                    }
                    throw new IllegalStateException("GPT Live disconnected without session.closed.");
                }

                if (started && spoken && !closing && now >= nextAudio) {
                    session.sendEvent(silence);
                    nextAudio = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(20);
                }
                if (spoken && greetingAudio && greetingTranscript && quietSince != 0
                    && now - quietSince >= TimeUnit.SECONDS.toNanos(1)) {
                    transcripts.endUtterance();
                    quietSince = 0;
                    if (!greetingComplete) {
                        drainAudio.run();
                        greetingComplete = true;
                    }
                }
                if (started && greetingComplete && !awaitingResponse && !closing && prompt == null) {
                    prompt = workers.submit(() -> nextPrompt.apply(transcripts));
                }
                if (prompt != null && prompt.isDone()) {
                    String text = prompt.get();
                    prompt = null;
                    deadline = System.nanoTime() + TIMEOUT.toNanos();
                    if (text == null) {
                        closing = true;
                        session.sendEvent(event("session.close"));
                    } else {
                        awaitingResponse = true;
                        if (!spoken) {
                            System.out.print("Assistant: ");
                        }
                        session.sendEvent(userMessage(text));
                        session.sendEvent(event("response.create"));
                    }
                }
                if ((!started || !greetingComplete || awaitingResponse || closing) && now >= deadline) {
                    throw new IllegalStateException("Timed out waiting for the GPT Live conversation.");
                }
            }
        } catch (InterruptedException error) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("GPT Live conversation interrupted.", error);
        } catch (ExecutionException error) {
            if (error.getCause() instanceof RuntimeException) {
                throw (RuntimeException) error.getCause();
            }
            throw new IllegalStateException("Unable to read console input.", error.getCause());
        } finally {
            receiver.cancel(true);
            if (prompt != null) {
                prompt.cancel(true);
            }
            transcripts.endInput();
            workers.shutdownNow();
        }
    }

    /**
     * Interactive entry point called by {@link GptLiveTextConversationAsyncSample} without command-line prompts.
     */
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
        Function<AudioTranscriptPrinter, Mono<String>> nextPrompt,
        Consumer<byte[]> playAudio, Runnable drainAudio, boolean spoken) {
        return Mono.defer(() -> runConversation(session.receiveEvents(), session::sendEvent,
            () -> "transport close code=" + session.getCloseCode() + ", reason=" + session.getCloseReason(),
            nextPrompt, playAudio, drainAudio, spoken));
    }

    private static Mono<Void> runConversation(Flux<BinaryData> events,
        Function<BinaryData, Mono<Void>> sendEvent, Supplier<String> closeDetails,
        Function<AudioTranscriptPrinter, Mono<String>> nextPrompt,
        Consumer<byte[]> playAudio, Runnable drainAudio, boolean spoken) {
        return runConversation(events, sendEvent, closeDetails, nextPrompt, playAudio, drainAudio, spoken, null);
    }

    private static Mono<Void> runConversation(Flux<BinaryData> events,
        Function<BinaryData, Mono<Void>> sendEvent, Supplier<String> closeDetails,
        Function<AudioTranscriptPrinter, Mono<String>> nextPrompt,
        Consumer<byte[]> playAudio, Runnable drainAudio, boolean spoken, AudioProcessor microphone) {
        return Mono.defer(() -> {
            boolean interactive = spoken && microphone == null;
            AtomicBoolean finalized = new AtomicBoolean();
            AtomicBoolean started = new AtomicBoolean();
            AtomicBoolean closeRequested = new AtomicBoolean();
            AtomicBoolean remoteHangup = new AtomicBoolean();
            AudioTranscriptPrinter transcripts = new AudioTranscriptPrinter(
                microphone == null && System.console() != null, interactive);
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
                    Map<?, ?> event = object(data);
                    String eventType = type(event);
                    if (spoken) {
                        transcripts.print(event);
                        if ("session.output_audio.delta".equals(eventType)) {
                            byte[] pcm = decodeAudio(event);
                            playAudio.accept(pcm);
                            greeting.onAudio(pcm);
                        } else if ("session.output_transcript.delta".equals(eventType)
                            && !string(event, "delta").trim().isEmpty()) {
                            greeting.onTranscript();
                        }
                    } else {
                        printEvent(event);
                    }
                    String type = type(backendEvent(event));
                    if ("session.started".equals(type)) {
                        if (!started.compareAndSet(false, true)) {
                            throw new IllegalStateException("Duplicate session.started event.");
                        }
                        if (microphone != null) {
                            microphone.start();
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
                        boolean requested = microphone != null
                            || closeRequested.get() && "close_requested".equals(event.get("reason"));
                        if (!requested && !remote) {
                            throw new IllegalStateException("GPT Live closed before all turns completed. Event: "
                                + data + "; " + closeDetails.get());
                        }
                        remoteHangup.set(remote || microphone != null);
                        System.out.println("Live session closed (" + event.get("reason") + ").");
                        finalized.set(true);
                        closed.tryEmitEmpty();
                    }
                })
                .takeUntil(data -> "session.closed".equals(type(object(data))))
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
            Mono<Void> conversation = ready.asMono().timeout(TIMEOUT)
                .then(Mono.defer(() -> interactive ? audioStarted.asMono().timeout(TIMEOUT) : Mono.empty()))
                .then(Mono.defer(() -> interactive
                    ? sendEvent.apply(commentary("Hi, how can I help you today?"))
                        .timeout(TIMEOUT)
                        .then(greeting.awaitPlayback(drainAudio))
                        .then(Mono.fromRunnable(transcripts::endUtterance))
                    : microphone == null ? Mono.empty()
                        : sendEvent.apply(userMessage("Greet me briefly with: Hi, how can I help you today?"))
                            .then(sendEvent.apply(event("response.create")))
                            .then(Mono.fromRunnable(() -> System.out.println(
                                "Opening greeting requested. Speak into the microphone. Press Enter to end the call.")))))
                .then(Mono.defer(() -> prompt(sendEvent, nextPrompt, responseCompleted, spoken, transcripts)))
                .then(Mono.defer(() -> {
                    closeRequested.set(true);
                    stopAudio.tryEmitEmpty();
                    return (interactive ? audioStopped.asMono() : Mono.<Void>empty())
                        .then(sendEvent.apply(event("session.close")))
                        .then(closed.asMono()).timeout(TIMEOUT);
                }));
            return interactive ? Mono.firstWithSignal(receive, audio, renderTranscripts, conversation)
                : Mono.firstWithSignal(receive, conversation);
        });
    }

    private static Mono<Void> prompt(Function<BinaryData, Mono<Void>> sendEvent,
        Function<AudioTranscriptPrinter, Mono<String>> nextPrompt,
        AtomicReference<Sinks.Empty<Void>> responseCompleted, boolean spoken,
        AudioTranscriptPrinter transcripts) {
        return nextPrompt.apply(transcripts).flatMap(text -> {
            Sinks.Empty<Void> completion = Sinks.empty();
            responseCompleted.set(completion);
            if (!spoken) {
                System.out.print("Assistant: ");
                System.out.flush();
            }
            return sendEvent.apply(userMessage(text))
                .then(sendEvent.apply(event("response.create")))
                .then(completion.asMono()).timeout(TIMEOUT)
                .thenReturn(true);
        }).defaultIfEmpty(false)
            .flatMap(keepGoing -> keepGoing
                ? Mono.defer(() -> prompt(sendEvent, nextPrompt, responseCompleted, spoken, transcripts)) : Mono.empty());
    }

    static Flux<BinaryData> silenceFrames() {
        // Twenty milliseconds of 24-kHz mono PCM16LE.
        BinaryData silence = audio(new byte[960]);
        return Mono.just(silence)
            .concatWith(Mono.just(silence).delayElement(Duration.ofMillis(20)).repeat());
    }

    static final class Greeting {
        // PCM16 peak levels up to 256 (about -42 dBFS) are treated as silence for this heuristic.
        static final int SILENCE_PEAK_THRESHOLD = 256;
        private final Sinks.Many<Integer> progress = Sinks.many().replay().latest();
        private int received;

        void onAudio(byte[] pcm) {
            if (hasSpeech(pcm)) {
                received |= 1;
                progress.tryEmitNext(received);
            }
        }

        void onTranscript() {
            received |= 2;
            progress.tryEmitNext(received);
        }

        Mono<Void> awaitPlayback(Runnable drainAudio) {
            // Output can continue with silent PCM after speaking; only speech activity restarts this timer.
            return quietPeriods().next()
                .timeout(TIMEOUT)
                .then(Mono.<Void>fromRunnable(drainAudio).subscribeOn(Schedulers.boundedElastic()));
        }

        Flux<Long> quietPeriods() {
            return progress.asFlux().filter(parts -> parts == 3)
                .switchMap(parts -> Mono.delay(Duration.ofSeconds(1)));
        }
    }

    static Mono<String> readPrompt(Reader input) {
        return readPrompt(input, new AudioTranscriptPrinter());
    }

    private static Mono<String> readPrompt(Reader input, AudioTranscriptPrinter transcripts) {
        return Mono.defer(() -> {
            BufferedReader reader = input instanceof BufferedReader ? (BufferedReader) input : new BufferedReader(input);
            return Mono.fromCallable(() -> readPromptBlocking(reader, transcripts)).subscribeOn(Schedulers.boundedElastic())
                .doOnCancel(transcripts::endInput);
        });
    }

    private static String readPromptBlocking(BufferedReader reader, AudioTranscriptPrinter transcripts) throws IOException {
        transcripts.beginInput();
        try {
            String line = reader.readLine();
            if (line == null) {
                return null;
            }
            String text = line.trim();
            return text.isEmpty() || "exit".equalsIgnoreCase(text) || "quit".equalsIgnoreCase(text) ? null : text;
        } finally {
            transcripts.endInput();
        }
    }

    private static boolean hasSpeech(byte[] pcm) {
        for (int offset = 0; offset < pcm.length; offset += 2) {
            int sample = (short) ((pcm[offset] & 0xff) | (pcm[offset + 1] << 8));
            if (sample < -Greeting.SILENCE_PEAK_THRESHOLD || sample > Greeting.SILENCE_PEAK_THRESHOLD) {
                return true;
            }
        }
        return false;
    }

    static Map<?, ?> object(BinaryData data) {
        return data.toObject(Map.class);
    }

    static Map<?, ?> object(Object value) {
        if (!(value instanceof Map<?, ?>)) {
            throw new IllegalArgumentException("Expected a native Live JSON object: " + value);
        }
        return (Map<?, ?>) value;
    }

    static String type(Map<?, ?> event) {
        return string(event, "type");
    }

    static String string(Map<?, ?> event, String field) {
        Object value = event.get(field);
        if (!(value instanceof String)) {
            throw new IllegalArgumentException("Missing or invalid Live event field: " + field);
        }
        return (String) value;
    }

    static BinaryData event(String type) {
        return event(type, null, null);
    }

    static BinaryData event(String type, String field, Object value) {
        Map<String, Object> event = new LinkedHashMap<>();
        event.put("type", type);
        if (field != null) {
            event.put(field, value);
        }
        return BinaryData.fromObject(event);
    }

    static BinaryData userMessage(String text) {
        Map<String, Object> part = new LinkedHashMap<>();
        part.put("type", "input_text");
        part.put("text", text);
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("type", "message");
        item.put("role", "user");
        item.put("content", java.util.Collections.singletonList(part));
        return event("response.item.create", "item", item);
    }

    static BinaryData audio(byte[] pcm) {
        return event("session.input_audio.append", "audio", Base64.getEncoder().encodeToString(pcm));
    }

    static BinaryData commentary(String content) {
        try (ByteArrayOutputStream output = new ByteArrayOutputStream();
            JsonWriter writer = JsonProviders.createWriter(output)) {
            writer.writeStartObject()
                .writeStringField("type", "session.commentary.append")
                .writeStringField("content", content)
                .writeNullField("delegation_id")
                .writeEndObject()
                .flush();
            return BinaryData.fromBytes(output.toByteArray());
        } catch (IOException error) {
            throw new IllegalStateException("Unable to serialize GPT Live commentary.", error);
        }
    }

    static Map<?, ?> backendEvent(Map<?, ?> event) {
        return "response.event".equals(type(event)) ? object(event.get("event")) : event;
    }

    static void checkError(Map<?, ?> event) {
        if ("error".equals(type(event)) || "response.failed".equals(type(event))
            || "response.incomplete".equals(type(event))) {
            Object error = event.get("error");
            if (error instanceof Map<?, ?> && ("content_filter".equals(((Map<?, ?>) error).get("code"))
                || "content_filter".equals(((Map<?, ?>) error).get("type")))) {
                throw new IllegalStateException("GPT Live service error: generation stopped by service moderation "
                    + "(content_filter). The call has ended; review the conversation and service diagnostics. Event: "
                    + BinaryData.fromObject(event));
            }
            throw new IllegalStateException("GPT Live service error: " + BinaryData.fromObject(event));
        }
    }

    static void printEvent(Map<?, ?> event) {
        checkError(event);
        if ("session.started".equals(type(event))) {
            Object conversationId = event.get("conversation_id");
            System.out.println(conversationId == null
                ? "Live session started; conversation_id not provided (persistence may be disabled)."
                : "Live session started; conversation_id=" + conversationId);
        } else if ("session.input_transcript.delta".equals(type(event))) {
            System.out.print(string(event, "delta"));
        } else if ("session.output_transcript.delta".equals(type(event))) {
            System.out.print(string(event, "delta"));
        } else if ("response.event".equals(type(event))) {
            Map<?, ?> backend = backendEvent(event);
            checkError(backend);
            if ("response.output_text.delta".equals(type(backend))) {
                System.out.print(string(backend, "delta"));
            }
        }
    }

    static final class AudioProcessor implements AutoCloseable {
        private static final Duration SEND_TIMEOUT = Duration.ofSeconds(10);
        private static final int CHUNK_BYTES = 2400;
        static final int MAX_PLAYBACK_BYTES = VoiceAgentRealtimeSampleUtils.SAMPLE_RATE * 2 * 60;
        private static final byte[] STOP = new byte[0];
        private final Function<BinaryData, Mono<Void>> sendAudio;
        private final AudioFormat format = new AudioFormat(VoiceAgentRealtimeSampleUtils.SAMPLE_RATE, 16, 1, true, false);
        private final BlockingQueue<byte[]> playback = new LinkedBlockingQueue<>(MAX_PLAYBACK_BYTES / 2);
        private int queuedPlaybackBytes;
        private final AtomicBoolean running = new AtomicBoolean();
        private final Sinks.Empty<Void> failure = Sinks.empty();
        private boolean closed;
        private TargetDataLine microphone;
        private SourceDataLine speaker;
        private Thread captureThread;
        private Thread playbackThread;

        AudioProcessor(BetaVoiceAgentWebSocketSessionAsyncClient session) {
            this(session, null, null);
        }

        AudioProcessor(BetaVoiceAgentWebSocketSessionAsyncClient session, TargetDataLine microphone, SourceDataLine speaker) {
            this(microphone, speaker, pcm -> session.appendInputAudio(pcm));
        }

        AudioProcessor(TargetDataLine microphone, SourceDataLine speaker, Function<BinaryData, Mono<Void>> sendAudio) {
            this.sendAudio = sendAudio;
            this.microphone = microphone;
            this.speaker = speaker;
        }

        Mono<Void> failureSignal() {
            return failure.asMono();
        }

        synchronized void start() {
            if (closed) {
                throw new IllegalStateException("Audio processor is already closed.");
            }
            try {
                if (microphone == null) {
                    microphone = AudioSystem.getTargetDataLine(format);
                }
                microphone.open(format, CHUNK_BYTES * 4);
                if (speaker == null) {
                    speaker = AudioSystem.getSourceDataLine(format);
                }
                speaker.open(format);
                microphone.start();
                speaker.start();
            } catch (LineUnavailableException | IllegalArgumentException error) {
                close();
                throw new IllegalStateException("A 24-kHz mono PCM16 microphone and speaker are required.", error);
            }

            running.set(true);
            captureThread = new Thread(this::capture, "voice-agent-microphone");
            playbackThread = new Thread(this::playback, "voice-agent-speaker");
            captureThread.setDaemon(true);
            playbackThread.setDaemon(true);
            captureThread.start();
            playbackThread.start();
        }

        private void capture() {
            byte[] buffer = new byte[CHUNK_BYTES];
            try {
                while (running.get()) {
                    int read = microphone.read(buffer, 0, buffer.length);
                    if (read > 0 && running.get()) {
                        sendAudio.apply(BinaryData.fromBytes(Arrays.copyOf(buffer, read))).block(SEND_TIMEOUT);
                    }
                }
            } catch (RuntimeException error) {
                fail(error);
            }
        }

        private void playback() {
            try {
                while (running.get()) {
                    byte[] pcm = playback.take();
                    if (pcm == STOP) {
                        break;
                    }
                    synchronized (playback) {
                        queuedPlaybackBytes -= pcm.length;
                    }
                    speaker.write(pcm, 0, pcm.length);
                }
            } catch (InterruptedException error) {
                Thread.currentThread().interrupt();
                fail(error);
            } catch (RuntimeException error) {
                fail(error);
            }
        }

        void queueAudio(byte[] pcm) {
            if (pcm == null || pcm.length == 0) {
                return;
            }
            if (pcm.length % 2 != 0) {
                fail(new IllegalArgumentException("PCM16 audio must contain complete two-byte samples."));
                return;
            }
            synchronized (playback) {
                if (!running.get()) {
                    return;
                }
                if (pcm.length <= MAX_PLAYBACK_BYTES - queuedPlaybackBytes && playback.offer(pcm)) {
                    queuedPlaybackBytes += pcm.length;
                    return;
                }
            }
            fail(new IllegalStateException("Audio playback backlog exceeded 60 seconds."));
        }

        private void fail(Throwable error) {
            if (running.compareAndSet(true, false)) {
                failure.tryEmitError(error);
            }
        }

        synchronized void skipPendingAudio() {
            clearPlayback();
            if (speaker != null) {
                speaker.flush();
            }
        }

        @Override
        public synchronized void close() {
            if (closed) {
                return;
            }
            closed = true;
            running.set(false);
            clearPlayback();
            playback.offer(STOP);
            try {
                closeLine(microphone);
            } finally {
                try {
                    closeLine(speaker);
                } finally {
                    if (captureThread != null) {
                        captureThread.interrupt();
                    }
                    if (playbackThread != null) {
                        playbackThread.interrupt();
                    }
                    join(captureThread);
                    join(playbackThread);
                }
            }
        }

        private void clearPlayback() {
            synchronized (playback) {
                byte[] discarded;
                while ((discarded = playback.poll()) != null) {
                    queuedPlaybackBytes -= discarded.length;
                }
            }
        }

        private static void closeLine(javax.sound.sampled.DataLine line) {
            if (line != null) {
                try {
                    line.stop();
                } finally {
                    line.close();
                }
            }
        }

        private static void join(Thread thread) {
            if (thread != null && thread != Thread.currentThread()) {
                try {
                    thread.join(SEND_TIMEOUT.toMillis());
                    if (thread.isAlive()) {
                        System.err.println("Audio thread did not stop: " + thread.getName());
                    }
                } catch (InterruptedException error) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    static final class AudioTranscriptPrinter {
        static final int MAX_BUFFERED_TRANSCRIPT_CHARACTERS = 65536;
        private String speaker;
        private boolean readingInput;
        private boolean lineOpen;
        private final boolean inputEchoed;
        private final boolean completeUtterances;
        private final StringBuilder pending = new StringBuilder();
        private final StringBuilder utterance = new StringBuilder();

        AudioTranscriptPrinter() {
            this(false);
        }

        AudioTranscriptPrinter(boolean inputEchoed) {
            this(inputEchoed, false);
        }

        AudioTranscriptPrinter(boolean inputEchoed, boolean completeUtterances) {
            this.inputEchoed = inputEchoed;
            this.completeUtterances = completeUtterances;
        }

        synchronized void beginInput() {
            if (readingInput) {
                throw new IllegalStateException("A console read is already active.");
            }
            if (lineOpen) {
                System.out.println();
            }
            speaker = null;
            lineOpen = false;
            readingInput = true;
            System.out.print("You: ");
            System.out.flush();
        }

        synchronized void endInput() {
            if (!readingInput) {
                return;
            }
            readingInput = false;
            if (!inputEchoed) {
                System.out.println();
            }
            if (pending.length() > 0) {
                System.out.print(pending);
                pending.setLength(0);
            }
            speaker = null;
        }

        synchronized void beginTurn() {
            speaker = null;
        }

        synchronized void endUtterance() {
            if (utterance.length() == 0) {
                return;
            }
            String spokenText = utterance.toString().trim();
            if (spokenText.isEmpty()) {
                utterance.setLength(0);
                return;
            }
            String text = (lineOpen ? System.lineSeparator() : "") + "Assistant: " + spokenText
                + System.lineSeparator();
            if (readingInput && text.length() > MAX_BUFFERED_TRANSCRIPT_CHARACTERS - pending.length()) {
                throw new IllegalStateException("Buffered transcript exceeded 65536 characters while reading input.");
            }
            utterance.setLength(0);
            speaker = null;
            write(text);
        }

        synchronized void print(Map<?, ?> event) {
            checkError(event);
            String eventType = type(event);
            if ("response.event".equals(eventType)) {
                checkError(backendEvent(event));
            } else if ("session.input_transcript.delta".equals(eventType)
                || "session.output_transcript.delta".equals(eventType)) {
                if (completeUtterances && "session.output_transcript.delta".equals(eventType)) {
                    String delta = string(event, "delta");
                    if (delta.length() > MAX_BUFFERED_TRANSCRIPT_CHARACTERS - pending.length() - utterance.length()) {
                        throw new IllegalStateException("Buffered transcript exceeded 65536 characters.");
                    }
                    utterance.append(delta);
                    return;
                }
                String nextSpeaker = "session.input_transcript.delta".equals(eventType) ? "You" : "Assistant";
                if (!nextSpeaker.equals(speaker)) {
                    write((lineOpen ? System.lineSeparator() : "") + nextSpeaker + ": ");
                    speaker = nextSpeaker;
                }
                write(string(event, "delta"));
            } else if ("session.started".equals(eventType)) {
                printEvent(event);
            } else if ("session.closed".equals(eventType)) {
                endUtterance();
                if (lineOpen) {
                    write(System.lineSeparator());
                }
                speaker = null;
            }
        }

        private void write(String text) {
            if (readingInput) {
                if (text.length() > MAX_BUFFERED_TRANSCRIPT_CHARACTERS - pending.length() - utterance.length()) {
                    throw new IllegalStateException("Buffered transcript exceeded 65536 characters while reading input.");
                }
                pending.append(text);
            } else {
                System.out.print(text);
            }
            if (!text.isEmpty()) {
                lineOpen = !text.endsWith("\n");
            }
        }
    }

    static AgentResource createAgent(AgentsClientBuilder builder, String model, String backendModel,
        String scenario, boolean tools, boolean keepAgent) {
        LiveResponsesDelegationSettingsInputParam responses = new LiveResponsesDelegationSettingsInputParam(backendModel)
                .setInstructions(tools
                    ? "Use get_weather for weather requests. After receiving its result, answer briefly."
                    : "Answer the user briefly and helpfully.");
        if (tools) {
            responses.setTools(java.util.Collections.singletonList(GptLiveFunctionToolSample.weatherTool()));
        }
        VoiceAgentLiveDefinition definition = new VoiceAgentLiveDefinition(VoiceAgentSubProtocol.LIVE, model)
                .setInstructions("You are a helpful voice assistant. Delegate requests to your Responses backend.")
                .setAudio(new LiveInitialSessionAudioParam().setFormat(new LiveSessionAudioFormatPCMParam(
                    LiveSessionAudioFormatPCMParamRate.TWO_FOUR_ZERO_ZERO_ZERO)))
                .setDelegation(BinaryData.fromObject(new LiveResponsesDelegationParam(responses)));
        String agentName = createAgentName(scenario);
        AgentsClient agents = builder.buildAgentsClient();
        agents.createAgentVersion(agentName, new CreateAgentVersionInput(definition));
        System.out.println("Created GPT Live agent: " + agentName);
        return new AgentResource(builder, agents, agentName, keepAgent);
    }

    static String createAgentName(String scenario) {
        return "gpt-live-" + scenario + "-" + UUID.randomUUID();
    }

    static final class AgentResource implements AutoCloseable {
        final AgentsClientBuilder builder;
        final String name;
        private final AgentsClient agents;
        private final boolean keep;

        AgentResource(AgentsClientBuilder builder, AgentsClient agents, String name, boolean keep) {
            this.builder = builder;
            this.agents = agents;
            this.name = name;
            this.keep = keep;
        }

        @Override
        public void close() {
            if (keep) {
                System.out.println("Kept GPT Live agent: " + name);
            } else {
                agents.deleteAgent(name);
                System.out.println("Deleted GPT Live agent: " + name);
            }
        }
    }
}
