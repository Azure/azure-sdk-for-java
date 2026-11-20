// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.agents;

import com.azure.ai.agents.AgentsClient;
import com.azure.ai.agents.AgentsClientBuilder;
import com.azure.ai.agents.models.AgentEndpointConfig;
import com.azure.ai.agents.models.AgentVersionDetails;
import com.azure.ai.agents.models.EntraAuthorizationScheme;
import com.azure.ai.agents.models.FixedRatioVersionSelectionRule;
import com.azure.ai.agents.models.PromptAgentDefinition;
import com.azure.ai.agents.models.ProtocolConfiguration;
import com.azure.ai.agents.models.ResponsesProtocolConfiguration;
import com.azure.ai.agents.models.UpdateAgentDetailsOptions;
import com.azure.ai.agents.models.VersionSelector;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.openai.client.OpenAIClient;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;

import java.util.Collections;
import java.util.UUID;

/**
 * Demonstrates configuring an agent endpoint to route all traffic to a specific version,
 * enable the Responses protocol, and authorize callers with Microsoft Entra ID.
 *
 * <p>Set {@code FOUNDRY_PROJECT_ENDPOINT} and {@code FOUNDRY_MODEL_NAME} before running.
 * The sample creates a uniquely named agent, pins its endpoint, creates a newer version,
 * and invokes the endpoint to demonstrate that it still serves the pinned version.
 * It deletes the sample agent and both versions when finished.</p>
 *
 * <p>Endpoint configuration is optional. New agents route to the latest version with the Responses
 * protocol and Microsoft Entra authentication enabled by default.</p>
 */
public class ConfigureAgentEndpoint {
    /**
     * Runs the sample.
     *
     * @param args unused
     */
    public static void main(String[] args) {
        String endpoint = Configuration.getGlobalConfiguration().get("FOUNDRY_PROJECT_ENDPOINT");
        String model = Configuration.getGlobalConfiguration().get("FOUNDRY_MODEL_NAME");
        String agentName = "configured-agent-" + UUID.randomUUID();

        AgentsClientBuilder builder = new AgentsClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint(endpoint);
        AgentsClient agentsClient = builder.buildAgentsClient();

        AgentVersionDetails agent = agentsClient.createAgentVersion(agentName,
            new PromptAgentDefinition(model).setInstructions("Always reply with exactly PINNED_VERSION."));
        try {
            System.out.printf("Agent created: %s (version %s)%n", agent.getName(), agent.getVersion());

            // BEGIN: com.azure.ai.agents.configure_agent_endpoint
            AgentEndpointConfig endpointConfig = new AgentEndpointConfig()
                .setVersionSelector(new VersionSelector().setVersionSelectionRule(
                    new FixedRatioVersionSelectionRule(100).setAgentVersion(agent.getVersion())))
                .setProtocolConfiguration(
                    new ProtocolConfiguration().setResponses(new ResponsesProtocolConfiguration()))
                .setAuthorizationSchemes(Collections.singletonList(new EntraAuthorizationScheme()));

            agentsClient.updateAgentDetails(agent.getName(),
                new UpdateAgentDetailsOptions().setAgentEndpoint(endpointConfig));
            // END: com.azure.ai.agents.configure_agent_endpoint
            System.out.println("Endpoint pinned to version: " + agent.getVersion());

            AgentVersionDetails latestAgent = agentsClient.createAgentVersion(agentName,
                new PromptAgentDefinition(model).setInstructions("Always reply with exactly LATEST_VERSION."));
            System.out.println("Newer version created: " + latestAgent.getVersion());

            OpenAIClient openAIClient = builder.buildAgentScopedOpenAIClient(agentName);
            Response response = openAIClient.responses().create(ResponseCreateParams.builder()
                .input("Which version are you?")
                .store(false)
                .build());

            response.output().forEach(item -> item.message().ifPresent(message ->
                message.content().forEach(content -> content.outputText().ifPresent(text ->
                    System.out.println("Assistant: " + text.text())))));
        } finally {
            agentsClient.deleteAgent(agentName);
            System.out.println("Sample agent and all its versions deleted.");
        }
    }
}
