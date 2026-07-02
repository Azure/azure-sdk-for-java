// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.implementation.telemetry;

import com.azure.ai.agents.models.AgentDefinition;
import com.azure.ai.agents.models.AgentVersionDetails;
import com.azure.ai.agents.models.HostedAgentDefinition;
import com.azure.ai.agents.models.PromptAgentDefinition;
import com.azure.ai.agents.models.ProtocolVersionRecord;
import com.azure.ai.agents.models.WorkflowAgentDefinition;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.util.BinaryData;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static com.azure.ai.agents.implementation.telemetry.GenAiConstants.AGENT_TYPE_HOSTED;
import static com.azure.ai.agents.implementation.telemetry.GenAiConstants.AGENT_TYPE_PROMPT;
import static com.azure.ai.agents.implementation.telemetry.GenAiConstants.AGENT_TYPE_WORKFLOW;
import static com.azure.ai.agents.implementation.telemetry.GenAiConstants.GEN_AI_AGENT_WORKFLOW;
import static com.azure.ai.agents.implementation.telemetry.GenAiConstants.GEN_AI_EVENT_CONTENT;
import static com.azure.ai.agents.implementation.telemetry.GenAiConstants.GEN_AI_PROVIDER_NAME;
import static com.azure.ai.agents.implementation.telemetry.GenAiConstants.GEN_AI_PROVIDER_NAME_VALUE;

/**
 * Tracing for the agent-management convenience methods on {@link com.azure.ai.agents.AgentsClient} and
 * {@link com.azure.ai.agents.AgentsAsyncClient}. Wraps {@code createAgentVersion} with a {@code create_agent} span,
 * extracting agent-type-specific attributes (prompt / hosted / workflow) and emitting a {@code gen_ai.agent.workflow}
 * event for workflow agents.
 *
 * <p>Constructed with a per-client {@link GenAiInstrumentation}; there is no global state.</p>
 */
public final class GenAiAgentTracing {

    private final GenAiInstrumentation instrumentation;

    /**
     * Creates a {@link GenAiAgentTracing}.
     *
     * @param instrumentation the per-client telemetry holder.
     */
    public GenAiAgentTracing(GenAiInstrumentation instrumentation) {
        this.instrumentation = instrumentation;
    }

    /**
     * Reference to the synchronous operation performing the actual create-agent-version call.
     */
    @FunctionalInterface
    public interface SyncCreateAgentVersionOperation {
        /**
         * Invokes the operation.
         *
         * @param request the serialized request body.
         * @param requestOptions the request options (carries the tracing context).
         * @return the created agent version details.
         */
        AgentVersionDetails invoke(BinaryData request, RequestOptions requestOptions);
    }

    /**
     * Reference to the asynchronous operation performing the actual create-agent-version call.
     */
    @FunctionalInterface
    public interface CreateAgentVersionOperation {
        /**
         * Invokes the operation.
         *
         * @param request the serialized request body.
         * @param requestOptions the request options (carries the tracing context).
         * @return the created agent version details.
         */
        Mono<AgentVersionDetails> invoke(BinaryData request, RequestOptions requestOptions);
    }

    /**
     * Traces the synchronous {@code createAgentVersion} convenience method.
     *
     * @param agentName the agent name.
     * @param definition the agent definition being created.
     * @param operation the operation performing the actual call.
     * @param request the serialized request body.
     * @param requestOptions the request options.
     * @return the created agent version details.
     */
    @SuppressWarnings("try")
    public AgentVersionDetails traceCreateAgentVersion(String agentName, AgentDefinition definition,
        SyncCreateAgentVersionOperation operation, BinaryData request, RequestOptions requestOptions) {
        GenAiTracingScope scope = instrumentation.startCreateAgent(agentName);
        if (scope == null) {
            return operation.invoke(request, requestOptions);
        }
        try {
            applyDefinitionAttributes(scope, agentName, definition);
            AgentVersionDetails result;
            try (AutoCloseable ignored = scope.makeSpanCurrent()) {
                result = operation.invoke(request, requestOptions.setContext(scope.getSpanContext()));
            }
            enrichFromResult(scope, result);
            return result;
        } catch (Exception e) {
            scope.recordError(e);
            sneakyThrows(e);
            return null;
        } finally {
            scope.close();
        }
    }

