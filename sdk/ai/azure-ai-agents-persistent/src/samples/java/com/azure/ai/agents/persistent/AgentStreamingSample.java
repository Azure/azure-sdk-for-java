// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.agents.persistent;

import com.azure.ai.agents.persistent.models.CodeInterpreterToolDefinition;
import com.azure.ai.agents.persistent.models.CreateAgentOptions;
import com.azure.ai.agents.persistent.models.CreateRunOptions;
import com.azure.ai.agents.persistent.models.MessageRole;
import com.azure.ai.agents.persistent.models.PersistentAgent;
import com.azure.ai.agents.persistent.models.PersistentAgentStreamEvent;
import com.azure.ai.agents.persistent.models.PersistentAgentThread;
import com.azure.ai.agents.persistent.models.StreamMessageUpdate;
import com.azure.ai.agents.persistent.models.StreamUpdate;
import com.azure.ai.agents.persistent.models.ThreadMessage;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import java.util.Arrays;
import java.util.stream.Stream;

import static com.azure.ai.agents.persistent.SampleUtils.printStreamUpdate;

public final class AgentStreamingSample {

    public static void main(String[] args) {
        PersistentAgentsClientBuilder clientBuilder = new PersistentAgentsClientBuilder().endpoint(Configuration.getGlobalConfiguration().get("ENDPOINT", "endpoint"))
            .credential(new DefaultAzureCredentialBuilder().build());
        PersistentAgentsClient agentsClient = clientBuilder.buildClient();
        PersistentAgentsAdministrationClient administrationClient = agentsClient.getPersistentAgentsAdministrationClient();
        ThreadsClient threadsClient = agentsClient.getThreadsClient();
        MessagesClient messagesClient = agentsClient.getMessagesClient();
        RunsClient runsClient = agentsClient.getRunsClient();

        String agentName = "agent_streaming_example";
        CreateAgentOptions createAgentOptions = new CreateAgentOptions("gpt-4o-mini")
            .setName(agentName)
            .setInstructions("You politely help with math questions. Use the code interpreter tool when asked to visualize numbers.")
            .setTools(Arrays.asList(new CodeInterpreterToolDefinition()));
        PersistentAgent agent = administrationClient.createAgent(createAgentOptions);

        PersistentAgentThread thread = threadsClient.createThread();
        ThreadMessage createdMessage = messagesClient.createMessage(
            thread.getId(),
            MessageRole.USER,
            "Hi, Assistant! Draw a graph for a line with a slope of 4 and y-intercept of 9.");

        CreateRunOptions createRunOptions = new CreateRunOptions(thread.getId(), agent.getId())
            .setAdditionalInstructions("");

        try {
            Stream<StreamUpdate> streamUpdates = runsClient.createRunStreaming(createRunOptions);

            streamUpdates.forEach(streamUpdate -> {
                if (streamUpdate.getKind() == PersistentAgentStreamEvent.THREAD_RUN_CREATED) {
                    System.out.println("----- Run started! -----");
                } else if (streamUpdate instanceof StreamMessageUpdate) {
                    StreamMessageUpdate messageUpdate = (StreamMessageUpdate) streamUpdate;
                    printStreamUpdate(messageUpdate);
                }
            });

            System.out.println();
        } catch (Exception ex) {
            throw ex;
        } finally {
            //cleanup
            threadsClient.deleteThread(thread.getId());
            administrationClient.deleteAgent(agent.getId());
        }
    }
}
