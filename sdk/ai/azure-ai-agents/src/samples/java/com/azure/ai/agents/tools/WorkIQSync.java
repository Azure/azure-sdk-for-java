// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.tools;

import com.azure.ai.agents.AgentsClient;
import com.azure.ai.agents.AgentsClientBuilder;
import com.azure.ai.agents.ResponsesClient;
import com.azure.ai.agents.models.AgentReference;
import com.azure.ai.agents.models.AgentVersionDetails;
import com.azure.ai.agents.models.AzureCreateResponseOptions;
import com.azure.ai.agents.models.PromptAgentDefinition;
import com.azure.ai.agents.models.WorkIqPreviewTool;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;
import com.openai.models.responses.ResponseOutputMessage;
import com.openai.models.responses.ToolChoiceOptions;

import java.util.Collections;

/**
 * This sample demonstrates how to create an agent with the Work IQ preview tool.
 *
 * <p>Work IQ uses the signed-in user's Microsoft 365 permissions. Configure a Work IQ project
 * connection in Microsoft Foundry before running this sample. See the
 * <a href="https://learn.microsoft.com/azure/foundry/agents/how-to/tools/work-iq">Work IQ
 * documentation</a> for setup and permission requirements.</p>
 *
 * <p>Before running the sample, set these environment variables:</p>
 * <ul>
 *   <li>FOUNDRY_PROJECT_ENDPOINT - The Azure AI Project endpoint.</li>
 *   <li>FOUNDRY_MODEL_NAME - The model deployment name.</li>
 *   <li>FOUNDRY_AGENT_NAME - Optional. The name of the agent. Defaults to {@code work-iq-agent}.</li>
 *   <li>WORK_IQ_PROJECT_CONNECTION_ID - The fully qualified Work IQ project connection resource ID.</li>
 *   <li>WORK_IQ_USER_INPUT - Optional. The natural-language question to send to the agent.</li>
 * </ul>
 */
public class WorkIQSync {
    public static void main(String[] args) {
        String endpoint = Configuration.getGlobalConfiguration().get("FOUNDRY_PROJECT_ENDPOINT");
        String model = Configuration.getGlobalConfiguration().get("FOUNDRY_MODEL_NAME");
        String agentName = Configuration.getGlobalConfiguration().get("FOUNDRY_AGENT_NAME", "work-iq-agent");
        String workIqConnectionId = Configuration.getGlobalConfiguration().get("WORK_IQ_PROJECT_CONNECTION_ID");
        String userInput = Configuration.getGlobalConfiguration().get("WORK_IQ_USER_INPUT",
            "Use Work IQ to summarize the available enterprise context.");

        AgentsClientBuilder builder = new AgentsClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint(endpoint);

        AgentsClient agentsClient = builder.buildAgentsClient();
        ResponsesClient responsesClient = builder.buildResponsesClient();

        // BEGIN: com.azure.ai.agents.define_work_iq
        // Create a Work IQ tool with a fully qualified project connection resource ID
        WorkIqPreviewTool workIqTool = new WorkIqPreviewTool(workIqConnectionId);
        // END: com.azure.ai.agents.define_work_iq

        PromptAgentDefinition agentDefinition = new PromptAgentDefinition(model)
            .setInstructions("You are a helpful assistant that can access Microsoft 365 data through Work IQ. "
                + "Use the Work IQ tool to search and retrieve information from emails, calendar events, "
                + "Teams messages, and other Microsoft 365 content.")
            .setTools(Collections.singletonList(workIqTool));

        AgentVersionDetails agent = agentsClient.createAgentVersion(agentName, agentDefinition);
        System.out.printf("Agent created: %s (version %s)%n", agent.getName(), agent.getVersion());

        try {
            AgentReference agentReference = new AgentReference(agent.getName())
                .setVersion(agent.getVersion());

            Response response = responsesClient.createAzureResponse(
                new AzureCreateResponseOptions().setAgentReference(agentReference),
                ResponseCreateParams.builder()
                    .toolChoice(ToolChoiceOptions.REQUIRED)
                    .input(userInput));

            System.out.println("Response status: " + response.status().map(Object::toString).orElse("unknown"));
            System.out.println("Agent response: " + getResponseText(response));
        } finally {
            agentsClient.deleteAgentVersion(agent.getName(), agent.getVersion());
            System.out.println("Agent deleted");
        }
    }

    private static String getResponseText(Response response) {
        if (response == null || response.output().isEmpty()) {
            return "<no output>";
        }

        return response.output().stream()
            .filter(item -> item.isMessage())
            .map(item -> item.asMessage().content())
            .filter(content -> !content.isEmpty())
            .map(content -> getContentText(content.get(content.size() - 1)))
            .reduce((first, second) -> second)
            .orElse("<no message output>");
    }

    private static String getContentText(ResponseOutputMessage.Content content) {
        if (content.outputText().isPresent()) {
            return content.outputText().get().text();
        }
        if (content.refusal().isPresent()) {
            return "Refusal: " + content.refusal().get().refusal();
        }
        return "<unsupported message content>";
    }
}
