// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.hostedagents;

import com.azure.ai.agents.AgentsClient;
import com.azure.ai.agents.BetaAgentsClient;
import com.azure.ai.agents.AgentsClientBuilder;
import com.azure.ai.agents.AgentsServiceVersion;
import com.azure.ai.agents.ClientTestBase;
import com.azure.ai.agents.hostedagents.utils.HostedAgentsSampleUtils;
import com.azure.ai.agents.hostedagents.utils.HostedAgentsSampleUtils.HostedAgentSessionResources;
import com.azure.ai.agents.models.AgentEndpointConfig;
import com.azure.ai.agents.models.AgentEndpointProtocol;
import com.azure.ai.agents.models.AgentSessionResource;
import com.azure.ai.agents.models.FixedRatioVersionSelectionRule;
import com.azure.ai.agents.models.SessionDirectoryEntry;
import com.azure.ai.agents.models.UpdateAgentDetailsOptions;
import com.azure.ai.agents.models.VersionSelector;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.test.TestMode;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Configuration;
import com.openai.client.OpenAIClient;
import com.openai.core.JsonValue;
import com.openai.models.responses.ResponseCreateParams;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static com.azure.core.test.TestProxyTestBase.getHttpClients;

public class HostedAgentContainerSamplesTests extends ClientTestBase {
    private static final String DISPLAY_NAME_WITH_ARGUMENTS = "{displayName} with [{arguments}]";
    private static final String REMOTE_FILE_PATH_1 = "/remote/data_file1.txt";
    private static final String REMOTE_FILE_PATH_2 = "/remote/data_file2.txt";

    static Stream<Arguments> getTestParameters() {
        List<Arguments> argumentsList = new ArrayList<>();
        getHttpClients().forEach(httpClient -> argumentsList.add(Arguments.of(httpClient, AgentsServiceVersion.V1)));
        return argumentsList.stream();
    }

    @Disabled("Recordings need to be refreshed for the composite Foundry-Features preview header.")
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void sessionsSample(HttpClient httpClient, AgentsServiceVersion serviceVersion) {
        AgentsClientBuilder builder = getClientBuilder(httpClient, serviceVersion);
        AgentsClient agentsClient = builder.buildAgentsClient();
        BetaAgentsClient betaAgentsClient = builder.beta().buildBetaAgentsClient();
        String image = getRequiredConfiguration("FOUNDRY_AGENT_CONTAINER_IMAGE");
        String agentName = HostedAgentsSampleUtils.SAMPLE_AGENT_NAME + "-sessions-test";

        HostedAgentSessionResources resources = null;
        try {
            resources = HostedAgentsSampleUtils.createAgentAndSession(agentsClient, betaAgentsClient, agentName, image);
            AgentSessionResource session = resources.getSession();

            AgentSessionResource fetched = betaAgentsClient.getSession(agentName, session.getAgentSessionId(), null);
            Assertions.assertNotNull(fetched);
            Assertions.assertEquals(session.getAgentSessionId(), fetched.getAgentSessionId());

            PagedIterable<AgentSessionResource> sessions
                = betaAgentsClient.listSessions(agentName, null, null, null, null, null);
            Assertions.assertTrue(
                sessions.stream().anyMatch(item -> session.getAgentSessionId().equals(item.getAgentSessionId())));

            try {
                betaAgentsClient.deleteSession(agentName, session.getAgentSessionId(), null);
            } catch (ResourceNotFoundException ignored) {
                // The session may already be deleted by the service.
            }
        } finally {
            HostedAgentsSampleUtils.cleanup(agentsClient, betaAgentsClient, agentName, resources);
        }
    }

    @Disabled("Recordings need to be refreshed for the composite Foundry-Features preview header.")
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void sessionFilesSample(HttpClient httpClient, AgentsServiceVersion serviceVersion) {
        AgentsClientBuilder builder = getClientBuilder(httpClient, serviceVersion);
        AgentsClient agentsClient = builder.buildAgentsClient();
        BetaAgentsClient betaAgentsClient = builder.beta().buildBetaAgentsClient();
        String image = getRequiredConfiguration("FOUNDRY_AGENT_CONTAINER_IMAGE");
        String agentName = HostedAgentsSampleUtils.SAMPLE_AGENT_NAME + "-files-test";

        HostedAgentSessionResources resources = null;
        try {
            resources = HostedAgentsSampleUtils.createAgentAndSession(agentsClient, betaAgentsClient, agentName, image);
            String sessionId = resources.getSession().getAgentSessionId();

            betaAgentsClient.uploadSessionFile(agentName, sessionId, REMOTE_FILE_PATH_1,
                BinaryData.fromString("Sample session file 1."), null);
            betaAgentsClient.uploadSessionFile(agentName, sessionId, REMOTE_FILE_PATH_2,
                BinaryData.fromString("Sample session file 2."), null);

            PagedIterable<SessionDirectoryEntry> files
                = betaAgentsClient.listSessionFiles(agentName, sessionId, "/remote", null, null, null, null, null);
            Assertions
                .assertTrue(files.stream().map(SessionDirectoryEntry::getName).anyMatch("data_file1.txt"::equals));

            BinaryData downloaded
                = betaAgentsClient.downloadSessionFile(agentName, sessionId, REMOTE_FILE_PATH_1, null);
            String fileContent = new String(downloaded.toBytes(), StandardCharsets.UTF_8);
            Assertions.assertEquals("Sample session file 1.", fileContent);

            betaAgentsClient.deleteSessionFile(agentName, sessionId, REMOTE_FILE_PATH_1, false, null);
            betaAgentsClient.deleteSessionFile(agentName, sessionId, REMOTE_FILE_PATH_2, false, null);
        } finally {
            HostedAgentsSampleUtils.cleanup(agentsClient, betaAgentsClient, agentName, resources);
        }
    }

