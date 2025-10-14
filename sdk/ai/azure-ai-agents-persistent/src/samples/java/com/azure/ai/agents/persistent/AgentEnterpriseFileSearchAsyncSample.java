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

import static com.azure.ai.agents.persistent.SampleUtils.printRunMessagesAsync;
import static com.azure.ai.agents.persistent.SampleUtils.waitForRunCompletionAsync;

public class AgentEnterpriseFileSearchAsyncSample {

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
        VectorStoresAsyncClient vectorStoresAsyncClient = agentsAsyncClient.getVectorStoresAsyncClient();

        // Track resources for cleanup
        AtomicReference<String> agentId = new AtomicReference<>();
        AtomicReference<String> threadId = new AtomicReference<>();
        AtomicReference<String> vectorStoreId = new AtomicReference<>();

        // Get data URI from configuration
        String dataUri = Configuration.getGlobalConfiguration().get("DATA_URI", "");

        // Set up vector store data source
        VectorStoreDataSource vectorStoreDataSource = new VectorStoreDataSource(
            "assistant-6FP6sNAo21Z7pVR2ouGoPp", VectorStoreDataSourceAssetType.URI_ASSET);

        // Create vector store
        vectorStoresAsyncClient
            .createVectorStore(
                null, "sample_vector_store_async",
                new VectorStoreConfiguration(Arrays.asList(vectorStoreDataSource)),
                null, null, null
            )
            .flatMap(vectorStore -> {
                vectorStoreId.set(vectorStore.getId());
                System.out.println("Created vector store: " + vectorStore.getId());

                // Create file search tool resource with the vector store
                FileSearchToolResource fileSearchToolResource = new FileSearchToolResource()
                    .setVectorStoreIds(Arrays.asList(vectorStore.getId()));

                // Create agent options
                String agentName = "enterprise_file_search_async_example";
                CreateAgentOptions createAgentOptions = new CreateAgentOptions("gpt-4o-mini")
                    .setName(agentName)
                    .setInstructions("You are a helpful agent specialized in searching through documents")
                    .setTools(Arrays.asList(new FileSearchToolDefinition()))
                    .setToolResources(new ToolResources().setFileSearch(fileSearchToolResource));

                // Create the agent
                return administrationAsyncClient.createAgent(createAgentOptions);
            })
            .flatMap(agent -> {
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
                    "What is data about? Please search and provide detailed information.");
            })
            .flatMap(message -> {
                System.out.println("Created message");

                // Create and start the run
                CreateRunOptions createRunOptions = new CreateRunOptions(threadId.get(), agentId.get())
                    .setAdditionalInstructions("Provide detailed information from the document search");

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
                    administrationAsyncClient.deleteAgent(agentId.get()).block();
                    System.out.println("Deleted agent: " + agentId.get());
                }

                // Vector stores might need to be cleaned up separately depending on your application
            })
            .doOnError(error -> System.err.println("An error occurred: " + error.getMessage()))
            .block(); // Only block at the end of the reactive chain
    }
}
