// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.agents;

import com.azure.ai.agents.AgentsClient;
import com.azure.ai.agents.AgentsClientBuilder;
import com.azure.ai.agents.implementation.models.CreateAgentVersionRequest;
import com.azure.ai.agents.models.AgentProtocol;
import com.azure.ai.agents.models.AgentVersionDetails;
import com.azure.ai.agents.models.HostedAgentDefinition;
import com.azure.ai.agents.models.ProtocolVersionRecord;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * This sample demonstrates how to create and delete a hosted agent
 * using a custom container image with the HostedAgentDefinition.
 *
 * <p>Before running the sample, set these environment variables:</p>
 * <ul>
 *   <li>FOUNDRY_PROJECT_ENDPOINT - The Azure AI Project endpoint.</li>
 * </ul>
 */
public class DeployHostedAgent {

    private static final String HOSTED_AGENTS_FEATURE = "HostedAgents=V1Preview";

    public static void main(String[] args) {
        String endpoint = Configuration.getGlobalConfiguration().get("FOUNDRY_PROJECT_ENDPOINT");

        AgentsClient agentsClient = new AgentsClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint(endpoint)
            .buildAgentsClient();

        // Create hosted agent with custom container image
        HostedAgentDefinition agentDefinition =
            new HostedAgentDefinition(
                Arrays.asList(
                    new ProtocolVersionRecord(
                        AgentProtocol.ACTIVITY_PROTOCOL, "v1")),
                "1",
                "2Gi")
            .setImage("your-registry.azurecr.io/your-image:tag")
            .setEnvironmentVariables(envVars(
                "AZURE_AI_PROJECT_ENDPOINT", endpoint,
                "MODEL_NAME", "gpt-4.1"));

        // Hosted agents is a preview feature — opt in via the Foundry-Features header
        RequestOptions requestOptions = new RequestOptions()
            .setHeader("Foundry-Features", HOSTED_AGENTS_FEATURE);

        BinaryData requestBody = BinaryData.fromObject(new CreateAgentVersionRequest(agentDefinition));
        AgentVersionDetails agentVersion = agentsClient
            .createAgentVersionWithResponse("my-hosted-agent", requestBody, requestOptions)
            .getValue()
            .toObject(AgentVersionDetails.class);

        System.out.printf("Agent created: %s, version: %s%n",
            agentVersion.getName(),
            agentVersion.getVersion());

        // Clean up — also needs the preview header
        agentsClient.deleteAgentVersionWithResponse(
            agentVersion.getName(),
            agentVersion.getVersion(),
            requestOptions);
        System.out.println("Agent version deleted.");
    }

    private static Map<String, String> envVars(String... keyValues) {
        Map<String, String> map = new HashMap<>();
        for (int i = 0; i < keyValues.length; i += 2) {
            map.put(keyValues[i], keyValues[i + 1]);
        }
        return map;
    }
}
