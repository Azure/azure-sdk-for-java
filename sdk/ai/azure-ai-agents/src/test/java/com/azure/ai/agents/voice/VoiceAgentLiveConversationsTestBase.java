// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.voice;

import com.azure.ai.agents.AgentsClient;
import com.azure.ai.agents.AgentsClientBuilder;
import com.azure.ai.agents.AgentsServiceVersion;
import com.azure.ai.agents.BetaVoiceAgentsLiveConversationsAsyncClient;
import com.azure.ai.agents.BetaVoiceAgentsLiveConversationsClient;
import com.azure.ai.agents.BetaVoiceAgentsLiveWebSocketSessionAsyncClient;
import com.azure.ai.agents.ClientTestBase;
import com.azure.ai.agents.models.VoiceAgentLiveConversation;
import com.azure.ai.agents.models.VoiceAgentLiveConversationItem;
import com.azure.ai.agents.models.VoiceAgentLiveOutputConversationItem;
import com.azure.ai.agents.models.VoiceAgentLiveWebSocketConnectionOptions;
import com.azure.ai.agents.models.VoiceConversationStatus;
import com.azure.ai.agents.models.VoiceRecording;
import com.azure.ai.agents.models.VoiceAudioItem;
import com.azure.ai.agents.models.VoiceAudioRole;
import com.azure.ai.agents.models.VoiceAudioCodec;
import com.azure.ai.agents.models.VoiceAudioContainerFormat;
import com.azure.core.http.HttpClient;
import com.azure.core.test.TestMode;
import com.azure.core.util.Configuration;
import com.azure.core.util.BinaryData;
import com.openai.core.JsonValue;
import org.junit.jupiter.params.provider.Arguments;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Records HTTP REST operations only. Record/live setup generates synthetic speech in a stored WebSocket session;
 * playback retrieves its recorded conversation ID and never opens a WebSocket or requires credentials.
 */
abstract class VoiceAgentLiveConversationsTestBase extends ClientTestBase {
    static final String DISPLAY_NAME_WITH_ARGUMENTS = "{displayName} with [{arguments}]";
    static final Duration TIMEOUT = Duration.ofMinutes(3);

    public static Stream<Arguments> getTestParameters() {
        return getHttpClients().map(client -> Arguments.of(client, AgentsServiceVersion.V1));
    }

    static void assertItemAudio(VoiceAudioItem audio, LiveConversationResource resource) {
        assertEquals(resource.conversationId, audio.getConversationId());
        assertEquals(resource.itemId, audio.getItemId());
        assertEquals(VoiceAudioRole.AGENT, audio.getRole());
        assertEquals(VoiceAudioContainerFormat.WAV, audio.getFormat());
        assertEquals(VoiceAudioCodec.PCM16, audio.getCodec());
        assertEquals(Integer.valueOf(24000), audio.getSampleRate());
        assertEquals(Integer.valueOf(1), audio.getChannels());
        assertNotNull(audio.getDurationMs());
        assertTrue(audio.getDurationMs().toMillis() > 0);
    }

    static void assertWav(BinaryData audio, int channels) {
        byte[] bytes = audio.toBytes();
        assertTrue(bytes.length > 44, "The download must contain WAV audio, not just a header.");
        assertEquals("RIFF", new String(bytes, 0, 4, StandardCharsets.US_ASCII));
        assertEquals("WAVE", new String(bytes, 8, 4, StandardCharsets.US_ASCII));
        try (AudioInputStream stream = AudioSystem.getAudioInputStream(new ByteArrayInputStream(bytes))) {
            assertEquals(channels, stream.getFormat().getChannels());
            assertEquals(24000.0f, stream.getFormat().getSampleRate());
            assertEquals(16, stream.getFormat().getSampleSizeInBits());
            assertTrue(stream.getFrameLength() > 0, "The WAV must contain audio frames.");
            assertTrue(stream.read(new byte[stream.getFormat().getFrameSize() * 128]) > 0);
        } catch (IOException | UnsupportedAudioFileException error) {
            throw new AssertionError("The downloaded content must be a readable PCM WAV.", error);
        }
    }

    static void assertRecording(VoiceRecording recording, String conversationId) {
        assertEquals(conversationId, recording.getConversationId());
        assertEquals(VoiceAudioContainerFormat.WAV, recording.getFormat());
        assertEquals(24000, recording.getSampleRate());
        assertEquals(2, recording.getChannels());
        assertTrue(recording.getDurationMs().toMillis() > 0);
        assertNotNull(recording.getChannelLayout());
        assertEquals(VoiceAudioRole.USER, recording.getChannelLayout().getLeft());
        assertEquals(VoiceAudioRole.AGENT, recording.getChannelLayout().getRight());
    }

