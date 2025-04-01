package com.azure.ai.projects.usage.agent;

import com.azure.ai.projects.AIProjectClientBuilder;
import com.azure.ai.projects.AgentsClient;
import com.azure.ai.projects.implementation.models.CreateRunRequest;
import com.azure.ai.projects.models.*;
import com.azure.ai.projects.models.streaming.StreamMessageUpdate;
import com.azure.ai.projects.models.streaming.StreamUpdate;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.http.rest.Response;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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

        var agentName = "agent_streaming_example";
        var createAgentOptions = new CreateAgentOptions("gpt-4o-mini")
            .setName(agentName)
            .setInstructions("You politely help with math questions. Use the code interpreter tool when asked to visualize numbers.")
            .setTools(List.of(new CodeInterpreterToolDefinition()));
        Agent agent = agentsClient.createAgent(createAgentOptions);

        var thread = agentsClient.createThread();
        var createdMessage = agentsClient.createMessage(
            thread.getId(),
            MessageRole.USER,
            "Hi, Assistant! Draw a graph for a line with a slope of 4 and y-intercept of 9.");

        var createRunOptions = new CreateRunOptions(thread.getId(), agent.getId())
            .setAdditionalInstructions("");

        try {
            Flux<StreamUpdate> streamingUpdates = agentsClient.createRunStreaming(createRunOptions);

            streamingUpdates.doOnNext(
                streamUpdate -> {
                    if (streamUpdate.getKind() == AgentStreamEvent.THREAD_RUN_CREATED) {
                        System.out.println("----- Run started! -----");
                    }
                    else if (streamUpdate instanceof StreamMessageUpdate) {
                        StreamMessageUpdate messageUpdate = (StreamMessageUpdate) streamUpdate;
                        messageUpdate.getMessage().getDelta().getContent().stream().forEach(delta -> {
                            if (delta instanceof MessageDeltaImageFileContent) {
                                MessageDeltaImageFileContent imgContent = (MessageDeltaImageFileContent) delta;
                                System.out.println("Image fileId: " + imgContent.getImageFile().getFileId());
                            }
                            else if (delta instanceof MessageDeltaTextContent) {
                                MessageDeltaTextContent textContent = (MessageDeltaTextContent) delta;
                                System.out.print(textContent.getText().getValue());
                            }
                        });
                    }
                }
            ).blockLast();

            System.out.println();
        }
        catch (Exception ex) {
            throw ex;
        }
        finally {
            //cleanup
            agentsClient.deleteThread(thread.getId());
            agentsClient.deleteAgent(agent.getId());
        }
    }
}
