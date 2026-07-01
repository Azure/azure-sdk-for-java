// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.telemetry;

import com.azure.ai.agents.models.AgentDefinition;
import com.azure.ai.agents.models.AgentVersionDetails;
import com.azure.ai.agents.models.HostedAgentDefinition;
import com.azure.ai.agents.models.PromptAgentDefinition;
import com.azure.ai.agents.models.WorkflowAgentDefinition;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import static com.azure.ai.agents.telemetry.GenAiConstants.AGENT_TYPE_PROMPT;
import static com.azure.ai.agents.telemetry.GenAiConstants.AGENT_TYPE_WORKFLOW;
import static com.azure.ai.agents.telemetry.GenAiConstants.GEN_AI_AGENT_WORKFLOW;
import static com.azure.ai.agents.telemetry.GenAiConstants.GEN_AI_EVENT_CONTENT;
import static com.azure.ai.agents.telemetry.GenAiConstants.GEN_AI_PROVIDER_NAME;
import static com.azure.ai.agents.telemetry.GenAiConstants.GEN_AI_PROVIDER_NAME_VALUE;

/**
 * Provides tracing integration for GenAI agent CRUD operations.
 *
 * <p>Usage pattern:</p>
 * <pre>{@code
 * AgentVersionDetails result = GenAiAgentTracing.traceCreateAgent(
 *     "MyAgent", endpoint, agentDefinition,
 *     () -> agentsClient.createAgentVersion("MyAgent", input));
 * }</pre>
 */
public final class GenAiAgentTracing {

    private GenAiAgentTracing() {
        // utility class
    }

    /**
     * Traces a create_agent operation by extracting relevant attributes from the agent definition.
     * This method handles the instanceof dispatch for all known definition types.
     *
     * @param <T> the return type of the operation.
     * @param agentName the agent name.
     * @param endpoint the service endpoint.
     * @param definition the agent definition (PromptAgentDefinition, HostedAgentDefinition, WorkflowAgentDefinition).
     * @param operation the supplier that performs the actual API call.
     * @return the result of the operation.
     */
    public static <T> T traceCreateAgentVersion(String agentName, URI endpoint, AgentDefinition definition,
        Supplier<T> operation) {
        if (definition instanceof PromptAgentDefinition) {
            PromptAgentDefinition prompt = (PromptAgentDefinition) definition;
            return traceCreateAgent(agentName, endpoint, null, null, AGENT_TYPE_PROMPT, prompt.getModel(),
                prompt.getTemperature(), prompt.getTopP(), prompt.getInstructions(), operation);
        } else if (definition instanceof WorkflowAgentDefinition) {
            WorkflowAgentDefinition workflow = (WorkflowAgentDefinition) definition;
            return traceCreateAgent(agentName, endpoint, null, null, AGENT_TYPE_WORKFLOW, null, null, null, null,
                workflow.getWorkflow(), operation);
        } else if (definition instanceof HostedAgentDefinition) {
            HostedAgentDefinition hosted = (HostedAgentDefinition) definition;
            String protocol = null;
            String protocolVersion = null;
            if (hosted.getContainerProtocolVersions() != null && !hosted.getContainerProtocolVersions().isEmpty()) {
                protocol = hosted.getContainerProtocolVersions().get(0).getProtocol() != null
                    ? hosted.getContainerProtocolVersions().get(0).getProtocol().toString()
                    : null;
                protocolVersion = hosted.getContainerProtocolVersions().get(0).getVersion();
            }
            return traceCreateHostedAgent(agentName, endpoint, null, null, null, null, null, null, hosted.getCpu(),
                hosted.getMemory(), hosted.getImage(), protocol, protocolVersion, operation);
        }
        // Unknown definition type — just execute without detailed attributes
        return traceCreateAgent(agentName, endpoint, null, null, null, null, null, null, null, operation);
    }

