// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.tools;

import com.azure.ai.agents.AgentsAsyncClient;
import com.azure.ai.agents.AgentsClientBuilder;
import com.azure.ai.agents.AgentsServiceVersion;
import com.azure.ai.agents.ResponsesAsyncClient;
import com.azure.ai.agents.models.AgentReference;
import com.azure.ai.agents.models.AgentVersionDetails;
import com.azure.ai.agents.models.AzureCreateResponseOptions;
import com.azure.core.http.HttpClient;
import com.openai.models.responses.Response;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

public class FabricIQSamplesAsyncTests extends FabricIQSamplesTestBase {

    @Timeout(value = 5, unit = TimeUnit.MINUTES)
    @Disabled("getting 500 from service")
    @ResourceLock(FABRIC_IQ_RESOURCE_LOCK)
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void fabricIqAsyncSample(HttpClient httpClient, AgentsServiceVersion serviceVersion) {
        AgentsClientBuilder builder = getClientBuilder(httpClient, serviceVersion);
        AgentsAsyncClient agentsAsyncClient = builder.buildAgentsAsyncClient();
        ResponsesAsyncClient responsesAsyncClient = builder.buildResponsesAsyncClient();

        String agentName = testResourceNamer.randomName("fabric-iq-async-", 40);
        AgentVersionDetails agent
            = agentsAsyncClient.createAgentVersion(agentName, createAgentDefinition()).block(Duration.ofMinutes(2));
        Assertions.assertNotNull(agent);

        AgentReference agentReference = new AgentReference(agent.getName()).setVersion(agent.getVersion());
        Response response
            = responsesAsyncClient
                .createAzureResponse(new AzureCreateResponseOptions().setAgentReference(agentReference),
                    createResponseParams())
                .block(Duration.ofMinutes(3));

        assertCompletedResponse(response);
        agentsAsyncClient.deleteAgentVersion(agent.getName(), agent.getVersion()).block(Duration.ofMinutes(1));
    }
}
