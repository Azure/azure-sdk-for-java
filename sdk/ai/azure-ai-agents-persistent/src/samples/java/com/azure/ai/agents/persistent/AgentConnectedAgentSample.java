// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.agents.persistent;

import com.azure.ai.agents.persistent.implementation.models.CreateAgentRequest;
import com.azure.ai.agents.persistent.models.ConnectedAgentDetails;
import com.azure.ai.agents.persistent.models.ConnectedAgentToolDefinition;
import com.azure.ai.agents.persistent.models.CreateAgentOptions;
import com.azure.ai.agents.persistent.models.CreateRunOptions;
import com.azure.ai.agents.persistent.models.MessageRole;
import com.azure.ai.agents.persistent.models.PersistentAgent;
import com.azure.ai.agents.persistent.models.PersistentAgentThread;
import com.azure.ai.agents.persistent.models.ThreadMessage;
import com.azure.ai.agents.persistent.models.ThreadRun;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import java.util.Arrays;

import static com.azure.ai.agents.persistent.SampleUtils.printRunMessages;
import static com.azure.ai.agents.persistent.SampleUtils.waitForRunCompletion;

public final class AgentConnectedAgentSample {

    public static void main(String[] args) {

        PersistentAgentsClientBuilder clientBuilder = new PersistentAgentsClientBuilder().endpoint(Configuration.getGlobalConfiguration().get("ENDPOINT", "endpoint"))
            .credential(new DefaultAzureCredentialBuilder().build());
        PersistentAgentsClient agentsClient = clientBuilder.buildClient();
        PersistentAgentsAdministrationClient administrationClient = agentsClient.getPersistentAgentsAdministrationClient();
        ThreadsClient threadsClient = agentsClient.getThreadsClient();
        MessagesClient messagesClient = agentsClient.getMessagesClient();
        RunsClient runsClient = agentsClient.getRunsClient();

        String connectedAgentName = "stock_price_bot";
        CreateAgentOptions connectedAgentCreateOptions = new CreateAgentOptions("gpt-4o-mini")
            .setName(connectedAgentName)
            .setInstructions("Your job is to get the stock price of a company. Just return $391.85 EOD 27-Apr-2025");
        PersistentAgent connectedAgent = administrationClient.createAgent(connectedAgentCreateOptions);

        ConnectedAgentToolDefinition connectedAgentToolDefinition = new ConnectedAgentToolDefinition(
            new ConnectedAgentDetails(connectedAgent.getId(), connectedAgent.getName(), "Gets the stock price of a company"));

        String agentName = "my-assistant";
        CreateAgentRequest createAgentRequest = new CreateAgentRequest("gpt-4o-mini")
            .setName(agentName)
            .setInstructions("You are a helpful assistant, and use the connected agent to get stock prices.")
            .setTools(Arrays.asList(connectedAgentToolDefinition));
        RequestOptions requestOptions = new RequestOptions()
            .setHeader("x-ms-enable-preview", "true");
        PersistentAgent agent = administrationClient.createAgentWithResponse(BinaryData.fromObject(createAgentRequest), requestOptions)
            .getValue().toObject(PersistentAgent.class);

        PersistentAgentThread thread = threadsClient.createThread();
        ThreadMessage createdMessage = messagesClient.createMessage(
            thread.getId(),
            MessageRole.USER,
            "What is the stock price of Microsoft?");

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
}
