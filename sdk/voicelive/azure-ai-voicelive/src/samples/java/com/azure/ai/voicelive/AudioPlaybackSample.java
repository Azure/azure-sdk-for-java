// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.voicelive;

import com.azure.ai.voicelive.models.ClientEventConversationItemCreate;
import com.azure.ai.voicelive.models.ClientEventResponseCreate;
import com.azure.ai.voicelive.models.ClientEventSessionUpdate;
import com.azure.ai.voicelive.models.InputAudioFormat;
import com.azure.ai.voicelive.models.InputTextContentPart;
import com.azure.ai.voicelive.models.InteractionModality;
import com.azure.ai.voicelive.models.OpenAIVoice;
import com.azure.ai.voicelive.models.OpenAIVoiceName;
import com.azure.ai.voicelive.models.OutputAudioFormat;
import com.azure.ai.voicelive.models.ServerEventType;
import com.azure.ai.voicelive.models.SessionResponse;
import com.azure.ai.voicelive.models.SessionResponseStatus;
import com.azure.ai.voicelive.models.SessionServerEvent;
import com.azure.ai.voicelive.models.SessionUpdateError;
import com.azure.ai.voicelive.models.SessionUpdateResponseAudioDelta;
import com.azure.ai.voicelive.models.SessionUpdateResponseAudioDone;
import com.azure.ai.voicelive.models.SessionUpdateResponseDone;
import com.azure.ai.voicelive.models.UserMessageItem;
import com.azure.ai.voicelive.models.VoiceLiveSessionOptions;
import com.azure.core.util.BinaryData;
import com.azure.identity.DefaultAzureCredentialBuilder;
import reactor.core.publisher.Mono;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Sample demonstrating how to receive and play audio responses from VoiceLive service.
 *
 * <p>Use this sample when you want to understand downstream audio playback only. It is a good next
 * step after the basic sample because it avoids microphone capture and focuses on speaker output.</p>
 *
 * <p>When you run it, the sample sends a fixed text prompt, asks the model to generate an audio
 * response, and plays the returned PCM audio through your default speaker or headphones.</p>
 *
 * <p>This sample shows how to:</p>
 * <ul>
 *   <li>Send a text message to trigger an audio response</li>
 *   <li>Subscribe to audio response events</li>
 *   <li>Receive audio data in chunks</li>
 *   <li>Play audio through speakers in real-time</li>
 *   <li>Handle audio playback threading</li>
 *   <li>Manage audio queue for smooth playback</li>
 * </ul>
 *
 * <p><strong>Related Samples:</strong></p>
 * <ul>
 *   <li>{@link BasicVoiceConversationSample} - Learn the basics first</li>
 *   <li>{@link AuthenticationMethodsSample} - Understand authentication options</li>
 *   <li>{@link MicrophoneInputSample} - Learn audio input (complement to this sample)</li>
 *   <li>{@link VoiceAssistantSample} - Complete voice assistant combining input and output</li>
 * </ul>
 *
 * <p><strong>Environment Variables:</strong></p>
 * <ul>
 *   <li>AZURE_VOICELIVE_ENDPOINT - (Required) The VoiceLive service endpoint URL</li>
 * </ul>
 *
 * <p>This sample uses {@link DefaultAzureCredentialBuilder} (Entra ID, recommended). For an example
 * of API key authentication, see {@link AuthenticationMethodsSample}.</p>
 *
 * <p><strong>Audio Requirements:</strong></p>
 * Requires working speakers or headphones. Audio format is 24kHz, 16-bit PCM, mono.
 *
 * <p><strong>Note:</strong> This sample sends a text message to trigger an audio response.
 * You should hear the assistant speak through your speakers/headphones.</p>
 *
 * <p><strong>How to Run:</strong></p>
 * <pre>{@code
 * mvn exec:java -Dexec.mainClass="com.azure.ai.voicelive.AudioPlaybackSample" -Dexec.classpathScope=test
 * }</pre>
 */
public final class AudioPlaybackSample {

    // Audio format constants required by VoiceLive
    private static final int SAMPLE_RATE = 24000;     // 24kHz
    private static final int CHANNELS = 1;            // Mono
    private static final int SAMPLE_SIZE_BITS = 16;   // 16-bit PCM
    private static final int CHUNK_SIZE = 1200;       // 50ms chunks
    private static final int AUDIO_QUEUE_CAPACITY = 1000;
    private static final long COMPLETION_TIMEOUT_SECONDS = 60;

