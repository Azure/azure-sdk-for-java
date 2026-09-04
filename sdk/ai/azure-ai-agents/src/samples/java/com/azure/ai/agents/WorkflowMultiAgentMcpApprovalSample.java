// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents;

import com.azure.ai.agents.models.AgentReference;
import com.azure.ai.agents.models.AgentVersionDetails;
import com.azure.ai.agents.models.AzureCreateResponseOptions;
import com.azure.ai.agents.models.CreateAgentVersionInput;
import com.azure.ai.agents.models.McpTool;
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

import java.util.Collections;

/**
 * Demonstrates MCP calls made by prompt agents within a workflow.
 *
 * <p>The current workflow service cannot resume nested MCP approval requests through the outer workflow response,
 * so this sample configures MCP tools with approval disabled.</p>
 *
 * <p>Before running the sample, set these environment variables:</p>
 * <ul>
 *   <li>{@code FOUNDRY_PROJECT_ENDPOINT} - The Azure AI Project endpoint.</li>
 *   <li>{@code FOUNDRY_MODEL_NAME} - The model deployment name.</li>
 * </ul>
 */
public class WorkflowMultiAgentMcpApprovalSample {
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
            McpTool mcpTool = new McpTool("api-specs")
                .setServerUrl("https://gitmcp.io/Azure/azure-rest-api-specs")
                .setRequireApproval("never");
            teacher = createPromptAgent(agentsClient, model, "workflow-teacher-mcp", mcpTool,
                "Check the student's answer using the MCP tool.");
            student = createPromptAgent(agentsClient, model, "workflow-student-mcp", mcpTool,
                "Use the MCP tool to answer the question.");
            workflow = agentsClient.createAgentVersion("student-teacher-workflow-mcp",
                new CreateAgentVersionInput(new WorkflowAgentDefinition().setWorkflow(
                    WorkflowSampleUtils.createStudentTeacherWorkflow(student.getName(), teacher.getName()))));

            conversation = conversations.create();
            AzureCreateResponseOptions options = new AzureCreateResponseOptions()
                .setAgentReference(new AgentReference(workflow.getName()));
            Response response = streamResponse(responsesClient, options,
                ResponseCreateParams.builder()
                    .conversation(conversation.id())
                    .input("Summarize the Azure REST API specifications repository."));
            System.out.println();
            SampleUtils.printResponseText(response);
        } finally {
            try {
                if (conversation != null) {
                    conversations.delete(conversation.id());
                }
                WorkflowMultiAgentSample.deleteVersion(agentsClient, workflow);
                WorkflowMultiAgentSample.deleteVersion(agentsClient, student);
                WorkflowMultiAgentSample.deleteVersion(agentsClient, teacher);
            } finally {
                openAIClient.close();
            }
        }
    }

    private static Response streamResponse(ResponsesClient responsesClient, AzureCreateResponseOptions options,
        ResponseCreateParams.Builder params) {
        ResponseAccumulator accumulator = ResponseAccumulator.create();
        IterableStream<ResponseStreamEvent> events = responsesClient.createStreamingAzureResponse(options, params);
        for (ResponseStreamEvent event : events) {
            accumulator.accumulate(event);
            event.outputTextDelta().ifPresent(textEvent -> System.out.print(textEvent.delta()));
        }
        return accumulator.response();
    }

    private static AgentVersionDetails createPromptAgent(AgentsClient client, String model, String name, McpTool tool,
        String instructions) {
        return client.createAgentVersion(name, new CreateAgentVersionInput(
            new PromptAgentDefinition(model)
                .setInstructions(instructions)
                .setTools(Collections.singletonList(tool))));
    }
}
