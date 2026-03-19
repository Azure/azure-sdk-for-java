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
import com.openai.models.responses.ResponseCodeInterpreterToolCall;
import com.openai.models.responses.ResponseCreateParams;
import com.openai.models.responses.ResponseOutputItem;
import com.openai.models.responses.ResponseOutputMessage;

import java.util.Collections;

/**
 * This sample demonstrates how to create an agent with the Code Interpreter tool
 * for executing Python code, data analysis, and visualization.
 *
 * <p>Before running the sample, set these environment variables:</p>
 * <ul>
 *   <li>FOUNDRY_PROJECT_ENDPOINT - The Azure AI Project endpoint.</li>
 *   <li>FOUNDRY_MODEL_NAME - The model deployment name.</li>
 * </ul>
 */
public class CodeInterpreterSync {
    public static void main(String[] args) {
        String endpoint = Configuration.getGlobalConfiguration().get("FOUNDRY_PROJECT_ENDPOINT");
        String model = Configuration.getGlobalConfiguration().get("FOUNDRY_MODEL_NAME");

        AgentsClientBuilder builder = new AgentsClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint(endpoint);

        AgentsClient agentsClient = builder.buildAgentsClient();
        ResponsesClient responsesClient = builder.buildResponsesClient();

        AgentVersionDetails agent = null;

        try {
            // BEGIN: com.azure.ai.agents.define_code_interpreter
            // Create a CodeInterpreterTool with default auto container configuration
            CodeInterpreterTool tool = new CodeInterpreterTool();
            // END: com.azure.ai.agents.define_code_interpreter

            // Create the agent definition with Code Interpreter tool enabled
            PromptAgentDefinition agentDefinition = new PromptAgentDefinition(model)
                .setInstructions("You are a helpful assistant that can execute Python code to solve problems. "
                    + "When asked to perform calculations or data analysis, use the code interpreter to run Python code.")
                .setTools(Collections.singletonList(tool));

            agent = agentsClient.createAgentVersion("code-interpreter-agent", agentDefinition);
            System.out.printf("Agent created: %s (version %s)%n", agent.getName(), agent.getVersion());

            AgentReference agentReference = new AgentReference(agent.getName())
                .setVersion(agent.getVersion());

            Response response = responsesClient.createWithAgent(agentReference,
                ResponseCreateParams.builder()
                    .input("Calculate the first 10 prime numbers and show me the Python code you used."));

            // Process and display the response
            for (ResponseOutputItem outputItem : response.output()) {
                if (outputItem.message().isPresent()) {
                    ResponseOutputMessage message = outputItem.message().get();
                    message.content().forEach(content -> {
                        content.outputText().ifPresent(text -> {
                            System.out.println("Assistant: " + text.text());
                        });
                    });
                }

                if (outputItem.codeInterpreterCall().isPresent()) {
                    ResponseCodeInterpreterToolCall codeCall = outputItem.codeInterpreterCall().get();
                    System.out.println("\n--- Code Interpreter Execution ---");
                    System.out.println("Call ID: " + codeCall.id());
                    codeCall.code().ifPresent(code -> {
                        System.out.println("Python Code Executed:\n" + code);
                    });
                    System.out.println("Status: " + codeCall.status());
                }
            }
        } finally {
            if (agent != null) {
                agentsClient.deleteAgentVersion(agent.getName(), agent.getVersion());
                System.out.println("Agent deleted");
            }
        }
    }
}
