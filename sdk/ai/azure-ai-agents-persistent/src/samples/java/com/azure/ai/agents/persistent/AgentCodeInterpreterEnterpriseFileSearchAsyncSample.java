// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.agents.persistent;

import com.azure.ai.agents.persistent.models.CodeInterpreterToolDefinition;
import com.azure.ai.agents.persistent.models.CreateAgentOptions;
import com.azure.ai.agents.persistent.models.CreateRunOptions;
import com.azure.ai.agents.persistent.models.MessageAttachment;
import com.azure.ai.agents.persistent.models.MessageRole;
import com.azure.ai.agents.persistent.models.VectorStoreDataSource;
import com.azure.ai.agents.persistent.models.VectorStoreDataSourceAssetType;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;

import static com.azure.ai.agents.persistent.SampleUtils.cleanUpResources;
import static com.azure.ai.agents.persistent.SampleUtils.printRunMessagesAsync;
import static com.azure.ai.agents.persistent.SampleUtils.waitForRunCompletionAsync;

public class AgentCodeInterpreterEnterpriseFileSearchAsyncSample {

    public static void main(String[] args) {
        PersistentAgentsClientBuilder clientBuilder = new PersistentAgentsClientBuilder()
            .endpoint(Configuration.getGlobalConfiguration().get("ENDPOINT", "endpoint"))
            .credential(new DefaultAzureCredentialBuilder().build());

        PersistentAgentsAsyncClient agentsAsyncClient = clientBuilder.buildAsyncClient();
        PersistentAgentsAdministrationAsyncClient administrationAsyncClient = agentsAsyncClient.getPersistentAgentsAdministrationAsyncClient();
        ThreadsAsyncClient threadsAsyncClient = agentsAsyncClient.getThreadsAsyncClient();
        MessagesAsyncClient messagesAsyncClient = agentsAsyncClient.getMessagesAsyncClient();
        RunsAsyncClient runsAsyncClient = agentsAsyncClient.getRunsAsyncClient();

        String agentName = "code_interpreter_enterprise_file_search_async";
        CodeInterpreterToolDefinition ciTool = new CodeInterpreterToolDefinition();
        CreateAgentOptions createAgentOptions = new CreateAgentOptions("gpt-4o-mini")
            .setName(agentName)
            .setInstructions("You are a helpful agent")
            .setTools(Arrays.asList(ciTool));

        String dataUri = Configuration.getGlobalConfiguration().get("DATA_URI", "");
        VectorStoreDataSource vectorStoreDataSource = new VectorStoreDataSource(
            dataUri, VectorStoreDataSourceAssetType.URI_ASSET);

        MessageAttachment messageAttachment = new MessageAttachment(
            Arrays.asList(BinaryData.fromObject(ciTool))
        ).setDataSource(vectorStoreDataSource);

        // Track resources for cleanup
        AtomicReference<String> agentId = new AtomicReference<>();
        AtomicReference<String> threadId = new AtomicReference<>();
        
        // Create full reactive chain
        administrationAsyncClient.createAgent(createAgentOptions)
            .flatMap(agent -> {
                System.out.println("Created agent: " + agent.getId());
                agentId.set(agent.getId());
                
                return threadsAsyncClient.createThread()
                    .flatMap(thread -> {
                        System.out.println("Created thread: " + thread.getId());
                        threadId.set(thread.getId());
                        
                        // Create initial message with attachment
                        return messagesAsyncClient.createMessage(
                            thread.getId(),
                            MessageRole.USER,
                            "What does the attachment say?",
                            Arrays.asList(messageAttachment),
                            null
                        ).flatMap(message -> {
                            System.out.println("Created initial message with attachment");
                            
                            // Create and start the run
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
            .doFinally(signalType -> cleanUpResources(threadId, threadsAsyncClient, agentId, administrationAsyncClient))
            .doOnError(error -> System.err.println("An error occurred: " + error.getMessage()))
            .block(); // Only block at the end of the reactive chain
    }
}
