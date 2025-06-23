// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.agents.persistent;

import com.azure.ai.agents.persistent.implementation.models.CreateAgentRequest;
import com.azure.ai.agents.persistent.models.ConnectedAgentDetails;
import com.azure.ai.agents.persistent.models.ConnectedAgentToolDefinition;
import com.azure.ai.agents.persistent.models.CreateAgentOptions;
import com.azure.ai.agents.persistent.models.CreateRunOptions;
import com.azure.ai.agents.persistent.models.MessageRole;
import com.azure.ai.agents.persistent.models.PersistentAgent;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import reactor.core.publisher.Mono;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;

import static com.azure.ai.agents.persistent.SampleUtils.printRunMessagesAsync;
import static com.azure.ai.agents.persistent.SampleUtils.waitForRunCompletionAsync;

public final class AgentConnectedAgentAsyncSample {

    public static void main(String[] args) {
        // Initialize async clients
        PersistentAgentsClientBuilder clientBuilder = new PersistentAgentsClientBuilder()
            .endpoint(Configuration.getGlobalConfiguration().get("ENDPOINT", "endpoint"))
            .credential(new DefaultAzureCredentialBuilder().build());

        PersistentAgentsAsyncClient agentsAsyncClient = clientBuilder.buildAsyncClient();
        PersistentAgentsAdministrationAsyncClient administrationAsyncClient = agentsAsyncClient.getPersistentAgentsAdministrationAsyncClient();
        ThreadsAsyncClient threadsAsyncClient = agentsAsyncClient.getThreadsAsyncClient();
        MessagesAsyncClient messagesAsyncClient = agentsAsyncClient.getMessagesAsyncClient();
        RunsAsyncClient runsAsyncClient = agentsAsyncClient.getRunsAsyncClient();

        // Track resources for cleanup
        AtomicReference<String> connectedAgentId = new AtomicReference<>();
        AtomicReference<String> mainAgentId = new AtomicReference<>();
        AtomicReference<String> threadId = new AtomicReference<>();

        // Define connected agent properties
        String connectedAgentName = "stock_price_bot_async";
        CreateAgentOptions connectedAgentCreateOptions = new CreateAgentOptions("gpt-4o-mini")
            .setName(connectedAgentName)
            .setInstructions("Your job is to get the stock price of a company. Just return $391.85 EOD 27-Apr-2025");

        // Create the connected agent first
        administrationAsyncClient.createAgent(connectedAgentCreateOptions)
            .flatMap(connectedAgent -> {
                connectedAgentId.set(connectedAgent.getId());
                System.out.println("Created connected agent: " + connectedAgent.getId());

                // Create main agent with connected agent tool
                ConnectedAgentToolDefinition connectedAgentToolDefinition = new ConnectedAgentToolDefinition(
                    new ConnectedAgentDetails(connectedAgent.getId(), connectedAgent.getName(), 
                    "Gets the stock price of a company"));

                String mainAgentName = "my-assistant-async";
                CreateAgentRequest createAgentRequest = new CreateAgentRequest("gpt-4o-mini")
                    .setName(mainAgentName)
                    .setInstructions("You are a helpful assistant, and use the connected agent to get stock prices.")
                    .setTools(Arrays.asList(connectedAgentToolDefinition));
                
                RequestOptions requestOptions = new RequestOptions()
                    .setHeader("x-ms-enable-preview", "true");
                
                return administrationAsyncClient.createAgentWithResponse(BinaryData.fromObject(createAgentRequest), requestOptions)
                    .flatMap(response -> {
                        return Mono.just(response.getValue().toObject(PersistentAgent.class));
                    });
            })
            .flatMap(mainAgent -> {
                mainAgentId.set(mainAgent.getId());
                System.out.println("Created main agent: " + mainAgent.getId());
                
                // Create a thread
                return threadsAsyncClient.createThread();
            })
            .flatMap(thread -> {
                threadId.set(thread.getId());
                System.out.println("Created thread: " + thread.getId());
                
                // Create message
                return messagesAsyncClient.createMessage(
                    thread.getId(),
                    MessageRole.USER,
                    "What is the stock price of Microsoft?");
            })
            .flatMap(message -> {
                System.out.println("Created message");
                
                // Create and start the run
                CreateRunOptions createRunOptions = new CreateRunOptions(threadId.get(), mainAgentId.get())
                    .setAdditionalInstructions("");
                
                return runsAsyncClient.createRun(createRunOptions)
                    .flatMap(threadRun -> {
                        System.out.println("Created run, waiting for completion...");
                        return waitForRunCompletionAsync(threadId.get(), threadRun, runsAsyncClient);
                    })
                    .flatMap(completedRun -> {
                        System.out.println("Run completed with status: " + completedRun.getStatus());
                        return printRunMessagesAsync(messagesAsyncClient, threadId.get());
                    });
            })
            .doFinally(signalType -> {
                System.out.println("Cleaning up resources...");
                // Clean up the thread
                if (threadId.get() != null) {
                    threadsAsyncClient.deleteThread(threadId.get()).block();
                    System.out.println("Deleted thread: " + threadId.get());
                }
                
                // Clean up the main agent
                if (mainAgentId.get() != null) {
                    administrationAsyncClient.deleteAgent(mainAgentId.get()).block();
                    System.out.println("Deleted main agent: " + mainAgentId.get());
                }
                
                // Clean up the connected agent
                if (connectedAgentId.get() != null) {
                    administrationAsyncClient.deleteAgent(connectedAgentId.get()).block();
                    System.out.println("Deleted connected agent: " + connectedAgentId.get());
                }
            })
            .doOnError(error -> System.err.println("An error occurred: " + error.getMessage()))
            .block(); // Only block at the end of the reactive chain
    }
}
