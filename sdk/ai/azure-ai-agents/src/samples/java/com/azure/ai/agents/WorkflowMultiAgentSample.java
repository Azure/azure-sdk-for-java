// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents;

import com.azure.ai.agents.models.AgentReference;
import com.azure.ai.agents.models.AgentVersionDetails;
import com.azure.ai.agents.models.AzureCreateResponseOptions;
import com.azure.ai.agents.models.CreateAgentVersionInput;
import com.azure.ai.agents.models.PromptAgentDefinition;
import com.azure.ai.agents.models.WorkflowAgentDefinition;
import com.azure.core.util.Configuration;
import com.azure.core.util.IterableStream;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.openai.client.OpenAIClient;
import com.openai.helpers.ResponseAccumulator;
import com.openai.models.conversations.Conversation;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;
import com.openai.models.responses.ResponseStreamEvent;
import com.openai.services.blocking.ConversationService;

/**
 * Demonstrates a workflow agent that invokes student and teacher prompt agents.
 *
 * <p>Before running the sample, set these environment variables:</p>
 * <ul>
 *   <li>{@code FOUNDRY_PROJECT_ENDPOINT} - The Azure AI Project endpoint.</li>
 *   <li>{@code FOUNDRY_MODEL_NAME} - The model deployment name.</li>
 * </ul>
 */
public class WorkflowMultiAgentSample {
    public static void main(String[] args) {
        Configuration configuration = Configuration.getGlobalConfiguration();
        String endpoint = configuration.get("FOUNDRY_PROJECT_ENDPOINT");
        String model = configuration.get("FOUNDRY_MODEL_NAME");

        AgentsClientBuilder builder = new AgentsClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint(endpoint)
            .allowPreview(true);
        AgentsClient agentsClient = builder.buildAgentsClient();
        OpenAIClient openAIClient = builder.buildOpenAIClient();
        ResponsesClient responsesClient = new ResponsesClient(openAIClient);
        ConversationService conversations = openAIClient.conversations();

        AgentVersionDetails teacher = null;
        AgentVersionDetails student = null;
        AgentVersionDetails workflow = null;
        Conversation conversation = null;
        try {
            teacher = agentsClient.createAgentVersion("teacher-agent",
                new CreateAgentVersionInput(new PromptAgentDefinition(model)
                    .setInstructions("Create a preschool math question and check the student's answer. "
                        + "Say [COMPLETE] when the answer is correct.")));
            student = agentsClient.createAgentVersion("student-agent",
                new CreateAgentVersionInput(new PromptAgentDefinition(model)
                    .setInstructions("Answer the teacher's math question.")));
            workflow = agentsClient.createAgentVersion("student-teacher-workflow",
                new CreateAgentVersionInput(new WorkflowAgentDefinition().setWorkflow(
                    WorkflowSampleUtils.createStudentTeacherWorkflow(student.getName(), teacher.getName()))));

            conversation = conversations.create();
            ResponseAccumulator accumulator = ResponseAccumulator.create();
            IterableStream<ResponseStreamEvent> events = responsesClient.createStreamingAzureResponse(
                new AzureCreateResponseOptions().setAgentReference(new AgentReference(workflow.getName())),
                ResponseCreateParams.builder().conversation(conversation.id()).input("1 + 1 = ?"));
            for (ResponseStreamEvent event : events) {
                accumulator.accumulate(event);
                event.outputTextDelta().ifPresent(textEvent -> System.out.print(textEvent.delta()));
            }
            System.out.println();
            Response response = accumulator.response();
            SampleUtils.printResponseText(response);
        } finally {
            try {
                if (conversation != null) {
                    conversations.delete(conversation.id());
                }
                deleteVersion(agentsClient, workflow);
                deleteVersion(agentsClient, student);
                deleteVersion(agentsClient, teacher);
            } finally {
                openAIClient.close();
            }
        }
    }

    static void deleteVersion(AgentsClient client, AgentVersionDetails agent) {
        if (agent != null) {
            client.deleteAgentVersion(agent.getName(), agent.getVersion());
        }
    }
}
