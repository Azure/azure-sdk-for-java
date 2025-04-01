package com.azure.ai.projects.usage.agent;

import com.azure.ai.projects.AIProjectClientBuilder;
import com.azure.ai.projects.AgentsClient;
import com.azure.ai.projects.implementation.models.CreateAgentRequest;
import com.azure.ai.projects.models.*;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

public class SampleAgentAzureFunction {

    @Test
    void azureFunctionExample() {
        AgentsClient agentsClient
            = new AIProjectClientBuilder().endpoint(Configuration.getGlobalConfiguration().get("ENDPOINT", "endpoint"))
            .subscriptionId(Configuration.getGlobalConfiguration().get("SUBSCRIPTIONID", "subscriptionid"))
            .resourceGroupName(Configuration.getGlobalConfiguration().get("RESOURCEGROUPNAME", "resourcegroupname"))
            .projectName(Configuration.getGlobalConfiguration().get("PROJECTNAME", "projectname"))
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildAgentsClient();

        var storageQueueUri = Configuration.getGlobalConfiguration().get("STORAGE_QUEUE_URI", "");

        FunctionDefinition fnDef = new FunctionDefinition(
            "jayantagentazfn",
            BinaryData.fromObject(
                Map.of(
                    "type", "object",
                    "properties", Map.of(
                        "location",
                        Map.of("type", "string", "description", "The location to look up")
                    ),
                    "required", new String[] {"location"}
                )
            )
        );
        AzureFunctionDefinition azureFnDef = new AzureFunctionDefinition(
            fnDef,
            new AzureFunctionBinding(new AzureFunctionStorageQueue(storageQueueUri,"agent-input")),
            new AzureFunctionBinding(new AzureFunctionStorageQueue(storageQueueUri,"agent-output"))
        );
        AzureFunctionToolDefinition azureFnTool = new AzureFunctionToolDefinition(azureFnDef);

        var agentName = "azure_function_example";
        RequestOptions requestOptions = new RequestOptions()
            .setHeader(HttpHeaderName.fromString("x-ms-enable-preview"), "true");
        CreateAgentRequest createAgentRequestObj = new CreateAgentRequest("gpt-4o-mini")
            .setName(agentName)
            .setInstructions("You are a helpful agent. Use the provided function any time "
                + "you are asked with the weather of any location")
            .setTools(List.of(azureFnTool));
        BinaryData createAgentRequest = BinaryData.fromObject(createAgentRequestObj);
        Agent agent = agentsClient.createAgentWithResponse(createAgentRequest, requestOptions)
            .getValue().toObject(Agent.class);

        var thread = agentsClient.createThread();
        var createdMessage = agentsClient.createMessage(
            thread.getId(),
            MessageRole.USER,
            "What is the weather in Seattle, WA?");

        //run agent
        var createRunOptions = new CreateRunOptions(thread.getId(), agent.getId())
            .setAdditionalInstructions("");
        var threadRun = agentsClient.createRun(createRunOptions);

        try {
            do {
                Thread.sleep(500);
                threadRun = agentsClient.getRun(thread.getId(), threadRun.getId());
            }
            while (
                threadRun.getStatus() == RunStatus.QUEUED
                    || threadRun.getStatus() == RunStatus.IN_PROGRESS
                    || threadRun.getStatus() == RunStatus.REQUIRES_ACTION);

            if (threadRun.getStatus() == RunStatus.FAILED) {
                System.out.println(threadRun.getLastError().getMessage());
            }

            var runMessages = agentsClient.listMessages(thread.getId());
            for (ThreadMessage message : runMessages.getData())
            {
                System.out.print(String.format("%1$s - %2$s : ", message.getCreatedAt(), message.getRole()));
                for (MessageContent contentItem : message.getContent())
                {
                    if (contentItem instanceof MessageTextContent)
                    {
                        System.out.print((((MessageTextContent) contentItem).getText().getValue()));
                    }
                    else if (contentItem instanceof MessageImageFileContent)
                    {
                        String imageFileId = (((MessageImageFileContent) contentItem).getImageFile().getFileId());
                        System.out.print("Image from ID: " + imageFileId);
                    }
                    System.out.println();
                }
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        finally {
            //cleanup
            agentsClient.deleteThread(thread.getId());
            agentsClient.deleteAgent(agent.getId());
        }
    }
}
