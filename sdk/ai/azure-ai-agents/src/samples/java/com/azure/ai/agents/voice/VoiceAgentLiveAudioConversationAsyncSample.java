// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.voice;

import com.azure.ai.agents.AgentsAsyncClient;
import com.azure.ai.agents.AgentsClientBuilder;
import com.azure.ai.agents.BetaVoiceAgentsConversationsAsyncClient;
import com.azure.ai.agents.BetaAgentsAsyncClient;
import com.azure.ai.agents.BetaVoiceAgentWebSocketAsyncClient;
import com.azure.ai.agents.VoiceAgentWebSocketSessionAsyncClient;
import com.azure.ai.agents.models.CreateAgentVersionInput;
import com.azure.ai.agents.models.RealtimeConversationItemInputAudioTranscriptionCompletedEvent;
import com.azure.ai.agents.models.RealtimeInputAudioBufferSpeechStartedEvent;
import com.azure.ai.agents.models.RealtimeErrorEvent;
import com.azure.ai.agents.models.RealtimeResponseAudioDeltaEvent;
import com.azure.ai.agents.models.RealtimeResponseAudioTranscriptDoneEvent;
import com.azure.ai.agents.models.RealtimeResponseCreatedEvent;
import com.azure.ai.agents.models.RealtimeResponseDoneEvent;
import com.azure.ai.agents.models.RealtimeSessionCreatedEvent;
import com.azure.ai.agents.models.VoiceAgentDefinition;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Schedulers;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Demonstrates an asynchronous hands-free voice conversation using Java Sound and server-side VAD.
 *
 * <p>To end the call, focus the terminal running the sample and press Enter. The sample then closes the WebSocket,
 * stops the microphone and speaker, reads the persisted conversation, and deletes the agent unless
 * {@code FOUNDRY_KEEP_VOICE_AGENT} is set to {@code true}.</p>
 *
 * <p>Disconnection or an audio failure also stops the call and releases the audio devices. Up to 60 seconds of PCM
 * audio can wait for playback so that faster-than-realtime responses do not block WebSocket reception. Exceeding
 * that limit ends the call rather than dropping speech or growing memory without a bound. This sample must be the
 * only reader of standard input.</p>
 *
 * <p>Before running the sample, set these environment variables:</p>
 * <ul>
 *   <li>{@code FOUNDRY_PROJECT_ENDPOINT} - The Azure AI Project endpoint.</li>
 *   <li>{@code FOUNDRY_VOICE_AGENT_NAME} - Optional. The voice agent name. Defaults to
 *   {@code sample-live-audio-conversation-agent-async-java}.</li>
 *   <li>{@code FOUNDRY_KEEP_VOICE_AGENT} - Optional. Set to {@code true} to keep the agent after the sample.
 *   Defaults to {@code false}.</li>
 * </ul>
 */
public class VoiceAgentLiveAudioConversationAsyncSample {
    private static final Duration SEND_TIMEOUT = Duration.ofSeconds(10);

    public static void main(String[] args) {
        Configuration configuration = Configuration.getGlobalConfiguration();
        String endpoint = configuration.get("FOUNDRY_PROJECT_ENDPOINT");
        String agentName = configuration.get("FOUNDRY_VOICE_AGENT_NAME",
            "sample-live-audio-conversation-agent-async-java");
        boolean keepAgent
            = Boolean.parseBoolean(configuration.get("FOUNDRY_KEEP_VOICE_AGENT", "false"));

        AgentsClientBuilder builder = new AgentsClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint(endpoint)
            .allowPreview(true);
        AgentsAsyncClient agents = builder.buildAgentsAsyncClient();
        BetaAgentsAsyncClient betaAgents = builder.beta().buildBetaAgentsAsyncClient();
        BetaVoiceAgentWebSocketAsyncClient realtime = builder.beta().buildBetaVoiceAgentWebSocketAsyncClient();
        BetaVoiceAgentsConversationsAsyncClient conversations
            = builder.beta().buildBetaVoiceAgentsConversationsAsyncClient();

        Map<String, String> request = new LinkedHashMap<>();
        request.put("kind", "voice");
        request.put("name", agentName);
        AtomicReference<String> conversationId = new AtomicReference<>();

        betaAgents.createAgentFromPrompt(BinaryData.fromObject(request))
            .flatMap(generated -> {
                VoiceAgentDefinition definition
                    = (VoiceAgentDefinition) generated.getVersions().getLatest().getDefinition();
                return agents.createAgentVersion(agentName,
                    new CreateAgentVersionInput(definition.setStore(true)));
            })
            .then(Mono.usingWhen(realtime.connect(agentName),
                session -> runConversation(session, conversationId),
                VoiceAgentWebSocketSessionAsyncClient::closeAsync,
                (session, error) -> session.closeAsync(),
                VoiceAgentWebSocketSessionAsyncClient::closeAsync))
            .then(Mono.defer(() -> conversationId.get() == null
                ? Mono.fromRunnable(() -> System.out.println("No persisted conversation ID was returned."))
                : VoiceAgentRealtimeSampleUtils.readConversation(conversations, agentName, conversationId.get())))
            .then(Mono.defer(() -> cleanupAgent(agents, agentName, keepAgent)))
            .onErrorResume(error -> Mono.defer(() -> cleanupAgent(agents, agentName, keepAgent))
                .onErrorResume(cleanupError -> Mono.empty())
                .then(Mono.error(error)))
            .block();
    }

