// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.agents.persistent;

import com.azure.ai.agents.persistent.models.CreateAgentOptions;
import com.azure.ai.agents.persistent.models.CreateRunOptions;
import com.azure.ai.agents.persistent.models.MessageRole;
import com.azure.ai.agents.persistent.models.OpenApiAnonymousAuthDetails;
import com.azure.ai.agents.persistent.models.OpenApiFunctionDefinition;
import com.azure.ai.agents.persistent.models.OpenApiToolDefinition;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;

import static com.azure.ai.agents.persistent.SampleUtils.cleanUpResources;
import static com.azure.ai.agents.persistent.SampleUtils.printRunMessagesAsync;
import static com.azure.ai.agents.persistent.SampleUtils.waitForRunCompletionAsync;

public final class AgentOpenApiAsyncSample {

    public static void main(String[] args) throws IOException, URISyntaxException {
        PersistentAgentsClientBuilder clientBuilder = new PersistentAgentsClientBuilder()
            .endpoint(Configuration.getGlobalConfiguration().get("ENDPOINT", "endpoint"))
            .credential(new DefaultAzureCredentialBuilder().build());

        PersistentAgentsAsyncClient agentsAsyncClient = clientBuilder.buildAsyncClient();
        PersistentAgentsAdministrationAsyncClient administrationAsyncClient = agentsAsyncClient.getPersistentAgentsAdministrationAsyncClient();
        ThreadsAsyncClient threadsAsyncClient = agentsAsyncClient.getThreadsAsyncClient();
        MessagesAsyncClient messagesAsyncClient = agentsAsyncClient.getMessagesAsyncClient();
        RunsAsyncClient runsAsyncClient = agentsAsyncClient.getRunsAsyncClient();

        Path filePath = getFile("weather_openapi.json");
        JsonReader reader = JsonProviders.createReader(Files.readAllBytes(filePath));

        OpenApiAnonymousAuthDetails oaiAuth = new OpenApiAnonymousAuthDetails();
        OpenApiToolDefinition openApiTool = new OpenApiToolDefinition(new OpenApiFunctionDefinition(
            "openapitool",
            reader.getNullable(nonNullReader -> BinaryData.fromObject(nonNullReader.readUntyped())),
            oaiAuth
        ));

        // Track resources for cleanup
        AtomicReference<String> agentId = new AtomicReference<>();
        AtomicReference<String> threadId = new AtomicReference<>();

        String agentName = "openAPI_async_example";
        CreateAgentOptions createAgentOptions = new CreateAgentOptions("gpt-4o-mini")
            .setName(agentName)
            .setInstructions("You are a helpful agent")
            .setTools(Arrays.asList(openApiTool));
        
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
                            "What's the weather in seattle?")
                            .flatMap(message -> {
                                System.out.println("Created message asking about weather");
                                
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

    private static Path getFile(String fileName) throws FileNotFoundException, URISyntaxException {
        URL resource = AgentOpenApiSample.class.getClassLoader().getResource(fileName);
        if (resource == null) {
            throw new FileNotFoundException("File not found");
        }
        File file = new File(resource.toURI());
        return file.toPath();
    }
}
