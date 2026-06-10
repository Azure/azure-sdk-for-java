// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.hostedagents;

import com.azure.ai.agents.AgentsClient;
import com.azure.ai.agents.AgentsClientBuilder;
import com.azure.ai.agents.BetaAgentsClient;
import com.azure.ai.agents.hostedagents.utils.HostedAgentsSampleUtils;
import com.azure.ai.agents.hostedagents.utils.HostedAgentsSampleUtils.HostedAgentSessionResources;
import com.azure.ai.agents.models.AgentSessionResource;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;

/**
 * This sample demonstrates how to create, retrieve, list, and delete hosted-agent sessions.
 *
 * <p>Sessions are currently a preview feature and only work with hosted agents.</p>
 *
 * <p>Before running the sample, set these environment variables:</p>
 * <ul>
 *   <li>FOUNDRY_PROJECT_ENDPOINT - The Azure AI Project endpoint.</li>
 *   <li>FOUNDRY_AGENT_CONTAINER_IMAGE - The hosted-agent container image.</li>
 * </ul>
 */
public class SessionsSample {
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
            AgentSessionResource session = resources.getSession();

            AgentSessionResource fetched = betaAgentsClient.getSession(agentName, session.getAgentSessionId(), null);
            System.out.printf("Retrieved session (id: %s, status: %s)%n", fetched.getAgentSessionId(),
                fetched.getStatus());

            System.out.println("Listing sessions for the agent...");
            PagedIterable<AgentSessionResource> sessions = betaAgentsClient.listSessions(agentName, null, null, null, null, null);
            for (AgentSessionResource item : sessions) {
                System.out.printf("  - %s (status: %s)%n", item.getAgentSessionId(), item.getStatus());
            }

            System.out.printf("Deleting session with id: %s...%n", session.getAgentSessionId());
            betaAgentsClient.deleteSession(agentName, session.getAgentSessionId(), null);
            System.out.printf("Session with id: %s deleted.%n", session.getAgentSessionId());
        } finally {
            HostedAgentsSampleUtils.cleanup(agentsClient, betaAgentsClient, agentName, resources);
        }
    }
}
