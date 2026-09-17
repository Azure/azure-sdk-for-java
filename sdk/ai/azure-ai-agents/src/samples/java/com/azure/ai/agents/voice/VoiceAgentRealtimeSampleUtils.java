// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.voice;

import com.azure.ai.agents.BetaVoiceAgentsConversationsAsyncClient;
import com.azure.ai.agents.BetaVoiceAgentsConversationsClient;
import com.azure.ai.agents.models.RealtimeServerEvent;
import com.azure.ai.agents.models.RealtimeErrorEvent;
import com.azure.ai.agents.models.RealtimeResponseAudioDeltaEvent;
import com.azure.ai.agents.models.RealtimeResponseAudioTranscriptDoneEvent;
import com.azure.ai.agents.models.RealtimeResponseDoneEvent;
import com.azure.ai.agents.models.RealtimeSessionCreatedEvent;
import com.azure.ai.agents.models.VoiceConversation;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.util.BinaryData;
import reactor.core.publisher.Mono;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

final class VoiceAgentRealtimeSampleUtils {
    static final int SAMPLE_RATE = 24000;

    private VoiceAgentRealtimeSampleUtils() {
    }

    static boolean handleResponseEvent(RealtimeServerEvent event, AtomicReference<String> conversationId,
        SpeakerPlayer player) {
        if (event instanceof RealtimeSessionCreatedEvent) {
            String id = ((RealtimeSessionCreatedEvent) event).getConversationId();
            if (id != null) {
                conversationId.set(id);
            }
        } else if (event instanceof RealtimeResponseAudioDeltaEvent) {
            player.play(((RealtimeResponseAudioDeltaEvent) event).getDelta());
        } else if (event instanceof RealtimeResponseAudioTranscriptDoneEvent) {
            System.out.println("Agent: "
                + ((RealtimeResponseAudioTranscriptDoneEvent) event).getTranscript());
        } else if (event instanceof RealtimeErrorEvent) {
            RealtimeErrorEvent error
                = (RealtimeErrorEvent) event;
            System.out.println("Session error: " + error.getError().message());
            return true;
        }
        return event instanceof RealtimeResponseDoneEvent;
    }

    static void readConversation(BetaVoiceAgentsConversationsClient conversations, String agentName,
        String conversationId) {
        VoiceConversation conversation = conversations.getAgentConversation(agentName, conversationId);
        System.out.printf("Conversation %s: status=%s, created=%s%n", conversation.getId(),
            conversation.getStatus(), conversation.getCreatedAt());
        for (BinaryData item : conversations.listAgentConversationItems(agentName, conversationId,
            new RequestOptions())) {
            printConversationItem(item);
        }
    }

    static Mono<Void> readConversation(BetaVoiceAgentsConversationsAsyncClient conversations, String agentName,
        String conversationId) {
        return conversations.getAgentConversation(agentName, conversationId)
            .doOnNext(conversation -> System.out.printf("Conversation %s: status=%s, created=%s%n",
                conversation.getId(), conversation.getStatus(), conversation.getCreatedAt()))
            .thenMany(conversations.listAgentConversationItems(agentName, conversationId, new RequestOptions()))
            .doOnNext(VoiceAgentRealtimeSampleUtils::printConversationItem)
            .then();
    }

    @SuppressWarnings("unchecked")
    private static void printConversationItem(BinaryData itemData) {
        Map<String, Object> item = itemData.toObject(Map.class);
        System.out.printf("  - %s id=%s%n", item.get("role") == null ? item.get("type") : item.get("role"),
            item.get("id"));
        Object contentValue = item.get("content");
        if (!(contentValue instanceof List<?>)) {
            return;
        }
        StringBuilder transcript = new StringBuilder();
        for (Object partValue : (List<?>) contentValue) {
            if (partValue instanceof Map<?, ?>) {
                Map<String, Object> part = (Map<String, Object>) partValue;
                Object text = part.get("transcript") == null ? part.get("text") : part.get("transcript");
                if (text != null && !text.toString().trim().isEmpty()) {
                    if (transcript.length() > 0) {
                        transcript.append(' ');
                    }
                    transcript.append(text.toString().trim());
                }
            }
        }
        if (transcript.length() > 0) {
            System.out.println("      " + transcript);
        }
    }

    static final class SpeakerPlayer implements AutoCloseable {
        private SourceDataLine line;
        private long bytesReceived;

        SpeakerPlayer() {
            AudioFormat format = new AudioFormat(SAMPLE_RATE, 16, 1, true, false);
            try {
                line = AudioSystem.getSourceDataLine(format);
                line.open(format);
                line.start();
            } catch (LineUnavailableException | IllegalArgumentException error) {
                line = null;
                System.out.println("(speaker playback unavailable; audio will be counted but not played)");
            }
        }

        synchronized void play(byte[] pcm) {
            if (pcm == null) {
                return;
            }
            bytesReceived += pcm.length;
            if (line != null) {
                line.write(pcm, 0, pcm.length);
            }
        }

        synchronized void discardQueuedAudio() {
            if (line != null) {
                line.flush();
            }
        }

        double getSecondsReceived() {
            return bytesReceived / 2.0 / SAMPLE_RATE;
        }

        boolean isEnabled() {
            return line != null;
        }

        @Override
        public synchronized void close() {
            if (line != null) {
                line.drain();
                line.stop();
                line.close();
                line = null;
            }
        }
    }
}
