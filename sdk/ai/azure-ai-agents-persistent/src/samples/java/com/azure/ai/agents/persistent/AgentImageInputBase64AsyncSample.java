// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.agents.persistent;

import com.azure.ai.agents.persistent.models.CreateAgentOptions;
import com.azure.ai.agents.persistent.models.CreateRunOptions;
import com.azure.ai.agents.persistent.models.MessageImageUrlParam;
import com.azure.ai.agents.persistent.models.MessageInputContentBlock;
import com.azure.ai.agents.persistent.models.MessageInputImageUrlBlock;
import com.azure.ai.agents.persistent.models.MessageInputTextBlock;
import com.azure.ai.agents.persistent.models.MessageRole;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static com.azure.ai.agents.persistent.SampleUtils.printRunMessagesAsync;
import static com.azure.ai.agents.persistent.SampleUtils.waitForRunCompletionAsync;

public final class AgentImageInputBase64AsyncSample {

    public static void main(String[] args) throws IOException, URISyntaxException {
        PersistentAgentsClientBuilder clientBuilder = new PersistentAgentsClientBuilder()
            .endpoint(Configuration.getGlobalConfiguration().get("ENDPOINT", "endpoint"))
            .credential(new DefaultAzureCredentialBuilder().build());

        PersistentAgentsAsyncClient agentsAsyncClient = clientBuilder.buildAsyncClient();
        PersistentAgentsAdministrationAsyncClient administrationAsyncClient = agentsAsyncClient.getPersistentAgentsAdministrationAsyncClient();
        ThreadsAsyncClient threadsAsyncClient = agentsAsyncClient.getThreadsAsyncClient();
        MessagesAsyncClient messagesAsyncClient = agentsAsyncClient.getMessagesAsyncClient();
        RunsAsyncClient runsAsyncClient = agentsAsyncClient.getRunsAsyncClient();

        // Track resources for cleanup
        AtomicReference<String> agentId = new AtomicReference<>();
        AtomicReference<String> threadId = new AtomicReference<>();

        // Prepare the image
        Path file = getFile("sample_image.jpg");
        byte[] imageContent = Files.readAllBytes(file);
        String imageBase64 = Base64.getEncoder().encodeToString(imageContent);
        String imageUrl = "data:image/png;base64," + imageBase64;

        List<MessageInputContentBlock> messageBlock = Arrays.asList(
            new MessageInputTextBlock("Hello, what is in the image"),
            new MessageInputImageUrlBlock(new MessageImageUrlParam(imageUrl))
        );

        // Create full reactive chain
        String agentName = "image_input_example_async";
        CreateAgentOptions createAgentOptions = new CreateAgentOptions("gpt-4o-mini")
            .setName(agentName)
            .setInstructions("You are a helpful agent");

        administrationAsyncClient.createAgent(createAgentOptions)
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
                    BinaryData.fromObject(messageBlock));
            })
            .flatMap(message -> {
                System.out.println("Created message with image");
                
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
            })
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
