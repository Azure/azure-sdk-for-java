// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.voice;

import com.azure.ai.agents.AgentsServiceVersion;
import com.azure.ai.agents.models.VoiceAudioItem;
import com.azure.ai.agents.models.VoiceAgentLiveConversation;
import com.azure.ai.agents.models.VoiceAgentLiveConversationItem;
import com.azure.ai.agents.models.VoiceAgentLiveOutputConversationItem;
import com.azure.ai.agents.models.VoiceConversationStatus;
import com.azure.core.http.HttpClient;
import com.azure.core.exception.ResourceNotFoundException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class VoiceAgentLiveConversationsRecordedTests extends VoiceAgentLiveConversationsTestBase {
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void downloadAgentConversationAudio(HttpClient httpClient, AgentsServiceVersion serviceVersion) {
        try (LiveConversationResource resource = createConversation(httpClient, serviceVersion)) {
            assertWav(resource.client.downloadAgentConversationAudio(resource.agentName, resource.conversationId), 2);
        }
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void getAgentConversationAudio(HttpClient httpClient, AgentsServiceVersion serviceVersion) {
        try (LiveConversationResource resource = createConversation(httpClient, serviceVersion)) {
            assertRecording(resource.client.getAgentConversationAudio(resource.agentName, resource.conversationId),
                resource.conversationId);
        }
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void downloadAgentConversationAudioItem(HttpClient httpClient, AgentsServiceVersion serviceVersion) {
        try (LiveConversationResource resource = createConversation(httpClient, serviceVersion)) {
            assertWav(resource.client.downloadAgentConversationAudioItem(resource.agentName, resource.conversationId,
                resource.itemId), 1);
        }
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void getAgentConversationAudioItem(HttpClient httpClient, AgentsServiceVersion serviceVersion) {
        try (LiveConversationResource resource = createConversation(httpClient, serviceVersion)) {
            VoiceAudioItem audio = resource.client.getAgentConversationAudioItem(resource.agentName,
                resource.conversationId, resource.itemId);
            assertItemAudio(audio, resource);
        }
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void getAgentConversationItem(HttpClient httpClient, AgentsServiceVersion serviceVersion) {
        try (LiveConversationResource resource = createConversation(httpClient, serviceVersion)) {
            VoiceAgentLiveOutputConversationItem output = assertInstanceOf(VoiceAgentLiveOutputConversationItem.class,
                resource.client.getAgentConversationItem(resource.agentName, resource.conversationId, resource.itemId));
            assertEquals(resource.itemId, output.getId());
            assertNotNull(output.getTranscription());
            assertFalse(output.getTranscription().trim().isEmpty());
            assertNotNull(output.getAudio());
            assertEquals(resource.itemId, output.getAudio().getItemId());
            assertThrows(ResourceNotFoundException.class, () -> resource.client
                .getAgentConversationItem(resource.agentName, resource.conversationId, "missing-item"));
        }
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void listAgentConversationItems(HttpClient httpClient, AgentsServiceVersion serviceVersion) {
        try (LiveConversationResource resource = createConversation(httpClient, serviceVersion)) {
            List<VoiceAgentLiveConversationItem> items
                = resource.client.listAgentConversationItems(resource.agentName, resource.conversationId)
                    .stream()
                    .collect(Collectors.toList());
            assertFalse(items.isEmpty());
            VoiceAgentLiveOutputConversationItem output = assertInstanceOf(VoiceAgentLiveOutputConversationItem.class,
                items.stream()
                    .filter(item -> resource.itemId.equals(item.getId()))
                    .findFirst()
                    .orElseThrow(() -> new AssertionError("The stored speech item must be listed.")));
            assertNotNull(output.getTranscription());
            assertFalse(output.getTranscription().trim().isEmpty());
            assertNotNull(output.getAudio());
            assertEquals(resource.itemId, output.getAudio().getItemId());
        }
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void deleteAgentConversation(HttpClient httpClient, AgentsServiceVersion serviceVersion) {
        try (LiveConversationResource resource = createConversation(httpClient, serviceVersion)) {
            resource.client.deleteAgentConversation(resource.agentName, resource.conversationId);
            resource.deleted = true;
            assertThrows(ResourceNotFoundException.class,
                () -> resource.client.getAgentConversation(resource.agentName, resource.conversationId));
            assertThrows(ResourceNotFoundException.class, () -> resource.client
                .getAgentConversationItem(resource.agentName, resource.conversationId, resource.itemId));
            assertEquals(0L, resource.client.listAgentConversations(resource.agentName).stream().count());
        }
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void getAgentConversation(HttpClient httpClient, AgentsServiceVersion serviceVersion) {
        try (LiveConversationResource resource = createConversation(httpClient, serviceVersion)) {
            VoiceAgentLiveConversation conversation
                = resource.client.getAgentConversation(resource.agentName, resource.conversationId);
            assertEquals(resource.conversationId, conversation.getId());
            assertEquals(VoiceConversationStatus.COMPLETED, conversation.getStatus());
            assertNotNull(conversation.getCreatedAt());
            assertNotNull(conversation.getCompletedAt());
            assertNotNull(conversation.getUsage());
        }
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void listAgentConversations(HttpClient httpClient, AgentsServiceVersion serviceVersion) {
        try (LiveConversationResource resource = createConversation(httpClient, serviceVersion)) {
            List<VoiceAgentLiveConversation> conversations
                = resource.client.listAgentConversations(resource.agentName).stream().collect(Collectors.toList());
            assertEquals(1, conversations.size());
            assertEquals(resource.conversationId, conversations.get(0).getId());
            assertNotNull(conversations.get(0).getCreatedAt());
        }
    }
}
