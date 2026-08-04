// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.agentserver.hostedagent.cli;

import com.azure.ai.agents.AgentsClient;
import com.azure.ai.agents.models.AgentDefinition;
import com.azure.ai.agents.models.AgentDetails;
import com.azure.ai.agents.models.AgentEndpointProtocol;
import com.azure.ai.agents.models.AgentIdentity;
import com.azure.ai.agents.models.AgentSessionResource;
import com.azure.ai.agents.models.AgentVersionDetails;
import com.azure.ai.agents.models.AgentVersionStatus;
import com.azure.ai.agents.models.ContainerConfiguration;
import com.azure.ai.agents.models.CreateAgentVersionInput;
import com.azure.ai.agents.models.HostedAgentDefinition;
import com.azure.ai.agents.models.ProtocolVersionRecord;
import com.azure.ai.agents.models.SessionLogEvent;
import com.azure.ai.agents.models.VersionRefIndicator;
import com.azure.core.exception.ResourceNotFoundException;

import java.time.Duration;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Thin wrapper around {@link AgentsClient} that provides the hosted-agent deployment operations for the
 * vnext experience: {@code deploy}, {@code status}, {@code list}, {@code delete} and log {@code streaming}.
 *
 * <p>All operations target container-based <em>hosted</em> agents and drive the {@code com.azure:azure-ai-agents}
 * SDK exclusively — there are no raw REST calls.</p>
 */
final class HostedAgentService {

    private static final String PROTOCOL_VERSION = "1.0.0";
    private static final int MAX_POLL_ATTEMPTS = 60;
    private static final Duration POLL_INTERVAL = Duration.ofSeconds(10);

    private final AgentsClient client;
    private final String endpoint;
    private final com.azure.core.credential.TokenCredential credential;

    HostedAgentService(AgentsClient client, String endpoint, com.azure.core.credential.TokenCredential credential) {
        this.client = client;
        this.endpoint = endpoint;
        this.credential = credential;
    }

    /**
     * Deploys a container image as a hosted agent version and waits for it to become {@code ACTIVE}.
     *
     * <p>Deployment is idempotent: if the agent does not exist it is created; otherwise a new version is
     * added. The service assigns the version number and deduplicates identical definitions, so re-running
     * with a new image produces a new version while re-running with the same image reuses the current one.</p>
     */
    AgentVersionDetails deploy(String name, String image, String cpu, String memory, Map<String, String> environment,
                               String description, String subscriptionId, boolean grantOpenAiAccess) {

        HostedAgentDefinition definition = new HostedAgentDefinition(cpu, memory)
            .setContainerConfiguration(new ContainerConfiguration(image))
            .setEnvironmentVariables(environment)
            .setProtocolVersions(Collections.singletonList(
                new ProtocolVersionRecord(AgentEndpointProtocol.RESPONSES, PROTOCOL_VERSION)));

        Map<String, String> metadata = new LinkedHashMap<>();
        metadata.put("enableVnextExperience", "true");

        CreateAgentVersionInput input = new CreateAgentVersionInput(definition)
            .setMetadata(metadata)
            .setDescription(description);

        System.out.printf("%s agent '%s' with image %s (cpu=%s, memory=%s)%n",
            agentExists(name) ? "Deploying new version of" : "Creating", name, image, cpu, memory);
        if (!environment.isEmpty()) {
            System.out.printf("  Environment: %s%n", String.join(", ", environment.keySet()));
        }

        AgentVersionDetails version = client.createAgentVersion(name, input);
        System.out.printf("Version %s created (status: %s). Polling until active…%n",
            version.getVersion(), version.getStatus());

        AgentVersionDetails active = waitForActive(name, version.getVersion());

        System.out.println();
        System.out.println("Verifying the agent identity can access Azure OpenAI…");
        OpenAiAccessVerifier.verify(endpoint, agentPrincipalId(name), subscriptionId, grantOpenAiAccess);

        return active;
    }

