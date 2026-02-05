// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents;

import com.azure.ai.agents.models.AgentReference;
import com.azure.ai.agents.models.AgentVersionDetails;
import com.azure.ai.agents.models.CodeInterpreterContainerAuto;
import com.azure.ai.agents.models.CodeInterpreterTool;
import com.azure.ai.agents.models.PromptAgentDefinition;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.openai.models.conversations.Conversation;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCodeInterpreterToolCall;
import com.openai.models.responses.ResponseCreateParams;
import com.openai.models.responses.ResponseOutputItem;
import com.openai.models.responses.ResponseOutputMessage;

import java.util.Collections;

/**
 * This sample demonstrates how to create an Azure AI Agent with the Code Interpreter tool
 * and use it to get responses that involve code execution.
 */
public class CodeInterpreterAgent {
    public static void main(String[] args) {
        String endpoint = Configuration.getGlobalConfiguration().get("AZURE_AGENTS_ENDPOINT");
        String model = Configuration.getGlobalConfiguration().get("AZURE_AGENTS_MODEL");
        System.out.println("Using endpoint: " + endpoint);
        System.out.println("Using model: " + model);

        AgentsClientBuilder builder = new AgentsClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .serviceVersion(AgentsServiceVersion.getLatest())
            .endpoint(endpoint);

        AgentsClient agentsClient = builder.buildAgentsClient();
        ResponsesClient responsesClient = builder.buildResponsesClient();
        ConversationsClient conversationsClient = builder.buildConversationsClient();

        AgentVersionDetails agent = null;
        Conversation conversation = null;

        try {
            // Create a CodeInterpreterTool with auto container configuration
            CodeInterpreterContainerAuto containerConfig = new CodeInterpreterContainerAuto();
            CodeInterpreterTool tool = new CodeInterpreterTool(BinaryData.fromObject(containerConfig));

            // Create the agent definition with Code Interpreter tool enabled
            PromptAgentDefinition agentDefinition = new PromptAgentDefinition(model)
                .setInstructions("You are a helpful assistant that can execute Python code to solve problems. "
                    + "When asked to perform calculations or data analysis, use the code interpreter to run Python code.")
                .setTools(Collections.singletonList(tool));

            agent = agentsClient.createAgentVersion("MyAgent", agentDefinition);
            System.out.printf("Agent created (id: %s, version: %s)\n", agent.getId(), agent.getVersion());

            AgentReference agentReference = new AgentReference(agent.getName()).setVersion(agent.getVersion());

            conversation = conversationsClient.getConversationService().create();
            System.out.println("Created Conversation: " + conversation.id());

            Response response = responsesClient.createWithAgentConversation(agentReference, conversation.id(), 
                    ResponseCreateParams.builder().input("Calculate the first 10 prime numbers and show me the Python code you used."));

            // Process and display the response
            System.out.println("\n=== Agent Response ===");
            for (ResponseOutputItem outputItem : response.output()) {
                // Handle message output
                if (outputItem.message().isPresent()) {
                    ResponseOutputMessage message = outputItem.message().get();
                    message.content().forEach(content -> {
                        content.outputText().ifPresent(text -> {
                            System.out.println("Assistant: " + text.text());
                        });
                    });
                }

                // Handle code interpreter tool call output
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

            System.out.println("\nResponse ID: " + response.id());
            System.out.println("Model Used: " + response.model());
        } finally {
            // Cleanup conversation and agent
            if (conversation != null) {
                conversationsClient.getConversationService().delete(conversation.id());
                System.out.println("Conversation deleted successfully.");
            }
            if (agent != null) {
                agentsClient.deleteAgentVersion(agent.getName(), agent.getVersion());
                System.out.println("Agent deleted successfully.");
            }
        }
    }
}
