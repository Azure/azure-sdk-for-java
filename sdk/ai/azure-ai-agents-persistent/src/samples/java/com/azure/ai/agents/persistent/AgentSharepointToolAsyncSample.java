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

import static com.azure.ai.agents.persistent.SampleUtils.cleanUpResources;
import static com.azure.ai.agents.persistent.SampleUtils.printRunMessagesAsync;
import static com.azure.ai.agents.persistent.SampleUtils.waitForRunCompletionAsync;

public final class AgentSharepointToolAsyncSample {

    public static void main(String[] args) {
        PersistentAgentsAdministrationClientBuilder clientBuilder = new PersistentAgentsAdministrationClientBuilder()
            .endpoint(Configuration.getGlobalConfiguration().get("ENDPOINT", "endpoint"))
            .credential(new DefaultAzureCredentialBuilder().build());
        
        PersistentAgentsAdministrationAsyncClient agentsAsyncClient = clientBuilder.buildAsyncClient();
        ThreadsAsyncClient threadsAsyncClient = clientBuilder.buildThreadsAsyncClient();
        MessagesAsyncClient messagesAsyncClient = clientBuilder.buildMessagesAsyncClient();
        RunsAsyncClient runsAsyncClient = clientBuilder.buildRunsAsyncClient();

        String sharepointConnectionId = Configuration.getGlobalConfiguration().get("SHAREPOINT_CONNECTION_ID", "");
        ToolConnectionList toolConnectionList = new ToolConnectionList()
            .setConnectionList(Arrays.asList(new ToolConnection(sharepointConnectionId)));
        MicrosoftFabricToolDefinition fabricToolDefinition = new MicrosoftFabricToolDefinition(toolConnectionList);

        String agentName = "sharepoint_tool_async_example";
        RequestOptions requestOptions = new RequestOptions().setHeader("x-ms-enable-preview", "true");
        CreateAgentRequest createAgentRequest = new CreateAgentRequest("gpt-4o")
            .setName(agentName)
            .setInstructions("You are a helpful agent")
            .setTools(Arrays.asList(fabricToolDefinition));

        // Track resources for cleanup
        AtomicReference<String> agentId = new AtomicReference<>();
        AtomicReference<String> threadId = new AtomicReference<>();
        
        // Create full reactive chain
        agentsAsyncClient.createAgentWithResponse(BinaryData.fromObject(createAgentRequest), requestOptions)
            .map(response -> {
                PersistentAgent agent = response.getValue().toObject(PersistentAgent.class);
                System.out.println("Created agent: " + agent.getId());
                agentId.set(agent.getId());
                return agent;
            })
            .flatMap(agent -> threadsAsyncClient.createThread())
            .flatMap(thread -> {
                System.out.println("Created thread: " + thread.getId());
                threadId.set(thread.getId());
                
                // Create initial message
                return messagesAsyncClient.createMessage(
                    thread.getId(),
                    MessageRole.USER,
                    "Give me summary of the document in the sharepoint site"
                ).flatMap(message -> {
                    System.out.println("Created initial message");
                    
                    // Create and start the run
                    CreateRunOptions createRunOptions = new CreateRunOptions(thread.getId(), agentId.get())
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
            })
            .doFinally(signalType -> cleanUpResources(threadId, threadsAsyncClient, agentId, agentsAsyncClient))
            .doOnError(error -> System.err.println("An error occurred: " + error.getMessage()))
            .block(); // Only block at the end of the reactive chain
    }
}
