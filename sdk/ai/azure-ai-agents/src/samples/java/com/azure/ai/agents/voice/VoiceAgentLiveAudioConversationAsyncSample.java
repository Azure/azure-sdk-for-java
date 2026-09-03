// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.voice;

import com.azure.ai.agents.AgentsAsyncClient;
import com.azure.ai.agents.AgentsClientBuilder;
import com.azure.ai.agents.BetaAgentEndpointConversationsAsyncClient;
import com.azure.ai.agents.BetaVoiceAgentWebSocketAsyncClient;
import com.azure.ai.agents.VoiceAgentWebSocketSessionAsyncClient;
import com.azure.ai.agents.models.CreateAgentVersionInput;
import com.azure.ai.agents.models.RealtimeServerEventConversationItemInputAudioTranscriptionCompleted;
import com.azure.ai.agents.models.RealtimeServerEventInputAudioBufferSpeechStarted;
import com.azure.ai.agents.models.RealtimeServerEventRealtimeServerEventError;
import com.azure.ai.agents.models.RealtimeServerEventResponseAudioDelta;
import com.azure.ai.agents.models.RealtimeServerEventResponseAudioTranscriptDone;
import com.azure.ai.agents.models.RealtimeServerEventResponseCreated;
import com.azure.ai.agents.models.RealtimeServerEventResponseDone;
import com.azure.ai.agents.models.RealtimeServerEventSessionCreated;
import com.azure.ai.agents.models.VoiceAgentDefinition;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;
import java.time.Duration;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Demonstrates an asynchronous hands-free voice conversation using Java Sound and server-side VAD.
 *
 * <p>Before running the sample, set these environment variables:</p>
 * <ul>
 *   <li>{@code FOUNDRY_PROJECT_ENDPOINT} - The Azure AI Project endpoint.</li>
 *   <li>{@code FOUNDRY_VOICE_AGENT_NAME} - Optional. The voice agent name. Defaults to
 *   {@code sample-live-audio-conversation-agent-async-java}.</li>
 * </ul>
 */
public class VoiceAgentLiveAudioConversationAsyncSample {
    private static final Duration SEND_TIMEOUT = Duration.ofSeconds(10);

    public static void main(String[] args) {
        Configuration configuration = Configuration.getGlobalConfiguration();
        String endpoint = configuration.get("FOUNDRY_PROJECT_ENDPOINT");
        String agentName = configuration.get("FOUNDRY_VOICE_AGENT_NAME",
            "sample-live-audio-conversation-agent-async-java");

        AgentsClientBuilder builder = new AgentsClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint(endpoint)
            .allowPreview(true);
        AgentsAsyncClient agents = builder.buildAgentsAsyncClient();
        BetaVoiceAgentWebSocketAsyncClient realtime = builder.buildBetaVoiceAgentWebSocketAsyncClient();
        BetaAgentEndpointConversationsAsyncClient conversations
            = builder.buildBetaAgentEndpointConversationsAsyncClient();

        Map<String, String> request = new LinkedHashMap<>();
        request.put("kind", "voice");
        request.put("name", agentName);
        AtomicReference<String> conversationId = new AtomicReference<>();

        agents.generateAgent(BinaryData.fromObject(request))
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
            .then(agents.deleteAgent(agentName))
            .onErrorResume(error -> agents.deleteAgent(agentName)
                .onErrorResume(cleanupError -> Mono.empty())
                .then(Mono.error(error)))
            .doOnSuccess(ignored -> System.out.println("Deleted voice agent: " + agentName))
            .block();
    }

