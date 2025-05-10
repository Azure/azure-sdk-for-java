// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.agents.persistent;

import com.azure.ai.agents.persistent.implementation.models.CreateAgentRequest;
import com.azure.ai.agents.persistent.models.CreateRunOptions;
import com.azure.ai.agents.persistent.models.MessageRole;
import com.azure.ai.agents.persistent.models.MicrosoftFabricToolDefinition;
import com.azure.ai.agents.persistent.models.PersistentAgent;
import com.azure.ai.agents.persistent.models.PersistentAgentThread;
import com.azure.ai.agents.persistent.models.ThreadMessage;
import com.azure.ai.agents.persistent.models.ThreadRun;
import com.azure.ai.agents.persistent.models.ToolConnection;
import com.azure.ai.agents.persistent.models.ToolConnectionList;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import java.util.Arrays;

import static com.azure.ai.agents.persistent.SampleUtils.printRunMessages;
import static com.azure.ai.agents.persistent.SampleUtils.waitForRunCompletion;

public final class AgentSharepointToolSample {

    public static void main(String[] args) {
        PersistentAgentsAdministrationClientBuilder clientBuilder = new PersistentAgentsAdministrationClientBuilder().endpoint(Configuration.getGlobalConfiguration().get("ENDPOINT", "endpoint"))
            .credential(new DefaultAzureCredentialBuilder().build());
        PersistentAgentsAdministrationClient agentsClient = clientBuilder.buildClient();
        ThreadsClient threadsClient = clientBuilder.buildThreadsClient();
        MessagesClient messagesClient = clientBuilder.buildMessagesClient();
        RunsClient runsClient = clientBuilder.buildRunsClient();

        String sharepointConnectionId = Configuration.getGlobalConfiguration().get("SHAREPOINT_CONNECTION_ID", "");
        ToolConnectionList toolConnectionList = new ToolConnectionList()
            .setConnectionList(Arrays.asList(new ToolConnection(sharepointConnectionId)));
        MicrosoftFabricToolDefinition fabricToolDefinition = new MicrosoftFabricToolDefinition(toolConnectionList);

        String agentName = "sharepoint_tool_example";
        RequestOptions requestOptions = new RequestOptions().setHeader("x-ms-enable-preview", "true");
        CreateAgentRequest createAgentRequest = new CreateAgentRequest("gpt-4o")
            .setName(agentName)
            .setInstructions("You are a helpful agent")
            .setTools(Arrays.asList(fabricToolDefinition));
        PersistentAgent agent = agentsClient.createAgentWithResponse(BinaryData.fromObject(createAgentRequest), requestOptions)
            .getValue().toObject(PersistentAgent.class);

        PersistentAgentThread thread = threadsClient.createThread();
        ThreadMessage createdMessage = messagesClient.createMessage(
            thread.getId(),
            MessageRole.USER,
            "Give me summary of the document in the sharepoint site");

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
            agentsClient.deleteAgent(agent.getId());
        }
    }
}
