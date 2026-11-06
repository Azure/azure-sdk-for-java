// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.voice;

import com.azure.ai.agents.AgentsClient;
import com.azure.ai.agents.AgentsClientBuilder;
import com.azure.ai.agents.models.CreateAgentVersionInput;
import com.azure.ai.agents.models.VoiceAgentLiveDefinition;
import com.azure.ai.agents.models.VoiceAgentLiveResponsesDelegation;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Configuration;
import com.openai.core.JsonValue;
import com.openai.models.live.ClientEvent;
import com.openai.models.live.CommentaryAppendEvent;
import com.openai.models.live.InputAudioAppendEvent;
import com.openai.models.live.ResponseCreateEvent;
import com.openai.models.live.ResponseItemCreateEvent;
import com.openai.models.live.ResponsesDelegationConfig;
import com.openai.models.live.ServerEvent;
import com.openai.models.live.SessionCloseEvent;
import com.openai.models.live.SessionConfig;
import com.openai.models.responses.EasyInputMessage;
import com.openai.models.responses.ResponseInputContent;
import com.openai.models.responses.ResponseInputText;
import com.openai.models.responses.ResponseStreamEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Predicate;
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

    static String readPromptBlocking(BufferedReader reader, AudioTranscriptPrinter transcripts) throws IOException {
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

    static Mono<Void> promptAsync(Function<ClientEvent, Mono<Void>> sendEvent,
        Function<AudioTranscriptPrinter, Mono<String>> nextPrompt,
        AtomicReference<Sinks.Empty<Void>> responseCompleted, boolean spoken,
        AudioTranscriptPrinter transcripts, ConversationEventHandler<?> handler) {
        return nextPrompt.apply(transcripts).flatMap(text -> {
            Sinks.Empty<Void> completion = Sinks.empty();
            responseCompleted.set(completion);
            handler.beginResponse();
            if (!spoken) {
                System.out.print("Assistant: ");
                System.out.flush();
            }
            return sendEvent.apply(userMessage(text))
                .then(sendEvent.apply(responseCreate()))
                .then(completion.asMono()).timeout(TIMEOUT)
                .thenReturn(true);
        }).defaultIfEmpty(false)
            .flatMap(keepGoing -> keepGoing
                ? Mono.defer(() -> promptAsync(sendEvent, nextPrompt, responseCompleted, spoken, transcripts, handler))
                : Mono.empty());
    }

    static Flux<ClientEvent> silenceFramesAsync() {
        // Twenty milliseconds of 24-kHz mono PCM16LE.
        ClientEvent silence = audio(new byte[960]);
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

        Mono<Void> awaitPlaybackAsync(Runnable drainAudio) {
            // Output can continue with silent PCM after speaking; only speech activity restarts this timer.
            return quietPeriodsAsync().next()
                .timeout(TIMEOUT)
                .then(Mono.<Void>fromRunnable(drainAudio).subscribeOn(Schedulers.boundedElastic()));
        }

        Flux<Long> quietPeriodsAsync() {
            return progress.asFlux().filter(parts -> parts == 3)
                .switchMap(parts -> Mono.delay(Duration.ofSeconds(1)));
        }
    }

    abstract static class ConversationEventHandler<T> {
        private final boolean spoken;
        private final Supplier<String> closeDetails;
        private final Predicate<String> acceptClose;
        private volatile boolean started;
        private volatile boolean awaitingResponse;
        private volatile boolean closing;
        private volatile boolean finalized;
        private volatile boolean remoteHangup;

        ConversationEventHandler(boolean spoken, Supplier<String> closeDetails, Predicate<String> acceptClose) {
            this.spoken = spoken;
            this.closeDetails = closeDetails;
            this.acceptClose = acceptClose;
        }

        final boolean canStreamAudio() {
            return spoken && started && !closing;
        }

        final boolean canReadPrompt(boolean greetingComplete) {
            return started && greetingComplete && !awaitingResponse && !closing;
        }

        final void beginResponse() {
            awaitingResponse = true;
        }

        final void requestClose() {
            closing = true;
        }

        final boolean awaitConversationAfterReceive() {
            if (!finalized) {
                throw new IllegalStateException("GPT Live disconnected without session.closed.");
            }
            return !remoteHangup;
        }

        final T process(ServerEvent event) {
            if (event.isSessionStarted()) {
                if (started) {
                    throw new IllegalStateException("Duplicate session.started event.");
                }
                started = true;
                return onStarted();
            }
            ResponseStreamEvent backend = backendEvent(event);
            if (backend != null) {
                if (backend.isCompleted()) {
                    // Spoken greetings or unsolicited responses have no pending prompt, even during closure.
                    if (spoken && started && !awaitingResponse) {
                        return ignored();
                    }
                    // Otherwise, completion requires a pending response in a started session that is not closing.
                    if (!started || !awaitingResponse || closing) {
                        throw new IllegalStateException("Unexpected GPT Live response lifecycle event.");
                    }
                }
                return onResponse(backend);
            }
            if (event.isSessionClosed()) {
                String reason = event.asSessionClosed().reason().toString();
                boolean remote = started && "remote_hangup".equals(reason);
                boolean accepted = acceptClose.test(reason);
                // Allow a remote hangup after startup, a caller-accepted reason, or acknowledgment of our close request.
                if (!remote && !accepted && !(closing && "close_requested".equals(reason))) {
                    throw new IllegalStateException("GPT Live closed before all turns completed. Event: " + event
                        + "; " + closeDetails.get());
                }
                remoteHangup = remote || accepted;
                finalized = true;
                System.out.println("Live session closed (" + reason + ").");
                return onClosed();
            }
            return ignored();
        }

        final boolean completeResponse(boolean continuation) {
            if (continuation) {
                return false;
            }
            awaitingResponse = false;
            if (!spoken) {
                System.out.println();
            }
            return true;
        }

        abstract T onStarted();

        abstract T onResponse(ResponseStreamEvent event);

        abstract T onClosed();

        abstract T ignored();
    }

    static byte[] decodeAudio(ServerEvent event) {
        byte[] pcm = Base64.getDecoder().decode(event.asSessionOutputAudioDelta().delta());
        if (pcm.length % 2 != 0) {
            throw new IllegalArgumentException("GPT Live PCM16 audio must contain complete two-byte samples.");
        }
        return pcm;
    }

    static boolean hasSpeech(byte[] pcm) {
        for (int offset = 0; offset < pcm.length; offset += 2) {
            int sample = (short) ((pcm[offset] & 0xff) | (pcm[offset + 1] << 8));
            if (sample < -Greeting.SILENCE_PEAK_THRESHOLD || sample > Greeting.SILENCE_PEAK_THRESHOLD) {
                return true;
            }
        }
        return false;
    }

    static BinaryData event(String type, String field, Object value) {
        Map<String, Object> event = new LinkedHashMap<>();
        event.put("type", type);
        if (field != null) {
            event.put(field, value);
        }
        return BinaryData.fromObject(event);
    }

    static ClientEvent userMessage(String text) {
        return ClientEvent.ofResponseItemCreate(ResponseItemCreateEvent.builder()
            .item(EasyInputMessage.builder()
                .role(EasyInputMessage.Role.USER)
                .type(EasyInputMessage.Type.MESSAGE)
                .contentOfResponseInputMessageContentList(java.util.Collections.singletonList(
                    ResponseInputContent.ofInputText(ResponseInputText.builder().text(text).build())))
                .build())
            .build());
    }

    static ClientEvent audio(byte[] pcm) {
        return ClientEvent.ofSessionInputAudioAppend(InputAudioAppendEvent.builder()
            .audio(Base64.getEncoder().encodeToString(pcm)).build());
    }

    static ClientEvent commentary(String content) {
        return ClientEvent.ofSessionCommentaryAppend(CommentaryAppendEvent.builder()
            .content(content)
            .delegationId(java.util.Optional.empty())
            .build());
    }

    static ClientEvent responseCreate() {
        return ClientEvent.ofResponseCreate(ResponseCreateEvent.builder().build());
    }

    static ClientEvent closeSession() {
        return ClientEvent.ofSessionClose(SessionCloseEvent.builder().build());
    }

    static ResponseStreamEvent backendEvent(ServerEvent event) {
        return event.isResponseEvent()
            ? JsonValue.from(event.asResponseEvent().event()._additionalProperties()).convert(ResponseStreamEvent.class)
            : null;
    }

    static void checkError(ServerEvent event) {
        if (event.isErrorEvent()) {
            com.openai.models.live.Error error = event.asErrorEvent().error();
            boolean moderated = "content_filter".equals(error._code().asString().orElse(null))
                || "content_filter".equals(error._type().asString().orElse(null));
            throw new IllegalStateException(moderated
                ? "GPT Live service error: generation stopped by service moderation "
                    + "(content_filter). The call has ended; review the conversation and service diagnostics. Event: "
                    + event
                : "GPT Live service error: " + event);
        }
        if (event.isResponseEvent()) {
            checkError(backendEvent(event));
        } else if (event._json().isPresent()) {
            checkError(event._json().get().convert(ResponseStreamEvent.class));
        }
    }

    static void checkError(ResponseStreamEvent event) {
        if (event.isError() || event.isFailed() || event.isIncomplete()) {
            Object error = JsonValue.from(event).convert(Map.class).get("error");
            if (error instanceof Map<?, ?> && ("content_filter".equals(((Map<?, ?>) error).get("code"))
                || "content_filter".equals(((Map<?, ?>) error).get("type")))) {
                throw new IllegalStateException("GPT Live service error: generation stopped by service moderation "
                    + "(content_filter). The call has ended; review the conversation and service diagnostics. Event: "
                    + event);
            }
            throw new IllegalStateException("GPT Live service error: " + event);
        }
    }

    static void printEvent(ServerEvent event) {
        checkError(event);
        if (event.isSessionStarted()) {
            JsonValue conversationId = event.asSessionStarted()._additionalProperties().get("conversation_id");
            System.out.println(conversationId == null
                || !conversationId.asString().isPresent()
                ? "Live session started; conversation_id not provided (persistence may be disabled)."
                : "Live session started; conversation_id=" + conversationId.asString().get());
        } else if (event.isSessionInputTranscriptDelta()) {
            System.out.print(event.asSessionInputTranscriptDelta().delta());
        } else if (event.isSessionOutputTranscriptDelta()) {
            System.out.print(event.asSessionOutputTranscriptDelta().delta());
        } else if (event.isResponseEvent()) {
            ResponseStreamEvent backend = backendEvent(event);
            if (backend.isOutputTextDelta()) {
                System.out.print(backend.asOutputTextDelta().delta());
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

        AudioProcessor(TargetDataLine microphone, SourceDataLine speaker, Function<BinaryData, Mono<Void>> sendAudio) {
            this.sendAudio = sendAudio;
            this.microphone = microphone;
            this.speaker = speaker;
        }

        Mono<Void> failureSignalAsync() {
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

        synchronized void print(ServerEvent event) {
            checkError(event);
            if (event.isSessionInputTranscriptDelta() || event.isSessionOutputTranscriptDelta()) {
                String delta = event.isSessionInputTranscriptDelta()
                    ? event.asSessionInputTranscriptDelta().delta() : event.asSessionOutputTranscriptDelta().delta();
                if (completeUtterances && event.isSessionOutputTranscriptDelta()) {
                    if (delta.length() > MAX_BUFFERED_TRANSCRIPT_CHARACTERS - pending.length() - utterance.length()) {
                        throw new IllegalStateException("Buffered transcript exceeded 65536 characters.");
                    }
                    utterance.append(delta);
                    return;
                }
                String nextSpeaker = event.isSessionInputTranscriptDelta() ? "You" : "Assistant";
                if (!nextSpeaker.equals(speaker)) {
                    write((lineOpen ? System.lineSeparator() : "") + nextSpeaker + ": ");
                    speaker = nextSpeaker;
                }
                write(delta);
            } else if (event.isSessionStarted()) {
                printEvent(event);
            } else if (event.isSessionClosed()) {
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
        ResponsesDelegationConfig.Builder responses = ResponsesDelegationConfig.builder().model(backendModel)
                .instructions(tools
                    ? "Use get_weather for weather requests. After receiving its result, answer briefly."
                    : "Answer the user briefly and helpfully.");
        if (tools) {
            responses.addTool(GptLiveFunctionToolSample.weatherTool());
        }
        VoiceAgentLiveDefinition definition = new VoiceAgentLiveDefinition(model)
                .setInstructions("You are a helpful voice assistant. Delegate requests to your Responses backend.")
                .setAudio(SessionConfig.Audio.builder()
                    .audioPcmFormat(com.openai.models.live.AudioFormat.AudioPcm.Rate._24000).build())
            .setDelegation(new VoiceAgentLiveResponsesDelegation(responses.build()));
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
