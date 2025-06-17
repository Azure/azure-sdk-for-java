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
import com.azure.core.util.BinaryData;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import java.io.File;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;

import static com.azure.ai.agents.persistent.SampleUtils.cleanUpResources;
import static com.azure.ai.agents.persistent.SampleUtils.printRunMessagesAsync;
import static com.azure.ai.agents.persistent.SampleUtils.waitForRunCompletionAsync;

public class AgentVectorStoreBatchFileSearchAsyncSample {

    public static void main(String[] args) throws FileNotFoundException, URISyntaxException {
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

        Path productFile = getFile("product_info.md");

        // Track resources for cleanup
        AtomicReference<String> agentId = new AtomicReference<>();
        AtomicReference<String> threadId = new AtomicReference<>();
        
        // Create vector store
        vectorStoresAsyncClient.createVectorStore(
                null, "my_vector_store_async",
                null, null, null, null)
            .flatMap(vectorStore -> {
                // Upload file for agent
                return filesAsyncClient.uploadFile(new UploadFileRequest(
                    new FileDetails(
                        BinaryData.fromFile(productFile))
                        .setFilename("sample_product_info.md"),
                    FilePurpose.AGENTS))
                    .flatMap(uploadedFile -> {
                        // Create vector store file batch
                        return vectorStoresAsyncClient.createVectorStoreFileBatch(
                            vectorStore.getId(), Arrays.asList(uploadedFile.getId()), null, null)
                            .map(batch -> {
                                // Return vector store for creating agent
                                return vectorStore;
                            });
                    });
            })
            .flatMap(vectorStore -> {
                // Create agent with file search tool
                FileSearchToolResource fileSearchToolResource = new FileSearchToolResource()
                    .setVectorStoreIds(Arrays.asList(vectorStore.getId()));

                String agentName = "vector_store_batch_file_search_async_example";
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

    private static Path getFile(String fileName) throws FileNotFoundException, URISyntaxException {
        URL resource = AgentVectorStoreBatchFileSearchAsyncSample.class.getClassLoader().getResource(fileName);
        if (resource == null) {
            throw new FileNotFoundException("File not found");
        }
        File file = new File(resource.toURI());
        return file.toPath();
    }
}
