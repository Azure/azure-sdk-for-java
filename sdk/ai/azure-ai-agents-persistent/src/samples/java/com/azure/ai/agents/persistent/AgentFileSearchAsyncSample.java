// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.agents.persistent;

import com.azure.ai.agents.persistent.models.CreateAgentOptions;
import com.azure.ai.agents.persistent.models.CreateRunOptions;
import com.azure.ai.agents.persistent.models.FileDetails;
import com.azure.ai.agents.persistent.models.FilePurpose;
import com.azure.ai.agents.persistent.models.FileSearchToolDefinition;
import com.azure.ai.agents.persistent.models.FileSearchToolResource;
import com.azure.ai.agents.persistent.models.MessageRole;
import com.azure.ai.agents.persistent.models.ToolResources;
import com.azure.ai.agents.persistent.models.UploadFileRequest;
import com.azure.ai.agents.persistent.models.VectorStoreStatus;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import reactor.core.publisher.Mono;
import java.time.Duration;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;

import static com.azure.ai.agents.persistent.SampleUtils.printRunMessagesAsync;
import static com.azure.ai.agents.persistent.SampleUtils.waitForRunCompletionAsync;

public class AgentFileSearchAsyncSample {

    public static void main(String[] args) {
        PersistentAgentsClientBuilder clientBuilder = new PersistentAgentsClientBuilder()
            .endpoint(Configuration.getGlobalConfiguration().get("ENDPOINT", "endpoint"))
            .credential(new DefaultAzureCredentialBuilder().build());

        PersistentAgentsAsyncClient agentsAsyncClient = clientBuilder.buildAsyncClient();
        PersistentAgentsAdministrationAsyncClient administrationAsyncClient = agentsAsyncClient.getPersistentAgentsAdministrationAsyncClient();
        ThreadsAsyncClient threadsAsyncClient = agentsAsyncClient.getThreadsAsyncClient();
        MessagesAsyncClient messagesAsyncClient = agentsAsyncClient.getMessagesAsyncClient();
        RunsAsyncClient runsAsyncClient = agentsAsyncClient.getRunsAsyncClient();
        FilesAsyncClient filesAsyncClient = agentsAsyncClient.getFilesAsyncClient();
        VectorStoresAsyncClient vectorStoresAsyncClient = agentsAsyncClient.getVectorStoresAsyncClient();

        // Track resources for cleanup
        AtomicReference<String> agentId = new AtomicReference<>();
        AtomicReference<String> threadId = new AtomicReference<>();
        AtomicReference<String> vectorStoreId = new AtomicReference<>();

        // Create full reactive chain
        filesAsyncClient.uploadFile(
            new UploadFileRequest(
                new FileDetails(
                    BinaryData.fromString("The word `apple` uses the code 442345, while the word `banana` uses the code 673457."))
                    .setFilename("sample_file_for_upload.txt"),
                FilePurpose.AGENTS))
            .flatMap(uploadedAgentFile -> {
                System.out.println("Uploaded file: " + uploadedAgentFile.getId());
                
                return vectorStoresAsyncClient.createVectorStore(
                    Arrays.asList(uploadedAgentFile.getId()),
                    "my_vector_store",
                    null, null, null, null);
            })
            .flatMap(vectorStore -> {
                System.out.println("Created vector store: " + vectorStore.getId());
                vectorStoreId.set(vectorStore.getId());
                
                // Poll until vector store is ready
                return Mono.fromSupplier(() -> vectorStore)
                    .expand(vs -> {
                        if (vs.getStatus() == VectorStoreStatus.IN_PROGRESS) {
                            return Mono.delay(Duration.ofMillis(500))
                                .then(vectorStoresAsyncClient.getVectorStore(vs.getId()));
                        } else {
                            return Mono.empty();
                        }
                    })
                    .filter(vs -> vs.getStatus() != VectorStoreStatus.IN_PROGRESS)
                    .last();
            })
            .flatMap(readyVectorStore -> {
                System.out.println("Vector store ready with status: " + readyVectorStore.getStatus());
                
                FileSearchToolResource fileSearchToolResource = new FileSearchToolResource()
                    .setVectorStoreIds(Arrays.asList(readyVectorStore.getId()));

                String agentName = "file_search_example_async";
                CreateAgentOptions createAgentOptions = new CreateAgentOptions("gpt-4o-mini")
                    .setName(agentName)
                    .setInstructions("You are a helpful agent that can help fetch data from files you know about.")
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
                
                return messagesAsyncClient.createMessage(
                    thread.getId(),
                    MessageRole.USER,
                    "Can you give me the documented codes for 'banana' and 'orange'?");
            })
            .flatMap(message -> {
                System.out.println("Created message");
                
                CreateRunOptions createRunOptions = new CreateRunOptions(threadId.get(), agentId.get())
                    .setAdditionalInstructions("");
                
                return runsAsyncClient.createRun(createRunOptions);
            })
            .flatMap(threadRun -> {
                System.out.println("Created run, waiting for completion...");
                return waitForRunCompletionAsync(threadId.get(), threadRun, runsAsyncClient);
            })
            .flatMap(completedRun -> {
                System.out.println("Run completed with status: " + completedRun.getStatus());
                return printRunMessagesAsync(messagesAsyncClient, threadId.get());
            })
            .doFinally(signalType -> {
                // Clean up resources
                if (threadId.get() != null) {
                    threadsAsyncClient.deleteThread(threadId.get()).block();
                    System.out.println("Thread deleted: " + threadId.get());
                }
                if (agentId.get() != null) {
                    administrationAsyncClient.deleteAgent(agentId.get()).block();
                    System.out.println("Agent deleted: " + agentId.get());
                }
                // Vector stores are not deleted in this sample
            })
            .doOnError(error -> System.err.println("An error occurred: " + error.getMessage()))
            .block(); // Only block at the end of the reactive chain
    }
}
