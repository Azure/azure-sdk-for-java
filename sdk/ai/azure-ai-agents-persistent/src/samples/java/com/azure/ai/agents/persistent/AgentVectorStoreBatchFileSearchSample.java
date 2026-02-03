// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.agents.persistent;

import com.azure.ai.agents.persistent.models.CreateAgentOptions;
import com.azure.ai.agents.persistent.models.CreateRunOptions;
import com.azure.ai.agents.persistent.models.FileDetails;
import com.azure.ai.agents.persistent.models.FileInfo;
import com.azure.ai.agents.persistent.models.FilePurpose;
import com.azure.ai.agents.persistent.models.FileSearchToolDefinition;
import com.azure.ai.agents.persistent.models.FileSearchToolResource;
import com.azure.ai.agents.persistent.models.MessageRole;
import com.azure.ai.agents.persistent.models.PersistentAgent;
import com.azure.ai.agents.persistent.models.PersistentAgentThread;
import com.azure.ai.agents.persistent.models.ThreadMessage;
import com.azure.ai.agents.persistent.models.ThreadRun;
import com.azure.ai.agents.persistent.models.ToolResources;
import com.azure.ai.agents.persistent.models.UploadFileRequest;
import com.azure.ai.agents.persistent.models.VectorStore;
import com.azure.ai.agents.persistent.models.VectorStoreFileBatch;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import java.io.File;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Arrays;

import static com.azure.ai.agents.persistent.SampleUtils.printRunMessages;
import static com.azure.ai.agents.persistent.SampleUtils.waitForRunCompletion;

public class AgentVectorStoreBatchFileSearchSample {

    public static void main(String[] args) throws FileNotFoundException, URISyntaxException {
        PersistentAgentsClientBuilder clientBuilder = new PersistentAgentsClientBuilder().endpoint(Configuration.getGlobalConfiguration().get("ENDPOINT", "endpoint"))
            .credential(new DefaultAzureCredentialBuilder().build());
        PersistentAgentsClient agentsClient = clientBuilder.buildClient();
        PersistentAgentsAdministrationClient administrationClient = agentsClient.getPersistentAgentsAdministrationClient();
        ThreadsClient threadsClient = agentsClient.getThreadsClient();
        MessagesClient messagesClient = agentsClient.getMessagesClient();
        RunsClient runsClient = agentsClient.getRunsClient();
        FilesClient filesClient = agentsClient.getFilesClient();
        VectorStoresClient vectorStoresClient = agentsClient.getVectorStoresClient();

        Path productFile = getFile("product_info.md");

        VectorStore vectorStore = vectorStoresClient.createVectorStore(
            null, "my_vector_store",
            null, null, null, null);

        FileInfo uploadedAgentFile = filesClient.uploadFile(new UploadFileRequest(
            new FileDetails(
                BinaryData.fromFile(productFile))
                .setFilename("sample_product_info.md"),
            FilePurpose.AGENTS));

        VectorStoreFileBatch vectorStoreFileBatch = vectorStoresClient.createVectorStoreFileBatch(
            vectorStore.getId(), Arrays.asList(uploadedAgentFile.getId()), null, null);

        FileSearchToolResource fileSearchToolResource = new FileSearchToolResource()
            .setVectorStoreIds(Arrays.asList(vectorStore.getId()));

        String agentName = "vector_store_batch_file_search_example";
        CreateAgentOptions createAgentOptions = new CreateAgentOptions("gpt-4o-mini")
            .setName(agentName)
            .setInstructions("You are a helpful agent")
            .setTools(Arrays.asList(new FileSearchToolDefinition()))
            .setToolResources(new ToolResources().setFileSearch(fileSearchToolResource));
        PersistentAgent agent = administrationClient.createAgent(createAgentOptions);

        PersistentAgentThread thread = threadsClient.createThread();
        ThreadMessage createdMessage = messagesClient.createMessage(
            thread.getId(),
            MessageRole.USER,
            "What feature does Smart Eyewear offer?");

        try {
            //run agent
            CreateRunOptions createRunOptions = new CreateRunOptions(thread.getId(), agent.getId())
                .setAdditionalInstructions("");
            ThreadRun threadRun = runsClient.createRun(createRunOptions);

            waitForRunCompletion(thread.getId(), threadRun, runsClient);
            printRunMessages(messagesClient, thread.getId());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            //cleanup
            threadsClient.deleteThread(thread.getId());
            administrationClient.deleteAgent(agent.getId());
        }
    }

    private static Path getFile(String fileName) throws FileNotFoundException, URISyntaxException {
        URL resource = AgentVectorStoreBatchFileSearchSample.class.getClassLoader().getResource(fileName);
        if (resource == null) {
            throw new FileNotFoundException("File not found");
        }
        File file = new File(resource.toURI());
        return file.toPath();
    }
}
