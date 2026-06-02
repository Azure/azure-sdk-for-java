// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.hostedagents;

import com.azure.ai.agents.AgentsClient;
import com.azure.ai.agents.AgentsClientBuilder;
import com.azure.ai.agents.hostedagents.HostedAgentsSampleUtils.HostedAgentSessionResources;
import com.azure.ai.agents.models.AgentDefinitionOptInKeys;
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

        AgentsClient agentsClient = new AgentsClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint(endpoint)
            .buildAgentsClient();

        HostedAgentSessionResources resources = null;
        try {
            resources = HostedAgentsSampleUtils.createAgentAndSession(agentsClient, agentName, image);
            AgentSessionResource session = resources.getSession();

            AgentSessionResource fetched = agentsClient.getSession(agentName, session.getAgentSessionId(),
                AgentDefinitionOptInKeys.HOSTED_AGENTS_V1_PREVIEW, null);
            System.out.printf("Retrieved session (id: %s, status: %s)%n", fetched.getAgentSessionId(),
                fetched.getStatus());

            System.out.println("Listing sessions for the agent...");
            PagedIterable<AgentSessionResource> sessions = agentsClient.listSessions(agentName,
                AgentDefinitionOptInKeys.HOSTED_AGENTS_V1_PREVIEW, null, null, null, null, null);
            for (AgentSessionResource item : sessions) {
                System.out.printf("  - %s (status: %s)%n", item.getAgentSessionId(), item.getStatus());
            }

            System.out.printf("Deleting session with id: %s...%n", session.getAgentSessionId());
            agentsClient.deleteSession(agentName, session.getAgentSessionId(),
                AgentDefinitionOptInKeys.HOSTED_AGENTS_V1_PREVIEW, null);
            System.out.printf("Session with id: %s deleted.%n", session.getAgentSessionId());
        } finally {
            HostedAgentsSampleUtils.cleanup(agentsClient, agentName, resources);
        }
    }
}