    private static Mono<Void> cleanupAgent(AgentsAsyncClient agents, String agentName, boolean keepAgent) {
        if (keepAgent) {
            return Mono.fromRunnable(() -> System.out.println("Kept voice agent: " + agentName));
        }
        return agents.deleteAgent(agentName)
            .doOnSuccess(ignored -> System.out.println("Deleted voice agent: " + agentName));
    }

    private static Mono<Void> runConversation(VoiceAgentWebSocketSessionAsyncClient session,
        AtomicReference<String> conversationId) {
        AudioProcessor processor = new AudioProcessor(session);
        AtomicBoolean responseActive = new AtomicBoolean();
        Mono<Void> receive = session.receiveEvents().concatMap(event -> {
            if (event instanceof RealtimeSessionCreatedEvent) {
                String id = ((RealtimeSessionCreatedEvent) event).getConversationId();
                if (id != null) {
                    conversationId.set(id);
                }
            } else if (event instanceof RealtimeInputAudioBufferSpeechStartedEvent) {
                if (responseActive.get()) {
                    processor.skipPendingAudio();
                    System.out.println("(listening...)");
                    return session.cancelResponse().timeout(SEND_TIMEOUT);
                }
            } else if (event instanceof RealtimeConversationItemInputAudioTranscriptionCompletedEvent) {
                System.out.println("You:  "
                    + ((RealtimeConversationItemInputAudioTranscriptionCompletedEvent) event)
                        .getTranscript().trim());
            } else if (event instanceof RealtimeResponseCreatedEvent) {
                responseActive.set(true);
            } else if (event instanceof RealtimeResponseDoneEvent) {
                responseActive.set(false);
            } else if (event instanceof RealtimeResponseAudioDeltaEvent) {
                processor.queueAudio(((RealtimeResponseAudioDeltaEvent) event).getDelta());
            } else if (event instanceof RealtimeResponseAudioTranscriptDoneEvent) {
                System.out.println("Agent: "
                    + ((RealtimeResponseAudioTranscriptDoneEvent) event).getTranscript());
            } else if (event instanceof RealtimeErrorEvent) {
                RealtimeErrorEvent error
                    = (RealtimeErrorEvent) event;
                System.out.println("Session error: " + error.getError().message());
            }
            return Mono.<Void>empty();
        }).then();
        return runConversation(receive, processor, System.in);
    }

    static Mono<Void> runConversation(Mono<Void> receive, AudioProcessor processor, InputStream input) {
        return Mono.usingWhen(Mono.fromSupplier(() -> processor), audio -> {
            System.out.println("Speak now; talk over the agent to interrupt it. Press Enter to end the session.");
            return Mono.fromRunnable(processor::start)
                .subscribeOn(Schedulers.boundedElastic())
                .then(Mono.firstWithSignal(receive, processor.failure.asMono(), waitForEnter(input)));
        }, VoiceAgentLiveAudioConversationAsyncSample::closeAudio,
            (audio, error) -> closeAudio(audio), VoiceAgentLiveAudioConversationAsyncSample::closeAudio);
    }

    private static Mono<Void> closeAudio(AudioProcessor processor) {
        return Mono.<Void>fromRunnable(processor::close).subscribeOn(Schedulers.boundedElastic());
    }

    static Mono<Void> waitForEnter(InputStream input) {
        return Flux.interval(Duration.ZERO, Duration.ofMillis(100), Schedulers.boundedElastic())
            .handle((tick, sink) -> {
                try {
                    int available = input.available();
                    for (int remaining = available; remaining > 0; remaining--) {
                        int next = input.read();
                        if (next == '\n' || next == '\r' || next == -1) {
                            sink.complete();
                            return;
                        }
                    }
                } catch (IOException error) {
                    sink.error(error);
                }
            }).then();
    }

    static final class AudioProcessor implements AutoCloseable {
        private static final int CHUNK_BYTES = 2400;
        static final int MAX_PLAYBACK_BYTES = VoiceAgentRealtimeSampleUtils.SAMPLE_RATE * 2 * 60;
        private static final byte[] STOP = new byte[0];
        private final VoiceAgentWebSocketSessionAsyncClient session;
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

        AudioProcessor(VoiceAgentWebSocketSessionAsyncClient session) {
            this(session, null, null);
        }

        AudioProcessor(VoiceAgentWebSocketSessionAsyncClient session, TargetDataLine microphone, SourceDataLine speaker) {
            this.session = session;
            this.microphone = microphone;
            this.speaker = speaker;
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
                        session.appendInputAudio(BinaryData.fromBytes(Arrays.copyOf(buffer, read))).block(SEND_TIMEOUT);
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
}