    private static Mono<Void> runConversation(VoiceAgentWebSocketSessionAsyncClient session,
        AtomicReference<String> conversationId) {
        return Mono.using(() -> new AudioProcessor(session), processor -> {
            processor.start();
            AtomicBoolean responseActive = new AtomicBoolean();
            Disposable receiver = session.receiveEvents().subscribe(event -> {
                if (event instanceof RealtimeServerEventSessionCreated) {
                    String id = ((RealtimeServerEventSessionCreated) event).getConversationId();
                    if (id != null) {
                        conversationId.set(id);
                    }
                } else if (event instanceof RealtimeServerEventInputAudioBufferSpeechStarted) {
                    if (responseActive.get()) {
                        processor.skipPendingAudio();
                        session.cancelResponse().onErrorResume(error -> Mono.empty()).subscribe();
                        System.out.println("(listening...)");
                    }
                } else if (event instanceof RealtimeServerEventConversationItemInputAudioTranscriptionCompleted) {
                    System.out.println("You:  "
                        + ((RealtimeServerEventConversationItemInputAudioTranscriptionCompleted) event)
                            .getTranscript().trim());
                } else if (event instanceof RealtimeServerEventResponseCreated) {
                    responseActive.set(true);
                } else if (event instanceof RealtimeServerEventResponseDone) {
                    responseActive.set(false);
                } else if (event instanceof RealtimeServerEventResponseAudioDelta) {
                    processor.queueAudio(((RealtimeServerEventResponseAudioDelta) event).getDelta());
                } else if (event instanceof RealtimeServerEventResponseAudioTranscriptDone) {
                    System.out.println("Agent: "
                        + ((RealtimeServerEventResponseAudioTranscriptDone) event).getTranscript());
                } else if (event instanceof RealtimeServerEventRealtimeServerEventError) {
                    RealtimeServerEventRealtimeServerEventError error
                        = (RealtimeServerEventRealtimeServerEventError) event;
                    System.out.println("Session error: " + error.getError().getMessage());
                }
            }, error -> System.err.println("Realtime session ended: " + error.getMessage()));
            System.out.println("Speak now; talk over the agent to interrupt it. Press Enter to end the session.");
            return Mono.fromRunnable(() -> new Scanner(System.in).nextLine())
                .subscribeOn(Schedulers.boundedElastic())
                .doFinally(signal -> receiver.dispose())
                .then();
        }, AudioProcessor::close);
    }

    private static final class AudioProcessor implements AutoCloseable {
        private static final int CHUNK_BYTES = 2400;
        private static final byte[] STOP = new byte[0];
        private final VoiceAgentWebSocketSessionAsyncClient session;
        private final AudioFormat format = new AudioFormat(VoiceAgentRealtimeSampleUtils.SAMPLE_RATE, 16, 1, true, false);
        private final BlockingQueue<byte[]> playback = new LinkedBlockingQueue<>();
        private final AtomicBoolean running = new AtomicBoolean();
        private TargetDataLine microphone;
        private SourceDataLine speaker;
        private Thread captureThread;
        private Thread playbackThread;

        AudioProcessor(VoiceAgentWebSocketSessionAsyncClient session) {
            this.session = session;
        }

        void start() {
            try {
                microphone = AudioSystem.getTargetDataLine(format);
                microphone.open(format, CHUNK_BYTES * 4);
                speaker = AudioSystem.getSourceDataLine(format);
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
            while (running.get()) {
                int read = microphone.read(buffer, 0, buffer.length);
                if (read > 0) {
                    try {
                        session.appendInputAudio(BinaryData.fromBytes(Arrays.copyOf(buffer, read))).block(SEND_TIMEOUT);
                    } catch (RuntimeException error) {
                        if (running.get()) {
                            System.err.println("Microphone upload stopped: " + error.getMessage());
                        }
                        running.set(false);
                    }
                }
            }
        }

        private void playback() {
            try {
                while (running.get()) {
                    byte[] pcm = playback.take();
                    if (pcm == STOP) {
                        break;
                    }
                    speaker.write(pcm, 0, pcm.length);
                }
            } catch (InterruptedException error) {
                Thread.currentThread().interrupt();
            }
        }

        void queueAudio(byte[] pcm) {
            if (pcm != null && pcm.length > 0) {
                playback.offer(pcm);
            }
        }

        void skipPendingAudio() {
            playback.clear();
            if (speaker != null) {
                speaker.flush();
            }
        }

        @Override
        public void close() {
            running.set(false);
            playback.clear();
            playback.offer(STOP);
            if (microphone != null) {
                microphone.stop();
                microphone.close();
                microphone = null;
            }
            if (speaker != null) {
                speaker.stop();
                speaker.close();
                speaker = null;
            }
            if (captureThread != null) {
                captureThread.interrupt();
            }
            if (playbackThread != null) {
                playbackThread.interrupt();
            }
        }
    }
}
