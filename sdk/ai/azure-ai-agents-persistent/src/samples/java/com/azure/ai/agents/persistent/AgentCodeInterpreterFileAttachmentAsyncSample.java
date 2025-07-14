// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.agents.persistent;

import com.azure.ai.agents.persistent.models.CodeInterpreterToolDefinition;
import com.azure.ai.agents.persistent.models.CreateAgentOptions;
import com.azure.ai.agents.persistent.models.CreateRunOptions;
import com.azure.ai.agents.persistent.models.FileDetails;
import com.azure.ai.agents.persistent.models.FilePurpose;
import com.azure.ai.agents.persistent.models.MessageAttachment;
import com.azure.ai.agents.persistent.models.MessageRole;
import com.azure.ai.agents.persistent.models.UploadFileRequest;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import reactor.core.publisher.Mono;
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

public class AgentCodeInterpreterFileAttachmentAsyncSample {

    public static void main(String[] args) {
        try {
            // Find and get the HTML file
            Path htmlFile = getFile("sample.html");

            // Initialize async clients
            PersistentAgentsClientBuilder clientBuilder = new PersistentAgentsClientBuilder()
                .endpoint(Configuration.getGlobalConfiguration().get("ENDPOINT", "endpoint"))
                .credential(new DefaultAzureCredentialBuilder().build());

            PersistentAgentsAsyncClient agentsAsyncClient = clientBuilder.buildAsyncClient();
            PersistentAgentsAdministrationAsyncClient administrationAsyncClient = agentsAsyncClient.getPersistentAgentsAdministrationAsyncClient();
            ThreadsAsyncClient threadsAsyncClient = agentsAsyncClient.getThreadsAsyncClient();
            MessagesAsyncClient messagesAsyncClient = agentsAsyncClient.getMessagesAsyncClient();
            RunsAsyncClient runsAsyncClient = agentsAsyncClient.getRunsAsyncClient();
            FilesAsyncClient filesAsyncClient = agentsAsyncClient.getFilesAsyncClient();

            // Track resources for cleanup
            AtomicReference<String> agentId = new AtomicReference<>();
            AtomicReference<String> threadId = new AtomicReference<>();
            AtomicReference<String> fileId = new AtomicReference<>();

            // Define agent properties
            String agentName = "code_interpreter_file_attachment_async_example";
            CodeInterpreterToolDefinition ciTool = new CodeInterpreterToolDefinition();
            CreateAgentOptions createAgentOptions = new CreateAgentOptions("gpt-4o-mini")
                .setName(agentName)
                .setInstructions("You are a helpful agent that analyzes HTML files")
                .setTools(Arrays.asList(ciTool));

            // Create file upload request
            UploadFileRequest uploadFileRequest = new UploadFileRequest(
                new FileDetails(BinaryData.fromFile(htmlFile))
                    .setFilename("sample.html"), 
                FilePurpose.AGENTS);

            // Build reactive chain
            Mono.zip(
                administrationAsyncClient.createAgent(createAgentOptions),
                filesAsyncClient.uploadFile(uploadFileRequest)
            ).flatMap(tuple -> {
                // Store resources for cleanup
                agentId.set(tuple.getT1().getId());
                fileId.set(tuple.getT2().getId());
                
                System.out.println("Created agent: " + tuple.getT1().getId());
                System.out.println("Uploaded file: " + tuple.getT2().getId());

                // Create message attachment with file reference
                MessageAttachment messageAttachment = new MessageAttachment(
                    Arrays.asList(BinaryData.fromObject(ciTool)))
                    .setFileId(tuple.getT2().getId());

                // Create thread
                return threadsAsyncClient.createThread()
                    .flatMap(thread -> {
                        threadId.set(thread.getId());
                        System.out.println("Created thread: " + thread.getId());

                        // Create message with attachment
                        return messagesAsyncClient.createMessage(
                            thread.getId(),
                            MessageRole.USER,
                            "What does the attachment say? Analyze the HTML structure and content.",
                            Arrays.asList(messageAttachment),
                            null
                        ).flatMap(message -> {
                            System.out.println("Created message with attachment");

                            // Create run options
                            CreateRunOptions createRunOptions = new CreateRunOptions(thread.getId(), tuple.getT1().getId())
                                .setAdditionalInstructions("Provide a detailed analysis of the HTML file");

                            // Create run and wait for completion
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
                // Clean up resources
                System.out.println("Cleaning up resources...");
                cleanUpResources(threadId, threadsAsyncClient, agentId, administrationAsyncClient);
                
                // Delete the file if it was created
                if (fileId.get() != null) {
                    filesAsyncClient.deleteFile(fileId.get()).block();
                    System.out.println("Deleted file: " + fileId.get());
                }
            })
            .doOnError(error -> System.err.println("An error occurred: " + error.getMessage()))
            .block(); // Only block at the end of the reactive chain
        } catch (FileNotFoundException | URISyntaxException e) {
            System.err.println("Error loading sample file: " + e.getMessage());
        }
    }

    private static Path getFile(String fileName) throws FileNotFoundException, URISyntaxException {
        URL resource = AgentCodeInterpreterFileAttachmentAsyncSample.class.getClassLoader().getResource(fileName);
        if (resource == null) {
            throw new FileNotFoundException("File not found: " + fileName);
        }
        File file = new File(resource.toURI());
        return file.toPath();
    }
}