    /**
     * Main method to run the audio playback sample.
     *
     * @param args Unused command line arguments
     */
    public static void main(String[] args) {
        String endpoint = System.getenv("AZURE_VOICELIVE_ENDPOINT");

        if (endpoint == null) {
            System.err.println("Please set AZURE_VOICELIVE_ENDPOINT environment variable");
            return;
        }

        if (!checkSpeakerAvailable()) {
            System.err.println("No compatible speaker found");
            return;
        }

        VoiceLiveAsyncClient client = new VoiceLiveClientBuilder()
            .endpoint(endpoint)
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildAsyncClient();

        System.out.println("Starting audio playback sample...");

        PlaybackController playback;
        try {
            playback = startPlayback();
        } catch (LineUnavailableException e) {
            System.err.println("Failed to start speaker: " + e.getMessage());
            return;
        }

        Mono<Void> sample = Mono.usingWhen(
            client.startSession("gpt-realtime", null),
            session -> {
                Mono<Void> responseCompletion = configureSession(session)
                    .then(sendPrompt(session))
                    .thenMany(session.receiveEvents().concatMap(event -> handleEvent(event, playback)))
                    .filter(Boolean::booleanValue)
                    .next()
                    .switchIfEmpty(Mono.error(new IllegalStateException(
                        "Event stream completed before a successful response.done event")))
                    .then();
                Mono<Void> playbackFailure = playback.completion().then(Mono.<Void>never());
                return Mono.firstWithSignal(responseCompletion, playbackFailure);
            },
            VoiceLiveSessionAsyncClient::closeAsync)
            .timeout(Duration.ofSeconds(COMPLETION_TIMEOUT_SECONDS))
            .doOnError(playback::abort)
            .doFinally(signalType -> playback.close());

        try {
            sample.block();
            System.out.println("\nSample completed - all queued audio was played and drained");
        } catch (Exception error) {
            System.err.println("Audio playback sample failed: " + rootMessage(error));
        }
    }

    private static Mono<Void> configureSession(VoiceLiveSessionAsyncClient session) {
        VoiceLiveSessionOptions sessionOptions = new VoiceLiveSessionOptions()
            .setInstructions("You are a helpful assistant. Respond to user messages with clear, friendly audio.")
            .setVoice(BinaryData.fromObject(new OpenAIVoice(OpenAIVoiceName.ALLOY)))
            .setModalities(Arrays.asList(InteractionModality.TEXT, InteractionModality.AUDIO))
            .setInputAudioFormat(InputAudioFormat.PCM16)
            .setOutputAudioFormat(OutputAudioFormat.PCM16)
            .setInputAudioSamplingRate(SAMPLE_RATE);

        return session.sendEvent(new ClientEventSessionUpdate(sessionOptions));
    }

    private static Mono<Void> sendPrompt(VoiceLiveSessionAsyncClient session) {
        InputTextContentPart textContent = new InputTextContentPart(
            "Please say 'Hello! This is a test of the audio playback system.' in a friendly voice.");
        UserMessageItem messageItem = new UserMessageItem(Collections.singletonList(textContent));
        ClientEventConversationItemCreate createEvent = new ClientEventConversationItemCreate().setItem(messageItem);

        return session.sendEvent(createEvent).then(session.sendEvent(new ClientEventResponseCreate()));
    }

    private static boolean checkSpeakerAvailable() {
        try {
            AudioFormat format = new AudioFormat(SAMPLE_RATE, SAMPLE_SIZE_BITS, CHANNELS, true, false);
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
            return AudioSystem.isLineSupported(info);
        } catch (Exception e) {
            return false;
        }
    }

    private static PlaybackController startPlayback() throws LineUnavailableException {
        AudioFormat format = new AudioFormat(
            AudioFormat.Encoding.PCM_SIGNED,
            SAMPLE_RATE,
            SAMPLE_SIZE_BITS,
            CHANNELS,
            CHANNELS * SAMPLE_SIZE_BITS / 8,
            SAMPLE_RATE,
            false);

        DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
        SourceDataLine speaker = (SourceDataLine) AudioSystem.getLine(info);
        speaker.open(format, CHUNK_SIZE * 4);
        speaker.start();

        PlaybackController playback = new PlaybackController(new SourceDataLineOutput(speaker), AUDIO_QUEUE_CAPACITY);
        playback.start();
        System.out.println("Audio playback started");
        return playback;
    }

