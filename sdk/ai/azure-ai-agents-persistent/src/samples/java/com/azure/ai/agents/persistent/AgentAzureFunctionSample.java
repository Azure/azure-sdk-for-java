// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.agents.persistent;

import com.azure.ai.agents.persistent.implementation.models.CreateAgentRequest;
import com.azure.ai.agents.persistent.models.AzureFunctionBinding;
import com.azure.ai.agents.persistent.models.AzureFunctionDefinition;
import com.azure.ai.agents.persistent.models.AzureFunctionStorageQueue;
import com.azure.ai.agents.persistent.models.AzureFunctionToolDefinition;
import com.azure.ai.agents.persistent.models.CreateRunOptions;
import com.azure.ai.agents.persistent.models.FunctionDefinition;
import com.azure.ai.agents.persistent.models.MessageRole;
import com.azure.ai.agents.persistent.models.PersistentAgent;
import com.azure.ai.agents.persistent.models.PersistentAgentThread;
import com.azure.ai.agents.persistent.models.ThreadMessage;
import com.azure.ai.agents.persistent.models.ThreadRun;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static com.azure.ai.agents.persistent.SampleUtils.printRunMessages;
import static com.azure.ai.agents.persistent.SampleUtils.waitForRunCompletion;

public class AgentAzureFunctionSample {

    public static void main(String[] args) {

        PersistentAgentsClientBuilder clientBuilder = new PersistentAgentsClientBuilder().endpoint(Configuration.getGlobalConfiguration().get("ENDPOINT", "endpoint"))
            .credential(new DefaultAzureCredentialBuilder().build());
        PersistentAgentsClient agentsClient = clientBuilder.buildClient();
        PersistentAgentsAdministrationClient administrationClient = agentsClient.getPersistentAgentsAdministrationClient();
        ThreadsClient threadsClient = agentsClient.getThreadsClient();
        MessagesClient messagesClient = agentsClient.getMessagesClient();
        RunsClient runsClient = agentsClient.getRunsClient();

        String storageQueueUri = Configuration.getGlobalConfiguration().get("STORAGE_QUEUE_URI", "");
        String azureFunctionName = Configuration.getGlobalConfiguration().get("AZURE_FUNCTION_NAME", "");

        FunctionDefinition fnDef = new FunctionDefinition(
            azureFunctionName,
            BinaryData.fromObject(
                mapOf(
                    "type", "object",
                    "properties", mapOf(
                        "location",
                        mapOf("type", "string", "description", "The location to look up")
                    ),
                    "required", new String[]{"location"}
                )
            )
        );
        AzureFunctionDefinition azureFnDef = new AzureFunctionDefinition(
            fnDef,
            new AzureFunctionBinding(new AzureFunctionStorageQueue(storageQueueUri, "agent-input")),
            new AzureFunctionBinding(new AzureFunctionStorageQueue(storageQueueUri, "agent-output"))
        );
        AzureFunctionToolDefinition azureFnTool = new AzureFunctionToolDefinition(azureFnDef);

        String agentName = "azure_function_example";
        RequestOptions requestOptions = new RequestOptions()
            .setHeader(HttpHeaderName.fromString("x-ms-enable-preview"), "true");
        CreateAgentRequest createAgentRequestObj = new CreateAgentRequest("gpt-4o-mini")
            .setName(agentName)
            .setInstructions("You are a helpful agent. Use the provided function any time "
                + "you are asked with the weather of any location")
            .setTools(Arrays.asList(azureFnTool));
        BinaryData createAgentRequest = BinaryData.fromObject(createAgentRequestObj);
        PersistentAgent agent = administrationClient.createAgentWithResponse(createAgentRequest, requestOptions)
            .getValue().toObject(PersistentAgent.class);

        PersistentAgentThread thread = threadsClient.createThread();
        ThreadMessage createdMessage = messagesClient.createMessage(
            thread.getId(),
            MessageRole.USER,
            "What is the weather in Seattle, WA?");

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

    // Use "Map.of" if available
    @SuppressWarnings("unchecked")
    private static <T> Map<String, T> mapOf(Object... inputs) {
        Map<String, T> map = new HashMap<>();
        for (int i = 0; i < inputs.length; i += 2) {
            String key = (String) inputs[i];
            T value = (T) inputs[i + 1];
            map.put(key, value);
        }
        return map;
    }
}
