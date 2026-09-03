// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents;

import com.azure.ai.agents.models.VoiceAudioContainerFormat;
import com.azure.ai.agents.models.VoiceAudioRole;
import com.azure.ai.agents.models.VoiceConversation;
import com.azure.ai.agents.models.VoiceConversationStatus;
import com.azure.ai.agents.models.VoiceRecordingResponse;
import com.azure.ai.agents.models.VoiceResponse;
import com.azure.ai.agents.models.VoiceResponseBaseStatus;
import com.azure.core.http.HttpHeaders;
import com.azure.core.test.utils.MockTokenCredential;
import com.azure.core.util.BinaryData;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class VoiceConversationMockTests {
    private static final String ENDPOINT = "https://localhost:8080/api/projects/project";
    private static final String AGENT_NAME = "voice-agent-test";
    private static final String CONVERSATION_ID = "conversation-test";
    private static final byte[] WAV_BYTES = "RIFF-test-wave".getBytes(StandardCharsets.UTF_8);

    @Test
    public void syncConversationAndAudioOperationsDeserializeResponses() {
        DeterministicHttpClient httpClient = new DeterministicHttpClient().enqueueJson(200, conversationJson())
            .enqueueJson(200, responseJson())
            .enqueueJson(200, recordingJson())
            .enqueue(200, new HttpHeaders().set(com.azure.core.http.HttpHeaderName.CONTENT_TYPE, "audio/wav"),
                WAV_BYTES);
        BetaAgentEndpointConversationsClient client
            = createBuilder(httpClient).buildBetaAgentEndpointConversationsClient();

        VoiceConversation conversation = client.getAgentConversation(AGENT_NAME, CONVERSATION_ID);
        assertEquals(CONVERSATION_ID, conversation.getId());
        assertEquals(VoiceConversationStatus.COMPLETED, conversation.getStatus());

        VoiceResponse response = client.getAgentConversationResponse(AGENT_NAME, CONVERSATION_ID, "response-test");
        assertEquals("response-test", response.getId());
        assertEquals(VoiceResponseBaseStatus.COMPLETED, response.getStatus());

        VoiceRecordingResponse recording = client.getAgentConversationAudio(AGENT_NAME, CONVERSATION_ID);
        assertEquals(VoiceAudioContainerFormat.WAV, recording.getFormat());
        assertEquals(24000, recording.getSampleRate());
        assertEquals(2, recording.getChannels());
        assertEquals("user", recording.getChannelLayout().getLeft());
        assertEquals("agent", recording.getChannelLayout().getRight());

        BinaryData content = client.getAgentConversationAudioContent(AGENT_NAME, CONVERSATION_ID);
        assertArrayEquals(WAV_BYTES, content.toBytes());
        assertTrue(httpClient.getRequest(0)
            .getUrl()
            .getPath()
            .endsWith("/agents/" + AGENT_NAME + "/endpoint/protocols/voice/conversations/" + CONVERSATION_ID));
        assertTrue(httpClient.getRequest(3).getUrl().getPath().endsWith("/audio/content"));
    }

    @Test
    public void asyncConversationItemAudioOperationsDeserializeResponses() {
        DeterministicHttpClient httpClient = new DeterministicHttpClient().enqueueJson(200, conversationJson())
            .enqueueJson(200, itemAudioJson())
            .enqueue(200, new HttpHeaders().set(com.azure.core.http.HttpHeaderName.CONTENT_TYPE, "audio/wav"),
                WAV_BYTES);
        BetaAgentEndpointConversationsAsyncClient client
            = createBuilder(httpClient).buildBetaAgentEndpointConversationsAsyncClient();

        Mono<Void> operations = client.getAgentConversation(AGENT_NAME, CONVERSATION_ID).doOnNext(conversation -> {
            assertEquals(CONVERSATION_ID, conversation.getId());
            assertEquals(VoiceConversationStatus.COMPLETED, conversation.getStatus());
        }).then(client.getAgentConversationItemAudio(AGENT_NAME, CONVERSATION_ID, "item-test")).doOnNext(itemAudio -> {
            assertEquals("item-test", itemAudio.getItemId());
            assertEquals(VoiceAudioRole.USER, itemAudio.getRole());
            assertEquals(VoiceAudioContainerFormat.WAV, itemAudio.getFormat());
        })
            .then(client.getAgentConversationItemAudioContent(AGENT_NAME, CONVERSATION_ID, "item-test"))
            .doOnNext(content -> assertArrayEquals(WAV_BYTES, content.toBytes()))
            .then();

        StepVerifier.create(operations).verifyComplete();
        assertEquals(3, httpClient.getRequests().size());
        assertTrue(httpClient.getRequest(1).getUrl().getPath().endsWith("/items/item-test/audio"));
        assertTrue(httpClient.getRequest(2).getUrl().getPath().endsWith("/items/item-test/audio/content"));
    }

    private static AgentsClientBuilder createBuilder(DeterministicHttpClient httpClient) {
        return new AgentsClientBuilder().endpoint(ENDPOINT)
            .credential(new MockTokenCredential())
            .httpClient(httpClient)
            .serviceVersion(AgentsServiceVersion.V1);
    }

    private static String conversationJson() {
        return "{\"id\":\"" + CONVERSATION_ID + "\",\"object\":\"voice.conversation\","
            + "\"status\":\"completed\",\"created_at\":1,\"completed_at\":2,\"metadata\":{}}";
    }

    private static String responseJson() {
        return "{\"id\":\"response-test\",\"conversation_id\":\"" + CONVERSATION_ID + "\","
            + "\"object\":\"realtime.response\",\"status\":\"completed\","
            + "\"output_modalities\":[\"audio\"],\"output\":[]}";
    }

    private static String recordingJson() {
        return "{\"conversation_id\":\"" + CONVERSATION_ID + "\",\"format\":\"wav\","
            + "\"sample_rate\":24000,\"channels\":2,"
            + "\"channel_layout\":{\"left\":\"user\",\"right\":\"agent\"},\"duration_ms\":1500}";
    }

    private static String itemAudioJson() {
        return "{\"conversation_id\":\"" + CONVERSATION_ID + "\",\"item_id\":\"item-test\","
            + "\"role\":\"user\",\"format\":\"wav\",\"codec\":\"pcm16\","
            + "\"sample_rate\":24000,\"channels\":1,\"duration_ms\":500}";
    }
}