    LiveConversationResource createConversation(HttpClient httpClient, AgentsServiceVersion serviceVersion) {
        AgentsClientBuilder builder = getClientBuilder(httpClient, serviceVersion, true);
        AgentsClient agents = builder.buildAgentsClient();
        BetaVoiceAgentsLiveConversationsClient client = builder.beta().buildBetaVoiceAgentsLiveConversationsClient();
        String agentName = testResourceNamer.randomName("live-rest-", 32);
        String model = recordedModel("FOUNDRY_GPT_LIVE_MODEL", "gpt-live-1");
        String backend = recordedModel("FOUNDRY_GPT_LIVE_BACKEND_MODEL", "gpt-5.6-terra");
        agents.createAgentVersion(agentName, GptLiveSampleUtils.createDefinition(model, backend, false));
        boolean ready = false;
        try {
            String conversationId = testResourceNamer.recordValueFromConfig(getTestMode() == TestMode.PLAYBACK
                ? "conversation"
                : createStoredSession(getWebSocketClientBuilder(serviceVersion), agentName));
            awaitCompleted(client, agentName, conversationId);
            VoiceAgentLiveConversationItem output = client.listAgentConversationItems(agentName, conversationId)
                .stream()
                .filter(VoiceAgentLiveOutputConversationItem.class::isInstance)
                .findFirst()
                .orElseThrow(() -> new AssertionError("The stored test conversation must contain assistant speech."));
            LiveConversationResource resource = new LiveConversationResource(agents, client,
                builder.beta().buildBetaVoiceAgentsLiveConversationsAsyncClient(), agentName, conversationId,
                output.getId());
            ready = true;
            return resource;
        } finally {
            if (!ready) {
                agents.deleteAgent(agentName);
            }
        }
    }

    private String recordedModel(String name, String defaultValue) {
        return testResourceNamer.recordValueFromConfig(
            getTestMode() == TestMode.PLAYBACK ? name : Configuration.getGlobalConfiguration().get(name, defaultValue));
    }

    private String createStoredSession(AgentsClientBuilder builder, String agentName) {
        AtomicReference<String> conversationId = new AtomicReference<>();
        AtomicInteger audioBytes = new AtomicInteger();
        Mono.usingWhen(
            builder.beta()
                .buildBetaVoiceAgentsLiveWebSocketAsyncClient()
                .openWebSocketSession(agentName, new VoiceAgentLiveWebSocketConnectionOptions().setStoreEnabled(true)),
            session -> GptLiveAudioConversationAsyncSample
                .runConversationCoreAsync(session.receiveEvents().doOnNext(event -> {
                    if (event.isSessionStarted()) {
                        JsonValue id = event.asSessionStarted()._additionalProperties().get("conversation_id");
                        assertNotNull(id, "A stored session must return a Foundry conversation ID.");
                        assertTrue(id.asString().isPresent(), "The conversation ID must be a string.");
                        conversationId.set(assertInstanceOf(String.class, id.asString().get(),
                            "The conversation ID must be a string."));
                    }
                }), session::sendEvent, () -> "Stored Live REST test fixture", transcripts -> Mono.empty(),
                    pcm -> audioBytes.addAndGet(pcm.length), () -> {
                    }, true, true, false, () -> Mono.empty(), Mono.empty(), backend -> Mono.just(false),
                    reason -> false)
                .onErrorMap(error -> new IllegalStateException("Live REST fixture session failed; close code="
                    + session.getCloseCode() + ", reason=" + session.getCloseReason(), error)),
            BetaVoiceAgentsLiveWebSocketSessionAsyncClient::closeAsync, (session, error) -> session.closeAsync(),
            BetaVoiceAgentsLiveWebSocketSessionAsyncClient::closeAsync).block(TIMEOUT);
        assertNotNull(conversationId.get());
        assertTrue(audioBytes.get() > 0, "The fixture must generate actual synthetic speech, not only text.");
        return conversationId.get();
    }

    private void awaitCompleted(BetaVoiceAgentsLiveConversationsClient client, String agentName,
        String conversationId) {
        for (int attempt = 0; attempt < 30; attempt++) {
            VoiceAgentLiveConversation conversation = client.getAgentConversation(agentName, conversationId);
            if (!VoiceConversationStatus.IN_PROGRESS.equals(conversation.getStatus())) {
                assertEquals(VoiceConversationStatus.COMPLETED, conversation.getStatus());
                return;
            }
            sleep(2000);
        }
        throw new AssertionError("The stored Live test conversation did not finish.");
    }

    static final class LiveConversationResource implements AutoCloseable {
        final BetaVoiceAgentsLiveConversationsClient client;
        final BetaVoiceAgentsLiveConversationsAsyncClient asyncClient;
        final String agentName;
        final String conversationId;
        final String itemId;
        private final AgentsClient agents;
        boolean deleted;

        LiveConversationResource(AgentsClient agents, BetaVoiceAgentsLiveConversationsClient client,
            BetaVoiceAgentsLiveConversationsAsyncClient asyncClient, String agentName, String conversationId,
            String itemId) {
            this.agents = agents;
            this.client = client;
            this.asyncClient = asyncClient;
            this.agentName = agentName;
            this.conversationId = conversationId;
            this.itemId = itemId;
        }

        @Override
        public void close() {
            try {
                if (!deleted) {
                    client.deleteAgentConversation(agentName, conversationId);
                }
            } finally {
                agents.deleteAgent(agentName);
            }
        }
    }
}
