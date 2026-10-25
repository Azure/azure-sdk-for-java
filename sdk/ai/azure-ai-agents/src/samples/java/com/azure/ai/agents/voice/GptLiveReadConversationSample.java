// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.voice;

import com.azure.ai.agents.AgentsClientBuilder;
import com.azure.ai.agents.BetaVoiceAgentsLiveConversationsClient;
import com.azure.ai.agents.models.PageOrder;
import com.azure.ai.agents.models.VoiceAgentLiveConversation;
import com.azure.ai.agents.models.VoiceAgentLiveConversationItem;
import com.azure.ai.agents.models.VoiceAgentLiveDelegationConversationItem;
import com.azure.ai.agents.models.VoiceAgentLiveInputConversationItem;
import com.azure.ai.agents.models.VoiceAgentLiveOutputConversationItem;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.policy.AddHeadersPolicy;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Lists persisted GPT Live conversations and reads one conversation's status, usage, and transcript items.
 * The session must have used {@code store=true}. Use {@code session.started.conversation_id}, not the native session ID.
 * Stored speech segments are not native turns or proof of playback; delegation results are not spoken transcripts.
 * This sample prints sensitive stored content. It does not create sessions, modify agents, or delete data.
 *
 * <p>Before running the sample, set these environment variables:</p>
 * <ul>
 *   <li>{@code FOUNDRY_PROJECT_ENDPOINT} - Required. The Azure AI Project endpoint. No default.</li>
 *   <li>{@code FOUNDRY_GPT_LIVE_AGENT_NAME} - Required. The existing GPT Live agent's name. No default.</li>
 *   <li>{@code FOUNDRY_GPT_LIVE_CONVERSATION_ID} - Required. The persisted Foundry conversation ID from
 *   {@code session.started.conversation_id}. No default.</li>
 * </ul>
 *
 * <p>Authenticates with {@code DefaultAzureCredential}. Run without command-line arguments.
 * Model deployment settings are not needed because this sample reads an existing conversation.</p>
 */
public class GptLiveReadConversationSample {
    public static void main(String[] args) {
        Configuration configuration = Configuration.getGlobalConfiguration();
        String endpoint = GptLiveSampleUtils.requireSetting(configuration, "FOUNDRY_PROJECT_ENDPOINT");
        String agentName = GptLiveSampleUtils.requireSetting(configuration, "FOUNDRY_GPT_LIVE_AGENT_NAME");
        String conversationId = GptLiveSampleUtils.requireSetting(configuration, "FOUNDRY_GPT_LIVE_CONVERSATION_ID");
        BetaVoiceAgentsLiveConversationsClient client = new AgentsClientBuilder().endpoint(endpoint)
            .credential(new DefaultAzureCredentialBuilder().build())
            .addPolicy(previewHeaders())
            .buildBetaVoiceAgentsLiveConversationsClient();
        readConversation(client, agentName, conversationId);
    }

    static AddHeadersPolicy previewHeaders() {
        // The generated Live REST builder methods do not automatically opt in to the voice preview.
        return new AddHeadersPolicy(new HttpHeaders().set("Foundry-Features", "VoiceAgents=V1Preview"));
    }

    static void readConversation(BetaVoiceAgentsLiveConversationsClient client, String agentName,
        String conversationId) {
        // Generated paging exposes cursor parameters but no automatic continuation token.
        String after = null;
        while (true) {
            List<VoiceAgentLiveConversation> page = client
                .listAgentConversations(agentName, 100, PageOrder.ASC, after, null).stream().collect(Collectors.toList());
            if (page.isEmpty()) {
                break;
            }
            for (VoiceAgentLiveConversation conversation : page) {
                System.out.printf("Available conversation: %s (%s)%n", conversation.getId(), conversation.getStatus());
            }
            after = nextCursor(page.get(page.size() - 1).getId(), after);
        }
        printConversation(client.getAgentConversation(agentName, conversationId));
        after = null;
        while (true) {
            List<VoiceAgentLiveConversationItem> page = client
                .listAgentConversationItems(agentName, conversationId, 100, PageOrder.ASC, after, null)
                .stream().collect(Collectors.toList());
            if (page.isEmpty()) {
                break;
            }
            for (VoiceAgentLiveConversationItem item : page) {
                printItem(client.getAgentConversationItem(agentName, conversationId, item.getId()));
            }
            after = nextCursor(page.get(page.size() - 1).getId(), after);
        }
    }

    static String nextCursor(String id, String previous) {
        if (id == null || id.isEmpty() || id.equals(previous)) {
            throw new IllegalStateException("The service returned a missing or non-advancing pagination cursor.");
        }
        return id;
    }

    static void printConversation(VoiceAgentLiveConversation conversation) {
        System.out.printf("Conversation %s: status=%s, created=%s, completed=%s, usageSeconds=%s%n",
            conversation.getId(), conversation.getStatus(), conversation.getCreatedAt(), conversation.getCompletedAt(),
            conversation.getUsage() == null ? null : conversation.getUsage().getSeconds());
    }

    static void printItem(VoiceAgentLiveConversationItem item) {
        System.out.printf("Item %s: type=%s%n", item.getId(), item.getType());
        if (item instanceof VoiceAgentLiveInputConversationItem) {
            System.out.println("User transcript: " + ((VoiceAgentLiveInputConversationItem) item).getTranscription());
        } else if (item instanceof VoiceAgentLiveOutputConversationItem) {
            System.out.println("Assistant transcript: " + ((VoiceAgentLiveOutputConversationItem) item).getTranscription());
        } else if (item instanceof VoiceAgentLiveDelegationConversationItem) {
            VoiceAgentLiveDelegationConversationItem delegation = (VoiceAgentLiveDelegationConversationItem) item;
            System.out.printf("Delegation target=%s, response=%s%n", delegation.getTarget(), delegation.getResponseId());
        }
    }

}