    /**
     * Traces the asynchronous {@code createAgentVersion} convenience method.
     *
     * @param agentName the agent name.
     * @param definition the agent definition being created.
     * @param operation the operation performing the actual call.
     * @param request the serialized request body.
     * @param requestOptions the request options.
     * @return a {@link Mono} emitting the created agent version details.
     */
    public Mono<AgentVersionDetails> traceCreateAgentVersionAsync(String agentName, AgentDefinition definition,
        CreateAgentVersionOperation operation, BinaryData request, RequestOptions requestOptions) {
        if (!instrumentation.isEnabled()) {
            return operation.invoke(request, requestOptions);
        }

        final Mono<GenAiTracingScope> resourceSupplier = Mono.fromSupplier(() -> {
            GenAiTracingScope scope = instrumentation.startCreateAgent(agentName);
            applyDefinitionAttributes(scope, agentName, definition);
            return scope;
        });

        final Function<GenAiTracingScope, Mono<AgentVersionDetails>> resourceClosure
            = scope -> operation.invoke(request, requestOptions.setContext(scope.getSpanContext())).map(result -> {
                enrichFromResult(scope, result);
                return result;
            });

        return Mono.usingWhen(resourceSupplier, resourceClosure, scope -> {
            scope.close();
            return Mono.empty();
        }, (scope, throwable) -> {
            scope.recordError(throwable);
            scope.close();
            return Mono.empty();
        }, scope -> {
            scope.close();
            return Mono.empty();
        });
    }

    private void applyDefinitionAttributes(GenAiTracingScope scope, String agentName, AgentDefinition definition) {
        if (definition instanceof PromptAgentDefinition) {
            PromptAgentDefinition prompt = (PromptAgentDefinition) definition;
            scope.setAgentAttributes(null, agentName, null, AGENT_TYPE_PROMPT);
            scope.setRequestModelAttributes(prompt.getModel(), prompt.getTemperature(), prompt.getTopP());
            scope.setSystemInstructions(prompt.getInstructions());
        } else if (definition instanceof WorkflowAgentDefinition) {
            WorkflowAgentDefinition workflow = (WorkflowAgentDefinition) definition;
            scope.setAgentAttributes(null, agentName, null, AGENT_TYPE_WORKFLOW);
            emitWorkflowEvent(scope, workflow.getWorkflow());
        } else if (definition instanceof HostedAgentDefinition) {
            HostedAgentDefinition hosted = (HostedAgentDefinition) definition;
            String protocol = null;
            String protocolVersion = null;
            List<ProtocolVersionRecord> protocolVersions = hosted.getProtocolVersions();
            if (protocolVersions != null && !protocolVersions.isEmpty()) {
                ProtocolVersionRecord record = protocolVersions.get(0);
                protocol = record.getProtocol() != null ? record.getProtocol().toString() : null;
                protocolVersion = record.getVersion();
            }
            String image
                = hosted.getContainerConfiguration() != null ? hosted.getContainerConfiguration().getImage() : null;
            scope.setAgentAttributes(null, agentName, null, AGENT_TYPE_HOSTED);
            scope.setHostedAgentAttributes(hosted.getCpu(), hosted.getMemory(), image, protocol, protocolVersion);
        } else {
            scope.setAgentAttributes(null, agentName, null, null);
        }
    }

    private void emitWorkflowEvent(GenAiTracingScope scope, String workflowDefinition) {
        String contentArray = GenAiMessageFormatter
            .formatWorkflowEventContent(instrumentation.isContentRecordingEnabled(), workflowDefinition);
        Map<String, Object> eventAttributes = new HashMap<>();
        eventAttributes.put(GEN_AI_PROVIDER_NAME, GEN_AI_PROVIDER_NAME_VALUE);
        eventAttributes.put(GEN_AI_EVENT_CONTENT, contentArray);
        scope.addEvent(GEN_AI_AGENT_WORKFLOW, eventAttributes);
    }

    private static void enrichFromResult(GenAiTracingScope scope, AgentVersionDetails result) {
        if (result != null) {
            String name = result.getName();
            String version = result.getVersion();
            if (name != null && version != null) {
                scope.setAgentIdAndVersion(name + ":" + version, version);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static <E extends Throwable> void sneakyThrows(Throwable e) throws E {
        throw (E) e;
    }
}
