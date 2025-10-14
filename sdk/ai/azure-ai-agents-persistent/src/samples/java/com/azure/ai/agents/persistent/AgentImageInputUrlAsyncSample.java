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
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static com.azure.ai.agents.persistent.SampleUtils.cleanUpResources;
import static com.azure.ai.agents.persistent.SampleUtils.printRunMessagesAsync;
import static com.azure.ai.agents.persistent.SampleUtils.waitForRunCompletionAsync;

public final class AgentImageInputUrlAsyncSample {

    public static void main(String[] args) {
        PersistentAgentsClientBuilder clientBuilder = new PersistentAgentsClientBuilder()
            .endpoint(Configuration.getGlobalConfiguration().get("ENDPOINT", "endpoint"))
            .credential(new DefaultAzureCredentialBuilder().build());

        PersistentAgentsAsyncClient agentsAsyncClient = clientBuilder.buildAsyncClient();
        PersistentAgentsAdministrationAsyncClient administrationAsyncClient = agentsAsyncClient.getPersistentAgentsAdministrationAsyncClient();
        ThreadsAsyncClient threadsAsyncClient = agentsAsyncClient.getThreadsAsyncClient();
        MessagesAsyncClient messagesAsyncClient = agentsAsyncClient.getMessagesAsyncClient();
        RunsAsyncClient runsAsyncClient = agentsAsyncClient.getRunsAsyncClient();

        String imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/d/dd/Gfp-wisconsin-madison-the-nature-boardwalk.jpg/2560px-Gfp-wisconsin-madison-the-nature-boardwalk.jpg";

        List<MessageInputContentBlock> messageBlock = Arrays.asList(
            new MessageInputTextBlock("Hello, what is in the image"),
            new MessageInputImageUrlBlock(new MessageImageUrlParam(imageUrl)));

        // Track resources for cleanup
        AtomicReference<String> agentId = new AtomicReference<>();
        AtomicReference<String> threadId = new AtomicReference<>();

        String agentName = "image_url_async_example";
        CreateAgentOptions createAgentOptions = new CreateAgentOptions("gpt-4o-mini")
            .setName(agentName)
            .setInstructions("You are a helpful agent");
        
        // Create full reactive chain
        administrationAsyncClient.createAgent(createAgentOptions)
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
                                System.out.println("Created message with image URL");
                                
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
