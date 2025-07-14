// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.agents.persistent;

import com.azure.ai.agents.persistent.models.CreateAgentOptions;
import com.azure.ai.agents.persistent.models.CreateRunOptions;
import com.azure.ai.agents.persistent.models.MessageRole;
import com.azure.ai.agents.persistent.models.PersistentAgentStreamEvent;
import com.azure.ai.agents.persistent.models.StreamMessageUpdate;
import com.azure.ai.agents.persistent.models.StreamThreadRunCreation;
import com.azure.ai.agents.persistent.models.StreamUpdate;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.concurrent.atomic.AtomicReference;

import static com.azure.ai.agents.persistent.SampleUtils.cleanUpResources;
import static com.azure.ai.agents.persistent.SampleUtils.printStreamUpdate;

public final class AgentStreamingAsyncSample {

    public static void main(String[] args) {
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

        String agentName = "streaming_async_example";
        CreateAgentOptions createAgentOptions = new CreateAgentOptions("gpt-4o-mini")
            .setName(agentName)
            .setInstructions("You are a helpful assistant. When I ask about my favorite city, answer that it is Seattle, WA. "
                + "When I ask about the weather in Seattle, tell me it's currently 70 degrees and partly cloudy.");

        // Create a fully reactive chain
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
                            "What's the weather like in my favorite city?")
                            .flatMap(message -> {
                                System.out.println("Created initial message");
                                
                                CreateRunOptions createRunOptions = new CreateRunOptions(thread.getId(), agent.getId())
                                    .setAdditionalInstructions("");
                                
                                System.out.println("----- Run started! -----");
                                
                                return handleStreamingRun(runsAsyncClient.createRunStreaming(createRunOptions));
                            });
                    });
            })
            .doFinally(signalType -> cleanUpResources(threadId, threadsAsyncClient, agentId, administrationAsyncClient))
            .doOnError(error -> System.err.println("An error occurred: " + error.getMessage()))
            .block(); // Only block at the end of the reactive chain
    }

    private static Mono<Void> handleStreamingRun(Flux<StreamUpdate> streamingUpdates) {
        return streamingUpdates
            .flatMap(streamUpdate -> {
                if (streamUpdate.getKind() == PersistentAgentStreamEvent.THREAD_RUN_CREATED) {
                    System.out.println("----- Run created! -----");
                } else if (streamUpdate instanceof StreamMessageUpdate) {
                    StreamMessageUpdate messageUpdate = (StreamMessageUpdate) streamUpdate;
                    printStreamUpdate(messageUpdate);
                } else if (streamUpdate.getKind() == PersistentAgentStreamEvent.THREAD_RUN_COMPLETED) {
                    StreamThreadRunCreation runCreation = (StreamThreadRunCreation) streamUpdate;
                    System.out.println("Run completed with status: " + runCreation.getMessage().getStatus());
                }
                return Mono.empty();
            })
            .then();
    }
}
