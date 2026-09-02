// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents;

import com.azure.ai.agents.models.AgentDetails;
import com.azure.ai.agents.models.AgentEndpointConfig;
import com.azure.ai.agents.models.AgentVersionDetails;
import com.azure.ai.agents.models.AzureCreateResponseOptions;
import com.azure.ai.agents.models.CreateAgentVersionInput;
import com.azure.ai.agents.models.FixedRatioVersionSelectionRule;
import com.azure.ai.agents.models.PromptAgentDefinition;
import com.azure.ai.agents.models.ProtocolConfiguration;
import com.azure.ai.agents.models.ResponsesProtocolConfiguration;
import com.azure.ai.agents.models.UpdateAgentDetailsOptions;
import com.azure.ai.agents.models.VersionSelector;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.openai.models.conversations.items.ItemCreateParams;
import com.openai.models.responses.EasyInputMessage;
import com.openai.models.responses.ResponseCreateParams;
import com.openai.services.async.ConversationServiceAsync;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Demonstrates asynchronous prompt-agent creation, endpoint routing, and a multi-turn conversation.
 *
 * <p>Before running the sample, set these environment variables:</p>
 * <ul>
 *   <li>{@code FOUNDRY_PROJECT_ENDPOINT} - The Azure AI Project endpoint.</li>
 *   <li>{@code FOUNDRY_MODEL_NAME} - The model deployment name.</li>
 * </ul>
 */
public class AgentBasicAsyncSample {
    public static void main(String[] args) {
        Configuration configuration = Configuration.getGlobalConfiguration();
        String endpoint = configuration.get("FOUNDRY_PROJECT_ENDPOINT");
        String model = configuration.get("FOUNDRY_MODEL_NAME");

        AgentsClientBuilder builder = new AgentsClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint(endpoint);
        AgentsAsyncClient agentsClient = builder.buildAgentsAsyncClient();
        ResponsesAsyncClient responsesClient = builder.buildResponsesAsyncClient();
        ConversationServiceAsync conversations = builder.buildOpenAIAsyncClient().conversations();

        String agentName = "basic-async-agent";
        AtomicReference<AgentVersionDetails> agentRef = new AtomicReference<>();
        AtomicReference<AgentEndpointConfig> originalEndpointRef = new AtomicReference<>();
        AtomicReference<String> conversationIdRef = new AtomicReference<>();

        Mono<Void> sample = agentsClient.createAgentVersion(agentName,
                new CreateAgentVersionInput(new PromptAgentDefinition(model)
                    .setInstructions("You are a helpful assistant that answers general questions.")))
            .doOnNext(agent -> {
                agentRef.set(agent);
                System.out.printf("Agent created: %s (version %s)%n", agent.getName(), agent.getVersion());
            })
            .flatMap(agent -> agentsClient.getAgent(agentName)
                .doOnNext(details -> originalEndpointRef.set(details.getAgentEndpoint()))
                .thenReturn(agent))
            .flatMap(agent -> {
                AgentEndpointConfig endpointConfig = new AgentEndpointConfig()
                    .setVersionSelector(new VersionSelector().setVersionSelectionRules(Collections.singletonList(
                        new FixedRatioVersionSelectionRule(100).setAgentVersion(agent.getVersion()))))
                    .setProtocolConfiguration(new ProtocolConfiguration()
                        .setResponses(new ResponsesProtocolConfiguration()));
                return agentsClient.updateAgentDetails(agentName,
                    new UpdateAgentDetailsOptions().setAgentEndpoint(endpointConfig));
            })
            .then(Mono.fromFuture(conversations.create()))
            .doOnNext(conversation -> {
                conversationIdRef.set(conversation.id());
                System.out.println("Conversation created: " + conversation.id());
            })
            .flatMap(conversation -> responsesClient.createAzureResponse(
                new AzureCreateResponseOptions().setAgentReference(
                    SampleUtils.toAgentReference(agentRef.get())),
                ResponseCreateParams.builder()
                    .conversation(conversation.id())
                    .input("What is the size of France in square miles?")))
            .doOnNext(SampleUtils::printResponseText)
            .flatMap(response -> Mono.fromFuture(conversations.items().create(ItemCreateParams.builder()
                .conversationId(conversationIdRef.get())
                .addItem(EasyInputMessage.builder()
                    .role(EasyInputMessage.Role.USER)
                    .content("What is its capital city?")
                    .build())
                .build())))
            .then(responsesClient.createAzureResponse(
                new AzureCreateResponseOptions().setAgentReference(
                    SampleUtils.toAgentReference(agentRef.get())),
                ResponseCreateParams.builder().conversation(conversationIdRef.get())))
            .doOnNext(SampleUtils::printResponseText)
            .then();

        sample.then(cleanup(agentsClient, conversations, agentName, agentRef, originalEndpointRef,
                conversationIdRef))
            .onErrorResume(error -> cleanup(agentsClient, conversations, agentName, agentRef,
                    originalEndpointRef, conversationIdRef)
                .then(Mono.error(error)))
            .block();
    }

    private static Mono<Void> cleanup(AgentsAsyncClient agentsClient, ConversationServiceAsync conversations,
        String agentName, AtomicReference<AgentVersionDetails> agentRef,
        AtomicReference<AgentEndpointConfig> originalEndpointRef, AtomicReference<String> conversationIdRef) {
        Mono<Void> deleteConversation = conversationIdRef.get() == null
            ? Mono.empty()
            : Mono.fromFuture(conversations.delete(conversationIdRef.get())).then();
        Mono<AgentDetails> restoreEndpoint = agentRef.get() == null
            ? Mono.empty()
            : agentsClient.updateAgentDetails(agentName,
                new UpdateAgentDetailsOptions().setAgentEndpoint(originalEndpointRef.get()));
        Mono<Void> deleteVersion = agentRef.get() == null
            ? Mono.empty()
            : agentsClient.deleteAgentVersion(agentName, agentRef.get().getVersion());
        return deleteConversation.then(restoreEndpoint).then(deleteVersion);
    }
}