    @Disabled("Agent-scoped OpenAI Responses invocation returns 400: API version not supported.")
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void agentEndpointSample(HttpClient httpClient, AgentsServiceVersion serviceVersion) {
        AgentsClientBuilder builder = getClientBuilder(httpClient, serviceVersion);
        AgentsClient agentsClient = builder.buildAgentsClient();
        BetaAgentsClient betaAgentsClient = builder.beta().buildBetaAgentsClient();
        String image = getRequiredConfiguration("FOUNDRY_AGENT_CONTAINER_IMAGE");
        String agentName = HostedAgentsSampleUtils.SAMPLE_AGENT_NAME + "-endpoint-test";

        HostedAgentSessionResources resources = null;
        try {
            resources = HostedAgentsSampleUtils.createAgentAndSession(agentsClient, betaAgentsClient, agentName, image);
            configureAgentEndpoint(betaAgentsClient, agentName, resources);

            OpenAIClient openAIClient = builder.buildAgentScopedOpenAIClient(agentName);
            com.openai.models.responses.Response response = openAIClient.responses()
                .create(ResponseCreateParams.builder()
                    .input("What is the size of France in square miles?")
                    .putAdditionalBodyProperty("agent_session_id",
                        JsonValue.from(resources.getSession().getAgentSessionId()))
                    .build());

            Assertions.assertNotNull(response);
            Assertions.assertFalse(response.output().isEmpty());
        } finally {
            HostedAgentsSampleUtils.cleanup(agentsClient, betaAgentsClient, agentName, resources);
        }
    }

    @Disabled("Agent-scoped OpenAI Responses invocation returns 400: API version not supported.")
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void sessionLogStreamSample(HttpClient httpClient, AgentsServiceVersion serviceVersion) throws IOException {
        AgentsClientBuilder builder = getClientBuilder(httpClient, serviceVersion);
        AgentsClient agentsClient = builder.buildAgentsClient();
        BetaAgentsClient betaAgentsClient = builder.beta().buildBetaAgentsClient();
        String image = getRequiredConfiguration("FOUNDRY_AGENT_CONTAINER_IMAGE");
        String agentName = HostedAgentsSampleUtils.SAMPLE_AGENT_NAME + "-logs-test";

        HostedAgentSessionResources resources = null;
        try {
            resources = HostedAgentsSampleUtils.createAgentAndSession(agentsClient, betaAgentsClient, agentName, image);
            configureAgentEndpoint(betaAgentsClient, agentName, resources);

            OpenAIClient openAIClient = builder.buildAgentScopedOpenAIClient(agentName);
            com.openai.models.responses.Response openAIResponse = openAIClient.responses()
                .create(ResponseCreateParams.builder()
                    .input("Say hello in one short sentence.")
                    .putAdditionalBodyProperty("agent_session_id",
                        JsonValue.from(resources.getSession().getAgentSessionId()))
                    .build());
            Assertions.assertNotNull(openAIResponse);

            com.azure.core.http.rest.Response<BinaryData> rawStream
                = betaAgentsClient.getSessionLogStreamWithResponse(agentName, resources.getAgent().getVersion(),
                    resources.getSession().getAgentSessionId(), new RequestOptions());
            Assertions.assertNotNull(rawStream.getValue());
            HostedAgentsSampleUtils.printSseFrames(rawStream.getValue(), 5);
        } finally {
            HostedAgentsSampleUtils.cleanup(agentsClient, betaAgentsClient, agentName, resources);
        }
    }

    private static void configureAgentEndpoint(BetaAgentsClient betaAgentsClient, String agentName,
        HostedAgentSessionResources resources) {
        AgentEndpointConfig endpointConfig = new AgentEndpointConfig()
            .setVersionSelector(new VersionSelector().setVersionSelectionRules(Collections.singletonList(
                new FixedRatioVersionSelectionRule(100).setAgentVersion(resources.getAgent().getVersion()))))
            .setProtocols(Collections.singletonList(AgentEndpointProtocol.RESPONSES));

        betaAgentsClient.updateAgentDetails(agentName,
            new UpdateAgentDetailsOptions().setAgentEndpoint(endpointConfig));
    }

    private String getRequiredConfiguration(String name) {
        if (getTestMode() == TestMode.PLAYBACK && "FOUNDRY_AGENT_CONTAINER_IMAGE".equals(name)) {
            return "REDACTED";
        }

        String value = Configuration.getGlobalConfiguration().get(name);
        Assertions.assertNotNull(value, name + " must be set.");
        return value;
    }
}
