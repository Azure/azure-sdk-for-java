// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.hostedagents;

import com.azure.ai.agents.AgentsClient;
import com.azure.ai.agents.AgentsClientBuilder;
import com.azure.ai.agents.AgentsServiceVersion;
import com.azure.ai.agents.ClientTestBase;
import com.azure.ai.agents.models.AgentEndpointConfig;
import com.azure.ai.agents.models.AgentEndpointProtocol;
import com.azure.ai.agents.models.AgentSessionResource;
import com.azure.ai.agents.models.AgentVersionDetails;
import com.azure.ai.agents.models.AgentVersionStatus;
import com.azure.ai.agents.models.ContainerConfiguration;
import com.azure.ai.agents.models.CreateAgentVersionInput;
import com.azure.ai.agents.models.FixedRatioVersionSelectionRule;
import com.azure.ai.agents.models.HostedAgentDefinition;
import com.azure.ai.agents.models.ProtocolVersionRecord;
import com.azure.ai.agents.models.ProtocolConfiguration;
import com.azure.ai.agents.models.ResponsesProtocolConfiguration;
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static com.azure.core.test.TestProxyTestBase.getHttpClients;

@Disabled("TODO: re-record once service no longer requires Foundry-Features opt-in keys for these operations.")
public class HostedAgentContainerSamplesTests extends ClientTestBase {
    private static final String DISPLAY_NAME_WITH_ARGUMENTS = "{displayName} with [{arguments}]";
    private static final String REMOTE_FILE_PATH_1 = "/remote/data_file1.txt";
    private static final String REMOTE_FILE_PATH_2 = "/remote/data_file2.txt";
    private static final String TEST_AGENT_NAME = "java-hosted-agent-test";
    private static final int MAX_POLL_ATTEMPTS = 60;
    private static final Duration POLL_INTERVAL = Duration.ofSeconds(10);
    private static final Duration STREAM_READ_TIMEOUT = Duration.ofSeconds(3);

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
        String image = getRequiredConfiguration("FOUNDRY_AGENT_CONTAINER_IMAGE");
        String agentName = TEST_AGENT_NAME + "-sessions";

