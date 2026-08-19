// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.hostedagents;

import com.azure.ai.agents.AgentsClient;
import com.azure.ai.agents.AgentsClientBuilder;
import com.azure.ai.agents.hostedagents.utils.HostedAgentsSampleUtils;
import com.azure.ai.agents.hostedagents.utils.HostedAgentsSampleUtils.HostedAgentSessionResources;
import com.azure.ai.agents.models.AgentSessionResource;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;

/**
 * This sample demonstrates disabling and enabling a hosted agent.
 *
 * <p>When disabled, a hosted agent cannot accept new sessions. Before running, set these environment variables:</p>
 * <ul>
 *   <li>FOUNDRY_PROJECT_ENDPOINT - The Azure AI Foundry project endpoint.</li>
 *   <li>FOUNDRY_AGENT_CONTAINER_IMAGE - The hosted-agent container image.</li>
 * </ul>
 */
public class HostedAgentDisableSample {
    public static void main(String[] args) {
        String endpoint = Configuration.getGlobalConfiguration().get("FOUNDRY_PROJECT_ENDPOINT");
        String image = Configuration.getGlobalConfiguration().get("FOUNDRY_AGENT_CONTAINER_IMAGE");
        String agentName = HostedAgentsSampleUtils.SAMPLE_AGENT_NAME;

        AgentsClientBuilder builder = new AgentsClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint(endpoint)
            .allowPreview(true);
        AgentsClient agentsClient = builder.buildAgentsClient();

        HostedAgentSessionResources resources = null;
        AgentSessionResource additionalSession = null;
        boolean agentDisabled = false;
        try {
            try {
                agentsClient.enableAgent(agentName);
            } catch (ResourceNotFoundException ignored) {
                // The sample agent does not already exist.
            }

            resources = HostedAgentsSampleUtils.createAgentAndSession(agentsClient, agentName, image);
            String agentVersion = resources.getAgent().getVersion();

            // BEGIN: com.azure.ai.agents.hostedagents.HostedAgentDisableSample.disableAgent

            agentsClient.disableAgent(agentName);
            agentDisabled = true;
            try {
                additionalSession = HostedAgentsSampleUtils.createSession(agentsClient, agentName, agentVersion);
                throw new IllegalStateException("A disabled agent unexpectedly created a session.");
            } catch (HttpResponseException ex) {
                if (ex.getResponse().getStatusCode() != 403) {
                    throw ex;
                }
                System.out.println("Creating a session for the disabled agent failed with HTTP 403 as expected.");
            }

            // END: com.azure.ai.agents.hostedagents.HostedAgentDisableSample.disableAgent
            // BEGIN: com.azure.ai.agents.hostedagents.HostedAgentDisableSample.enableAgent

            agentsClient.enableAgent(agentName);
            agentDisabled = false;
            additionalSession = HostedAgentsSampleUtils.createSession(agentsClient, agentName, agentVersion);

            // END: com.azure.ai.agents.hostedagents.HostedAgentDisableSample.enableAgent
        } finally {
            if (agentDisabled) {
                try {
                    agentsClient.enableAgent(agentName);
                } catch (ResourceNotFoundException ignored) {
                    // The sample agent may not have been created.
                } catch (RuntimeException ex) {
                    System.err.println("Unable to restore the agent to the enabled state: " + ex.getMessage());
                }
            }

            if (additionalSession != null) {
                try {
                    agentsClient.deleteSession(agentName, additionalSession.getAgentSessionId());
                } catch (ResourceNotFoundException ignored) {
                    // The sample may have already deleted the session.
                }
            }
            HostedAgentsSampleUtils.cleanup(agentsClient, agentName, resources);
        }
    }
}
