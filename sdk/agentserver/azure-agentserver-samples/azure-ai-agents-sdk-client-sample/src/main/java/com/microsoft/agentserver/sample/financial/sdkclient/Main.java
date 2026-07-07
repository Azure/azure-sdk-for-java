// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.agentserver.sample.financial.sdkclient;

import com.azure.ai.agents.AgentsClient;
import com.azure.ai.agents.AgentsClientBuilder;
import com.azure.ai.agents.models.AgentEndpointConfig;
import com.azure.ai.agents.models.AgentEndpointProtocol;
import com.azure.ai.agents.models.AgentSessionResource;
import com.azure.ai.agents.models.AgentVersionDetails;
import com.azure.ai.agents.models.AgentVersionStatus;
import com.azure.ai.agents.models.ContainerConfiguration;
import com.azure.ai.agents.models.CreateAgentVersionInput;
import com.azure.ai.agents.models.FixedRatioVersionSelectionRule;
import com.azure.ai.agents.models.HostedAgentDefinition;
import com.azure.ai.agents.models.ProtocolConfiguration;
import com.azure.ai.agents.models.ProtocolVersionRecord;
import com.azure.ai.agents.models.ResponsesProtocolConfiguration;
import com.azure.ai.agents.models.UpdateAgentDetailsOptions;
import com.azure.ai.agents.models.VersionSelector;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.openai.client.OpenAIClient;
import com.openai.core.JsonValue;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;
import com.openai.models.responses.ResponseOutputItem;
import com.openai.models.responses.ResponseOutputMessage;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * End-to-end sample that deploys the {@code azure-agentserver-langchain4j-financial-jersey-sample}
 * container image as a <em>hosted agent</em> in Azure AI Foundry and then invokes it through the
 * {@code com.azure:azure-ai-agents} Java SDK.
 *
 * <p>The flow mirrors {@code AgentEndpointSample} from the azure-ai-agents SDK samples:</p>
 * <ol>
 *   <li>Create a hosted agent version from the financial-jersey container image.</li>
 *   <li>Poll until the version is {@code ACTIVE}.</li>
 *   <li>Create a session pinned to that version.</li>
 *   <li>Configure the agent endpoint to route all traffic to the version via the Responses protocol.</li>
 *   <li>Invoke the agent using an agent-scoped OpenAI Responses client.</li>
 *   <li>Delete the session and agent version.</li>
 * </ol>
 *
 * <p>Agent endpoints and sessions are preview features and only work with hosted agents, so the
 * {@link AgentsClientBuilder} is built with {@code allowPreview(true)}.</p>
 *
 * <p>Environment variables:</p>
 * <ul>
 *   <li>{@code FOUNDRY_PROJECT_ENDPOINT} (required) - the Azure AI Foundry project endpoint.</li>
 *   <li>{@code FOUNDRY_AGENT_CONTAINER_IMAGE} (required) - the financial-jersey container image reference
 *       (for example {@code javaaiacr3pwp6x.azurecr.io/lc4jfinancialjersey:latest}).</li>
 *   <li>{@code MODEL_DEPLOYMENT_NAME} (optional, default {@code gpt-5.4}) - the model deployment the
 *       financial agent should use. Injected into the container environment.</li>
 *   <li>{@code AGENT_NAME} (optional, default {@code java-financial-jersey-sdk-sample}).</li>
 *   <li>{@code AGENT_CPU} (optional, default {@code 2}) and {@code AGENT_MEMORY} (optional, default {@code 4Gi}).</li>
 *   <li>{@code PROMPT} (optional) - the question to ask the financial agent.</li>
 * </ul>
 */
public final class Main {

    private static final String DEFAULT_AGENT_NAME = "java-financial-jersey-sdk-sample";
    private static final String DEFAULT_MODEL_DEPLOYMENT = "gpt-5.4";
    private static final String DEFAULT_CPU = "2";
    private static final String DEFAULT_MEMORY = "4Gi";
    private static final String DEFAULT_PROMPT = "Transfer $12 from Alice to Bob.";

    private static final int MAX_POLL_ATTEMPTS = 60;
    private static final Duration POLL_INTERVAL = Duration.ofSeconds(10);

    private Main() {
    }

