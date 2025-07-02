// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.agents.persistent;

import com.azure.ai.agents.persistent.models.CreateAgentOptions;
import com.azure.ai.agents.persistent.models.CreateRunOptions;
import com.azure.ai.agents.persistent.models.MessageRole;
import com.azure.ai.agents.persistent.models.OpenApiAnonymousAuthDetails;
import com.azure.ai.agents.persistent.models.OpenApiFunctionDefinition;
import com.azure.ai.agents.persistent.models.OpenApiToolDefinition;
import com.azure.ai.agents.persistent.models.PersistentAgent;
import com.azure.ai.agents.persistent.models.PersistentAgentThread;
import com.azure.ai.agents.persistent.models.ThreadMessage;
import com.azure.ai.agents.persistent.models.ThreadRun;
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

import static com.azure.ai.agents.persistent.SampleUtils.printRunMessages;
import static com.azure.ai.agents.persistent.SampleUtils.waitForRunCompletion;

public class AgentOpenApiSample {

    public static void main(String[] args) throws IOException, URISyntaxException {
        PersistentAgentsClientBuilder clientBuilder = new PersistentAgentsClientBuilder().endpoint(Configuration.getGlobalConfiguration().get("ENDPOINT", "endpoint"))
            .credential(new DefaultAzureCredentialBuilder().build());
        PersistentAgentsClient agentsClient = clientBuilder.buildClient();
        PersistentAgentsAdministrationClient administrationClient = agentsClient.getPersistentAgentsAdministrationClient();
        ThreadsClient threadsClient = agentsClient.getThreadsClient();
        MessagesClient messagesClient = agentsClient.getMessagesClient();
        RunsClient runsClient = agentsClient.getRunsClient();

        Path filePath = getFile("weather_openapi.json");
        JsonReader reader = JsonProviders.createReader(Files.readAllBytes(filePath));

        OpenApiAnonymousAuthDetails oaiAuth = new OpenApiAnonymousAuthDetails();
        OpenApiToolDefinition openApiTool = new OpenApiToolDefinition(new OpenApiFunctionDefinition(
            "openapitool",
            reader.getNullable(nonNullReader -> BinaryData.fromObject(nonNullReader.readUntyped())),
            oaiAuth
        ));

        String agentName = "openAPI_example";
        CreateAgentOptions createAgentOptions = new CreateAgentOptions("gpt-4o-mini")
            .setName(agentName)
            .setInstructions("You are a helpful agent")
            .setTools(Arrays.asList(openApiTool));
        PersistentAgent agent = administrationClient.createAgent(createAgentOptions);

        PersistentAgentThread thread = threadsClient.createThread();
        ThreadMessage createdMessage = messagesClient.createMessage(
            thread.getId(),
            MessageRole.USER,
            "What's the weather in seattle?");

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
        URL resource = AgentOpenApiSample.class.getClassLoader().getResource(fileName);
        if (resource == null) {
            throw new FileNotFoundException("File not found");
        }
        File file = new File(resource.toURI());
        return file.toPath();
    }
}