    /**
     * Prints a detailed status report for a single agent and every one of its versions.
     */
    void status(String name) {
        AgentDetails agent;
        try {
            agent = client.getAgent(name);
        } catch (ResourceNotFoundException e) {
            System.out.printf("Agent '%s' not found.%n", name);
            return;
        }

        AgentVersionDetails latest = agent.getVersions() == null ? null : agent.getVersions().getLatest();
        String latestVersion = latest == null ? "?" : latest.getVersion();

        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.printf("  Agent:          %s%n", agent.getName());
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.printf("  Id:             %s%n", agent.getId());
        System.out.printf("  Latest version: %s%n", latestVersion);
        if (latest != null) {
            printVersionDetail("  ", latest);
        }
        System.out.println();

        List<AgentVersionDetails> versions = listVersionsDescending(name);
        System.out.printf("Versions (%d):%n", versions.size());
        for (AgentVersionDetails version : versions) {
            String marker = version.getVersion().equals(latestVersion) ? " (latest)" : "";
            System.out.printf("  v%s%s: status=%s  image=%s  created=%s%n",
                version.getVersion(), marker, version.getStatus(), imageOf(version), version.getCreatedAt());
        }
    }

    /**
     * Lists every hosted agent in the project along with the status of each of its versions.
     */
    void list() {
        List<AgentDetails> agents = StreamSupport.stream(client.listAgents().spliterator(), false)
            .toList();

        if (agents.isEmpty()) {
            System.out.println("No agents found.");
            return;
        }

        System.out.printf("Found %d agent(s):%n%n", agents.size());
        for (AgentDetails agent : agents) {
            System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            System.out.printf("Agent: %s%n", agent.getName());
            System.out.printf("Id:    %s%n", agent.getId());

            List<AgentVersionDetails> versions = listVersionsDescending(agent.getName());
            if (versions.isEmpty()) {
                System.out.println("  Versions: (none)");
            } else {
                for (AgentVersionDetails version : versions) {
                    System.out.printf("  Version %s: status=%s  image=%s%n",
                        version.getVersion(), version.getStatus(), imageOf(version));
                }
            }
            System.out.println();
        }
    }

    /**
     * Deletes every version of the agent and then the agent itself.
     */
    void delete(String name) {
        if (!agentExists(name)) {
            System.out.printf("Agent '%s' not found. Nothing to delete.%n", name);
            return;
        }

        List<AgentVersionDetails> versions = listVersionsDescending(name);
        for (AgentVersionDetails version : versions) {
            System.out.printf("Deleting version %s…%n", version.getVersion());
            try {
                client.deleteAgentVersion(name, version.getVersion());
            } catch (ResourceNotFoundException ignored) {
                // Already gone.
            }
        }

        System.out.printf("Deleting agent '%s'…%n", name);
        try {
            client.deleteAgent(name);
        } catch (ResourceNotFoundException ignored) {
            // Already gone.
        }
        System.out.printf("Agent '%s' deleted.%n", name);
    }

    /**
     * Streams container console logs for a session over the SSE log-stream API. When {@code version} is
     * {@code null} the latest version is used; when {@code sessionId} is {@code null} a fresh session is
     * created against that version so there is something to stream.
     */
    void streamLogs(String name, String version, String sessionId) {
        String resolvedVersion = version != null ? version : latestVersion(name);
        System.out.printf("Using version: %s%n", resolvedVersion);

        String resolvedSession = sessionId;
        if (resolvedSession == null) {
            AgentSessionResource session = client.createSession(name, new VersionRefIndicator(resolvedVersion));
            resolvedSession = session.getAgentSessionId();
            System.out.printf("Created session: %s (status: %s)%n", resolvedSession, session.getStatus());
        }

        System.out.printf("%nStreaming console logs for %s v%s session %s%nPress Ctrl-C to stop.%n%n",
            name, resolvedVersion, resolvedSession);

        OpenAiAccessVerifier.LogAdvisor advisor = new OpenAiAccessVerifier.LogAdvisor(endpoint, agentPrincipalId(name));

        for (SessionLogEvent event : client.getSessionLogStream(name, resolvedVersion, resolvedSession)) {
            System.out.printf("%s: %s%n", event.getEvent(), event.getData());
            advisor.inspect(event.getData());
        }
        System.out.println("\nStopped.");
    }