    /**
     * Handles one server event and reports whether the successful terminal response was reached.
     */
    static Mono<Boolean> handleEvent(SessionServerEvent event, PlaybackController playback) {
        ServerEventType eventType = event.getType();

        if (eventType == ServerEventType.SESSION_CREATED) {
            System.out.println("Session created");
        } else if (eventType == ServerEventType.SESSION_UPDATED) {
            System.out.println("Session updated - ready to receive audio");
        } else if (event instanceof SessionUpdateResponseAudioDelta) {
            byte[] audioData = ((SessionUpdateResponseAudioDelta) event).getDelta();
            if (audioData != null && audioData.length > 0 && !playback.queueAudio(audioData)) {
                System.err.println("Warning: audio queue full, dropping chunk of " + audioData.length + " bytes");
            }
        } else if (event instanceof SessionUpdateResponseAudioDone) {
            SessionUpdateResponseAudioDone audioDone = (SessionUpdateResponseAudioDone) event;
            System.out.println("Audio response complete: responseId=" + audioDone.getResponseId()
                + ", itemId=" + audioDone.getItemId()
                + ", outputIndex=" + audioDone.getOutputIndex()
                + ", contentIndex=" + audioDone.getContentIndex()
                + ", acceptedBytes=" + playback.getAcceptedAudioBytes()
                + ", droppedChunks=" + playback.getDroppedAudioChunks());
        } else if (event instanceof SessionUpdateResponseDone) {
            SessionResponse response = ((SessionUpdateResponseDone) event).getResponse();
            String responseId = response == null ? null : response.getId();
            SessionResponseStatus status = response == null ? null : response.getStatus();
            System.out.println("Response complete: responseId=" + responseId + ", status=" + status);

            try {
                validateCompletedResponse(responseId, status, playback.getAcceptedAudioBytes());
            } catch (IllegalStateException error) {
                return Mono.error(error);
            }

            return playback.finishGracefully().thenReturn(true);
        } else if (event instanceof SessionUpdateError) {
            SessionUpdateError errorEvent = (SessionUpdateError) event;
            String message = errorEvent.getError() == null ? "Unknown VoiceLive error" : errorEvent.getError().getMessage();
            return Mono.error(new IllegalStateException(message));
        }

        return Mono.just(false);
    }

    static void validateCompletedResponse(String responseId, SessionResponseStatus status, long acceptedAudioBytes) {
        if (!SessionResponseStatus.COMPLETED.equals(status)) {
            throw new IllegalStateException(
                "Response " + responseId + " ended with status " + status + " instead of completed");
        }
        if (acceptedAudioBytes == 0) {
            throw new IllegalStateException("Response " + responseId + " completed without any playable audio");
        }
    }

    private static String rootMessage(Throwable error) {
        Throwable current = error;
        while (current.getCause() != null) {
            current = current.getCause();
        }
        return current.getMessage() == null ? current.getClass().getSimpleName() : current.getMessage();
    }

    interface AudioOutput {
        boolean isOpen();

        int write(byte[] audioData, int offset, int length);

        void drain();

        void stop();

        void flush();

        void close();
    }

    private static final class SourceDataLineOutput implements AudioOutput {
        private final SourceDataLine speaker;

        private SourceDataLineOutput(SourceDataLine speaker) {
            this.speaker = speaker;
        }

        @Override
        public boolean isOpen() {
            return speaker.isOpen();
        }

        @Override
        public int write(byte[] audioData, int offset, int length) {
            return speaker.write(audioData, offset, length);
        }

        @Override
        public void drain() {
            speaker.drain();
        }

        @Override
        public void stop() {
            speaker.stop();
        }

        @Override
        public void flush() {
            speaker.flush();
        }

        @Override
        public void close() {
            speaker.close();
        }
    }

    enum PlaybackState {
        RUNNING,
        DRAIN_REQUESTED,
        COMPLETED,
        ABORTED
    }

    private static final byte[] DRAIN_MARKER = new byte[0];
    private static final byte[] ABORT_MARKER = new byte[0];

    /**
     * Owns the playback worker and signals completion only after every queued packet is written and drained.
     */
    static final class PlaybackController implements AutoCloseable {
        private final AudioOutput output;
        private final Object lifecycleLock = new Object();
        private final BlockingQueue<byte[]> queue;
        private final Semaphore audioQueueSlots;
        private final CompletableFuture<Void> completion = new CompletableFuture<>();
        private final AtomicReference<PlaybackState> state = new AtomicReference<>(PlaybackState.RUNNING);
        private final AtomicLong acceptedAudioBytes = new AtomicLong();
        private final AtomicLong droppedAudioChunks = new AtomicLong();
        private final Thread playbackThread;

