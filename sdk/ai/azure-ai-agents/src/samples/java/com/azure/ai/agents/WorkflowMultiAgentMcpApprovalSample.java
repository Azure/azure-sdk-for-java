// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents;

import com.azure.ai.agents.models.AgentVersionDetails;
import com.azure.ai.agents.models.AzureCreateResponseOptions;
import com.azure.ai.agents.models.CreateAgentVersionInput;
import com.azure.ai.agents.models.McpTool;
import com.azure.ai.agents.models.PromptAgentDefinition;
import com.azure.ai.agents.models.WorkflowAgentDefinition;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;
import com.openai.models.responses.ResponseInputItem;
import com.openai.models.responses.ResponseOutputItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Demonstrates approving MCP calls made by prompt agents within a workflow.
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
        ResponsesClient responsesClient = builder.buildResponsesClient();
        AgentVersionDetails teacher = null;
        AgentVersionDetails student = null;
        AgentVersionDetails workflow = null;

        try {
            McpTool mcpTool = new McpTool("api-specs")
                .setServerUrl("https://gitmcp.io/Azure/azure-rest-api-specs")
                .setRequireApproval("always");
            teacher = createPromptAgent(agentsClient, model, "workflow-teacher-mcp", mcpTool,
                "Check the student's answer using the MCP tool.");
            student = createPromptAgent(agentsClient, model, "workflow-student-mcp", mcpTool,
                "Use the MCP tool to answer the question.");
            workflow = agentsClient.createAgentVersion("student-teacher-workflow-mcp",
                new CreateAgentVersionInput(new WorkflowAgentDefinition().setWorkflow(
                    WorkflowSampleUtils.createStudentTeacherWorkflow(student.getName(), teacher.getName()))));

            AzureCreateResponseOptions options = new AzureCreateResponseOptions()
                .setAgentReference(SampleUtils.toAgentReference(workflow));
            Response response = responsesClient.createAzureResponse(options,
                ResponseCreateParams.builder().input("Summarize the Azure REST API specifications repository."));

            List<ResponseInputItem> approvals = new ArrayList<>();
            for (ResponseOutputItem item : response.output()) {
                if (item.isMcpApprovalRequest()) {
                    approvals.add(ResponseInputItem.ofMcpApprovalResponse(
                        ResponseInputItem.McpApprovalResponse.builder()
                            .approvalRequestId(item.asMcpApprovalRequest().id())
                            .approve(true)
                            .build()));
                }
            }
            if (!approvals.isEmpty()) {
                response = responsesClient.createAzureResponse(options,
                    ResponseCreateParams.builder()
                        .previousResponseId(response.id())
                        .inputOfResponse(approvals));
            }
            SampleUtils.printResponseText(response);
        } finally {
            WorkflowMultiAgentSample.deleteVersion(agentsClient, workflow);
            WorkflowMultiAgentSample.deleteVersion(agentsClient, student);
            WorkflowMultiAgentSample.deleteVersion(agentsClient, teacher);
        }
    }

    private static AgentVersionDetails createPromptAgent(AgentsClient client, String model, String name, McpTool tool,
        String instructions) {
        return client.createAgentVersion(name, new CreateAgentVersionInput(
            new PromptAgentDefinition(model)
                .setInstructions(instructions)
                .setTools(Collections.singletonList(tool))));
    }
}
