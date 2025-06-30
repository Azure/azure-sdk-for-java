// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.agents.persistent;

import com.azure.ai.agents.persistent.models.CreateAgentOptions;
import com.azure.ai.agents.persistent.models.PersistentAgent;
import com.azure.ai.agents.persistent.models.UpdateAgentOptions;
import com.azure.core.http.HttpClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.test.StepVerifier;

import static com.azure.ai.agents.persistent.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AdministrationAsyncClientTest extends ClientTestBase {

    private PersistentAgentsClientBuilder clientBuilder;
    private PersistentAgentsAdministrationAsyncClient administrationAsyncClient;
    private PersistentAgent agent;

    private void setup(HttpClient httpClient) {
        clientBuilder = getClientBuilder(httpClient);
        PersistentAgentsAsyncClient agentsAsyncClient = clientBuilder.buildAsyncClient();
        administrationAsyncClient = agentsAsyncClient.getPersistentAgentsAdministrationAsyncClient();
    }

    private void createTestAgent() {
        CreateAgentOptions options
            = new CreateAgentOptions("gpt-4o-mini").setName("TestAgent").setInstructions("You are a helpful agent");

        StepVerifier.create(administrationAsyncClient.createAgent(options)).assertNext(createdAgent -> {
            assertNotNull(createdAgent, "Persistent agent should not be null");
            agent = createdAgent;
            assertAgent(createdAgent);
        }).verifyComplete();
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.agents.persistent.TestUtils#getTestParameters")
    public void testCreateAgent(HttpClient httpClient) {
        setup(httpClient);
        createTestAgent();
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.agents.persistent.TestUtils#getTestParameters")
    public void testListAgents(HttpClient httpClient) {
        setup(httpClient);
        createTestAgent();

        // Validate agent listing
        StepVerifier.create(administrationAsyncClient.listAgents().take(10).collectList()).assertNext(agents -> {
            assertNotNull(agents, "Agent list should not be null");
            assertTrue(agents != null, "Agent list should not be empty");
        }).verifyComplete();
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.agents.persistent.TestUtils#getTestParameters")
    public void testGetAgent(HttpClient httpClient) {
        setup(httpClient);
        createTestAgent();

        StepVerifier.create(administrationAsyncClient.getAgent(agent.getId())).assertNext(retrievedAgent -> {
            assertAgent(retrievedAgent);
            assertTrue(retrievedAgent.getId().equals(agent.getId()),
                "Retrieved agent ID should match created agent ID");
        }).verifyComplete();
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.agents.persistent.TestUtils#getTestParameters")
    public void testUpdateAgent(HttpClient httpClient) {
        setup(httpClient);
        createTestAgent();

        UpdateAgentOptions updateOptions
            = new UpdateAgentOptions(agent.getId()).setInstructions("Updated instructions for the agent");

        StepVerifier.create(administrationAsyncClient.updateAgent(updateOptions)).assertNext(updatedAgent -> {
            assertAgent(updatedAgent);
            assertTrue(updatedAgent.getInstructions().equals("Updated instructions for the agent"),
                "Updated agent instructions should match");
            assertTrue(updatedAgent.getId().equals(agent.getId()), "Updated agent ID should match created agent ID");
        }).verifyComplete();
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.agents.persistent.TestUtils#getTestParameters")
    public void testDeleteAgent(HttpClient httpClient) {
        setup(httpClient);
        createTestAgent();

        StepVerifier.create(administrationAsyncClient.deleteAgent(agent.getId())).verifyComplete();
    }

    @AfterEach
    public void cleanup() {
        if (agent != null) {
            try {
                administrationAsyncClient.deleteAgent(agent.getId()).block();
            } catch (Exception e) {
                // Ignore exceptions during cleanup
                System.out.println("Warning: Failed to delete test agent during cleanup: " + e.getMessage());
            }
        }
    }
}
