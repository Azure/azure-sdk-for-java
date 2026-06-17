// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.hostedagents;

import com.azure.ai.agents.AgentsClient;
import com.azure.ai.agents.AgentsClientBuilder;
import com.azure.ai.agents.BetaAgentsClient;
import com.azure.ai.agents.hostedagents.utils.HostedAgentsSampleUtils;
import com.azure.ai.agents.hostedagents.utils.HostedAgentsSampleUtils.HostedAgentSessionResources;
import com.azure.ai.agents.models.AgentEndpointConfig;
import com.azure.ai.agents.models.AgentEndpointProtocol;
import com.azure.ai.agents.models.FixedRatioVersionSelectionRule;
import com.azure.ai.agents.models.UpdateAgentDetailsOptions;
import com.azure.ai.agents.models.VersionSelector;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.openai.client.OpenAIClient;
import com.openai.core.JsonValue;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;

import java.util.Collections;

/**
 * This sample demonstrates configuring a hosted agent endpoint and invoking the OpenAI Responses API through it.
 *
 * <p>Agent endpoints and sessions are currently preview features and only work with hosted agents.</p>
 *
 * <p>Before running the sample, set these environment variables:</p>
 * <ul>
 *   <li>FOUNDRY_PROJECT_ENDPOINT - The Azure AI Project endpoint.</li>
 *   <li>FOUNDRY_AGENT_CONTAINER_IMAGE - The hosted-agent container image.</li>
 * </ul>
 */
public class AgentEndpointSample {
    public static void main(String[] args) {
        String endpoint = Configuration.getGlobalConfiguration().get("FOUNDRY_PROJECT_ENDPOINT");
        String image = Configuration.getGlobalConfiguration().get("FOUNDRY_AGENT_CONTAINER_IMAGE");
        String agentName = HostedAgentsSampleUtils.SAMPLE_AGENT_NAME;

        AgentsClientBuilder builder = new AgentsClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint(endpoint);

        AgentsClient agentsClient = builder.allowPreview(true).buildAgentsClient();
        BetaAgentsClient betaAgentsClient = builder.beta().buildBetaAgentsClient();

        HostedAgentSessionResources resources = null;
        try {
            resources = HostedAgentsSampleUtils.createAgentAndSession(agentsClient, betaAgentsClient, agentName, image);

            AgentEndpointConfig endpointConfig = new AgentEndpointConfig()
                .setVersionSelector(new VersionSelector().setVersionSelectionRules(Collections.singletonList(
                    new FixedRatioVersionSelectionRule(100)
                        .setAgentVersion(resources.getAgent().getVersion()))))
                .setProtocols(Collections.singletonList(AgentEndpointProtocol.RESPONSES));

            betaAgentsClient.updateAgentDetails(agentName,
                new UpdateAgentDetailsOptions().setAgentEndpoint(endpointConfig));
            System.out.printf("Agent endpoint configured for agent: %s%n", agentName);

            OpenAIClient openAIClient = builder.buildAgentScopedOpenAIClient(agentName);
            Response response = openAIClient.responses().create(ResponseCreateParams.builder()
                .input("What is the size of France in square miles?")
                .putAdditionalBodyProperty("agent_session_id",
                    JsonValue.from(resources.getSession().getAgentSessionId()))
                .build());

            HostedAgentsSampleUtils.printResponseOutput(response);
        } finally {
            HostedAgentsSampleUtils.cleanup(agentsClient, betaAgentsClient, agentName, resources);
        }
    }
}
