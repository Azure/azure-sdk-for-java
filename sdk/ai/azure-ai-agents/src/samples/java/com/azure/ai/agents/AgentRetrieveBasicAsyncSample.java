// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents;

import com.azure.ai.agents.models.AgentReference;
import com.azure.ai.agents.models.AgentVersionDetails;
import com.azure.ai.agents.models.AzureCreateResponseOptions;
import com.azure.ai.agents.models.CreateAgentVersionInput;
import com.azure.ai.agents.models.PromptAgentDefinition;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.openai.client.OpenAIClientAsync;
import com.openai.models.conversations.items.ItemCreateParams;
import com.openai.models.responses.EasyInputMessage;
import com.openai.models.responses.ResponseCreateParams;
import com.openai.services.async.ConversationServiceAsync;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Demonstrates asynchronously retrieving an agent and conversation before creating a response.
 *
 * <p>Before running the sample, set these environment variables:</p>
 * <ul>
 *   <li>{@code FOUNDRY_PROJECT_ENDPOINT} - The Azure AI Project endpoint.</li>
 *   <li>{@code FOUNDRY_MODEL_NAME} - The model deployment name.</li>
 * </ul>
 */
public class AgentRetrieveBasicAsyncSample {
    public static void main(String[] args) {
        Configuration configuration = Configuration.getGlobalConfiguration();
        String endpoint = configuration.get("FOUNDRY_PROJECT_ENDPOINT");
        String model = configuration.get("FOUNDRY_MODEL_NAME");

        AgentsClientBuilder builder = new AgentsClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint(endpoint);
        AgentsAsyncClient agentsClient = builder.buildAgentsAsyncClient();
        ResponsesAsyncClient responsesClient = builder.buildResponsesAsyncClient();
        OpenAIClientAsync openAIClient = builder.buildOpenAIAsyncClient();
        ConversationServiceAsync conversations = openAIClient.conversations();

        AtomicReference<AgentVersionDetails> agentRef = new AtomicReference<>();
        AtomicReference<String> conversationIdRef = new AtomicReference<>();

        try {
            agentsClient.createAgentVersion("retrieve-async-agent",
                    new CreateAgentVersionInput(new PromptAgentDefinition(model)
                        .setInstructions("You are a helpful assistant.")))
                .doOnNext(agentRef::set)
                .flatMap(agent -> agentsClient.getAgent(agent.getName()))
                .doOnNext(agent -> System.out.printf("Retrieved agent: %s (%s)%n", agent.getName(), agent.getId()))
                .then(Mono.defer(() -> Mono.fromFuture(conversations.create())))
                .doOnNext(conversation -> conversationIdRef.set(conversation.id()))
                .flatMap(conversation -> Mono.fromFuture(conversations.retrieve(conversation.id())))
                .doOnNext(conversation -> System.out.println("Retrieved conversation: " + conversation.id()))
                .flatMap(conversation -> Mono.fromFuture(conversations.items().create(ItemCreateParams.builder()
                    .conversationId(conversation.id())
                    .addItem(EasyInputMessage.builder()
                        .role(EasyInputMessage.Role.USER)
                        .content("How many feet are in a mile?")
                        .build())
                    .build())))
                .then(Mono.defer(() -> {
                    AgentVersionDetails agent = agentRef.get();
                    AgentReference reference = SampleUtils.toAgentReference(agent);
                    return responsesClient.createAzureResponse(
                        new AzureCreateResponseOptions().setAgentReference(reference),
                        ResponseCreateParams.builder().conversation(conversationIdRef.get()));
                }))
                .doOnNext(SampleUtils::printResponseText)
                .then(cleanup(agentsClient, conversations, agentRef, conversationIdRef))
                .onErrorResume(error -> cleanup(agentsClient, conversations, agentRef, conversationIdRef)
                    .then(Mono.error(error)))
                .block();
        } finally {
            openAIClient.close();
        }
    }

    private static Mono<Void> cleanup(AgentsAsyncClient agentsClient, ConversationServiceAsync conversations,
        AtomicReference<AgentVersionDetails> agentRef, AtomicReference<String> conversationIdRef) {
        Mono<Void> deleteConversation = conversationIdRef.get() == null
            ? Mono.empty()
            : Mono.fromFuture(conversations.delete(conversationIdRef.get())).then();
        AgentVersionDetails agent = agentRef.get();
        Mono<Void> deleteAgent = agent == null
            ? Mono.empty()
            : agentsClient.deleteAgentVersion(agent.getName(), agent.getVersion());
        return deleteConversation.then(deleteAgent);
    }
}
