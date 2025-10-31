// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents;

import com.azure.ai.agents.models.AgentContainerOperationObject;
import com.azure.ai.agents.models.PromptAgentDefinition;
import com.azure.core.http.HttpClient;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.test.StepVerifier;
import reactor.util.function.Tuples;

import static com.azure.ai.agents.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;
import static org.junit.jupiter.api.Assertions.*;

@Disabled("Disabled for lack of recordings. Needs to be enabled on the Public Preview release.")
public class AgentsAsyncTests extends ClientTestBase {

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.agents.TestUtils#getTestParameters")
    public void basicCRUDOperations(HttpClient httpClient, AgentsServiceVersion serviceVersion) {
        AgentsAsyncClient client = getAgentsAsyncClient(httpClient, serviceVersion);
        String agentName = "test_agent_java";
        String agentModel = "gpt-4o";

        PromptAgentDefinition request = new PromptAgentDefinition(agentModel);

        StepVerifier.create(client.createAgentVersion(agentName, request).flatMap(created -> {
            // Assertions for created agent version
            assertNotNull(created);
            assertNotNull(created.getId());
            assertEquals(agentName, created.getName());

            return client.getAgent(agentName).doOnNext(retrieved -> {
                // Assertions for retrieved agent
                assertNotNull(retrieved);
                assertNotNull(retrieved.getId());
                assertNotNull(retrieved.getName());
                assertEquals(created.getId(), retrieved.getVersions().getLatest().getId());
                assertEquals(created.getName(), retrieved.getName());
            })
                .thenMany(client.listAgents())
                .filter(agent -> agent.getName().equals(agentName))
                .next()
                .doOnNext(agent -> {
                    assertEquals(agent.getVersions().getLatest().getId(), created.getId());
                    assertEquals(agent.getVersions().getLatest().getName(), created.getName());
                })
                .then(client.deleteAgentVersion(agentName, created.getVersion()));
        })).assertNext(deletedAgent -> {
            assertEquals(agentName, deletedAgent.getName());
            assertTrue(deletedAgent.isDeleted());
        }).verifyComplete();
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.agents.TestUtils#getTestParameters")
    public void basicVersionedAgentCRUDOperations(HttpClient httpClient, AgentsServiceVersion serviceVersion) {
        AgentsAsyncClient client = getAgentsAsyncClient(httpClient, serviceVersion);
        String agentName = "test_agent_java";
        String agentModel = "gpt-4o";

        PromptAgentDefinition request = new PromptAgentDefinition(agentModel);

        StepVerifier.create(client.createAgentVersion(agentName, request).flatMap(created -> {
            // Assertions for created agent version
            assertNotNull(created);
            assertNotNull(created.getId());
            assertEquals(agentName, created.getName());

            return client.getAgentVersion(agentName, created.getVersion()).doOnNext(retrieved -> {
                // Assertions for retrieved agent version
                assertNotNull(retrieved);
                assertNotNull(retrieved.getId());
                assertNotNull(retrieved.getName());
                assertEquals(created.getId(), retrieved.getId());
                assertEquals(created.getName(), retrieved.getName());
            })
                .thenMany(client.listAgentVersions(agentName))
                .filter(agentVersion -> agentVersion.getVersion().equals(created.getVersion()))
                .next()
                .doOnNext(agentVersion -> {
                    assertEquals(agentVersion.getId(), created.getId());
                    assertEquals(agentVersion.getName(), created.getName());
                })
                .then(client.deleteAgentVersion(agentName, created.getVersion()));
        })).assertNext(deletedAgent -> {
            assertEquals(agentName, deletedAgent.getName());
            assertTrue(deletedAgent.isDeleted());
        }).verifyComplete();
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.agents.TestUtils#getTestParameters")
    public void operationOperations(HttpClient httpClient, AgentsServiceVersion serviceVersion) {
        AgentsAsyncClient client = getAgentsAsyncClient(httpClient, serviceVersion);
        String agentName = "test_agent_java";
        String agentModel = "gpt-4o";

        PromptAgentDefinition request = new PromptAgentDefinition(agentModel);

        StepVerifier.create(client.createAgentVersion(agentName, request).flatMap(created -> {
            // Assertions for created agent version
            assertNotNull(created);
            assertNotNull(created.getId());
            assertEquals(agentName, created.getName());

            return client.listAgentContainerOperations(created.getId()).doOnNext(retrieved -> {
                // Assertions for retrieved operation
                assertNotNull(retrieved);
                assertNotNull(retrieved.getId());
                assertEquals(created.getId(), retrieved.getId());
            })
                .flatMap(operation -> client.getAgentContainerOperation(operation.getAgentId(), operation.getId())
                    .map(it -> Tuples.of(operation, it)))
                .doOnNext(tuple -> {
                    AgentContainerOperationObject operation = tuple.getT1();
                    AgentContainerOperationObject retrievedOperation = tuple.getT2();
                    assertEquals(operation.getId(), retrievedOperation.getId());
                    assertEquals(operation.getAgentId(), retrievedOperation.getAgentId());
                    assertEquals(operation.getStatus(), retrievedOperation.getStatus());
                })
                .then(client.deleteAgentVersion(created.getName(), created.getVersion()));
        })).assertNext(deletedAgent -> {
            assertEquals(agentName, deletedAgent.getName());
            assertTrue(deletedAgent.isDeleted());
        }).verifyComplete();
    }
}
