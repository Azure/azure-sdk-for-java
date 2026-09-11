// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.voice;

import com.azure.ai.agents.BetaAgentEndpointConversationsClient;
import com.azure.ai.agents.AgentsClientBuilder;
import com.azure.ai.agents.models.VoiceItemAudioResponse;
import com.azure.ai.agents.models.VoiceRecordingResponse;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/**
 * Demonstrates downloading whole-call and item-level audio from a persisted voice conversation.
 *
 * <p>Before running the sample, set these environment variables:</p>
 * <ul>
 *   <li>{@code FOUNDRY_PROJECT_ENDPOINT} - The Azure AI Project endpoint.</li>
 *   <li>{@code FOUNDRY_VOICE_AGENT_NAME} - The voice agent name.</li>
 *   <li>{@code FOUNDRY_VOICE_CONVERSATION_ID} - The persisted voice conversation ID.</li>
 * </ul>
 */
public class VoiceAgentReadConversationAudioSample {
    public static void main(String[] args) throws IOException {
        Configuration configuration = Configuration.getGlobalConfiguration();
        String endpoint = configuration.get("FOUNDRY_PROJECT_ENDPOINT");
        String agentName = configuration.get("FOUNDRY_VOICE_AGENT_NAME");
        String conversationId = configuration.get("FOUNDRY_VOICE_CONVERSATION_ID");
        BetaAgentEndpointConversationsClient conversations = new AgentsClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint(endpoint)
            .buildBetaAgentEndpointConversationsClient();

        VoiceRecordingResponse recording = conversations.getAgentConversationAudio(agentName, conversationId);
        System.out.printf("Recording: format=%s, rate=%d, channels=%d, duration=%s%n",
            recording.getFormat(), recording.getSampleRate(), recording.getChannels(), recording.getDurationMs());
        if (recording.getBlobUri() != null) {
            System.out.println("Recording is stored in customer storage: " + recording.getBlobUri());
        } else {
            Path output = Files.createTempFile(conversationId + "-", ".wav");
            Files.write(output, conversations.getAgentConversationAudioContent(agentName, conversationId).toBytes());
            System.out.println("Wrote merged recording: " + output);
        }

        for (BinaryData itemData : conversations.listAgentConversationItems(agentName, conversationId,
            new RequestOptions())) {
            @SuppressWarnings("unchecked")
            Map<String, Object> item = itemData.toObject(Map.class);
            String itemId = (String) item.get("id");
            if (itemId == null) {
                continue;
            }
            try {
                VoiceItemAudioResponse metadata = conversations.getAgentConversationItemAudio(
                    agentName, conversationId, itemId);
                if (metadata.getBlobUri() != null) {
                    System.out.println("Item audio is stored in customer storage: " + metadata.getBlobUri());
                } else {
                    Path output = Files.createTempFile(conversationId + "-" + itemId + "-", ".wav");
                    Files.write(output, conversations.getAgentConversationItemAudioContent(
                        agentName, conversationId, itemId).toBytes());
                    System.out.println("Wrote item audio: " + output);
                }
                break;
            } catch (ResourceNotFoundException ignored) {
                // This transcript item has no persisted audio.
            }
        }
    }
}
