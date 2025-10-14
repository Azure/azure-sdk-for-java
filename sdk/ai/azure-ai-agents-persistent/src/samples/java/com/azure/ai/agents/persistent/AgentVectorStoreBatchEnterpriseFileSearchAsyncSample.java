// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.agents.persistent;

import com.azure.ai.agents.persistent.models.CreateAgentOptions;
import com.azure.ai.agents.persistent.models.CreateRunOptions;
import com.azure.ai.agents.persistent.models.FileSearchToolDefinition;
import com.azure.ai.agents.persistent.models.FileSearchToolResource;
import com.azure.ai.agents.persistent.models.MessageRole;
import com.azure.ai.agents.persistent.models.ToolResources;
import com.azure.ai.agents.persistent.models.VectorStoreConfiguration;
import com.azure.ai.agents.persistent.models.VectorStoreDataSource;
import com.azure.ai.agents.persistent.models.VectorStoreDataSourceAssetType;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;

import static com.azure.ai.agents.persistent.SampleUtils.cleanUpResources;
import static com.azure.ai.agents.persistent.SampleUtils.printRunMessagesAsync;
import static com.azure.ai.agents.persistent.SampleUtils.waitForRunCompletionAsync;

public class AgentVectorStoreBatchEnterpriseFileSearchAsyncSample {

    public static void main(String[] args) {
        PersistentAgentsClientBuilder clientBuilder = new PersistentAgentsClientBuilder()
            .endpoint(Configuration.getGlobalConfiguration().get("ENDPOINT", "endpoint"))
            .credential(new DefaultAzureCredentialBuilder().build());

        PersistentAgentsAsyncClient agentsAsyncClient = clientBuilder.buildAsyncClient();
        PersistentAgentsAdministrationAsyncClient administrationAsyncClient = agentsAsyncClient.getPersistentAgentsAdministrationAsyncClient();
        ThreadsAsyncClient threadsAsyncClient = agentsAsyncClient.getThreadsAsyncClient();
        MessagesAsyncClient messagesAsyncClient = agentsAsyncClient.getMessagesAsyncClient();
        RunsAsyncClient runsAsyncClient = agentsAsyncClient.getRunsAsyncClient();
        VectorStoresAsyncClient vectorStoresAsyncClient = agentsAsyncClient.getVectorStoresAsyncClient();

        String dataUri = Configuration.getGlobalConfiguration().get("DATA_URI", "");
        VectorStoreDataSource vectorStoreDataSource = new VectorStoreDataSource(
            "assistant-6FP6sNAo21Z7pVR2ouGoPp", VectorStoreDataSourceAssetType.ID_ASSET);

        // Track resources for cleanup
        AtomicReference<String> agentId = new AtomicReference<>();
        AtomicReference<String> threadId = new AtomicReference<>();
        
        // Create vector store
        vectorStoresAsyncClient.createVectorStore(
                null, "sample_vector_store_async",
                new VectorStoreConfiguration(Arrays.asList(vectorStoreDataSource)),
                null, null, null)
            .flatMap(vs -> {
                // Create vector store file batch
                return vectorStoresAsyncClient.createVectorStoreFileBatch(
                    vs.getId(), null, Arrays.asList(vectorStoreDataSource), null)
                    .map(batch -> {
                        // Return vector store ID for creating the agent
                        return vs.getId();
                    });
            })
            .flatMap(vectorStoreId -> {
                // Create agent with file search tool
                FileSearchToolResource fileSearchToolResource = new FileSearchToolResource()
                    .setVectorStoreIds(Arrays.asList(vectorStoreId));

                String agentName = "vector_store_batch_enterprise_file_search_async_example";
                CreateAgentOptions createAgentOptions = new CreateAgentOptions("gpt-4o-mini")
                    .setName(agentName)
                    .setInstructions("You are a helpful agent")
                    .setTools(Arrays.asList(new FileSearchToolDefinition()))
                    .setToolResources(new ToolResources().setFileSearch(fileSearchToolResource));
                
                return administrationAsyncClient.createAgent(createAgentOptions);
            })
            .flatMap(agent -> {
                System.out.println("Created agent: " + agent.getId());
                agentId.set(agent.getId());
                
                return threadsAsyncClient.createThread();
            })
            .flatMap(thread -> {
                System.out.println("Created thread: " + thread.getId());
                threadId.set(thread.getId());
                
                // Create initial message
                return messagesAsyncClient.createMessage(
                    thread.getId(),
                    MessageRole.USER,
                    "What feature does Smart Eyewear offer?"
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
            .doFinally(signalType -> cleanUpResources(threadId, threadsAsyncClient, agentId, administrationAsyncClient))
            .doOnError(error -> System.err.println("An error occurred: " + error.getMessage()))
            .block(); // Only block at the end of the reactive chain
    }
}
