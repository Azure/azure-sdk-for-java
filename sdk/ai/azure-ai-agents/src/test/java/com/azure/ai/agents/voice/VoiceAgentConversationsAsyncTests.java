// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.voice;

import com.azure.ai.agents.models.VoiceConversation;
import com.azure.ai.agents.AgentsAsyncClient;
import com.azure.ai.agents.AgentsClientBuilder;
import com.azure.ai.agents.BetaVoiceAgentsConversationsAsyncClient;
import com.azure.ai.agents.BetaVoiceAgentWebSocketSessionAsyncClient;
import com.azure.ai.agents.models.CreateAgentVersionInput;
import com.azure.ai.agents.models.RealtimeResponseDoneEvent;
import com.azure.ai.agents.models.RealtimeSessionCreatedEvent;
import com.azure.ai.agents.models.VoiceAgentAudioConfiguration;
import com.azure.ai.agents.models.VoiceAgentAudioOutputConfiguration;
import com.azure.ai.agents.models.VoiceAgentDefinition;
import com.azure.ai.agents.models.VoiceModelType;
import com.azure.ai.agents.models.VoiceOutputModality;
import com.azure.ai.agents.models.VoiceType;
import com.azure.ai.agents.models.VoiceConversationStatus;
import com.azure.ai.agents.models.VoiceAudioItem;
import com.azure.ai.agents.models.VoiceRecording;
import com.azure.ai.agents.models.VoiceResponse;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.core.test.utils.MockTokenCredential;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Persisted voice REST coverage, separate from native OpenAI conversation tests.
 * Deterministic cases use scripted HTTP responses, not service recordings. The live parity case requires
 * AZURE_TEST_MODE=LIVE, FOUNDRY_PROJECT_ENDPOINT, FOUNDRY_VOICE_MODEL_NAME and DefaultAzureCredential authentication.
 * It creates and deletes its own agent and conversation; no microphone or speaker is required.
 * Interrupted/generated audio is excluded, matching the Python parity test.
 */
public class VoiceAgentConversationsAsyncTests {
    private static final Duration TIMEOUT = Duration.ofSeconds(30);
    private static final String AGENT = "test-conversations-read-agent-async-java";
    private static final String CONVERSATION = "conversation-1";
    private static final String ROOT = "/agents/" + AGENT + "/endpoint/protocols/voice/conversations";
    private static final String PATH = ROOT + "/" + CONVERSATION;
    private static final String ENVELOPE
        = "{\"id\":\"conversation-1\",\"status\":\"completed\",\"created_at\":1700000000}";
    private static final String RESPONSE = "{\"id\":\"response-1\",\"status\":\"completed\"}";
    private static final String USER_ITEM = "{\"id\":\"user-1\",\"type\":\"message\",\"role\":\"user\",\"content\":[]}";
    private static final String ASSISTANT_ITEM
        = "{\"id\":\"assistant-1\",\"type\":\"message\",\"role\":\"assistant\",\"content\":[]}";