    public static void main(String[] args) {
        String endpoint = requireEnv("FOUNDRY_PROJECT_ENDPOINT");
        String image = requireEnv("FOUNDRY_AGENT_CONTAINER_IMAGE");
        String agentName = getEnvOrDefault("AGENT_NAME", DEFAULT_AGENT_NAME);
        String modelDeployment = getEnvOrDefault("MODEL_DEPLOYMENT_NAME", DEFAULT_MODEL_DEPLOYMENT);
        String cpu = getEnvOrDefault("AGENT_CPU", DEFAULT_CPU);
        String memory = getEnvOrDefault("AGENT_MEMORY", DEFAULT_MEMORY);
        String prompt = getEnvOrDefault("PROMPT", DEFAULT_PROMPT);

        AgentsClientBuilder builder = new AgentsClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint(endpoint);
        AgentsClient agentsClient = builder.allowPreview(true).buildAgentsClient();

        AgentVersionDetails agent = null;
        AgentSessionResource session = null;
        try {
            agent = createFinancialAgentVersion(agentsClient, agentName, image, modelDeployment, cpu, memory);
            AgentVersionDetails activeVersion = waitForAgentVersionActive(agentsClient, agentName, agent.getVersion());

            session = createSession(agentsClient, agentName, agent.getVersion());
            configureEndpoint(agentsClient, agentName, agent.getVersion());

            // The hosted agent calls Azure OpenAI under its own managed identity. Print the exact
            // role assignments that identity needs, so the RBAC can be granted before invoking.
            printManagedIdentityRoleGuidance(endpoint, activeVersion);

            OpenAIClient openAIClient = builder.buildAgentScopedOpenAIClient(agentName);
            System.out.printf("Invoking agent %s with prompt: %s%n", agentName, prompt);
            Response response = openAIClient.responses().create(ResponseCreateParams.builder()
                .input(prompt)
                .putAdditionalBodyProperty("agent_session_id", JsonValue.from(session.getAgentSessionId()))
                .build());

            printResponseOutput(response);
        } finally {
            if (getEnvOrDefault("SKIP_CLEANUP", "false").equalsIgnoreCase("true")) {
                System.out.printf("SKIP_CLEANUP=true; leaving agent '%s' version %s and session %s in place.%n",
                    agentName, agent == null ? "?" : agent.getVersion(),
                    session == null ? "?" : session.getAgentSessionId());
            } else {
                cleanup(agentsClient, agentName, agent, session);
            }
        }
    }

    private static AgentVersionDetails createFinancialAgentVersion(AgentsClient agentsClient, String agentName,
                                                                   String image, String modelDeployment, String cpu, String memory) {
        Map<String, String> containerEnv = new HashMap<>();
        containerEnv.put("MODEL_DEPLOYMENT_NAME", modelDeployment);

        HostedAgentDefinition definition = new HostedAgentDefinition(cpu, memory)
            .setContainerConfiguration(new ContainerConfiguration(image))
            .setEnvironmentVariables(containerEnv)
            .setProtocolVersions(Collections.singletonList(
                new ProtocolVersionRecord(AgentEndpointProtocol.RESPONSES, "1.0.0")));

        Map<String, String> metadata = new HashMap<>();
        metadata.put("enableVnextExperience", "true");

        CreateAgentVersionInput input = new CreateAgentVersionInput(definition)
            .setMetadata(metadata)
            .setDescription("Financial-jersey hosted agent deployed by the Azure AI Agents Java SDK sample.");

        // Idempotent deployment: creating an agent version implicitly creates the agent if it does not
        // already exist, and otherwise adds a new version to the existing agent. The service assigns the
        // version number and deduplicates identical definitions, so re-running with a new container image
        // produces a new version while re-running with the same image reuses the current one.
        if (agentExists(agentsClient, agentName)) {
            System.out.printf("Agent '%s' already exists; deploying a new version with image %s%n",
                agentName, image);
        } else {
            System.out.printf("Agent '%s' does not exist; creating it with image %s%n", agentName, image);
        }

        AgentVersionDetails agent = agentsClient.createAgentVersion(agentName, input);
        System.out.printf("Agent version ready (name: %s, version: %s)%n", agent.getName(), agent.getVersion());
        return agent;
    }

    private static boolean agentExists(AgentsClient agentsClient, String agentName) {
        try {
            agentsClient.getAgent(agentName);
            return true;
        } catch (ResourceNotFoundException e) {
            return false;
        }
    }

    private static AgentSessionResource createSession(AgentsClient agentsClient, String agentName, String version) {
        Map<String, Object> versionIndicator = new HashMap<>();
        versionIndicator.put("agent_version", version);
        versionIndicator.put("type", "version_ref");
        Map<String, Object> request = new HashMap<>();
        request.put("version_indicator", versionIndicator);

        AgentSessionResource session = agentsClient
            .createSessionWithResponse(agentName, BinaryData.fromObject(request), new RequestOptions())
            .getValue().toObject(AgentSessionResource.class);
        System.out.printf("Session created (id: %s, status: %s)%n", session.getAgentSessionId(), session.getStatus());
        return session;
    }

    private static void configureEndpoint(AgentsClient agentsClient, String agentName, String version) {
        AgentEndpointConfig endpointConfig = new AgentEndpointConfig()
            .setVersionSelector(new VersionSelector().setVersionSelectionRules(Collections.singletonList(
                new FixedRatioVersionSelectionRule(100).setAgentVersion(version))))
            .setProtocolConfiguration(new ProtocolConfiguration().setResponses(new ResponsesProtocolConfiguration()));

        agentsClient.updateAgentDetails(agentName, new UpdateAgentDetailsOptions().setAgentEndpoint(endpointConfig));
        System.out.printf("Agent endpoint configured for agent: %s%n", agentName);
    }

