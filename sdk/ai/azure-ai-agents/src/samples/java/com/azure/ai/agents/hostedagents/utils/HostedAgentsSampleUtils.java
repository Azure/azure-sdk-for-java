// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.hostedagents.utils;

import com.azure.ai.agents.AgentsAsyncClient;
import com.azure.ai.agents.AgentsClient;
import com.azure.ai.agents.BetaAgentsAsyncClient;
import com.azure.ai.agents.BetaAgentsClient;
import com.azure.ai.agents.models.AgentProtocol;
import com.azure.ai.agents.models.AgentSessionResource;
import com.azure.ai.agents.models.AgentVersionDetails;
import com.azure.ai.agents.models.AgentVersionStatus;
import com.azure.ai.agents.models.ContainerConfiguration;
import com.azure.ai.agents.models.CreateAgentVersionInput;
import com.azure.ai.agents.models.HostedAgentDefinition;
import com.azure.ai.agents.models.ProtocolVersionRecord;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.util.BinaryData;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseOutputItem;
import com.openai.models.responses.ResponseOutputMessage;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public final class HostedAgentsSampleUtils {
    public static final String SAMPLE_AGENT_NAME = "java-hosted-agent-sample";

    private static final int MAX_POLL_ATTEMPTS = 60;
    private static final Duration POLL_INTERVAL = Duration.ofSeconds(10);

    // Session log streaming is an endless server-sent-event stream: the service keeps the
    // connection open and never signals completion, so it is the client's responsibility to
    // close the connection. This sample reads for at most STREAM_READ_TIMEOUT before closing.
    private static final Duration STREAM_READ_TIMEOUT = Duration.ofSeconds(3);

    private HostedAgentsSampleUtils() {
    }

    public static HostedAgentSessionResources createAgentAndSession(AgentsClient agentsClient,
        BetaAgentsClient betaAgentsClient, String agentName, String image) {
        AgentVersionDetails agent = createHostedAgentVersion(agentsClient, agentName, image);
        waitForAgentVersionActive(agentsClient, agentName, agent.getVersion());

        AgentSessionResource session = betaAgentsClient.createSessionWithResponse(agentName,
            BinaryData.fromObject(createSessionRequest(agent.getVersion())), new RequestOptions()).getValue()
            .toObject(AgentSessionResource.class);
        System.out.printf("Session created (id: %s, status: %s)%n", session.getAgentSessionId(), session.getStatus());

        return new HostedAgentSessionResources(agent, session);
    }

    public static Mono<HostedAgentSessionResources> createAgentAndSessionAsync(AgentsAsyncClient agentsAsyncClient,
        BetaAgentsAsyncClient betaAgentsAsyncClient, String agentName, String image) {
        return createHostedAgentVersionAsync(agentsAsyncClient, agentName, image)
            .flatMap(agent -> waitForAgentVersionActiveAsync(agentsAsyncClient, agentName, agent.getVersion())
                .then(betaAgentsAsyncClient.createSessionWithResponse(agentName,
                    BinaryData.fromObject(createSessionRequest(agent.getVersion())), new RequestOptions())
                    .map(response -> response.getValue().toObject(AgentSessionResource.class)))
                .map(session -> {
                    System.out.printf("Session created (id: %s, status: %s)%n", session.getAgentSessionId(),
                        session.getStatus());
                    return new HostedAgentSessionResources(agent, session);
                }));
    }

    public static void cleanup(AgentsClient agentsClient, BetaAgentsClient betaAgentsClient, String agentName,
        HostedAgentSessionResources resources) {
        if (resources == null) {
            return;
        }

        if (resources.getSession() != null) {
            try {
                betaAgentsClient.deleteSession(agentName, resources.getSession().getAgentSessionId(), null);
                System.out.printf("Session with id: %s deleted.%n", resources.getSession().getAgentSessionId());
            } catch (ResourceNotFoundException ignored) {
                // The sample may have already deleted the session.
            }
        }

        if (resources.getAgent() != null) {
            try {
                agentsClient.deleteAgentVersion(agentName, resources.getAgent().getVersion());
                System.out.printf("Agent version %s deleted.%n", resources.getAgent().getVersion());
            } catch (ResourceNotFoundException ignored) {
                // The sample may have already deleted the agent version.
            }
        }
    }

    public static Mono<Void> cleanupAsync(AgentsAsyncClient agentsAsyncClient, BetaAgentsAsyncClient betaAgentsAsyncClient,
        String agentName, HostedAgentSessionResources resources) {
        if (resources == null) {
            return Mono.empty();
        }

        Mono<Void> deleteSession = Mono.empty();
        if (resources.getSession() != null) {
            String sessionId = resources.getSession().getAgentSessionId();
            deleteSession = betaAgentsAsyncClient.deleteSession(agentName, sessionId, null)
                .doOnSuccess(unused -> System.out.printf("Session with id: %s deleted.%n", sessionId))
                .onErrorResume(ResourceNotFoundException.class, ignored -> Mono.empty());
        }

        Mono<Void> deleteAgentVersion = Mono.empty();
        if (resources.getAgent() != null) {
            String version = resources.getAgent().getVersion();
            deleteAgentVersion = agentsAsyncClient.deleteAgentVersion(agentName, version)
                .doOnSuccess(unused -> System.out.printf("Agent version %s deleted.%n", version))
                .onErrorResume(ResourceNotFoundException.class, ignored -> Mono.empty());
        }

        return deleteSession.then(deleteAgentVersion);
    }

    public static void printResponseOutput(Response response) {
        for (ResponseOutputItem outputItem : response.output()) {
            if (outputItem.message().isPresent()) {
                ResponseOutputMessage message = outputItem.message().get();
                message.content().forEach(content -> content.outputText()
                    .ifPresent(text -> System.out.println("Response output: " + text.text())));
            }
        }
    }

    public static void printSseFrames(BinaryData streamData, int maxLogEvents) throws IOException {
        int eventCount = 0;
        String eventName = null;
        StringBuilder data = new StringBuilder();

        // The session log stream is endless, so the client must close the connection itself.
        // Schedule a watchdog that closes the stream after STREAM_READ_TIMEOUT; closing it
        // unblocks the in-progress readLine() call and ends the loop below.
        InputStream stream = streamData.toStream();
        ScheduledExecutorService watchdog = Executors.newSingleThreadScheduledExecutor();
        watchdog.schedule(() -> closeQuietly(stream), STREAM_READ_TIMEOUT.toMillis(), TimeUnit.MILLISECONDS);

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line;
            while (eventCount < maxLogEvents && (line = reader.readLine()) != null) {
                if (line.isEmpty()) {
                    if (eventName != null || data.length() > 0) {
                        eventCount++;
                        System.out.println("SSE event: " + eventName);
                        System.out.println("SSE data: " + data.toString());
                        System.out.println();
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
            // Expected when the watchdog closes the endless stream after the timeout; the stream
            // has no natural end, so a read failure after closing is the normal way to stop.
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
        CreateAgentVersionInput input = new CreateAgentVersionInput(createHostedAgentDefinition(image))
            .setMetadata(sampleMetadata())
            .setDescription("Hosted agent sample created by the Azure AI Agents Java SDK.");

        AgentVersionDetails agent = agentsClient.createAgentVersion(agentName, input);
        System.out.printf("Agent created (name: %s, version: %s)%n", agent.getName(), agent.getVersion());
        return agent;
    }

    private static Mono<AgentVersionDetails> createHostedAgentVersionAsync(AgentsAsyncClient agentsAsyncClient,
        String agentName, String image) {
        CreateAgentVersionInput input = new CreateAgentVersionInput(createHostedAgentDefinition(image))
            .setMetadata(sampleMetadata())
            .setDescription("Hosted agent sample created by the Azure AI Agents Java SDK.");

        return agentsAsyncClient.createAgentVersion(agentName, input)
            .doOnNext(agent -> System.out.printf("Agent created (name: %s, version: %s)%n", agent.getName(),
                agent.getVersion()));
    }

    private static HostedAgentDefinition createHostedAgentDefinition(String image) {
        return new HostedAgentDefinition("0.5", "1Gi")
            .setContainerConfiguration(new ContainerConfiguration(image))
            .setProtocolVersions(Collections.singletonList(
                new ProtocolVersionRecord(AgentProtocol.RESPONSES, "1.0.0")));
    }

    private static void waitForAgentVersionActive(AgentsClient agentsClient, String agentName, String agentVersion) {
        for (int attempt = 1; attempt <= MAX_POLL_ATTEMPTS; attempt++) {
            sleep(POLL_INTERVAL);
            AgentVersionDetails versionDetails = agentsClient.getAgentVersionDetails(agentName, agentVersion);
            AgentVersionStatus status = versionDetails.getStatus();
            System.out.printf("Agent version status: %s (attempt %d)%n", status, attempt);

            if (AgentVersionStatus.ACTIVE == status) {
                return;
            }
            if (AgentVersionStatus.FAILED == status) {
                throw new RuntimeException("Agent version provisioning failed: " + agentVersion);
            }
        }

        throw new RuntimeException("Timed out waiting for agent version to become active: " + agentVersion);
    }

    private static Mono<AgentVersionDetails> waitForAgentVersionActiveAsync(AgentsAsyncClient agentsAsyncClient,
        String agentName, String agentVersion) {
        return Flux.range(1, MAX_POLL_ATTEMPTS)
            .delayElements(POLL_INTERVAL)
            .concatMap(attempt -> agentsAsyncClient.getAgentVersionDetails(agentName, agentVersion)
                .flatMap(versionDetails -> {
                    AgentVersionStatus status = versionDetails.getStatus();
                    System.out.printf("Agent version status: %s (attempt %d)%n", status, attempt);

                    if (AgentVersionStatus.ACTIVE == status) {
                        return Mono.just(versionDetails);
                    }
                    if (AgentVersionStatus.FAILED == status) {
                        return Mono.error(new RuntimeException("Agent version provisioning failed: " + agentVersion));
                    }
                    return Mono.empty();
                }))
            .next()
            .switchIfEmpty(Mono.error(new RuntimeException(
                "Timed out waiting for agent version to become active: " + agentVersion)));
    }

    private static Map<String, Object> createSessionRequest(String agentVersion) {
        Map<String, Object> versionIndicator = new HashMap<>();
        versionIndicator.put("agent_version", agentVersion);
        versionIndicator.put("type", "version_ref");

        Map<String, Object> request = new HashMap<>();
        request.put("version_indicator", versionIndicator);
        return request;
    }

    private static Map<String, String> sampleMetadata() {
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

    public static final class HostedAgentSessionResources {
        private final AgentVersionDetails agent;
        private final AgentSessionResource session;

        HostedAgentSessionResources(AgentVersionDetails agent, AgentSessionResource session) {
            this.agent = agent;
            this.session = session;
        }

        public AgentVersionDetails getAgent() {
            return agent;
        }

        public AgentSessionResource getSession() {
            return session;
        }
    }
}