        PlaybackController(AudioOutput output, int audioQueueCapacity) {
            if (audioQueueCapacity <= 0) {
                throw new IllegalArgumentException("audioQueueCapacity must be positive");
            }
            this.output = output;
            // The semaphore bounds audio packets while the command queue always accepts terminal markers.
            this.queue = new LinkedBlockingQueue<>();
            this.audioQueueSlots = new Semaphore(audioQueueCapacity);
            this.playbackThread = new Thread(this::playbackLoop, "AudioPlayback");
            this.playbackThread.setDaemon(true);
        }

        void start() {
            playbackThread.start();
        }

        boolean queueAudio(byte[] audioData) {
            if (audioData == null || audioData.length == 0) {
                return false;
            }
            synchronized (lifecycleLock) {
                if (state.get() != PlaybackState.RUNNING) {
                    return false;
                }
                if (!audioQueueSlots.tryAcquire()) {
                    droppedAudioChunks.incrementAndGet();
                    return false;
                }

                queue.offer(audioData);
                acceptedAudioBytes.addAndGet(audioData.length);
                return true;
            }
        }

        Mono<Void> finishGracefully() {
            synchronized (lifecycleLock) {
                if (state.compareAndSet(PlaybackState.RUNNING, PlaybackState.DRAIN_REQUESTED)) {
                    queue.offer(DRAIN_MARKER);
                }
                if (state.get() == PlaybackState.ABORTED) {
                    return Mono.error(new IllegalStateException("Playback was aborted"));
                }
            }
            return Mono.fromFuture(completion);
        }

        void abort(Throwable cause) {
            synchronized (lifecycleLock) {
                PlaybackState current = state.get();
                if (current == PlaybackState.COMPLETED || current == PlaybackState.ABORTED) {
                    return;
                }
                state.set(PlaybackState.ABORTED);
                queue.clear();
                queue.offer(ABORT_MARKER);
            }

            shutdownOutput(true);
            playbackThread.interrupt();
            completion.completeExceptionally(cause == null
                ? new IllegalStateException("Playback aborted")
                : cause);
        }

        long getAcceptedAudioBytes() {
            return acceptedAudioBytes.get();
        }

        long getDroppedAudioChunks() {
            return droppedAudioChunks.get();
        }

        PlaybackState getState() {
            return state.get();
        }

        Mono<Void> completion() {
            return Mono.fromFuture(completion);
        }

        private void playbackLoop() {
            Throwable failure = null;
            try {
                while (true) {
                    byte[] audioData = queue.take();
                    if (audioData == DRAIN_MARKER) {
                        output.drain();
                        synchronized (lifecycleLock) {
                            state.compareAndSet(PlaybackState.DRAIN_REQUESTED, PlaybackState.COMPLETED);
                        }
                        break;
                    }
                    if (audioData == ABORT_MARKER) {
                        break;
                    }

                    audioQueueSlots.release();
                    if (state.get() != PlaybackState.ABORTED) {
                        writeFully(audioData);
                    }
                }
            } catch (InterruptedException error) {
                Thread.currentThread().interrupt();
                if (state.get() != PlaybackState.ABORTED) {
                    failure = error;
                    state.set(PlaybackState.ABORTED);
                }
            } catch (Throwable error) {
                failure = error;
                state.set(PlaybackState.ABORTED);
            } finally {
                shutdownOutput(state.get() == PlaybackState.ABORTED);
                if (state.get() == PlaybackState.COMPLETED) {
                    completion.complete(null);
                } else if (!completion.isDone()) {
                    completion.completeExceptionally(failure == null
                        ? new IllegalStateException("Playback aborted before drain completed")
                        : failure);
                }
            }
        }

        private void writeFully(byte[] audioData) {
            if (!output.isOpen()) {
                throw new IllegalStateException("Speaker closed before queued audio was written");
            }
            int bytesWritten = output.write(audioData, 0, audioData.length);
            if (bytesWritten != audioData.length) {
                throw new IllegalStateException(
                    "Speaker wrote " + bytesWritten + " of " + audioData.length + " queued bytes");
            }
        }

        private void shutdownOutput(boolean flush) {
            if (flush) {
                try {
                    output.flush();
                } catch (Exception ignored) {
                    // Best-effort abort cleanup.
                }
            }
            try {
                output.stop();
            } catch (Exception ignored) {
                // Best-effort cleanup.
            }
            try {
                output.close();
            } catch (Exception ignored) {
                // Best-effort cleanup.
            }
        }

        @Override
        public void close() {
            PlaybackState currentState = state.get();
            if (currentState != PlaybackState.COMPLETED && currentState != PlaybackState.ABORTED) {
                abort(new IllegalStateException("Playback closed before graceful completion"));
            }
        }
    }

    private AudioPlaybackSample() {
    }
}
