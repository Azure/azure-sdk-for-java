// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.telemetry;

import java.net.URI;
import java.util.function.Supplier;

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
    public static <T> T traceCreateAgent(String agentName, URI endpoint, String agentId, String agentVersion,
        String agentType, String model, Double temperature, Double topP, String instructions, Supplier<T> operation) {
        GenAiTracingScope scope = GenAiTracingScope.startCreateAgent(agentName, endpoint);
        if (scope == null) {
            return operation.get();
        }

        try {
            scope.setAgentAttributes(agentId, agentName, agentVersion, agentType);
            scope.setRequestModelAttributes(model, temperature, topP);
            scope.setSystemInstructions(instructions);

            T result = operation.get();
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
     */
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

            T result = operation.get();
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
     */
    public static <T> T traceCreateConversation(URI endpoint, Supplier<T> operation) {
        GenAiTracingScope scope = GenAiTracingScope.startCreateConversation(endpoint);
        if (scope == null) {
            return operation.get();
        }

        try {
            T result = operation.get();
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
}
