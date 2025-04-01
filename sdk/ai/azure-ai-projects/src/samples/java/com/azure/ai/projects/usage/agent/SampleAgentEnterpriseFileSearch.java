package com.azure.ai.projects.usage.agent;

import com.azure.ai.projects.AIProjectClientBuilder;
import com.azure.ai.projects.AgentsClient;
import com.azure.ai.projects.models.*;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import org.junit.jupiter.api.Test;

import java.util.List;

public class SampleAgentEnterpriseFileSearch {

    @Test
    void enterpriseFileSearchExample() {
        AgentsClient agentsClient
            = new AIProjectClientBuilder().endpoint(Configuration.getGlobalConfiguration().get("ENDPOINT", "endpoint"))
            .subscriptionId(Configuration.getGlobalConfiguration().get("SUBSCRIPTIONID", "subscriptionid"))
            .resourceGroupName(Configuration.getGlobalConfiguration().get("RESOURCEGROUPNAME", "resourcegroupname"))
            .projectName(Configuration.getGlobalConfiguration().get("PROJECTNAME", "projectname"))
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildAgentsClient();

        var dataUri = "azureml://subscriptions/696debc0-8b66-4d84-87b1-39f43917d76c/resourcegroups/rg-jayant/workspaces/jayant-project-2aqa/datastores/workspaceblobstore/paths/product_info_1.md";
        VectorStoreDataSource vectorStoreDataSource = new VectorStoreDataSource(
            dataUri, VectorStoreDataSourceAssetType.URI_ASSET);

        VectorStore vs = agentsClient.createVectorStore(
            null, "sample_vector_store",
            new VectorStoreConfiguration(List.of(vectorStoreDataSource)),
            null,null, null
        );

        FileSearchToolResource fileSearchToolResource = new FileSearchToolResource()
            .setVectorStoreIds(List.of(vs.getId()));

        var agentName = "enterprise_file_search_example";
        var createAgentOptions = new CreateAgentOptions("gpt-4o-mini")
            .setName(agentName)
            .setInstructions("You are a helpful agent")
            .setTools(List.of(new FileSearchToolDefinition()))
            .setToolResources(new ToolResources().setFileSearch(fileSearchToolResource));
        Agent agent = agentsClient.createAgent(createAgentOptions);

        var thread = agentsClient.createThread();
        var createdMessage = agentsClient.createMessage(
            thread.getId(),
            MessageRole.USER,
            "What is data about?");

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
