// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.agents;

import com.azure.ai.agents.models.AgentDetails;
import com.azure.ai.agents.models.AgentKind;
import com.azure.ai.agents.models.AgentVersionDetails;
import com.azure.ai.agents.models.ExternalAgentDefinition;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.http.HttpClient;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static com.azure.ai.agents.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ExternalAgentsTests extends ClientTestBase {
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.agents.TestUtils#getTestParameters")
    public void externalAgentLifecycle(HttpClient httpClient, AgentsServiceVersion serviceVersion) {
        AgentsClient client = getAgentsSyncClient(httpClient, serviceVersion);
        String name = testResourceNamer.randomName("external-agent-", 63);
        AgentVersionDetails created
            = client.createAgentVersion(name, new ExternalAgentDefinition().setOtelAgentId(name));
        try {
            assertEquals(name, created.getName());
            assertInstanceOf(ExternalAgentDefinition.class, created.getDefinition());
            AgentVersionDetails version = client.getAgentVersionDetails(name, created.getVersion());
            assertEquals(created.getVersion(), version.getVersion());
            assertEquals(name, client.getAgent(name).getName());

            assertTrue(client.listAgentVersions(name)
                .stream()
                .anyMatch(item -> created.getVersion().equals(item.getVersion())));
            assertTrue(client.listAgents(AgentKind.EXTERNAL, null, null, null, null)
                .stream()
                .map(AgentDetails::getName)
                .anyMatch(name::equals));

        } finally {
            client.deleteAgent(name);
        }
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.agents.TestUtils#getTestParameters")
    public void deleteExternalAgentVersion(HttpClient httpClient, AgentsServiceVersion serviceVersion) {
        AgentsClient client = getAgentsSyncClient(httpClient, serviceVersion);
        String name = testResourceNamer.randomName("external-version-", 63);
        String version
            = client.createAgentVersion(name, new ExternalAgentDefinition().setOtelAgentId(name)).getVersion();
        try {
            client.deleteAgentVersion(name, version);
        } finally {
            try {
                client.deleteAgent(name);
            } catch (ResourceNotFoundException ignored) {
                // Removing the final version can remove its agent.
            }
        }
    }
}
