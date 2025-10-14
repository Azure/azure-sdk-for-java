// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.agents.persistent;

import com.azure.ai.agents.persistent.models.CreateAgentOptions;
import com.azure.ai.agents.persistent.models.CreateRunOptions;
import com.azure.ai.agents.persistent.models.FileDetails;
import com.azure.ai.agents.persistent.models.FileInfo;
import com.azure.ai.agents.persistent.models.FilePurpose;
import com.azure.ai.agents.persistent.models.MessageImageFileParam;
import com.azure.ai.agents.persistent.models.MessageInputContentBlock;
import com.azure.ai.agents.persistent.models.MessageInputImageFileBlock;
import com.azure.ai.agents.persistent.models.MessageInputTextBlock;
import com.azure.ai.agents.persistent.models.MessageRole;
import com.azure.ai.agents.persistent.models.UploadFileRequest;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import reactor.core.publisher.Mono;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static com.azure.ai.agents.persistent.SampleUtils.cleanUpResources;
import static com.azure.ai.agents.persistent.SampleUtils.printRunMessagesAsync;
import static com.azure.ai.agents.persistent.SampleUtils.waitForRunCompletionAsync;

public final class AgentImageInputFileAsyncSample {

    public static void main(String[] args) throws IOException, URISyntaxException {
        PersistentAgentsClientBuilder clientBuilder = new PersistentAgentsClientBuilder()
            .endpoint(Configuration.getGlobalConfiguration().get("ENDPOINT", "endpoint"))
            .credential(new DefaultAzureCredentialBuilder().build());

        PersistentAgentsAsyncClient agentsAsyncClient = clientBuilder.buildAsyncClient();
        PersistentAgentsAdministrationAsyncClient administrationAsyncClient = agentsAsyncClient.getPersistentAgentsAdministrationAsyncClient();
        ThreadsAsyncClient threadsAsyncClient = agentsAsyncClient.getThreadsAsyncClient();
        MessagesAsyncClient messagesAsyncClient = agentsAsyncClient.getMessagesAsyncClient();
        RunsAsyncClient runsAsyncClient = agentsAsyncClient.getRunsAsyncClient();
        FilesAsyncClient filesAsyncClient = agentsAsyncClient.getFilesAsyncClient();

        Path file = getFile("sample_image.jpg");

        // Track resources for cleanup
        AtomicReference<String> agentId = new AtomicReference<>();
        AtomicReference<String> threadId = new AtomicReference<>();

        // Create full reactive chain
        Mono<FileInfo> uploadFileMono = filesAsyncClient.uploadFile(
            new UploadFileRequest(
                new FileDetails(
                    BinaryData.fromFile(file))
                    .setFilename("sample_image.jpg"),
                FilePurpose.AGENTS));

        uploadFileMono
            .flatMap(uploadedAgentFile -> {
                MessageImageFileParam fileParam = new MessageImageFileParam(uploadedAgentFile.getId());
                List<MessageInputContentBlock> messageBlock = Arrays.asList(
                    new MessageInputTextBlock("Hello, what is in the image"),
                    new MessageInputImageFileBlock(fileParam));

                String agentName = "image_input_async_example";
                CreateAgentOptions createAgentOptions = new CreateAgentOptions("gpt-4o")
                    .setName(agentName)
                    .setInstructions("You are a helpful agent");

                return administrationAsyncClient.createAgent(createAgentOptions)
                    .flatMap(agent -> {
                        System.out.println("Created agent: " + agent.getId());
                        agentId.set(agent.getId());

                        return threadsAsyncClient.createThread()
                            .flatMap(thread -> {
                                System.out.println("Created thread: " + thread.getId());
                                threadId.set(thread.getId());

                                return messagesAsyncClient.createMessage(
                                        thread.getId(),
                                        MessageRole.USER,
                                        BinaryData.fromObject(messageBlock))
                                    .flatMap(message -> {
                                        System.out.println("Created message with image file");

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
                    });
            })
            .doFinally(signalType -> cleanUpResources(threadId, threadsAsyncClient, agentId, administrationAsyncClient))
            .doOnError(error -> System.err.println("An error occurred: " + error.getMessage()))
            .block(); // Only block at the end of the reactive chain
    }

    private static Path getFile(String fileName) throws FileNotFoundException, URISyntaxException {
        URL resource = AgentCodeInterpreterFileAttachmentSample.class.getClassLoader().getResource(fileName);
        if (resource == null) {
            throw new FileNotFoundException("File not found");
        }
        File file = new File(resource.toURI());
        return file.toPath();
    }
}