    /**
     * Traces a create_agent operation.
     *
     * @param <T> the return type of the operation.
     * @param agentName the agent name.
     * @param endpoint the service endpoint.
     * @param agentId the agent ID (e.g., "name:version").
     * @param agentVersion the agent version string.
     * @param agentType the agent type ("prompt", "hosted", "workflow").
     * @param model the model name.
     * @param temperature temperature parameter (may be null).
     * @param topP top_p parameter (may be null).
     * @param instructions system instructions text (content-gated).
     * @param operation the supplier that performs the actual API call.
     * @return the result of the operation.
     */
    @SuppressWarnings("try")
    public static <T> T traceCreateAgent(String agentName, URI endpoint, String agentId, String agentVersion,
        String agentType, String model, Double temperature, Double topP, String instructions, Supplier<T> operation) {
        return traceCreateAgent(agentName, endpoint, agentId, agentVersion, agentType, model, temperature, topP,
            instructions, null, operation);
    }

    /**
     * Traces a create_agent operation, optionally emitting a workflow event.
     *
     * @param <T> the return type of the operation.
     * @param agentName the agent name.
     * @param endpoint the service endpoint.
     * @param agentId the agent ID (e.g., "name:version").
     * @param agentVersion the agent version string.
     * @param agentType the agent type ("prompt", "hosted", "workflow").
     * @param model the model name.
     * @param temperature temperature parameter (may be null).
     * @param topP top_p parameter (may be null).
     * @param instructions system instructions text (content-gated).
     * @param workflowDefinition the workflow CSDL/YAML definition (only for workflow agents, may be null).
     * @param operation the supplier that performs the actual API call.
     * @return the result of the operation.
     * @throws RuntimeException if the operation throws a checked exception.
     */
    @SuppressWarnings("try")
    public static <T> T traceCreateAgent(String agentName, URI endpoint, String agentId, String agentVersion,
        String agentType, String model, Double temperature, Double topP, String instructions, String workflowDefinition,
        Supplier<T> operation) {
        GenAiTracingScope scope = GenAiTracingScope.startCreateAgent(agentName, endpoint);
        if (scope == null) {
            return operation.get();
        }

        try {
            scope.setAgentAttributes(agentId, agentName, agentVersion, agentType);
            scope.setRequestModelAttributes(model, temperature, topP);
            scope.setSystemInstructions(instructions);

            // Emit workflow event for workflow-type agents
            if (AGENT_TYPE_WORKFLOW.equals(agentType)) {
                emitWorkflowEvent(scope, workflowDefinition);
            }

            T result;
            try (AutoCloseable ignored = scope.makeSpanCurrent()) {
                result = operation.get();
            }

            // Enrich with agent id/version from the response
            enrichFromResult(scope, result);

            return result;
        } catch (Throwable ex) {
            scope.recordError(ex);
            if (ex instanceof RuntimeException) {
                throw (RuntimeException) ex;
            }
            throw new RuntimeException(ex);
        } finally {
            scope.close();
        }
    }

    /**
     * Traces a create_agent operation with hosted agent attributes.
     *
     * @param <T> the return type of the operation.
     * @param agentName the agent name.
     * @param endpoint the service endpoint.
     * @param agentId the agent ID.
     * @param agentVersion the agent version.
     * @param model the model name.
     * @param temperature temperature (may be null).
     * @param topP top_p (may be null).
     * @param instructions system instructions (content-gated).
     * @param cpu hosted CPU allocation.
     * @param memory hosted memory allocation.
     * @param image hosted container image.
     * @param protocol hosted protocol.
     * @param protocolVersion hosted protocol version.
     * @param operation the supplier that performs the actual API call.
     * @return the result of the operation.
     * @throws RuntimeException if the operation throws a checked exception.
     */
    @SuppressWarnings("try")
    public static <T> T traceCreateHostedAgent(String agentName, URI endpoint, String agentId, String agentVersion,
        String model, Double temperature, Double topP, String instructions, String cpu, String memory, String image,
        String protocol, String protocolVersion, Supplier<T> operation) {
        GenAiTracingScope scope = GenAiTracingScope.startCreateAgent(agentName, endpoint);
        if (scope == null) {
            return operation.get();
        }

        try {
            scope.setAgentAttributes(agentId, agentName, agentVersion, GenAiConstants.AGENT_TYPE_HOSTED);
            scope.setRequestModelAttributes(model, temperature, topP);
            scope.setSystemInstructions(instructions);
            scope.setHostedAgentAttributes(cpu, memory, image, protocol, protocolVersion);

            T result;
            try (AutoCloseable ignored = scope.makeSpanCurrent()) {
                result = operation.get();
            }

            // Enrich with agent id/version from the response
            enrichFromResult(scope, result);

            return result;
        } catch (Throwable ex) {
            scope.recordError(ex);
            if (ex instanceof RuntimeException) {
                throw (RuntimeException) ex;
            }
            throw new RuntimeException(ex);
        } finally {
            scope.close();
        }
    }

