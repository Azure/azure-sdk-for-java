// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.tools;

import com.azure.ai.agents.AgentsAsyncClient;
import com.azure.ai.agents.AgentsClient;
import com.azure.ai.agents.AgentsClientBuilder;
import com.azure.ai.agents.AgentsServiceVersion;
import com.azure.ai.agents.ClientTestBase;
import com.azure.ai.agents.ResponsesAsyncClient;
import com.azure.ai.agents.ResponsesClient;
import com.azure.core.http.HttpClient;
import com.azure.core.test.TestMode;
import com.azure.core.util.Configuration;
import com.azure.ai.agents.models.AgentReference;
import com.azure.ai.agents.models.AgentVersionDetails;
import com.azure.ai.agents.models.AzureCreateResponseOptions;
import com.azure.ai.agents.models.FabricIQPreviewTool;
import com.azure.ai.agents.models.PromptAgentDefinition;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;
import com.openai.models.responses.ResponseStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static com.azure.core.test.TestProxyTestBase.getHttpClients;

public class FabricIQSamplesTests extends ClientTestBase {
    private static final String DISPLAY_NAME_WITH_ARGUMENTS = "{displayName} with [{arguments}]";
    private static final String DEFAULT_USER_INPUT = "Show weather events in Texas.";

    static Stream<Arguments> getTestParameters() {
        List<Arguments> argumentsList = new ArrayList<>();
        getHttpClients().forEach(httpClient -> argumentsList.add(Arguments.of(httpClient, AgentsServiceVersion.V1)));
        return argumentsList.stream();
    }

    @Timeout(value = 5, unit = TimeUnit.MINUTES)
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void fabricIqSyncSample(HttpClient httpClient, AgentsServiceVersion serviceVersion) {
        AgentsClientBuilder builder = getClientBuilder(httpClient, serviceVersion);
        AgentsClient agentsClient = builder.buildAgentsClient();
        ResponsesClient responsesClient = builder.buildResponsesClient();

        String agentName = testResourceNamer.randomName("fabric-iq-sync-", 40);
        AgentVersionDetails agent = null;

        try {
            agent = agentsClient.createAgentVersion(agentName, createAgentDefinition());
            Assertions.assertNotNull(agent);

            AgentReference agentReference = new AgentReference(agent.getName()).setVersion(agent.getVersion());
            Response response = responsesClient.createAzureResponse(
                new AzureCreateResponseOptions().setAgentReference(agentReference),
                ResponseCreateParams.builder().input(getUserInput()));

            assertCompletedResponse(response);
        } finally {
            if (agent != null) {
                agentsClient.deleteAgentVersion(agent.getName(), agent.getVersion());
            }
        }
    }

    @Timeout(value = 5, unit = TimeUnit.MINUTES)
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void fabricIqAsyncSample(HttpClient httpClient, AgentsServiceVersion serviceVersion) {
        AgentsClientBuilder builder = getClientBuilder(httpClient, serviceVersion);
        AgentsAsyncClient agentsAsyncClient = builder.buildAgentsAsyncClient();
        ResponsesAsyncClient responsesAsyncClient = builder.buildResponsesAsyncClient();

        String agentName = testResourceNamer.randomName("fabric-iq-async-", 40);
        AgentVersionDetails agent = null;

        try {
            agent
                = agentsAsyncClient.createAgentVersion(agentName, createAgentDefinition()).block(Duration.ofMinutes(2));
            Assertions.assertNotNull(agent);

            AgentReference agentReference = new AgentReference(agent.getName()).setVersion(agent.getVersion());
            Response response
                = responsesAsyncClient
                    .createAzureResponse(new AzureCreateResponseOptions().setAgentReference(agentReference),
                        ResponseCreateParams.builder().input(getUserInput()))
                    .block(Duration.ofMinutes(3));

            assertCompletedResponse(response);
        } finally {
            if (agent != null) {
                agentsAsyncClient.deleteAgentVersion(agent.getName(), agent.getVersion()).block(Duration.ofMinutes(1));
            }
        }
    }

    private PromptAgentDefinition createAgentDefinition() {
        FabricIQPreviewTool fabricIqTool
            = new FabricIQPreviewTool(getRecordedConfig("FABRIC_IQ_PROJECT_CONNECTION_ID")).setServerLabel("fabric_iq")
                .setRequireApproval("never")
                .setName("fabric_iq_lookup")
                .setDescription("Use FabricIQ to answer questions grounded in enterprise data.");

        return new PromptAgentDefinition(getRecordedConfig("FOUNDRY_MODEL_NAME"))
            .setInstructions("Use the available Fabric IQ tools to answer questions and perform tasks.")
            .setTools(Collections.singletonList(fabricIqTool));
    }

    private String getRecordedConfig(String name) {
        if (getTestMode() == TestMode.PLAYBACK) {
            return testResourceNamer.recordValueFromConfig(name);
        }

        String value = Configuration.getGlobalConfiguration().get(name);
        if (getTestMode() == TestMode.RECORD) {
            testResourceNamer.recordValueFromConfig(name);
        }
        return value;
    }

    private static String getUserInput() {
        return Configuration.getGlobalConfiguration().get("FABRIC_IQ_USER_INPUT", DEFAULT_USER_INPUT);
    }

    private static void assertCompletedResponse(Response response) {
        Assertions.assertNotNull(response);
        Assertions.assertTrue(response.status().isPresent());
        Assertions.assertEquals(ResponseStatus.COMPLETED, response.status().get());
        Assertions.assertFalse(response.output().isEmpty());
        Assertions.assertTrue(response.output().stream().anyMatch(item -> item.isMessage()));
    }
}
