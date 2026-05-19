// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.tools;

import com.azure.ai.agents.AgentsAsyncClient;
import com.azure.ai.agents.AgentsClient;
import com.azure.ai.agents.AgentsServiceVersion;
import com.azure.ai.agents.ClientTestBase;
import com.azure.ai.agents.ResponsesAsyncClient;
import com.azure.ai.agents.ResponsesClient;
import com.azure.ai.agents.models.AgentReference;
import com.azure.ai.agents.models.AgentVersionDetails;
import com.azure.ai.agents.models.AzureCreateResponseOptions;
import com.azure.ai.agents.models.FabricIQPreviewTool;
import com.azure.ai.agents.models.PromptAgentDefinition;
import com.azure.core.http.HttpClient;
import com.azure.core.util.Configuration;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import static com.azure.core.test.TestProxyTestBase.getHttpClients;

@Disabled("Requires FABRIC_IQ_PROJECT_CONNECTION_ID, which is missing from the current Java work resources.")
public class FabricIQSamplesTests extends ClientTestBase {
    private static final String DISPLAY_NAME_WITH_ARGUMENTS = "{displayName} with [{arguments}]";

    static Stream<Arguments> getTestParameters() {
        List<Arguments> argumentsList = new ArrayList<>();
        getHttpClients().forEach(httpClient -> argumentsList.add(Arguments.of(httpClient, AgentsServiceVersion.V1)));
        return argumentsList.stream();
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void fabricIqSyncSample(HttpClient httpClient, AgentsServiceVersion serviceVersion) {
        AgentsClient agentsClient = getClientBuilder(httpClient, serviceVersion).buildAgentsClient();
        ResponsesClient responsesClient = getClientBuilder(httpClient, serviceVersion).buildResponsesClient();
        String agentName = "fabric-iq-agent-test";

        AgentVersionDetails agent = agentsClient.createAgentVersion(agentName, createAgentDefinition());
        Assertions.assertNotNull(agent);
        Assertions.assertEquals(agentName, agent.getName());

        try {
            AgentReference agentReference = new AgentReference(agent.getName()).setVersion(agent.getVersion());
            Response response = responsesClient.createAzureResponse(
                new AzureCreateResponseOptions().setAgentReference(agentReference),
                ResponseCreateParams.builder().input("Use FabricIQ to summarize the available enterprise context."));

            Assertions.assertNotNull(response);
            Assertions.assertFalse(response.output().isEmpty());
        } finally {
            agentsClient.deleteAgentVersion(agent.getName(), agent.getVersion());
        }
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void fabricIqAsyncSample(HttpClient httpClient, AgentsServiceVersion serviceVersion) {
        AgentsAsyncClient agentsAsyncClient = getClientBuilder(httpClient, serviceVersion).buildAgentsAsyncClient();
        ResponsesAsyncClient responsesAsyncClient
            = getClientBuilder(httpClient, serviceVersion).buildResponsesAsyncClient();
        String agentName = "fabric-iq-async-agent-test";
        AtomicReference<AgentVersionDetails> agentRef = new AtomicReference<>();

        Mono<Void> testFlow
            = agentsAsyncClient.createAgentVersion(agentName, createAgentDefinition()).flatMap(agent -> {
                agentRef.set(agent);
                Assertions.assertEquals(agentName, agent.getName());
                AgentReference agentReference = new AgentReference(agent.getName()).setVersion(agent.getVersion());

                return responsesAsyncClient.createAzureResponse(
                    new AzureCreateResponseOptions().setAgentReference(agentReference), ResponseCreateParams.builder()
                        .input("Use FabricIQ to summarize the available enterprise context."));
            }).doOnNext(response -> {
                Assertions.assertNotNull(response);
                Assertions.assertFalse(response.output().isEmpty());
            }).then(Mono.defer(() -> {
                AgentVersionDetails agent = agentRef.get();
                if (agent == null) {
                    return Mono.empty();
                }
                return agentsAsyncClient.deleteAgentVersion(agent.getName(), agent.getVersion());
            }));

        StepVerifier.create(testFlow).verifyComplete();
    }

    private static PromptAgentDefinition createAgentDefinition() {
        String model = getRequiredConfiguration("FOUNDRY_MODEL_NAME");
        String fabricIqConnectionId = getRequiredConfiguration("FABRIC_IQ_PROJECT_CONNECTION_ID");

        FabricIQPreviewTool fabricIqTool = new FabricIQPreviewTool(fabricIqConnectionId).setServerLabel("fabric_iq")
            .setRequireApproval("never")
            .setName("fabric_iq_lookup")
            .setDescription("Use FabricIQ to answer questions grounded in enterprise data.");

        return new PromptAgentDefinition(model)
            .setInstructions("You are a data assistant that can use FabricIQ for grounded enterprise answers.")
            .setTools(Collections.singletonList(fabricIqTool));
    }

    private static String getRequiredConfiguration(String name) {
        String value = Configuration.getGlobalConfiguration().get(name);
        Assertions.assertNotNull(value, name + " must be set.");
        return value;
    }
}
