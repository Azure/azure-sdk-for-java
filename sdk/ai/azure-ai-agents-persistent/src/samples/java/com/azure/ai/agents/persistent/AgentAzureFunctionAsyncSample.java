// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.agents.persistent;

import com.azure.ai.agents.persistent.implementation.models.CreateAgentRequest;
import com.azure.ai.agents.persistent.models.AzureFunctionBinding;
import com.azure.ai.agents.persistent.models.AzureFunctionDefinition;
import com.azure.ai.agents.persistent.models.AzureFunctionStorageQueue;
import com.azure.ai.agents.persistent.models.AzureFunctionToolDefinition;
import com.azure.ai.agents.persistent.models.CreateRunOptions;
import com.azure.ai.agents.persistent.models.FunctionDefinition;
import com.azure.ai.agents.persistent.models.MessageRole;
import com.azure.ai.agents.persistent.models.PersistentAgent;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static com.azure.ai.agents.persistent.SampleUtils.printRunMessagesAsync;
import static com.azure.ai.agents.persistent.SampleUtils.waitForRunCompletionAsync;

public class AgentAzureFunctionAsyncSample {

    public static void main(String[] args) {
        PersistentAgentsClientBuilder clientBuilder = new PersistentAgentsClientBuilder()
            .endpoint(Configuration.getGlobalConfiguration().get("ENDPOINT", "endpoint"))
            .credential(new DefaultAzureCredentialBuilder().build());

        PersistentAgentsAsyncClient agentsAsyncClient = clientBuilder.buildAsyncClient();
        PersistentAgentsAdministrationAsyncClient administrationAsyncClient = agentsAsyncClient.getPersistentAgentsAdministrationAsyncClient();
        ThreadsAsyncClient threadsAsyncClient = agentsAsyncClient.getThreadsAsyncClient();
        MessagesAsyncClient messagesAsyncClient = agentsAsyncClient.getMessagesAsyncClient();
        RunsAsyncClient runsAsyncClient = agentsAsyncClient.getRunsAsyncClient();

        String storageQueueUri = Configuration.getGlobalConfiguration().get("STORAGE_QUEUE_URI", "");
        String azureFunctionName = Configuration.getGlobalConfiguration().get("AZURE_FUNCTION_NAME", "");

        FunctionDefinition fnDef = new FunctionDefinition(
            azureFunctionName,
            BinaryData.fromObject(
                mapOf(
                    "type", "object",
                    "properties", mapOf(
                        "location",
                        mapOf("type", "string", "description", "The location to look up")
                    ),
                    "required", new String[]{"location"}
                )
            )
        );
        AzureFunctionDefinition azureFnDef = new AzureFunctionDefinition(
            fnDef,
            new AzureFunctionBinding(new AzureFunctionStorageQueue(storageQueueUri, "agent-input")),
            new AzureFunctionBinding(new AzureFunctionStorageQueue(storageQueueUri, "agent-output"))
        );
        AzureFunctionToolDefinition azureFnTool = new AzureFunctionToolDefinition(azureFnDef);

        String agentName = "azure_function_example_async";
        RequestOptions requestOptions = new RequestOptions()
            .setHeader(HttpHeaderName.fromString("x-ms-enable-preview"), "true");
        CreateAgentRequest createAgentRequestObj = new CreateAgentRequest("gpt-4o-mini")
            .setName(agentName)
            .setInstructions("You are a helpful agent. Use the provided function any time "
                + "you are asked with the weather of any location")
            .setTools(Arrays.asList(azureFnTool));
        BinaryData createAgentRequest = BinaryData.fromObject(createAgentRequestObj);

        // Track resources for cleanup
        AtomicReference<String> agentId = new AtomicReference<>();
        AtomicReference<String> threadId = new AtomicReference<>();
        
        // Create full reactive chain to showcase reactive programming
        administrationAsyncClient.createAgentWithResponse(createAgentRequest, requestOptions)
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
                            "What is the weather in Seattle, WA?"
                        ).flatMap(message -> {
                            System.out.println("Created initial message");
                            
                            // Create run with the agent
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
                    administrationAsyncClient.deleteAgent(agentId.get())
                        .doOnSuccess(ignored -> System.out.println("Agent deleted: " + agentId.get()))
                        .doOnError(error -> System.err.println("Failed to delete agent: " + error.getMessage()))
                        .subscribe();
                }
            })
            .doOnError(error -> System.err.println("An error occurred: " + error.getMessage()))
            .block(); // Only block at the end of the reactive chain
    }

    // Use "Map.of" if available
    @SuppressWarnings("unchecked")
    private static <T> Map<String, T> mapOf(Object... inputs) {
        Map<String, T> map = new HashMap<>();
        for (int i = 0; i < inputs.length; i += 2) {
            String key = (String) inputs[i];
            T value = (T) inputs[i + 1];
            map.put(key, value);
        }
        return map;
    }
}
