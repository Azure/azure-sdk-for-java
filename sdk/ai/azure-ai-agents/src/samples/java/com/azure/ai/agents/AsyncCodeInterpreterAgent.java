// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents;

import com.azure.ai.agents.models.AgentReference;
import com.azure.ai.agents.models.AgentVersionDetails;
import com.azure.ai.agents.models.CodeInterpreterContainerAuto;
import com.azure.ai.agents.models.CodeInterpreterTool;
import com.azure.ai.agents.models.PromptAgentDefinition;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.openai.models.conversations.Conversation;
import com.openai.models.responses.ResponseCodeInterpreterToolCall;
import com.openai.models.responses.ResponseCreateParams;
import com.openai.models.responses.ResponseOutputItem;
import com.openai.models.responses.ResponseOutputMessage;
import com.azure.ai.agents.implementation.OpenAIJsonHelper;
import com.openai.core.JsonValue;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.function.Tuple2;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

/**
 * This sample demonstrates how to create an Azure AI Agent with the Code Interpreter tool
 * and use it to get responses that involve code execution using the async client.
 */
public class AsyncCodeInterpreterAgent {
    public static void main(String[] args) throws InterruptedException {
        String endpoint = Configuration.getGlobalConfiguration().get("AZURE_AGENTS_ENDPOINT");
        String model = Configuration.getGlobalConfiguration().get("AZURE_AGENTS_MODEL");

        AgentsClientBuilder builder = new AgentsClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .serviceVersion(AgentsServiceVersion.getLatest())
            .endpoint(endpoint);

        AgentsAsyncClient agentsAsyncClient = builder.buildAgentsAsyncClient();
        ResponsesAsyncClient responsesAsyncClient = builder.buildResponsesAsyncClient();
        ConversationsAsyncClient conversationsAsyncClient = builder.buildConversationsAsyncClient();

        // For cleanup
        AtomicReference<AgentVersionDetails> agentRef = new AtomicReference<>();
        AtomicReference<Conversation> conversationRef = new AtomicReference<>();

        // Create a CodeInterpreterTool with auto container configuration
        CodeInterpreterContainerAuto containerConfig = new CodeInterpreterContainerAuto();
        CodeInterpreterTool tool = new CodeInterpreterTool(BinaryData.fromObject(containerConfig));

        // Create the agent definition with Code Interpreter tool enabled
        PromptAgentDefinition agentDefinition = new PromptAgentDefinition(model)
            .setInstructions("You are a helpful assistant that can execute Python code to solve problems. "
                + "When asked to perform calculations or data analysis, use the code interpreter to run Python code.")
            .setTools(Collections.singletonList(tool));

        agentsAsyncClient.createAgentVersion("MyAgent", agentDefinition)
            .flatMap(agent ->
                Mono.fromFuture(conversationsAsyncClient.getConversationServiceAsync().create())
                    .map(conversation -> new Pair<>(agent, conversation)))
            .flatMap(pair -> {
                AgentVersionDetails agent = pair.getFirst();
                Conversation conversation = pair.getSecond();
                agentRef.set(agent);
                conversationRef.set(conversation);

                AgentReference agentReference = new AgentReference(agent.getName()).setVersion(agent.getVersion());

                return responsesAsyncClient.createWithAgentConversation(agentReference, conversation.id(),
                        ResponseCreateParams.builder()
                                .input("Calculate the first 10 prime numbers and show me the Python code you used."));
            })
            .doOnNext(response -> {
                // Process and display the response
                System.out.println("\n=== Agent Response ===");
                for (ResponseOutputItem outputItem : response.output()) {
                    // Handle message output
                    if (outputItem.message().isPresent()) {
                        ResponseOutputMessage message = outputItem.message().get();
                        message.content().forEach(content -> {
                            content.outputText().ifPresent(text -> {
                                System.out.println("Assistant: " + text.text());
                            });
                        });
                    }

                    // Handle code interpreter tool call output
                    if (outputItem.codeInterpreterCall().isPresent()) {
                        ResponseCodeInterpreterToolCall codeCall = outputItem.codeInterpreterCall().get();
                        System.out.println("\n--- Code Interpreter Execution ---");
                        System.out.println("Call ID: " + codeCall.id());
                        codeCall.code().ifPresent(code -> {
                            System.out.println("Python Code Executed:\n" + code);
                        });
                        System.out.println("Status: " + codeCall.status());
                    }
                }

                System.out.println("\nResponse ID: " + response.id());
                System.out.println("Model Used: " + response.model());
            })
            // Cleanup conversation and agent
            .then(Mono.defer(() -> {
                Conversation conversation = conversationRef.get();
                if (conversation != null) {
                    return Mono.fromCallable(() -> conversationsAsyncClient.getConversationServiceAsync().delete(conversation.id()).get())
                        .subscribeOn(Schedulers.boundedElastic())
                        .doOnSuccess(v -> System.out.println("Conversation deleted successfully."));
                }
                return Mono.empty();
            }))
            .then(Mono.defer(() -> {
                AgentVersionDetails agent = agentRef.get();
                if (agent != null) {
                    return agentsAsyncClient.deleteAgentVersion(agent.getName(), agent.getVersion())
                        .doOnSuccess(v -> System.out.println("Agent deleted successfully."));
                }
                return Mono.empty();
            }))
            .doOnError(error -> {
                System.err.println("Error: " + error.getMessage());
                error.printStackTrace();
            }).timeout(Duration.ofSeconds(30))
                .block();
    }

    private static class Pair<T, R> {
        private final T first;
        private final R second;

        public Pair(T first, R second) {
            this.first = first;
            this.second = second;
        }

        public T getFirst() {
            return first;
        }

        public R getSecond() {
            return second;
        }
    }
}
