// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.implementation.telemetry;

import com.azure.ai.agents.models.AgentDefinition;
import com.azure.ai.agents.models.AgentVersionDetails;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Configuration;
import com.azure.core.util.ConfigurationProperty;
import com.azure.core.util.ConfigurationPropertyBuilder;
import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.tracing.SpanKind;
import com.azure.core.util.tracing.StartSpanOptions;
import com.azure.core.util.tracing.Tracer;
import reactor.core.publisher.Mono;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Tracing for the agent-management convenience methods on
 * {@link com.azure.ai.agents.AgentsClient} and {@link com.azure.ai.agents.AgentsAsyncClient}.
 *
 * <p>This type emits OpenTelemetry GenAI semantic-convention spans for agent operations. It follows the
 * azure-core tracing model used by other libraries in this repository (for example
 * {@code azure-ai-inference}): a per-client {@link Tracer} is built by the client builder from
 * {@link com.azure.core.util.ClientOptions#getTracingOptions()} and injected into the clients. There is no
 * global static state and no explicit opt-in call — tracing activates whenever an OpenTelemetry
 * {@code Tracer} is configured for the process.</p>
 *
 * <p>Agent content (for example the agent description) is only captured when content recording is explicitly
 * enabled via the {@code AZURE_TRACING_GEN_AI_CONTENT_RECORDING_ENABLED} environment variable (or the
 * {@code azure.tracing.gen_ai.content_recording_enabled} system property); it is off by default.</p>
 */
public final class AgentsClientTracer {

    /**
     * OpenTelemetry schema URL for the GenAI semantic conventions emitted by this tracer.
     */
    public static final String OTEL_SCHEMA_URL = "https://opentelemetry.io/schemas/1.29.0";

    private static final ClientLogger LOGGER = new ClientLogger(AgentsClientTracer.class);
    private static final String AGENTS_GEN_AI_SYSTEM_NAME = "az.ai.agents";
    private static final String OPERATION_CREATE_AGENT = "create_agent";
    private static final int DEFAULT_HTTPS_PORT = 443;
    private static final StartSpanOptions START_SPAN_OPTIONS = new StartSpanOptions(SpanKind.CLIENT);

    // GenAI semantic-convention attribute keys.
    private static final String GEN_AI_OPERATION_NAME = "gen_ai.operation.name";
    private static final String GEN_AI_SYSTEM = "gen_ai.system";
    private static final String GEN_AI_AGENT_NAME = "gen_ai.agent.name";
    private static final String GEN_AI_AGENT_ID = "gen_ai.agent.id";
    private static final String GEN_AI_AGENT_DESCRIPTION = "gen_ai.agent.description";
    private static final String GEN_AI_AGENT_KIND = "gen_ai.azure.agent.kind";
    private static final String SERVER_ADDRESS = "server.address";
    private static final String SERVER_PORT = "server.port";

    private static final ConfigurationProperty<Boolean> CAPTURE_MESSAGE_CONTENT
        = ConfigurationPropertyBuilder.ofBoolean("azure.tracing.gen_ai.content_recording_enabled")
            .environmentVariableName("AZURE_TRACING_GEN_AI_CONTENT_RECORDING_ENABLED")
            .systemPropertyName("azure.tracing.gen_ai.content_recording_enabled")
            .shared(true)
            .defaultValue(false)
            .build();
    private static final Configuration GLOBAL_CONFIG = Configuration.getGlobalConfiguration();

    private final String host;
    private final int port;
    private final boolean captureContent;
    private final Tracer tracer;

    /**
     * Reference to the synchronous operation performing the actual create-agent-version call.
     */
    @FunctionalInterface
    public interface SyncCreateAgentVersionOperation {
        /**
         * Invokes the operation.
         *
         * @param request the serialized request body.
         * @param requestOptions the request options (carries the tracing {@link Context}).
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
         * @param requestOptions the request options (carries the tracing {@link Context}).
         * @return the created agent version details.
         */
        Mono<AgentVersionDetails> invoke(BinaryData request, RequestOptions requestOptions);
    }

    /**
     * Creates an {@link AgentsClientTracer}.
     *
     * @param endpoint the service endpoint; used to populate {@code server.address}/{@code server.port}.
     * @param configuration the {@link Configuration} used to resolve the content-recording flag; if {@code null}
     *     the global configuration is used.
     * @param tracer the azure-core {@link Tracer} the client builder created from the tracing options.
     */
    public AgentsClientTracer(String endpoint, Configuration configuration, Tracer tracer) {
        final URL url = parse(endpoint);
        if (url != null) {
            this.host = url.getHost();
            this.port = url.getPort() == -1 ? url.getDefaultPort() : url.getPort();
        } else {
            this.host = null;
            this.port = -1;
        }
        this.captureContent = configuration == null
            ? GLOBAL_CONFIG.get(CAPTURE_MESSAGE_CONTENT)
            : configuration.get(CAPTURE_MESSAGE_CONTENT);
        this.tracer = tracer;
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
        if (!tracer.isEnabled()) {
            return operation.invoke(request, requestOptions);
        }
        final Context span = tracer.start(spanName(agentName), START_SPAN_OPTIONS, parentSpan(requestOptions));
        if (tracer.isRecording(span)) {
            setRequestAttributes(agentName, definition, span);
        }
        try (AutoCloseable ignored = tracer.makeSpanCurrent(span)) {
            final AgentVersionDetails result = operation.invoke(request, requestOptions.setContext(span));
            if (tracer.isRecording(span)) {
                setResponseAttributes(result, span);
            }
            tracer.end(null, null, span);
            return result;
        } catch (Exception e) {
            tracer.end(null, e, span);
            sneakyThrows(e);
        }
        return null;
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
        if (!tracer.isEnabled()) {
            return operation.invoke(request, requestOptions);
        }

        final Mono<Context> resourceSupplier = Mono.fromSupplier(() -> {
            final Context span = tracer.start(spanName(agentName), START_SPAN_OPTIONS, parentSpan(requestOptions));
            if (tracer.isRecording(span)) {
                setRequestAttributes(agentName, definition, span);
            }
            return span;
        });

        final Function<Context, Mono<AgentVersionDetails>> resourceClosure
            = span -> operation.invoke(request, requestOptions.setContext(span)).map(result -> {
                if (tracer.isRecording(span)) {
                    setResponseAttributes(result, span);
                }
                return result;
            });

        final Function<Context, Mono<Void>> asyncComplete = span -> {
            tracer.end(null, null, span);
            return Mono.empty();
        };

        final BiFunction<Context, Throwable, Mono<Void>> asyncError = (span, throwable) -> {
            tracer.end(null, throwable, span);
            return Mono.empty();
        };

        final Function<Context, Mono<Void>> asyncCancel = span -> {
            tracer.end("cancelled", null, span);
            return Mono.empty();
        };

        return Mono.usingWhen(resourceSupplier, resourceClosure, asyncComplete, asyncError, asyncCancel);
    }

    private String spanName(String agentName) {
        return CoreUtils.isNullOrEmpty(agentName) ? OPERATION_CREATE_AGENT : OPERATION_CREATE_AGENT + " " + agentName;
    }

    private void setRequestAttributes(String agentName, AgentDefinition definition, Context span) {
        tracer.setAttribute(GEN_AI_OPERATION_NAME, OPERATION_CREATE_AGENT, span);
        tracer.setAttribute(GEN_AI_SYSTEM, AGENTS_GEN_AI_SYSTEM_NAME, span);
        if (!CoreUtils.isNullOrEmpty(agentName)) {
            tracer.setAttribute(GEN_AI_AGENT_NAME, agentName, span);
        }
        if (definition != null && definition.getKind() != null) {
            tracer.setAttribute(GEN_AI_AGENT_KIND, definition.getKind().toString(), span);
        }
        if (host != null) {
            tracer.setAttribute(SERVER_ADDRESS, host, span);
            if (port != DEFAULT_HTTPS_PORT) {
                tracer.setAttribute(SERVER_PORT, port, span);
            }
        }
    }

    private void setResponseAttributes(AgentVersionDetails result, Context span) {
        if (result == null) {
            return;
        }
        if (!CoreUtils.isNullOrEmpty(result.getId())) {
            tracer.setAttribute(GEN_AI_AGENT_ID, result.getId(), span);
        }
        // Description may contain user-authored content, so it is only captured when content recording is enabled.
        if (captureContent && !CoreUtils.isNullOrEmpty(result.getDescription())) {
            tracer.setAttribute(GEN_AI_AGENT_DESCRIPTION, result.getDescription(), span);
        }
    }

    private static URL parse(String endpoint) {
        if (CoreUtils.isNullOrEmpty(endpoint)) {
            return null;
        }
        try {
            return new URI(endpoint).toURL();
        } catch (MalformedURLException | URISyntaxException e) {
            LOGGER.atWarning().log("Service endpoint URI parse error.", e);
        }
        return null;
    }

    private static Context parentSpan(RequestOptions requestOptions) {
        return requestOptions.getContext() == null ? Context.NONE : requestOptions.getContext();
    }

    @SuppressWarnings("unchecked")
    private static <E extends Throwable> void sneakyThrows(Throwable e) throws E {
        throw (E) e;
    }
}
