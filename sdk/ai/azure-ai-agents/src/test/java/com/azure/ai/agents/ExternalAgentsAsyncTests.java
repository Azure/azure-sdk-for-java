// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.agents;

import com.azure.ai.agents.models.AgentKind;
import com.azure.ai.agents.models.ExternalAgentDefinition;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.http.HttpClient;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static com.azure.ai.agents.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ExternalAgentsAsyncTests extends ClientTestBase {
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.agents.TestUtils#getTestParameters")
    public void externalAgentLifecycle(HttpClient httpClient, AgentsServiceVersion serviceVersion) {
        AgentsAsyncClient client = getAgentsAsyncClient(httpClient, serviceVersion);
        String name = testResourceNamer.randomName("external-agent-async-", 63);
        String version
            = client.createAgentVersion(name, new ExternalAgentDefinition().setOtelAgentId(name)).doOnNext(created -> {
                assertEquals(name, created.getName());
                assertInstanceOf(ExternalAgentDefinition.class, created.getDefinition());
            }).block().getVersion();
        try {
            assertEquals(version, client.getAgentVersionDetails(name, version).block().getVersion());
            assertEquals(name, client.getAgent(name).block().getName());
            assertTrue(client.listAgentVersions(name).any(item -> version.equals(item.getVersion())).block());
            assertTrue(client.listAgents(AgentKind.EXTERNAL, null, null, null, null)
                .any(item -> name.equals(item.getName()))
                .block());
        } finally {
            client.deleteAgent(name).block();
        }
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.agents.TestUtils#getTestParameters")
    public void deleteExternalAgentVersion(HttpClient httpClient, AgentsServiceVersion serviceVersion) {
        AgentsAsyncClient client = getAgentsAsyncClient(httpClient, serviceVersion);
        String name = testResourceNamer.randomName("external-version-async-", 63);
        String version
            = client.createAgentVersion(name, new ExternalAgentDefinition().setOtelAgentId(name)).block().getVersion();
        try {
            client.deleteAgentVersion(name, version).block();
        } finally {
            try {
                client.deleteAgent(name).block();
            } catch (ResourceNotFoundException ignored) {
                // Removing the final version can remove its agent.
            }
        }
    }
}
