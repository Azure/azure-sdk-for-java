// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.agents.persistent;

import com.azure.ai.agents.persistent.models.CodeInterpreterToolDefinition;
import com.azure.ai.agents.persistent.models.CreateAgentOptions;
import com.azure.ai.agents.persistent.models.CreateRunOptions;
import com.azure.ai.agents.persistent.models.MessageRole;
import com.azure.ai.agents.persistent.models.ThreadMessageOptions;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;

import static com.azure.ai.agents.persistent.SampleUtils.printRunMessagesAsync;
import static com.azure.ai.agents.persistent.SampleUtils.waitForRunCompletionAsync;

public final class AgentAdditionalMessageAsyncSample {

    public static void main(String[] args) {
        PersistentAgentsClientBuilder clientBuilder = new PersistentAgentsClientBuilder()
            .endpoint(Configuration.getGlobalConfiguration().get("ENDPOINT", "endpoint"))
            .credential(new DefaultAzureCredentialBuilder().build());
        PersistentAgentsAsyncClient agentsAsyncClient = clientBuilder.buildAsyncClient();
        PersistentAgentsAdministrationAsyncClient administrationAsyncClient = agentsAsyncClient.getPersistentAgentsAdministrationAsyncClient();
        ThreadsAsyncClient threadsAsyncClient = agentsAsyncClient.getThreadsAsyncClient();
        MessagesAsyncClient messagesAsyncClient = agentsAsyncClient.getMessagesAsyncClient();
        RunsAsyncClient runsAsyncClient = agentsAsyncClient.getRunsAsyncClient();

        String agentName = "additional_message_example_async";
        CreateAgentOptions createAgentOptions = new CreateAgentOptions("gpt-4o-mini")
            .setName(agentName)
            .setInstructions("You are a personal electronics tutor. Write and run code to answer questions.")
            .setTools(Arrays.asList(new CodeInterpreterToolDefinition()));

        // Track resources for cleanup
        AtomicReference<String> agentId = new AtomicReference<>();
        AtomicReference<String> threadId = new AtomicReference<>();
        
        // Create full reactive chain to showcase reactive programming
        administrationAsyncClient.createAgent(createAgentOptions)
            .flatMap(agent -> {
                System.out.println("Created agent: " + agent.getId());
                agentId.set(agent.getId());
                
                return threadsAsyncClient.createThread()
                    .flatMap(thread -> {
                        System.out.println("Created thread: " + thread.getId());
                        threadId.set(thread.getId());
                        
                        // Create initial message
                        return messagesAsyncClient.createMessage(
                            thread.getId(),
                            MessageRole.USER,
                            "What is the impedance formula?"
                        ).flatMap(message -> {
                            System.out.println("Created initial message");
                            
                            // Create run with additional messages
                            CreateRunOptions createRunOptions = new CreateRunOptions(thread.getId(), agent.getId())
                                .setAdditionalMessages(Arrays.asList(
                                    new ThreadMessageOptions(
                                        MessageRole.AGENT, BinaryData.fromString("E=mc^2")
                                    ),
                                    new ThreadMessageOptions(
                                        MessageRole.USER, BinaryData.fromString("What is the impedance formula?")
                                    )
                                ));
                            
                            return runsAsyncClient.createRun(createRunOptions)
                                .flatMap(threadRun -> {
                                    System.out.println("Created run, waiting for completion...");
                                    return waitForRunCompletionAsync(thread.getId(), threadRun, runsAsyncClient);
                                })
                                .flatMap(completedRun -> {
                                    System.out.println("Run completed with status: " + completedRun.getStatus());
                                    return printRunMessagesAsync(messagesAsyncClient, thread.getId());
                                });
                        });
                    });
            })
            .doFinally(signalType -> {
                // Always clean up resources regardless of success or failure
                System.out.println("Cleaning up resources...");
                
                // Clean up thread if created
                if (threadId.get() != null) {
                    threadsAsyncClient.deleteThread(threadId.get())
                        .doOnSuccess(ignored -> System.out.println("Thread deleted: " + threadId.get()))
                        .doOnError(error -> System.err.println("Failed to delete thread: " + error.getMessage()))
                        .subscribe();
                }
                
                // Clean up agent if created
                if (agentId.get() != null) {
                    administrationAsyncClient.deleteAgent(agentId.get())
                        .doOnSuccess(ignored -> System.out.println("Agent deleted: " + agentId.get()))
                        .doOnError(error -> System.err.println("Failed to delete agent: " + error.getMessage()))
                        .subscribe();
                }
            })
            .doOnError(error -> System.err.println("An error occurred: " + error.getMessage()))
            .block(); // Only block at the end of the reactive chain
    }
}
