// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.voice;

import com.azure.ai.agents.AgentsClientBuilder;
import com.azure.ai.agents.BetaVoiceAgentsLiveConversationsAsyncClient;
import com.azure.ai.agents.models.PageOrder;
import com.azure.ai.agents.models.VoiceAgentLiveConversation;
import com.azure.ai.agents.models.VoiceAgentLiveConversationItem;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Asynchronously reads persisted GPT Live conversations and transcript items.
 * The session must have used {@code store=true}. The agent and conversation must still exist.
 * Like {@link GptLiveReadConversationSample}, this sample prints sensitive stored content without modifying data.
 * Blocking occurs only at the standalone main method boundary.
 *
 * <p>Before running the sample, set these environment variables:</p>
 * <ul>
 *   <li>{@code FOUNDRY_PROJECT_ENDPOINT} - Required. The Azure AI Project endpoint. No default.</li>
 *   <li>{@code FOUNDRY_GPT_LIVE_AGENT_NAME} - Required. The existing GPT Live agent's name. No default.</li>
 *   <li>{@code FOUNDRY_GPT_LIVE_CONVERSATION_ID} - Required. The persisted Foundry conversation ID from
 *   {@code session.started.conversation_id}, not the native session ID. No default.</li>
 * </ul>
 *
 * <p>Authenticates with {@code DefaultAzureCredential}. Run without command-line arguments.
 * Model deployment settings are not needed because this sample reads an existing conversation.</p>
 */
public class GptLiveReadConversationAsyncSample {
    public static void main(String[] args) {
        Configuration configuration = Configuration.getGlobalConfiguration();
        String endpoint = GptLiveSampleUtils.requireSetting(configuration, "FOUNDRY_PROJECT_ENDPOINT");
        String agentName = GptLiveSampleUtils.requireSetting(configuration, "FOUNDRY_GPT_LIVE_AGENT_NAME");
        String conversationId
            = GptLiveSampleUtils.requireSetting(configuration, "FOUNDRY_GPT_LIVE_CONVERSATION_ID");
        BetaVoiceAgentsLiveConversationsAsyncClient client = new AgentsClientBuilder().endpoint(endpoint)
            .credential(new DefaultAzureCredentialBuilder().build())
            .addPolicy(GptLiveReadConversationSample.previewHeaders())
            .buildBetaVoiceAgentsLiveConversationsAsyncClient();
        readConversation(client, agentName, conversationId).block();
    }

    static Mono<Void> readConversation(BetaVoiceAgentsLiveConversationsAsyncClient client, String agentName,
        String conversationId) {
        return listConversations(client, agentName, null)
            .doOnNext(conversation -> System.out.printf("Available conversation: %s (%s)%n",
                conversation.getId(), conversation.getStatus()))
            .then(Mono.defer(() -> client.getAgentConversation(agentName, conversationId)
                .doOnNext(GptLiveReadConversationSample::printConversation)))
            .thenMany(listItems(client, agentName, conversationId, null))
            .concatMap(item -> client.getAgentConversationItem(agentName, conversationId, item.getId()), 0)
            .doOnNext(GptLiveReadConversationSample::printItem)
            .then();
    }

    private static Flux<VoiceAgentLiveConversation> listConversations(BetaVoiceAgentsLiveConversationsAsyncClient client,
        String agentName, String after) {
        return client.listAgentConversations(agentName, 100, PageOrder.ASC, after, null).collectList()
            .flatMapMany(page -> {
                if (page.isEmpty()) {
                    return Flux.empty();
                }
                String next = GptLiveReadConversationSample.nextCursor(page.get(page.size() - 1).getId(), after);
                return Flux.fromIterable(page).concatWith(Flux.defer(() -> listConversations(client, agentName, next)));
            });
    }

    private static Flux<VoiceAgentLiveConversationItem> listItems(BetaVoiceAgentsLiveConversationsAsyncClient client,
        String agentName, String conversationId, String after) {
        return client.listAgentConversationItems(agentName, conversationId, 100, PageOrder.ASC, after, null).collectList()
            .flatMapMany(page -> {
                if (page.isEmpty()) {
                    return Flux.empty();
                }
                String next = GptLiveReadConversationSample.nextCursor(page.get(page.size() - 1).getId(), after);
                return Flux.fromIterable(page)
                    .concatWith(Flux.defer(() -> listItems(client, agentName, conversationId, next)));
            });
    }
}
