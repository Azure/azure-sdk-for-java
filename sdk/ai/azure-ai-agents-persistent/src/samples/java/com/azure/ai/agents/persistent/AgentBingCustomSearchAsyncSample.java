// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.persistent;

import com.azure.ai.agents.persistent.implementation.models.CreateAgentRequest;
import com.azure.ai.agents.persistent.models.BingCustomSearchConfiguration;
import com.azure.ai.agents.persistent.models.BingCustomSearchConfigurationList;
import com.azure.ai.agents.persistent.models.BingCustomSearchToolDefinition;
import com.azure.ai.agents.persistent.models.CreateRunOptions;
import com.azure.ai.agents.persistent.models.MessageRole;
import com.azure.ai.agents.persistent.models.PersistentAgent;
import com.azure.ai.agents.persistent.models.ToolConnection;
import com.azure.ai.agents.persistent.models.ToolConnectionList;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;

import static com.azure.ai.agents.persistent.SampleUtils.printRunMessagesAsync;
import static com.azure.ai.agents.persistent.SampleUtils.waitForRunCompletionAsync;

public class AgentBingCustomSearchAsyncSample {

    public static void main(String[] args) {
        PersistentAgentsAdministrationClientBuilder clientBuilder = new PersistentAgentsAdministrationClientBuilder()
            .endpoint(Configuration.getGlobalConfiguration().get("ENDPOINT", "endpoint"))
            .credential(new DefaultAzureCredentialBuilder().build());
        
        PersistentAgentsAdministrationAsyncClient agentsAsyncClient = clientBuilder.buildAsyncClient();
        ThreadsAsyncClient threadsAsyncClient = clientBuilder.buildThreadsAsyncClient();
        MessagesAsyncClient messagesAsyncClient = clientBuilder.buildMessagesAsyncClient();
        RunsAsyncClient runsAsyncClient = clientBuilder.buildRunsAsyncClient();

        String bingConnectionId = Configuration.getGlobalConfiguration().get("BING_SEARCH_CONNECTION_ID", "");
        String bingConfigurationId = Configuration.getGlobalConfiguration().get("BING_SEARCH_CONFIGURATION_ID", "");

        ToolConnectionList toolConnectionList = new ToolConnectionList()
            .setConnectionList(Arrays.asList(new ToolConnection(bingConnectionId)));

        BingCustomSearchConfiguration searchConfiguration = new BingCustomSearchConfiguration(bingConnectionId, bingConfigurationId);
        BingCustomSearchConfigurationList searchConfigurationList = new BingCustomSearchConfigurationList(Arrays.asList(searchConfiguration));

        BingCustomSearchToolDefinition bingCustomSearchToolDefinition = new BingCustomSearchToolDefinition(searchConfigurationList);

        String agentName = "bing_custom_search_example_async";
        CreateAgentRequest createAgentRequest = new CreateAgentRequest("gpt-4o-mini")
            .setName(agentName)
            .setInstructions("You are a helpful agent")
            .setTools(Arrays.asList(bingCustomSearchToolDefinition));
        
        RequestOptions requestOptions = new RequestOptions()
            .setHeader("x-ms-enable-preview", "true");
        
        // Track resources for cleanup
        AtomicReference<String> agentId = new AtomicReference<>();
        AtomicReference<String> threadId = new AtomicReference<>();
        
        // Create full reactive chain
        agentsAsyncClient.createAgentWithResponse(BinaryData.fromObject(createAgentRequest), requestOptions)
            .flatMap(response -> {
                PersistentAgent agent = response.getValue().toObject(PersistentAgent.class);
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
                            "How does wikipedia explain Euler's Identity?"
                        ).flatMap(message -> {
                            System.out.println("Created initial message");
                            
                            // Create run
                            CreateRunOptions createRunOptions = new CreateRunOptions(thread.getId(), agent.getId())
                                .setAdditionalInstructions("");
                            
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
                    agentsAsyncClient.deleteAgent(agentId.get())
                        .doOnSuccess(ignored -> System.out.println("Agent deleted: " + agentId.get()))
                        .doOnError(error -> System.err.println("Failed to delete agent: " + error.getMessage()))
                        .subscribe();
                }
            })
            .doOnError(error -> System.err.println("An error occurred: " + error.getMessage()))
            .block(); // Only block at the end of the reactive chain
    }
}
