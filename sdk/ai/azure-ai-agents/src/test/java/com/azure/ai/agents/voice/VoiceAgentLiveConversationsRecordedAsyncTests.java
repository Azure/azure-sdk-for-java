// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.voice;

import com.azure.ai.agents.AgentsServiceVersion;
import com.azure.ai.agents.models.VoiceAgentLiveOutputConversationItem;
import com.azure.ai.agents.models.VoiceConversationStatus;
import com.azure.core.http.HttpClient;
import com.azure.core.exception.ResourceNotFoundException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class VoiceAgentLiveConversationsRecordedAsyncTests extends VoiceAgentLiveConversationsTestBase {
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void downloadAgentConversationAudio(HttpClient httpClient, AgentsServiceVersion serviceVersion) {
        try (LiveConversationResource resource = createConversation(httpClient, serviceVersion)) {
            StepVerifier
                .create(
                    resource.asyncClient.downloadAgentConversationAudio(resource.agentName, resource.conversationId))
                .assertNext(audio -> assertWav(audio, 2))
                .verifyComplete();
        }
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void getAgentConversationAudio(HttpClient httpClient, AgentsServiceVersion serviceVersion) {
        try (LiveConversationResource resource = createConversation(httpClient, serviceVersion)) {
            StepVerifier
                .create(resource.asyncClient.getAgentConversationAudio(resource.agentName, resource.conversationId))
                .assertNext(recording -> assertRecording(recording, resource.conversationId))
                .verifyComplete();
        }
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void downloadAgentConversationAudioItem(HttpClient httpClient, AgentsServiceVersion serviceVersion) {
        try (LiveConversationResource resource = createConversation(httpClient, serviceVersion)) {
            StepVerifier
                .create(resource.asyncClient.downloadAgentConversationAudioItem(resource.agentName,
                    resource.conversationId, resource.itemId))
                .assertNext(audio -> assertWav(audio, 1))
                .verifyComplete();
        }
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void getAgentConversationAudioItem(HttpClient httpClient, AgentsServiceVersion serviceVersion) {
        try (LiveConversationResource resource = createConversation(httpClient, serviceVersion)) {
            StepVerifier
                .create(resource.asyncClient.getAgentConversationAudioItem(resource.agentName, resource.conversationId,
                    resource.itemId))
                .assertNext(audio -> assertItemAudio(audio, resource))
                .verifyComplete();
        }
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void getAgentConversationItem(HttpClient httpClient, AgentsServiceVersion serviceVersion) {
        try (LiveConversationResource resource = createConversation(httpClient, serviceVersion)) {
            StepVerifier.create(resource.asyncClient.getAgentConversationItem(resource.agentName,
                resource.conversationId, resource.itemId)).assertNext(item -> {
                    VoiceAgentLiveOutputConversationItem output
                        = assertInstanceOf(VoiceAgentLiveOutputConversationItem.class, item);
                    assertEquals(resource.itemId, output.getId());
                    assertNotNull(output.getTranscription());
                    assertFalse(output.getTranscription().trim().isEmpty());
                    assertNotNull(output.getAudio());
                    assertEquals(resource.itemId, output.getAudio().getItemId());
                }).verifyComplete();
            StepVerifier.create(resource.asyncClient.getAgentConversationItem(resource.agentName,
                resource.conversationId, "missing-item")).expectError(ResourceNotFoundException.class).verify(TIMEOUT);
        }
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void listAgentConversationItems(HttpClient httpClient, AgentsServiceVersion serviceVersion) {
        try (LiveConversationResource resource = createConversation(httpClient, serviceVersion)) {
            StepVerifier
                .create(resource.asyncClient.listAgentConversationItems(resource.agentName, resource.conversationId)
                    .collectList())
                .assertNext(items -> {
                    assertFalse(items.isEmpty());
                    VoiceAgentLiveOutputConversationItem output
                        = assertInstanceOf(VoiceAgentLiveOutputConversationItem.class,
                            items.stream()
                                .filter(item -> resource.itemId.equals(item.getId()))
                                .findFirst()
                                .orElseThrow(() -> new AssertionError("The stored speech item must be listed.")));
                    assertNotNull(output.getTranscription());
                    assertFalse(output.getTranscription().trim().isEmpty());
                    assertNotNull(output.getAudio());
                    assertEquals(resource.itemId, output.getAudio().getItemId());
                })
                .verifyComplete();
        }
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void deleteAgentConversation(HttpClient httpClient, AgentsServiceVersion serviceVersion) {
        try (LiveConversationResource resource = createConversation(httpClient, serviceVersion)) {
            StepVerifier
                .create(resource.asyncClient.deleteAgentConversation(resource.agentName, resource.conversationId)
                    .doOnSuccess(ignored -> resource.deleted = true))
                .verifyComplete();
            StepVerifier.create(resource.asyncClient.getAgentConversation(resource.agentName, resource.conversationId))
                .expectError(ResourceNotFoundException.class)
                .verify(TIMEOUT);
            StepVerifier.create(resource.asyncClient.getAgentConversationItem(resource.agentName,
                resource.conversationId, resource.itemId)).expectError(ResourceNotFoundException.class).verify(TIMEOUT);
            StepVerifier.create(resource.asyncClient.listAgentConversations(resource.agentName).count())
                .expectNext(0L)
                .verifyComplete();
        }
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void getAgentConversation(HttpClient httpClient, AgentsServiceVersion serviceVersion) {
        try (LiveConversationResource resource = createConversation(httpClient, serviceVersion)) {
            StepVerifier.create(resource.asyncClient.getAgentConversation(resource.agentName, resource.conversationId))
                .assertNext(conversation -> {
                    assertEquals(resource.conversationId, conversation.getId());
                    assertEquals(VoiceConversationStatus.COMPLETED, conversation.getStatus());
                    assertNotNull(conversation.getCreatedAt());
                    assertNotNull(conversation.getCompletedAt());
                    assertNotNull(conversation.getUsage());
                })
                .verifyComplete();
        }
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void listAgentConversations(HttpClient httpClient, AgentsServiceVersion serviceVersion) {
        try (LiveConversationResource resource = createConversation(httpClient, serviceVersion)) {
            StepVerifier.create(resource.asyncClient.listAgentConversations(resource.agentName).collectList())
                .assertNext(conversations -> {
                    assertEquals(1, conversations.size());
                    assertEquals(resource.conversationId, conversations.get(0).getId());
                    assertNotNull(conversations.get(0).getCreatedAt());
                })
                .verifyComplete();
        }
    }
}
