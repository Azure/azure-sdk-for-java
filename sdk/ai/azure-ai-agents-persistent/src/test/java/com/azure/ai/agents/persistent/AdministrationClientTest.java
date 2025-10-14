// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.agents.persistent;

import com.azure.ai.agents.persistent.models.CreateAgentOptions;
import com.azure.ai.agents.persistent.models.PersistentAgent;
import com.azure.ai.agents.persistent.models.UpdateAgentOptions;
import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.PagedIterable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static com.azure.ai.agents.persistent.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;
import static com.azure.ai.agents.persistent.TestUtils.size;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class AdministrationClientTest extends ClientTestBase {

    private PersistentAgentsClientBuilder clientBuilder;
    private PersistentAgentsAdministrationClient administrationClient;
    private PersistentAgent agent;

    private PersistentAgent createAgent(String agentName) {
        CreateAgentOptions options
            = new CreateAgentOptions("gpt-4o-mini").setName(agentName).setInstructions("You are a helpful agent");
        PersistentAgent createdAgent = administrationClient.createAgent(options);
        assertNotNull(createdAgent, "Persistent agent should not be null");
        return createdAgent;
    }

    private void setup(HttpClient httpClient) {
        clientBuilder = getClientBuilder(httpClient);
        PersistentAgentsClient agentsClient = clientBuilder.buildClient();
        administrationClient = agentsClient.getPersistentAgentsAdministrationClient();
        agent = createAgent("TestAgent");
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.agents.persistent.TestUtils#getTestParameters")
    public void testCreateAgent(HttpClient httpClient) {
        setup(httpClient);
        assertAgent(agent);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.agents.persistent.TestUtils#getTestParameters")
    public void testListAgents(HttpClient httpClient) {
        setup(httpClient);

        // Validate the agent listing
        PagedIterable<PersistentAgent> agents = administrationClient.listAgents();

        assertNotNull(agents, "Agent list should not be null");
        assertTrue(size(agents) > 0, "Agent list should not be empty");
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.agents.persistent.TestUtils#getTestParameters")
    public void testGetAgent(HttpClient httpClient) {
        setup(httpClient);

        PersistentAgent retrievedAgent = administrationClient.getAgent(agent.getId());
        assertAgent(retrievedAgent);
        assertTrue(retrievedAgent.getId().equals(agent.getId()), "Retrieved agent ID should match created agent ID");
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.agents.persistent.TestUtils#getTestParameters")
    public void testUpdateAgent(HttpClient httpClient) {
        setup(httpClient);

        UpdateAgentOptions updateOptions
            = new UpdateAgentOptions(agent.getId()).setInstructions("Updated instructions for the agent");
        PersistentAgent updatedAgent = administrationClient.updateAgent(updateOptions);
        assertAgent(updatedAgent);
        assertTrue(updatedAgent.getInstructions().equals("Updated instructions for the agent"),
            "Updated agent instructions should match");
        assertTrue(updatedAgent.getId().equals(agent.getId()), "Updated agent ID should match created agent ID");
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.agents.persistent.TestUtils#getTestParameters")
    public void testDeleteAgent(HttpClient httpClient) {
        setup(httpClient);
        try {
            administrationClient.deleteAgent(agent.getId());
        } catch (Exception e) {
            // If deletion fails, we still want to assert that the agent was created
            fail("Agent deletion failed: " + e.getMessage());
        }
        agent = null;
    }

    @AfterEach
    public void cleanup() {
        if (agent != null) {
            try {
                administrationClient.deleteAgent(agent.getId());
            } catch (Exception e) {
                System.out.println("Failed to clean up agent: " + e.getMessage());
            }
        }
    }
}
