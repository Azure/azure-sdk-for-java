// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.persistent.implementation;

import com.azure.ai.agents.persistent.models.CreateAgentOptions;
import com.azure.ai.agents.persistent.models.PersistentAgent;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.util.Configuration;
import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.metrics.Meter;
import com.azure.core.util.tracing.Tracer;
import reactor.core.publisher.Mono;
import java.util.HashMap;
import java.util.Map;

/**
 * Tracer for the convenience methods in {@link com.azure.ai.agents.persistent.PersistentAgentsAdministrationClient} and
 * {@link com.azure.ai.agents.persistent.PersistentAgentsAdministrationAsyncClient}.
 * <p>
 * For more about the OTel semantic conventions this type enables, see
 * <a href="https://github.com/open-telemetry/semantic-conventions/blob/v1.27.0/docs/gen-ai">Gen AI semantic conventions</a>.
 * </p>
 */
public class PersistentAgentsAdministrationClientTracer extends ClientTracer {

    private static final ClientLogger LOGGER = new ClientLogger(PersistentAgentsAdministrationClientTracer.class);

    static final String GEN_AI_AGENT_NAME_KEY = "gen_ai.agent.name";
    static final String GEN_AI_REQUEST_TEMPERATURE_KEY = "gen_ai.request.temperature";
    static final String GEN_AI_REQUEST_TOP_P_KEY = "gen_ai.request.top_p";
    static final String GEN_AI_REQUEST_MODEL_KEY = "gen_ai.request.model";
    static final String GEN_AI_AGENT_ID_KEY = "gen_ai.agent.id";
    static final String GEN_AI_SYSTEM_VALUE = "az.ai.agents";

    /**
     * Creates PersistentAgentsAdministrationClientTracer.
     *
     * @param endpoint the service endpoint.
     * @param configuration the {@link Configuration} instance to check if message content needs to be captured,
     *     if {@code null} is passed then {@link Configuration#getGlobalConfiguration()} will be used.
     * @param tracer the Tracer instance.
     */
    public PersistentAgentsAdministrationClientTracer(String endpoint, Configuration configuration, Tracer tracer,
        Meter meter) {
        super(endpoint, configuration, tracer, meter);
    }

    //<editor-fold desc="Tracing CreateAgent">

    static final String OPERATION_CREATE_AGENT = "create_agent";

    /**
     * Traces the synchronous convenience API - create agent operation.
     *
     * @param createAgentOptions input options containing agent creation parameters.
     * @param operation the operation performing the actual create agent call.
     * @param requestOptions The requestOptions parameter for the {@code operation}.
     * @return persistent agent created from the request.
     */
    public PersistentAgent traceCreateAgentSync(CreateAgentOptions createAgentOptions,
        Operation<PersistentAgent> operation, RequestOptions requestOptions) {

        return this.traceSyncOperation(getCreateAgentSpanName(createAgentOptions), operation, requestOptions,
            (span) -> {
                traceCreateAgentInvocationAttributes(createAgentOptions, span);
            }, this::traceCreateAgentResponseAttributes);
    }

    /**
     * Traces the asynchronous convenience API - create agent operation.
     *
     * @param createAgentOptions input options containing agent creation parameters.
     * @param operation the operation performing the actual create agent call.
     * @param requestOptions The requestOptions parameter for the {@code operation}.
     * @return persistent agent created from the request.
     */
    public Mono<PersistentAgent> traceCreateAgentAsync(CreateAgentOptions createAgentOptions,
        Operation<Mono<PersistentAgent>> operation, RequestOptions requestOptions) {

        return this.traceAsyncMonoOperation(getCreateAgentSpanName(createAgentOptions), operation, requestOptions,
            (span) -> {
                traceCreateAgentInvocationAttributes(createAgentOptions, span);
            }, (span, traceAttributes, result) -> result.flatMap(agent -> {
                traceCreateAgentResponseAttributes(span, traceAttributes, agent);
                return Mono.empty();
            }));
    }

    String getCreateAgentSpanName(CreateAgentOptions options) {
        if (options == null || CoreUtils.isNullOrEmpty(options.getName())) {
            return OPERATION_CREATE_AGENT;
        }
        return OPERATION_CREATE_AGENT + " " + options.getName();
    }

    void traceCreateAgentInvocationAttributes(CreateAgentOptions createAgentOptions, Context span) {
        // Set request attributes
        if (createAgentOptions != null) {
            this.setAttributeIfNotNull(GEN_AI_REQUEST_MODEL_KEY, createAgentOptions.getModel(), span);
            this.setAttributeIfNotNull(GEN_AI_AGENT_NAME_KEY, createAgentOptions.getName(), span);
            this.setAttributeIfNotNull(GEN_AI_REQUEST_TEMPERATURE_KEY, createAgentOptions.getTemperature(), span);
            this.setAttributeIfNotNull(GEN_AI_REQUEST_TOP_P_KEY, createAgentOptions.getTopP(), span);
        }

        // Record system message event if content capture is enabled
        if (traceContent
            && createAgentOptions != null
            && !CoreUtils.isNullOrEmpty(createAgentOptions.getInstructions())) {
            Map<String, Object> eventAttributes = new HashMap<>();
            eventAttributes.put(GEN_AI_SYSTEM_KEY, GEN_AI_SYSTEM_VALUE);

            Map<String, Object> contentMap = new HashMap<>();
            putIfNotNullOrEmpty(contentMap, "content", createAgentOptions.getInstructions());

            String eventContent = toJsonString(contentMap);
            if (eventContent != null) {
                eventAttributes.put(GEN_AI_EVENT_CONTENT, eventContent);
                tracer.addEvent(EVENT_NAME_SYSTEM_MESSAGE, eventAttributes, null, span);
            }
        }
    }

    void traceCreateAgentResponseAttributes(Context span, Map<String, Object> traceAttributes, PersistentAgent agent) {
        if (agent != null && !CoreUtils.isNullOrEmpty(agent.getId())) {
            tracer.setAttribute(GEN_AI_AGENT_ID_KEY, agent.getId(), span);
        }
    }

    //</editor-fold>
}
