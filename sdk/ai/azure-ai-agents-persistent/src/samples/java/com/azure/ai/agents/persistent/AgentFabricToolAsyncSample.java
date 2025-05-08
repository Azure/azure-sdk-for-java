// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.agents.persistent;

import com.azure.ai.agents.persistent.implementation.models.CreateAgentRequest;
import com.azure.ai.agents.persistent.models.CreateRunOptions;
import com.azure.ai.agents.persistent.models.MessageRole;
import com.azure.ai.agents.persistent.models.MicrosoftFabricToolDefinition;
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

public final class AgentFabricToolAsyncSample {

    public static void main(String[] args) {
        // Initialize async clients
        PersistentAgentsAdministrationClientBuilder clientBuilder = new PersistentAgentsAdministrationClientBuilder()
            .endpoint(Configuration.getGlobalConfiguration().get("ENDPOINT", "endpoint"))
            .credential(new DefaultAzureCredentialBuilder().build());
        
        PersistentAgentsAdministrationAsyncClient agentsAsyncClient = clientBuilder.buildAsyncClient();
        ThreadsAsyncClient threadsAsyncClient = clientBuilder.buildThreadsAsyncClient();
        MessagesAsyncClient messagesAsyncClient = clientBuilder.buildMessagesAsyncClient();
        RunsAsyncClient runsAsyncClient = clientBuilder.buildRunsAsyncClient();

        // Track resources for cleanup
        AtomicReference<String> agentId = new AtomicReference<>();
        AtomicReference<String> threadId = new AtomicReference<>();

        // Get Fabric connection ID from configuration
        String fabricConnectionId = Configuration.getGlobalConfiguration().get("FABRIC_CONNECTION_ID", "");
        System.out.println("Using Fabric connection ID: " + fabricConnectionId);
        
        // Create tool connections list with Fabric connection
        ToolConnectionList toolConnectionList = new ToolConnectionList()
            .setConnectionList(Arrays.asList(new ToolConnection(fabricConnectionId)));
        
        // Create Fabric tool definition
        MicrosoftFabricToolDefinition fabricToolDefinition = new MicrosoftFabricToolDefinition(toolConnectionList);

        // Create agent request with Fabric tool
        String agentName = "fabric_tool_async_example";
        RequestOptions requestOptions = new RequestOptions().setHeader("x-ms-enable-preview", "true");
        CreateAgentRequest createAgentRequest = new CreateAgentRequest("gpt-4o-mini")
            .setName(agentName)
            .setInstructions("You are a helpful agent specialized in querying Microsoft Fabric data")
            .setTools(Arrays.asList(fabricToolDefinition));

        // Create agent with Fabric tool
        agentsAsyncClient.createAgentWithResponse(BinaryData.fromObject(createAgentRequest), requestOptions)
            .flatMap(response -> {
                // Extract agent from response
                PersistentAgent agent =
                    response.getValue().toObject(PersistentAgent.class);
                agentId.set(agent.getId());
                System.out.println("Created agent: " + agent.getId());
                
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
                    "Give me any row from DailyActivity table from connected fabric usage metrics source");
            })
            .flatMap(message -> {
                System.out.println("Created message");
                
                // Create and start the run
                CreateRunOptions createRunOptions = new CreateRunOptions(threadId.get(), agentId.get())
                    .setAdditionalInstructions("Please provide detailed information from the Fabric data");
                
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
                
                // Delete thread
                if (threadId.get() != null) {
                    threadsAsyncClient.deleteThread(threadId.get()).block();
                    System.out.println("Deleted thread: " + threadId.get());
                }
                
                // Delete agent
                if (agentId.get() != null) {
                    agentsAsyncClient.deleteAgent(agentId.get()).block();
                    System.out.println("Deleted agent: " + agentId.get());
                }
            })
            .doOnError(error -> System.err.println("An error occurred: " + error.getMessage()))
            .block(); // Only block at the end of the reactive chain
    }
}
