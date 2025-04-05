// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.projects.usage.agent;

import com.azure.ai.projects.AIProjectClientBuilder;
import com.azure.ai.projects.AgentsClient;
import com.azure.ai.projects.models.Agent;
import com.azure.ai.projects.models.AgentStreamEvent;
import com.azure.ai.projects.models.AgentThread;
import com.azure.ai.projects.models.CodeInterpreterToolDefinition;
import com.azure.ai.projects.models.CreateAgentOptions;
import com.azure.ai.projects.models.CreateRunOptions;
import com.azure.ai.projects.models.MessageDeltaImageFileContent;
import com.azure.ai.projects.models.MessageDeltaTextContent;
import com.azure.ai.projects.models.MessageRole;
import com.azure.ai.projects.models.ThreadMessage;
import com.azure.ai.projects.models.streaming.StreamMessageUpdate;
import com.azure.ai.projects.models.streaming.StreamUpdate;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;

import java.util.Arrays;

public class SampleAgentStreaming {

    @Test
    void agentStreamingExample() {
        AgentsClient agentsClient
            = new AIProjectClientBuilder().endpoint(Configuration.getGlobalConfiguration().get("ENDPOINT", "endpoint"))
            .subscriptionId(Configuration.getGlobalConfiguration().get("SUBSCRIPTIONID", "subscriptionid"))
            .resourceGroupName(Configuration.getGlobalConfiguration().get("RESOURCEGROUPNAME", "resourcegroupname"))
            .projectName(Configuration.getGlobalConfiguration().get("PROJECTNAME", "projectname"))
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildAgentsClient();

        String agentName = "agent_streaming_example";
        CreateAgentOptions createAgentOptions = new CreateAgentOptions("gpt-4o-mini")
            .setName(agentName)
            .setInstructions("You politely help with math questions. Use the code interpreter tool when asked to visualize numbers.")
            .setTools(Arrays.asList(new CodeInterpreterToolDefinition()));
        Agent agent = agentsClient.createAgent(createAgentOptions);

        AgentThread thread = agentsClient.createThread();
        ThreadMessage createdMessage = agentsClient.createMessage(
            thread.getId(),
            MessageRole.USER,
            "Hi, Assistant! Draw a graph for a line with a slope of 4 and y-intercept of 9.");

        CreateRunOptions createRunOptions = new CreateRunOptions(thread.getId(), agent.getId())
            .setAdditionalInstructions("");

        try {
            Flux<StreamUpdate> streamingUpdates = agentsClient.createRunStreaming(createRunOptions);

            streamingUpdates.doOnNext(
                streamUpdate -> {
                    if (streamUpdate.getKind() == AgentStreamEvent.THREAD_RUN_CREATED) {
                        System.out.println("----- Run started! -----");
                    } else if (streamUpdate instanceof StreamMessageUpdate) {
                        StreamMessageUpdate messageUpdate = (StreamMessageUpdate) streamUpdate;
                        messageUpdate.getMessage().getDelta().getContent().stream().forEach(delta -> {
                            if (delta instanceof MessageDeltaImageFileContent) {
                                MessageDeltaImageFileContent imgContent = (MessageDeltaImageFileContent) delta;
                                System.out.println("Image fileId: " + imgContent.getImageFile().getFileId());
                            } else if (delta instanceof MessageDeltaTextContent) {
                                MessageDeltaTextContent textContent = (MessageDeltaTextContent) delta;
                                System.out.print(textContent.getText().getValue());
                            }
                        });
                    }
                }
            ).blockLast();

            System.out.println();
        } catch (Exception ex) {
            throw ex;
        } finally {
            //cleanup
            agentsClient.deleteThread(thread.getId());
            agentsClient.deleteAgent(agent.getId());
        }
    }
}