    private static AgentVersionDetails waitForAgentVersionActive(AgentsClient agentsClient, String agentName,
                                                                 String version) {
        for (int attempt = 1; attempt <= MAX_POLL_ATTEMPTS; attempt++) {
            sleep(POLL_INTERVAL);
            AgentVersionDetails details = agentsClient.getAgentVersionDetails(agentName, version);
            AgentVersionStatus status = details.getStatus();
            System.out.printf("Agent version status: %s (attempt %d)%n", status, attempt);
            if (AgentVersionStatus.ACTIVE == status) {
                return details;
            }
            if (AgentVersionStatus.FAILED == status) {
                throw new RuntimeException("Agent version provisioning failed: " + version);
            }
        }
        throw new RuntimeException("Timed out waiting for agent version to become active: " + version);
    }

    /**
     * Prints the exact {@code az role assignment create} command needed to grant the hosted agent's
     * managed identity access to Azure OpenAI. The financial agent calls the model under this identity,
     * so without this role the responses endpoint returns {@code 500 server_error} (caused by a
     * {@code 401 PermissionDenied} inside the container).
     */
    private static void printManagedIdentityRoleGuidance(String endpoint, AgentVersionDetails activeVersion) {
        String principalId = activeVersion.getInstanceIdentity() == null
            ? null : activeVersion.getInstanceIdentity().getPrincipalId();
        String accountName = accountNameFromEndpoint(endpoint);

        String assignee = principalId == null ? "<agent-principal-id>" : principalId;
        String subscriptionId = getEnvOrDefault("AZURE_SUBSCRIPTION_ID", "<subscription-id>");

        System.out.println();
        System.out.println("============================================================================");
        System.out.println(" Managed identity role assignment required before invoking the agent");
        System.out.println("============================================================================");
        System.out.printf(" Agent managed identity principal id: %s%n", assignee);
        System.out.println(" The hosted agent calls Azure OpenAI under this identity. Grant it the role");
        System.out.println(" below (idempotent - safe to re-run), then wait ~1-2 min for RBAC to");
        System.out.println(" propagate before the responses call.");
        System.out.println();
        System.out.println(" # Target the subscription that hosts the AI Services account:");
        System.out.printf(" az account set --subscription \"%s\"%n", subscriptionId);
        System.out.println();
        System.out.println(" # Resolve the AI Services account ARM resource id:");
        System.out.printf(" ACCOUNT_ID=$(az cognitiveservices account list --subscription \"%s\" "
            + "--query \"[?name=='%s'].id | [0]\" -o tsv)%n", subscriptionId, accountName);
        System.out.println();
        System.out.println(" # Cognitive Services OpenAI User on the account (data-plane model access):");
        System.out.println(" az role assignment create \\");
        System.out.printf("   --assignee-object-id %s \\%n", assignee);
        System.out.println("   --assignee-principal-type ServicePrincipal \\");
        System.out.println("   --role \"Cognitive Services OpenAI User\" \\");
        System.out.println("   --scope \"$ACCOUNT_ID\"");
        System.out.println("============================================================================");
        System.out.println();
    }

    private static String accountNameFromEndpoint(String endpoint) {
        try {
            String host = java.net.URI.create(endpoint).getHost();
            if (host != null && host.contains(".")) {
                return host.substring(0, host.indexOf('.'));
            }
        } catch (RuntimeException ignored) {
            // Fall through to placeholder.
        }
        return "<ai-services-account>";
    }

    private static void printResponseOutput(Response response) {
        boolean[] printedAny = { false };
        for (ResponseOutputItem outputItem : response.output()) {
            if (outputItem.message().isPresent()) {
                ResponseOutputMessage message = outputItem.message().get();
                message.content().forEach(content -> content.outputText()
                    .ifPresent(text -> {
                        System.out.println("Response output: " + text.text());
                        printedAny[0] = true;
                    }));
            }
        }
        if (!printedAny[0]) {
            System.out.println("No text output was returned. If the invoke failed with a 500/server_error, "
                + "verify the agent's managed identity has the role printed above.");
        }
    }

    private static void cleanup(AgentsClient agentsClient, String agentName, AgentVersionDetails agent,
                                AgentSessionResource session) {
        if (session != null) {
            try {
                agentsClient.deleteSession(agentName, session.getAgentSessionId());
                System.out.printf("Session with id: %s deleted.%n", session.getAgentSessionId());
            } catch (ResourceNotFoundException ignored) {
                // Already deleted.
            }
        }
        if (agent != null) {
            try {
                agentsClient.deleteAgentVersion(agentName, agent.getVersion());
                System.out.printf("Agent version %s deleted.%n", agent.getVersion());
            } catch (ResourceNotFoundException ignored) {
                // Already deleted.
            }
        }
    }

    private static String requireEnv(String name) {
        String value = Configuration.getGlobalConfiguration().get(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Required environment variable is not set: " + name);
        }
        return value;
    }

    private static String getEnvOrDefault(String name, String defaultValue) {
        String value = Configuration.getGlobalConfiguration().get(name);
        return (value == null || value.isBlank()) ? defaultValue : value;
    }

    private static void sleep(Duration duration) {
        try {
            Thread.sleep(duration.toMillis());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while waiting for hosted agent provisioning.", e);
        }
    }
}