        HostedAgentSessionResources resources = null;
        try {
            resources = createAgentAndSession(agentsClient, agentName, image);
            AgentSessionResource session = resources.getSession();

            AgentSessionResource fetched = agentsClient.getSession(agentName, session.getAgentSessionId());
            Assertions.assertNotNull(fetched);
            Assertions.assertEquals(session.getAgentSessionId(), fetched.getAgentSessionId());

            PagedIterable<AgentSessionResource> sessions = agentsClient.listSessions(agentName, null, null, null, null);
            Assertions.assertTrue(
                sessions.stream().anyMatch(item -> session.getAgentSessionId().equals(item.getAgentSessionId())));

            try {
                agentsClient.deleteSession(agentName, session.getAgentSessionId());
            } catch (ResourceNotFoundException ignored) {
                // The session may already be deleted by the service.
            }
        } finally {
            cleanup(agentsClient, agentName, resources);
        }
    }

    @Disabled("Recordings need to be refreshed for the composite Foundry-Features preview header.")
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void sessionFilesSample(HttpClient httpClient, AgentsServiceVersion serviceVersion) {
        AgentsClientBuilder builder = getClientBuilder(httpClient, serviceVersion);
        AgentsClient agentsClient = builder.buildAgentsClient();
        String image = getRequiredConfiguration("FOUNDRY_AGENT_CONTAINER_IMAGE");
        String agentName = TEST_AGENT_NAME + "-files";

        HostedAgentSessionResources resources = null;
        try {
            resources = createAgentAndSession(agentsClient, agentName, image);
            String sessionId = resources.getSession().getAgentSessionId();

            agentsClient.uploadSessionFile(agentName, sessionId, REMOTE_FILE_PATH_1,
                BinaryData.fromString("Sample session file 1."));
            agentsClient.uploadSessionFile(agentName, sessionId, REMOTE_FILE_PATH_2,
                BinaryData.fromString("Sample session file 2."));

            PagedIterable<SessionDirectoryEntry> files
                = agentsClient.listSessionFiles(agentName, sessionId, "/remote", null, null, null, null);
            Assertions
                .assertTrue(files.stream().map(SessionDirectoryEntry::getName).anyMatch("data_file1.txt"::equals));

            BinaryData downloaded = agentsClient.downloadSessionFile(agentName, sessionId, REMOTE_FILE_PATH_1);
            String fileContent = new String(downloaded.toBytes(), StandardCharsets.UTF_8);
            Assertions.assertEquals("Sample session file 1.", fileContent);

            agentsClient.deleteSessionFile(agentName, sessionId, REMOTE_FILE_PATH_1, false);
            agentsClient.deleteSessionFile(agentName, sessionId, REMOTE_FILE_PATH_2, false);
        } finally {
            cleanup(agentsClient, agentName, resources);
        }
    }

    @Disabled("Agent-scoped OpenAI Responses invocation returns 400: API version not supported.")
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void agentEndpointSample(HttpClient httpClient, AgentsServiceVersion serviceVersion) {
        AgentsClientBuilder builder = getClientBuilder(httpClient, serviceVersion);
        AgentsClient agentsClient = builder.buildAgentsClient();
        String image = getRequiredConfiguration("FOUNDRY_AGENT_CONTAINER_IMAGE");
        String agentName = TEST_AGENT_NAME + "-endpoint";

        HostedAgentSessionResources resources = null;
        try {
            resources = createAgentAndSession(agentsClient, agentName, image);
            configureAgentEndpoint(agentsClient, agentName, resources);

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
            cleanup(agentsClient, agentName, resources);
        }
    }

    @Disabled("Agent-scoped OpenAI Responses invocation returns 400: API version not supported.")
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void sessionLogStreamSample(HttpClient httpClient, AgentsServiceVersion serviceVersion) throws IOException {
        AgentsClientBuilder builder = getClientBuilder(httpClient, serviceVersion);
        AgentsClient agentsClient = builder.buildAgentsClient();
        String image = getRequiredConfiguration("FOUNDRY_AGENT_CONTAINER_IMAGE");
        String agentName = TEST_AGENT_NAME + "-logs";

        HostedAgentSessionResources resources = null;
        try {
            resources = createAgentAndSession(agentsClient, agentName, image);
            configureAgentEndpoint(agentsClient, agentName, resources);

            OpenAIClient openAIClient = builder.buildAgentScopedOpenAIClient(agentName);
            com.openai.models.responses.Response openAIResponse = openAIClient.responses()
                .create(ResponseCreateParams.builder()
                    .input("Say hello in one short sentence.")
                    .putAdditionalBodyProperty("agent_session_id",
                        JsonValue.from(resources.getSession().getAgentSessionId()))
                    .build());
            Assertions.assertNotNull(openAIResponse);

            com.azure.core.http.rest.Response<BinaryData> rawStream
                = agentsClient.getSessionLogStreamWithResponse(agentName, resources.getAgent().getVersion(),
                    resources.getSession().getAgentSessionId(), new RequestOptions());
            Assertions.assertNotNull(rawStream.getValue());
            printSseFrames(rawStream.getValue(), 5);
        } finally {
            cleanup(agentsClient, agentName, resources);
        }
    }

    private static void configureAgentEndpoint(AgentsClient agentsClient, String agentName,
        HostedAgentSessionResources resources) {
        AgentEndpointConfig endpointConfig = new AgentEndpointConfig()
            .setVersionSelector(new VersionSelector().setVersionSelectionRules(Collections.singletonList(
                new FixedRatioVersionSelectionRule(100).setAgentVersion(resources.getAgent().getVersion()))))
            .setProtocolConfiguration(new ProtocolConfiguration().setResponses(new ResponsesProtocolConfiguration()));

        agentsClient.updateAgentDetails(agentName, new UpdateAgentDetailsOptions().setAgentEndpoint(endpointConfig));
    }

    private static HostedAgentSessionResources createAgentAndSession(AgentsClient agentsClient, String agentName,
        String image) {
        AgentVersionDetails agent = createHostedAgentVersion(agentsClient, agentName, image);
        waitForAgentVersionActive(agentsClient, agentName, agent.getVersion());

        AgentSessionResource session
            = agentsClient
                .createSessionWithResponse(agentName, BinaryData.fromObject(createSessionRequest(agent.getVersion())),
                    new RequestOptions())
                .getValue()
                .toObject(AgentSessionResource.class);

        return new HostedAgentSessionResources(agent, session);
    }

    private static void cleanup(AgentsClient agentsClient, String agentName, HostedAgentSessionResources resources) {
        if (resources == null) {
            return;
        }

        if (resources.getSession() != null) {
            try {
                agentsClient.deleteSession(agentName, resources.getSession().getAgentSessionId());
            } catch (ResourceNotFoundException ignored) {
                // The test may have already deleted the session.
            }
        }

        if (resources.getAgent() != null) {
            try {
                agentsClient.deleteAgentVersion(agentName, resources.getAgent().getVersion());
            } catch (ResourceNotFoundException ignored) {
                // The agent version may have already been deleted.
            }
        }
    }

    private static void printSseFrames(BinaryData streamData, int maxLogEvents) throws IOException {
        int eventCount = 0;
        String eventName = null;
        StringBuilder data = new StringBuilder();

        InputStream stream = streamData.toStream();
        ScheduledExecutorService watchdog = Executors.newSingleThreadScheduledExecutor();
        watchdog.schedule(() -> closeQuietly(stream), STREAM_READ_TIMEOUT.toMillis(), TimeUnit.MILLISECONDS);

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line;
            while (eventCount < maxLogEvents && (line = reader.readLine()) != null) {
                if (line.isEmpty()) {
                    if (eventName != null || data.length() > 0) {
                        eventCount++;
                    }
                    eventName = null;
                    data.setLength(0);
                } else if (line.startsWith("event: ")) {
                    eventName = line.substring("event: ".length());
                } else if (line.startsWith("data: ")) {
                    if (data.length() > 0) {
                        data.append(System.lineSeparator());
                    }
                    data.append(line.substring("data: ".length()));
                }
            }
        } catch (IOException e) {
            // Expected when the watchdog closes the endless stream after the timeout.
        } finally {
            watchdog.shutdownNow();
        }
    }

    private static void closeQuietly(InputStream stream) {
        try {
            stream.close();
        } catch (IOException ignored) {
            // Closing only to interrupt the blocking read; nothing to do if it fails.
        }
    }

    private static AgentVersionDetails createHostedAgentVersion(AgentsClient agentsClient, String agentName,
        String image) {
        CreateAgentVersionInput input
            = new CreateAgentVersionInput(createHostedAgentDefinition(image)).setMetadata(testMetadata())
                .setDescription("Hosted agent test created by the Azure AI Agents Java SDK.");

        return agentsClient.createAgentVersion(agentName, input);
    }

    private static HostedAgentDefinition createHostedAgentDefinition(String image) {
        return new HostedAgentDefinition("0.5", "1Gi").setContainerConfiguration(new ContainerConfiguration(image))
            .setProtocolVersions(
                Collections.singletonList(new ProtocolVersionRecord(AgentEndpointProtocol.RESPONSES, "1.0.0")));
    }

    private static void waitForAgentVersionActive(AgentsClient agentsClient, String agentName, String agentVersion) {
        for (int attempt = 1; attempt <= MAX_POLL_ATTEMPTS; attempt++) {
            sleep(POLL_INTERVAL);
            AgentVersionDetails versionDetails = agentsClient.getAgentVersionDetails(agentName, agentVersion);
            AgentVersionStatus status = versionDetails.getStatus();

            if (AgentVersionStatus.ACTIVE == status) {
                return;
            }
            if (AgentVersionStatus.FAILED == status) {
                throw new RuntimeException("Agent version provisioning failed: " + agentVersion);
            }
        }

        throw new RuntimeException("Timed out waiting for agent version to become active: " + agentVersion);
    }

    private static Map<String, Object> createSessionRequest(String agentVersion) {
        Map<String, Object> versionIndicator = new HashMap<>();
        versionIndicator.put("agent_version", agentVersion);
        versionIndicator.put("type", "version_ref");

        Map<String, Object> request = new HashMap<>();
        request.put("version_indicator", versionIndicator);
        return request;
    }

    private static Map<String, String> testMetadata() {
        Map<String, String> metadata = new HashMap<>();
        metadata.put("enableVnextExperience", "true");
        return metadata;
    }

    private static void sleep(Duration duration) {
        try {
            Thread.sleep(duration.toMillis());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while waiting for hosted agent provisioning.", e);
        }
    }

    private String getRequiredConfiguration(String name) {
        if (getTestMode() == TestMode.PLAYBACK && "FOUNDRY_AGENT_CONTAINER_IMAGE".equals(name)) {
            return "REDACTED";
        }

        String value = Configuration.getGlobalConfiguration().get(name);
        Assertions.assertNotNull(value, name + " must be set.");
        return value;
    }

    private static final class HostedAgentSessionResources {
        private final AgentVersionDetails agent;
        private final AgentSessionResource session;

        private HostedAgentSessionResources(AgentVersionDetails agent, AgentSessionResource session) {
            this.agent = agent;
            this.session = session;
        }

        AgentVersionDetails getAgent() {
            return agent;
        }

        AgentSessionResource getSession() {
            return session;
        }
    }
}