    @Test
    @EnabledIfEnvironmentVariable(named = "AZURE_TEST_MODE", matches = "LIVE")
    public void readLivePersistedConversation() {
        Configuration configuration = Configuration.getGlobalConfiguration();
        String endpoint = configuration.get("FOUNDRY_PROJECT_ENDPOINT");
        String model = configuration.get("FOUNDRY_VOICE_MODEL_NAME");
        assertNotNull(endpoint, "FOUNDRY_PROJECT_ENDPOINT is required for live parity testing.");
        assertNotNull(model, "FOUNDRY_VOICE_MODEL_NAME is required for live parity testing.");
        String agentName = "test-voice-read-" + UUID.randomUUID();
        AgentsClientBuilder builder = new AgentsClientBuilder().endpoint(endpoint)
            .credential(new DefaultAzureCredentialBuilder().build())
            .allowPreview(true);
        AgentsAsyncClient agents = builder.buildAgentsAsyncClient();
        BetaVoiceAgentsConversationsAsyncClient conversations
            = builder.beta().buildBetaVoiceAgentsConversationsAsyncClient();
        VoiceAgentDefinition definition = new VoiceAgentDefinition().setModelType(VoiceModelType.MANAGED)
            .setModel(model)
            .setInstructions("You are a helpful voice assistant. Keep replies short.")
            .setAudio(new VoiceAgentAudioConfiguration()
                .setOutput(new VoiceAgentAudioOutputConfiguration().setVoice("en-US-AvaNeural")
                    .setVoiceType(VoiceType.AZURE_STANDARD)))
            .setOutputModalities(Collections.singletonList(VoiceOutputModality.AUDIO))
            .setStore(true);
        AtomicReference<String> conversationId = new AtomicReference<>();
        boolean created = false;
        boolean reading = false;
        try {
            agents.createAgentVersion(agentName, new CreateAgentVersionInput(definition)).block(TIMEOUT);
            created = true;
            Mono.usingWhen(builder.beta().buildBetaVoiceAgentWebSocketAsyncClient().connect(agentName),
                session -> session.receiveEvents().index().concatMap(indexed -> {
                    if (indexed.getT1() == 0) {
                        assertTrue(indexed.getT2() instanceof RealtimeSessionCreatedEvent,
                            "The first event must be session.created.");
                        conversationId.set(((RealtimeSessionCreatedEvent) indexed.getT2()).getConversationId());
                        assertNotNull(conversationId.get(), "store=True must return a conversation ID.");
                        return session.sendText("Say hello.")
                            .then(session.createResponse())
                            .thenReturn(indexed.getT2());
                    }
                    return Mono.just(indexed.getT2());
                })
                    .filter(RealtimeResponseDoneEvent.class::isInstance)
                    .next()
                    .switchIfEmpty(Mono.error(new AssertionError("Session ended without response.done.")))
                    .timeout(Duration.ofSeconds(45))
                    .then(),
                BetaVoiceAgentWebSocketSessionAsyncClient::closeAsync, (session, error) -> session.closeAsync(),
                BetaVoiceAgentWebSocketSessionAsyncClient::closeAsync).block(Duration.ofSeconds(90));
            Mono.delay(Duration.ofSeconds(30)).block(Duration.ofSeconds(35));
            reading = true;
            assertPersistedConversation(conversations, agentName, conversationId.get());
        } finally {
            try {
                if (!reading && conversationId.get() != null) {
                    conversations.deleteAgentConversation(agentName, conversationId.get()).block(TIMEOUT);
                }
            } finally {
                if (created) {
                    agents.deleteAgent(agentName).block(TIMEOUT);
                }
            }
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    public void readPersistedConversation(boolean blobStorage) {
        ScriptedTransport transport = new ScriptedTransport();
        enqueueTranscript(transport, ENVELOPE);
        String blob = blobStorage ? ",\"blob_uri\":\"https://storage.example/recording.wav\"" : "";
        transport.get(PATH + "/audio", 200, "{\"conversation_id\":\"conversation-1\",\"format\":\"wav\","
            + "\"sample_rate\":24000,\"channels\":2,\"channel_layout\":{},\"duration_ms\":1000" + blob + "}");
        if (!blobStorage) {
            transport.audio(PATH + "/audio/content");
        }
        transport.get(PATH + "/items/user-1/audio", 404,
            "{\"error\":{\"code\":\"NotFound\",\"message\":\"No audio\"}}");
        transport.get(PATH + "/items/assistant-1/audio", 200,
            "{\"conversation_id\":\"conversation-1\",\"item_id\":\"assistant-1\",\"role\":\"assistant\"" + blob + "}");
        if (!blobStorage) {
            transport.audio(PATH + "/items/assistant-1/audio/content");
        }
        transport.delete(PATH);

        assertPersistedConversation(client(transport), AGENT, CONVERSATION);
        transport.assertComplete();
    }

    @Test
    public void incompleteConversationFailsInsteadOfSkippingAudioAndStillDeletes() {
        ScriptedTransport transport = new ScriptedTransport();
        enqueueTranscript(transport, ENVELOPE.replace("completed", "in_progress"));
        transport.delete(PATH);
        assertThrows(AssertionError.class, () -> assertPersistedConversation(client(transport), AGENT, CONVERSATION));
        transport.assertComplete();
    }

    @Test
    public void nonNotFoundItemAudioErrorPropagatesAndStillDeletes() {
        ScriptedTransport transport = new ScriptedTransport();
        enqueueTranscript(transport, ENVELOPE);
        transport.get(PATH + "/audio", 200,
            "{\"conversation_id\":\"conversation-1\",\"format\":\"wav\","
                + "\"sample_rate\":24000,\"channels\":2,\"channel_layout\":{},\"duration_ms\":1000,"
                + "\"blob_uri\":\"https://storage.example/recording.wav\"}");
        transport.get(PATH + "/items/user-1/audio", 403,
            "{\"error\":{\"code\":\"Forbidden\",\"message\":\"Audio access denied\"}}");
        transport.delete(PATH);
        HttpResponseException error = assertThrows(HttpResponseException.class,
            () -> assertPersistedConversation(client(transport), AGENT, CONVERSATION));
        assertEquals(403, error.getResponse().getStatusCode());
        transport.assertComplete();
    }

    private static void enqueueTranscript(ScriptedTransport transport, String envelope) {
        transport.get(ROOT, 200, page(envelope));
        transport.get(PATH, 200, envelope);
        transport.get(PATH + "/responses", 200, page(RESPONSE));
        transport.get(PATH + "/responses/response-1", 200, RESPONSE);
        transport.get(PATH + "/responses/response-1/items", 200, page());
        transport.get(PATH + "/items", 200, page(USER_ITEM, ASSISTANT_ITEM));
        transport.get(PATH + "/items/user-1", 200, USER_ITEM);
    }

    private static BetaVoiceAgentsConversationsAsyncClient client(HttpClient transport) {
        return new AgentsClientBuilder().endpoint("https://localhost")
            .credential(new MockTokenCredential())
            .httpClient(transport)
            .allowPreview(true)
            .beta()
            .buildBetaVoiceAgentsConversationsAsyncClient();
    }

    private static void assertPersistedConversation(BetaVoiceAgentsConversationsAsyncClient client, String agentName,
        String conversationId) {
        try {
            assertEquals(Boolean.TRUE,
                client.listAgentConversations(agentName)
                    .any(conversation -> conversationId.equals(conversation.getId()))
                    .block(TIMEOUT));
            VoiceConversation conversation = client.getAgentConversation(agentName, conversationId).block(TIMEOUT);
            assertNotNull(conversation);
            assertEquals(conversationId, conversation.getId());
            assertTrue(
                Arrays
                    .asList(VoiceConversationStatus.IN_PROGRESS, VoiceConversationStatus.COMPLETED,
                        VoiceConversationStatus.FAILED)
                    .contains(conversation.getStatus()));
            assertNotNull(conversation.getCreatedAt());

            List<VoiceResponse> responses
                = client.listAgentConversationResponses(agentName, conversationId).collectList().block(TIMEOUT);
            assertNotNull(responses);
            assertFalse(responses.isEmpty());
            String responseId = responses.get(0).getId();
            VoiceResponse response
                = client.getAgentConversationResponse(agentName, conversationId, responseId).block(TIMEOUT);
            assertNotNull(response);
            assertEquals(responseId, response.getId());
            client.listAgentConversationResponseItems(agentName, conversationId, responseId, new RequestOptions())
                .collectList()
                .block(TIMEOUT);

            List<BinaryData> items = client.listAgentConversationItems(agentName, conversationId, new RequestOptions())
                .collectList()
                .block(TIMEOUT);
            assertNotNull(items);
            assertFalse(items.isEmpty());
            String firstId = itemId(items.get(0));
            assertNotNull(firstId);
            assertFalse(firstId.isEmpty());
            BinaryData fetched
                = client.getAgentConversationItemWithResponse(agentName, conversationId, firstId, new RequestOptions())
                    .block(TIMEOUT)
                    .getValue();
            assertEquals(firstId, itemId(fetched));

            assertEquals(VoiceConversationStatus.COMPLETED, conversation.getStatus(),
                "Audio assertions require a finalized conversation.");
            VoiceRecording recording = client.getAgentConversationAudio(agentName, conversationId).block(TIMEOUT);
            assertNotNull(recording);
            assertNotNull(recording.getFormat());
            if (recording.getBlobUri() == null || recording.getBlobUri().isEmpty()) {
                assertAudio(client.downloadAgentConversationAudio(agentName, conversationId));
            }
            for (BinaryData item : items) {
                String id = itemId(item);
                if (id == null || id.isEmpty()) {
                    continue;
                }
                VoiceAudioItem audio;
                try {
                    audio = client.getAgentConversationAudioItem(agentName, conversationId, id).block(TIMEOUT);
                } catch (HttpResponseException error) {
                    if (error.getResponse().getStatusCode() == 404) {
                        continue;
                    }
                    throw error;
                }
                assertNotNull(audio);
                assertNotNull(audio.getRole());
                if (audio.getBlobUri() == null || audio.getBlobUri().isEmpty()) {
                    assertAudio(client.downloadAgentConversationAudioItem(agentName, conversationId, id));
                }
                break;
            }
        } finally {
            client.deleteAgentConversation(agentName, conversationId).block(TIMEOUT);
        }
    }

    private static String itemId(BinaryData item) {
        return (String) item.toObject(Map.class).get("id");
    }

    private static void assertAudio(Mono<BinaryData> content) {
        BinaryData audio = content.block(TIMEOUT);
        assertNotNull(audio);
        assertTrue(audio.toBytes().length > 0);
    }

    private static String page(String... entries) {
        return "{\"data\":[" + String.join(",", entries) + "],\"has_more\":false}";
    }

    private static final class ScriptedTransport implements HttpClient {
        private final Deque<java.util.function.Function<HttpRequest, HttpResponse>> requests = new ArrayDeque<>();

        void get(String path, int status, String json) {
            expect(HttpMethod.GET, path, status, "application/json", BinaryData.fromString(json).toBytes());
        }

        void audio(String path) {
            expect(HttpMethod.GET, path, 200, "audio/wav", new byte[] { 82, 73, 70, 70, 0, 1, 2, 3 });
        }

        void delete(String path) {
            expect(HttpMethod.DELETE, path, 204, "application/json", new byte[0]);
        }

        private void expect(HttpMethod method, String path, int status, String contentType, byte[] body) {
            requests.add(request -> {
                assertEquals(method, request.getHttpMethod());
                assertEquals(path, request.getUrl().getPath());
                return new MockHttpResponse(request, status,
                    new HttpHeaders().set(HttpHeaderName.CONTENT_TYPE, contentType), body);
            });
        }

        @Override
        public Mono<HttpResponse> send(HttpRequest request) {
            return Mono.fromSupplier(() -> {
                assertFalse(requests.isEmpty(), "Unexpected request: " + request.getUrl());
                return requests.removeFirst().apply(request);
            });
        }

        @Override
        public HttpResponse sendSync(HttpRequest request, Context context) {
            throw new AssertionError("The async client must not use synchronous HTTP.");
        }

        void assertComplete() {
            assertTrue(requests.isEmpty(), "Not all expected voice REST operations were called.");
        }
    }
}
