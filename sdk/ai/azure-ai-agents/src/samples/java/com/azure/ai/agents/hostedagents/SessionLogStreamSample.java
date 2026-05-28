// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.hostedagents;

import com.azure.ai.agents.AgentsClient;
import com.azure.ai.agents.AgentsClientBuilder;
import com.azure.ai.agents.hostedagents.HostedAgentsSampleUtils.HostedAgentSessionResources;
import com.azure.ai.agents.models.AgentDefinitionOptInKeys;
import com.azure.ai.agents.models.AgentEndpointConfig;
import com.azure.ai.agents.models.AgentEndpointProtocol;
import com.azure.ai.agents.models.FixedRatioVersionSelectionRule;
import com.azure.ai.agents.models.UpdateAgentDetailsOptions;
import com.azure.ai.agents.models.VersionSelector;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.http.rest.Response;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.openai.client.OpenAIClient;
import com.openai.core.JsonValue;
import com.openai.models.responses.ResponseCreateParams;

import java.io.IOException;
import java.util.Collections;

/**
 * This sample demonstrates streaming hosted-agent session logs.
 *
 * <p>Session log streaming is currently a preview feature and only works with hosted-agent sessions.</p>
 *
 * <p>Before running the sample, set these environment variables:</p>
 * <ul>
 *   <li>FOUNDRY_PROJECT_ENDPOINT - The Azure AI Project endpoint.</li>
 *   <li>FOUNDRY_AGENT_CONTAINER_IMAGE - The hosted-agent container image.</li>
 * </ul>
 */
public class SessionLogStreamSample {
    public static void main(String[] args) throws IOException {
        String endpoint = Configuration.getGlobalConfiguration().get("FOUNDRY_PROJECT_ENDPOINT");
        String image = Configuration.getGlobalConfiguration().get("FOUNDRY_AGENT_CONTAINER_IMAGE");
        String agentName = HostedAgentsSampleUtils.SAMPLE_AGENT_NAME;

        AgentsClientBuilder builder = new AgentsClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint(endpoint);

        AgentsClient agentsClient = builder.buildAgentsClient();

        HostedAgentSessionResources resources = null;
        try {
            resources = HostedAgentsSampleUtils.createAgentAndSession(agentsClient, agentName, image);

            AgentEndpointConfig endpointConfig = new AgentEndpointConfig()
                .setVersionSelector(new VersionSelector().setVersionSelectionRules(Collections.singletonList(
                    new FixedRatioVersionSelectionRule(100)
                        .setAgentVersion(resources.getAgent().getVersion()))))
                .setProtocols(Collections.singletonList(AgentEndpointProtocol.RESPONSES));

            agentsClient.updateAgentDetails(agentName,
                new UpdateAgentDetailsOptions().setAgentEndpoint(endpointConfig),
                AgentDefinitionOptInKeys.AGENT_ENDPOINT_V1_PREVIEW);
            System.out.printf("Agent endpoint configured for agent: %s%n", agentName);

            OpenAIClient openAIClient = builder.buildAgentScopedOpenAIClient(agentName);
            com.openai.models.responses.Response openAIResponse = openAIClient.responses().create(
                ResponseCreateParams.builder()
                    .input("Say hello in one short sentence.")
                    .putAdditionalBodyProperty("agent_session_id",
                        JsonValue.from(resources.getSession().getAgentSessionId()))
                    .build());
            HostedAgentsSampleUtils.printResponseOutput(openAIResponse);

            System.out.println("Streaming session logs...");
            Response<BinaryData> rawStream = agentsClient.getSessionLogStreamWithResponse(agentName,
                resources.getAgent().getVersion(), resources.getSession().getAgentSessionId(), new RequestOptions());
            HostedAgentsSampleUtils.printSseFrames(rawStream.getValue(), 30);
        } finally {
            HostedAgentsSampleUtils.cleanup(agentsClient, agentName, resources);
        }
    }
}
