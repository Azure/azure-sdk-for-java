// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.agents.persistent;

import com.azure.ai.agents.persistent.models.AISearchIndexResource;
import com.azure.ai.agents.persistent.models.AzureAISearchResource;
import com.azure.ai.agents.persistent.models.AzureAISearchToolDefinition;
import com.azure.ai.agents.persistent.models.CreateAgentOptions;
import com.azure.ai.agents.persistent.models.CreateRunOptions;
import com.azure.ai.agents.persistent.models.MessageRole;
import com.azure.ai.agents.persistent.models.PersistentAgent;
import com.azure.ai.agents.persistent.models.PersistentAgentThread;
import com.azure.ai.agents.persistent.models.ThreadMessage;
import com.azure.ai.agents.persistent.models.ThreadRun;
import com.azure.ai.agents.persistent.models.ToolResources;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import java.util.Arrays;

import static com.azure.ai.agents.persistent.SampleUtils.printRunMessages;
import static com.azure.ai.agents.persistent.SampleUtils.waitForRunCompletion;

public class AgentAzureAISearchSample {

    public static void main(String[] args) {

        PersistentAgentsAdministrationClientBuilder clientBuilder = new PersistentAgentsAdministrationClientBuilder().endpoint(Configuration.getGlobalConfiguration().get("ENDPOINT", "endpoint"))
            .credential(new DefaultAzureCredentialBuilder().build());
        PersistentAgentsAdministrationClient agentsClient = clientBuilder.buildClient();
        ThreadsClient threadsClient = clientBuilder.buildThreadsClient();
        MessagesClient messagesClient = clientBuilder.buildMessagesClient();
        RunsClient runsClient = clientBuilder.buildRunsClient();

        String aiSearchConnectionId = Configuration.getGlobalConfiguration().get("AI_SEARCH_CONNECTION_ID", "");

        ToolResources toolResources = new ToolResources()
            .setAzureAISearch(new AzureAISearchResource()
                .setIndexList(Arrays.asList(new AISearchIndexResource(aiSearchConnectionId, "azureblob-index"))));

        String agentName = "ai_search_example";
        CreateAgentOptions createAgentOptions = new CreateAgentOptions("gpt-4o-mini")
            .setName(agentName)
            .setInstructions("You are a helpful agent")
            .setTools(Arrays.asList(new AzureAISearchToolDefinition()))
            .setToolResources(toolResources);
        PersistentAgent agent = agentsClient.createAgent(createAgentOptions);

        PersistentAgentThread thread = threadsClient.createThread();
        ThreadMessage createdMessage = messagesClient.createMessage(
            thread.getId(),
            MessageRole.USER,
            "Best horror movie?");

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
