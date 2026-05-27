// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.tools;

import com.azure.ai.agents.AgentsClient;
import com.azure.ai.agents.AgentsClientBuilder;
import com.azure.ai.agents.AgentsServiceVersion;
import com.azure.ai.agents.ResponsesClient;
import com.azure.core.http.HttpClient;
import com.azure.ai.agents.models.AgentReference;
import com.azure.ai.agents.models.AgentVersionDetails;
import com.azure.ai.agents.models.AzureCreateResponseOptions;
import com.openai.models.responses.Response;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.concurrent.TimeUnit;

public class FabricIQSamplesTests extends FabricIQSamplesTestBase {

    @Timeout(value = 5, unit = TimeUnit.MINUTES)
    @Disabled("getting 500 from service")
    @ResourceLock(FABRIC_IQ_RESOURCE_LOCK)
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void fabricIqSyncSample(HttpClient httpClient, AgentsServiceVersion serviceVersion) {
        AgentsClientBuilder builder = getClientBuilder(httpClient, serviceVersion);
        AgentsClient agentsClient = builder.buildAgentsClient();
        ResponsesClient responsesClient = builder.buildResponsesClient();

        String agentName = testResourceNamer.randomName("fabric-iq-sync-", 40);
        AgentVersionDetails agent = agentsClient.createAgentVersion(agentName, createAgentDefinition());
        Assertions.assertNotNull(agent);

        AgentReference agentReference = new AgentReference(agent.getName());
        Response response = responsesClient.createAzureResponse(
            new AzureCreateResponseOptions().setAgentReference(agentReference), createResponseParams());

        assertCompletedResponse(response);
        agentsClient.deleteAgentVersion(agent.getName(), agent.getVersion());
    }
}
