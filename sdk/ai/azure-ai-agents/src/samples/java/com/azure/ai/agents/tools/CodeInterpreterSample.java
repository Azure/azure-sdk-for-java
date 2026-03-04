// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.tools;

import com.azure.ai.agents.AgentsClient;
import com.azure.ai.agents.AgentsClientBuilder;
import com.azure.ai.agents.ResponsesClient;
import com.azure.ai.agents.models.AgentReference;
import com.azure.ai.agents.models.AgentVersionDetails;
import com.azure.ai.agents.models.CodeInterpreterTool;
import com.azure.ai.agents.models.PromptAgentDefinition;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;

import java.util.Collections;

/**
 * This sample demonstrates how to create an agent with the Code Interpreter tool
 * for data visualization and code execution.
 *
 * <p>Before running the sample, set these environment variables:</p>
 * <ul>
 *   <li>FOUNDRY_PROJECT_ENDPOINT - The Azure AI Project endpoint.</li>
 *   <li>FOUNDRY_MODEL_DEPLOYMENT_NAME - The model deployment name.</li>
 * </ul>
 */
public class CodeInterpreterSample {
    public static void main(String[] args) {
        String endpoint = Configuration.getGlobalConfiguration().get("FOUNDRY_PROJECT_ENDPOINT");
        String model = Configuration.getGlobalConfiguration().get("FOUNDRY_MODEL_DEPLOYMENT_NAME");

        AgentsClientBuilder builder = new AgentsClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint(endpoint);

        AgentsClient agentsClient = builder.buildAgentsClient();
        ResponsesClient responsesClient = builder.buildResponsesClient();

        // Create code interpreter tool
        CodeInterpreterTool codeInterpreter = new CodeInterpreterTool();

        // Create agent with code interpreter for data visualization
        PromptAgentDefinition agentDefinition = new PromptAgentDefinition(model)
            .setInstructions("You are a data visualization assistant. When asked to create charts, "
                + "write and run Python code using matplotlib to generate them.")
            .setTools(Collections.singletonList(codeInterpreter));

        AgentVersionDetails agent = agentsClient.createAgentVersion("chart-agent", agentDefinition);
        System.out.printf("Agent created: %s (version %s)%n", agent.getName(), agent.getVersion());

        try {
            // Request a bar chart with inline data
            AgentReference agentReference = new AgentReference(agent.getName())
                .setVersion(agent.getVersion());

            Response response = responsesClient.createWithAgent(
                agentReference,
                ResponseCreateParams.builder()
                    .input("Create a bar chart showing quarterly revenue for 2025: "
                        + "Q1=$2.1M, Q2=$2.8M, Q3=$3.2M, Q4=$2.9M. "
                        + "Use a blue color scheme, add data labels on each bar, "
                        + "and title the chart 'Quarterly Revenue 2025'. "
                        + "Save the chart as a PNG file."));

            System.out.println("Response: " + response.output());
        } finally {
            // Clean up
            agentsClient.deleteAgentVersion(agent.getName(), agent.getVersion());
            System.out.println("Agent deleted");
        }
    }
}