    /**
     * Dumps recent Application Insights telemetry for the agent and returns (the non-streaming log view).
     * Unlike {@link #streamLogs} this reads historical data from the agent's Application Insights resource
     * rather than opening a live session, so it is the right choice for inspecting what already happened.
     */
    void dumpLogs(String name, String appInsightsResourceId, AppInsightsLogs.LogType type, String since, int limit) {
        if (appInsightsResourceId == null || appInsightsResourceId.isEmpty()) {
            throw new IllegalArgumentException("--app-insights <resource-id> is required for the 'insights' log source.");
        }
        new AppInsightsLogs(credential).dump(appInsightsResourceId, name, type, since, limit);
    }

    /**
     * Returns the managed identity principal id assigned to the agent, or {@code null} if it cannot be
     * determined (for example the agent or its identity has not been provisioned yet).
     */
    private String agentPrincipalId(String name) {
        try {
            AgentDetails agent = client.getAgent(name);
            AgentIdentity identity = agent.getInstanceIdentity();
            return identity == null ? null : identity.getPrincipalId();
        } catch (RuntimeException e) {
            return null;
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────────

    private AgentVersionDetails waitForActive(String name, String version) {
        for (int attempt = 1; attempt <= MAX_POLL_ATTEMPTS; attempt++) {
            AgentVersionDetails details = client.getAgentVersionDetails(name, version);
            AgentVersionStatus status = details.getStatus();
            System.out.printf("  Attempt %d/%d: %s%n", attempt, MAX_POLL_ATTEMPTS, status);
            if (AgentVersionStatus.ACTIVE == status) {
                System.out.println("Agent is active. Deploy succeeded.");
                return details;
            }
            if (AgentVersionStatus.FAILED == status) {
                throw new IllegalStateException("Agent version provisioning failed: " + version);
            }
            sleep(POLL_INTERVAL);
        }
        throw new IllegalStateException("Timed out waiting for agent version to become active: " + version);
    }

    private String latestVersion(String name) {
        return listVersionsDescending(name).stream()
            .findFirst()
            .map(AgentVersionDetails::getVersion)
            .orElseThrow(() -> new IllegalStateException("No versions found for agent '" + name + "'."));
    }

    private List<AgentVersionDetails> listVersionsDescending(String name) {
        return StreamSupport.stream(client.listAgentVersions(name).spliterator(), false)
            .sorted(Comparator.comparingLong(HostedAgentService::versionNumber).reversed())
            .collect(Collectors.toList());
    }

    private boolean agentExists(String name) {
        try {
            client.getAgent(name);
            return true;
        } catch (ResourceNotFoundException e) {
            return false;
        }
    }

    private void printVersionDetail(String indent, AgentVersionDetails version) {
        System.out.printf("%sStatus:         %s%n", indent, version.getStatus());
        System.out.printf("%sImage:          %s%n", indent, imageOf(version));
        if (version.getDefinition() instanceof HostedAgentDefinition) {
            HostedAgentDefinition def = (HostedAgentDefinition) version.getDefinition();
            System.out.printf("%sCPU / Memory:   %s / %s%n", indent, def.getCpu(), def.getMemory());
            Map<String, String> env = def.getEnvironmentVariables();
            if (env != null && !env.isEmpty()) {
                System.out.printf("%sEnv vars:       %s%n", indent, String.join(", ", env.keySet()));
            }
        }
        System.out.printf("%sCreated:        %s%n", indent, version.getCreatedAt());
    }

    private static String imageOf(AgentVersionDetails version) {
        AgentDefinition definition = version.getDefinition();
        if (definition instanceof HostedAgentDefinition) {
            ContainerConfiguration container = ((HostedAgentDefinition) definition).getContainerConfiguration();
            if (container != null && container.getImage() != null) {
                return container.getImage();
            }
        }
        return "?";
    }

    private static long versionNumber(AgentVersionDetails version) {
        try {
            return Long.parseLong(version.getVersion());
        } catch (NumberFormatException e) {
            return Long.MIN_VALUE;
        }
    }

    private static void sleep(Duration duration) {
        try {
            Thread.sleep(duration.toMillis());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while waiting for hosted agent provisioning.", e);
        }
    }
}