    /**
     * Traces a create_conversation operation.
     *
     * @param <T> the return type of the operation.
     * @param endpoint the service endpoint.
     * @param operation the supplier that performs the actual API call.
     * @return the result of the operation.
     * @throws RuntimeException if the operation throws a checked exception.
     */
    @SuppressWarnings("try")
    public static <T> T traceCreateConversation(URI endpoint, Supplier<T> operation) {
        GenAiTracingScope scope = GenAiTracingScope.startCreateConversation(endpoint);
        if (scope == null) {
            return operation.get();
        }

        try {
            T result;
            try (AutoCloseable ignored = scope.makeSpanCurrent()) {
                result = operation.get();
            }
            return result;
        } catch (Throwable ex) {
            scope.recordError(ex);
            if (ex instanceof RuntimeException) {
                throw (RuntimeException) ex;
            }
            throw new RuntimeException(ex);
        } finally {
            scope.close();
        }
    }

    /**
     * Emits a gen_ai.agent.workflow event on the span.
     * When content recording is enabled, includes the workflow definition.
     */
    private static void emitWorkflowEvent(GenAiTracingScope scope, String workflowDefinition) {
        String contentArray = formatWorkflowEventContent(workflowDefinition);

        Map<String, Object> eventAttributes = new HashMap<>();
        eventAttributes.put(GEN_AI_PROVIDER_NAME, GEN_AI_PROVIDER_NAME_VALUE);
        eventAttributes.put(GEN_AI_EVENT_CONTENT, contentArray);
        scope.addEvent(GEN_AI_AGENT_WORKFLOW, eventAttributes);
    }

    /**
     * Formats the content array for a gen_ai.agent.workflow event.
     * Package-private for testing.
     */
    static String formatWorkflowEventContent(String workflowDefinition) {
        if (GenAiTracingConfiguration.isContentRecordingEnabled()
            && workflowDefinition != null
            && !workflowDefinition.isEmpty()) {
            return "[{\"type\":\"workflow\",\"content\":" + jsonEscape(workflowDefinition) + "}]";
        } else {
            return "[]";
        }
    }

    private static String jsonEscape(String text) {
        if (text == null) {
            return "null";
        }
        StringBuilder sb = new StringBuilder("\"");
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            switch (c) {
                case '"':
                    sb.append("\\\"");
                    break;

                case '\\':
                    sb.append("\\\\");
                    break;

                case '\n':
                    sb.append("\\n");
                    break;

                case '\r':
                    sb.append("\\r");
                    break;

                case '\t':
                    sb.append("\\t");
                    break;

                default:
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
            }
        }
        sb.append("\"");
        return sb.toString();
    }

    /**
     * Enriches the tracing scope with agent id and version from the operation result.
     */
    private static <T> void enrichFromResult(GenAiTracingScope scope, T result) {
        if (result instanceof AgentVersionDetails) {
            AgentVersionDetails details = (AgentVersionDetails) result;
            String name = details.getName();
            String version = details.getVersion();
            if (name != null && version != null) {
                scope.setAgentIdAndVersion(name + ":" + version, version);
            }
        }
    }
}
