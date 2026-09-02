// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents;

import com.azure.ai.agents.models.AgentVersionDetails;
import com.azure.ai.agents.models.AzureCreateResponseOptions;
import com.azure.ai.agents.models.CreateAgentVersionInput;
import com.azure.ai.agents.models.PromptAgentDefinition;
import com.azure.ai.agents.models.WorkflowAgentDefinition;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;

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
        ResponsesClient responsesClient = builder.buildResponsesClient();

        AgentVersionDetails teacher = null;
        AgentVersionDetails student = null;
        AgentVersionDetails workflow = null;
        try {
            teacher = agentsClient.createAgentVersion("teacher-agent",
                new CreateAgentVersionInput(new PromptAgentDefinition(model)
                    .setInstructions("Check the student's answer, then explain the correct answer.")));
            student = agentsClient.createAgentVersion("student-agent",
                new CreateAgentVersionInput(new PromptAgentDefinition(model)
                    .setInstructions("Answer the teacher's math question.")));
            workflow = agentsClient.createAgentVersion("student-teacher-workflow",
                new CreateAgentVersionInput(new WorkflowAgentDefinition().setWorkflow(
                    WorkflowSampleUtils.createStudentTeacherWorkflow(student.getName(), teacher.getName()))));

            Response response = responsesClient.createAzureResponse(
                new AzureCreateResponseOptions().setAgentReference(SampleUtils.toAgentReference(workflow)),
                ResponseCreateParams.builder().input("What is 12 multiplied by 8?"));
            SampleUtils.printResponseText(response);
        } finally {
            deleteVersion(agentsClient, workflow);
            deleteVersion(agentsClient, student);
            deleteVersion(agentsClient, teacher);
        }
    }

    static void deleteVersion(AgentsClient client, AgentVersionDetails agent) {
        if (agent != null) {
            client.deleteAgentVersion(agent.getName(), agent.getVersion());
        }
    }
}
