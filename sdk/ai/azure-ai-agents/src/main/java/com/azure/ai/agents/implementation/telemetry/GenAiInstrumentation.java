// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.implementation.telemetry;

import com.azure.core.util.Configuration;
import com.azure.core.util.ConfigurationProperty;
import com.azure.core.util.ConfigurationPropertyBuilder;
import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.metrics.DoubleHistogram;
import com.azure.core.util.metrics.Meter;
import com.azure.core.util.tracing.SpanKind;
import com.azure.core.util.tracing.StartSpanOptions;
import com.azure.core.util.tracing.Tracer;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import static com.azure.ai.agents.implementation.telemetry.GenAiConstants.DEFAULT_HTTPS_PORT;
import static com.azure.ai.agents.implementation.telemetry.GenAiConstants.GEN_AI_OPERATION_NAME;
import static com.azure.ai.agents.implementation.telemetry.GenAiConstants.GEN_AI_PROVIDER_NAME;
import static com.azure.ai.agents.implementation.telemetry.GenAiConstants.GEN_AI_PROVIDER_NAME_VALUE;
import static com.azure.ai.agents.implementation.telemetry.GenAiConstants.GEN_AI_SYSTEM;
import static com.azure.ai.agents.implementation.telemetry.GenAiConstants.GEN_AI_SYSTEM_VALUE;
import static com.azure.ai.agents.implementation.telemetry.GenAiConstants.METRIC_OPERATION_DURATION;
import static com.azure.ai.agents.implementation.telemetry.GenAiConstants.METRIC_TOKEN_USAGE;
import static com.azure.ai.agents.implementation.telemetry.GenAiConstants.METRIC_UNIT_SECONDS;
import static com.azure.ai.agents.implementation.telemetry.GenAiConstants.METRIC_UNIT_TOKENS;
import static com.azure.ai.agents.implementation.telemetry.GenAiConstants.OPERATION_CHAT;
import static com.azure.ai.agents.implementation.telemetry.GenAiConstants.OPERATION_CREATE_AGENT;
import static com.azure.ai.agents.implementation.telemetry.GenAiConstants.OPERATION_CREATE_CONVERSATION;
import static com.azure.ai.agents.implementation.telemetry.GenAiConstants.OPERATION_INVOKE_AGENT;
import static com.azure.ai.agents.implementation.telemetry.GenAiConstants.SERVER_ADDRESS;
import static com.azure.ai.agents.implementation.telemetry.GenAiConstants.SERVER_PORT;

/**
 * Per-client GenAI telemetry holder. Owns the azure-core {@link Tracer} and {@link Meter} (and the GenAI
 * histograms) that the client builder created from {@code ClientOptions} tracing/metrics options, along with the
 * resolved endpoint host/port and the content-recording flag.
 *
 * <p>This replaces the process-global static state used elsewhere: an instance is created per client by
 * {@code AgentsClientBuilder} and injected into the clients. Tracing/metrics are emitted only when an
 * OpenTelemetry implementation is configured for the process (i.e. {@link Tracer#isEnabled()} or a histogram is
 * enabled).</p>
 */
public final class GenAiInstrumentation {

    /**
     * OpenTelemetry schema URL for the GenAI semantic conventions emitted by this instrumentation.
     */
    public static final String OTEL_SCHEMA_URL = "https://opentelemetry.io/schemas/1.29.0";

    private static final ClientLogger LOGGER = new ClientLogger(GenAiInstrumentation.class);

    private static final ConfigurationProperty<Boolean> CAPTURE_MESSAGE_CONTENT
        = ConfigurationPropertyBuilder.ofBoolean("azure.tracing.gen_ai.content_recording_enabled")
            .environmentVariableName("AZURE_TRACING_GEN_AI_CONTENT_RECORDING_ENABLED")
            .systemPropertyName("azure.tracing.gen_ai.content_recording_enabled")
            .shared(true)
            .defaultValue(false)
            .build();
    private static final Configuration GLOBAL_CONFIG = Configuration.getGlobalConfiguration();

    private final Tracer tracer;
    private final Meter meter;
    private final DoubleHistogram durationHistogram;
    private final DoubleHistogram tokenUsageHistogram;
    private final boolean captureContent;
    private final String host;
    private final int port;

    /**
     * Creates a {@link GenAiInstrumentation}.
     *
     * @param endpoint the service endpoint; used to populate {@code server.address}/{@code server.port}.
     * @param configuration the {@link Configuration} used to resolve the content-recording flag; if {@code null}
     *     the global configuration is used.
     * @param tracer the azure-core {@link Tracer} built from the client's tracing options.
     * @param meter the azure-core {@link Meter} built from the client's metrics options.
     */
    public GenAiInstrumentation(String endpoint, Configuration configuration, Tracer tracer, Meter meter) {
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
        this.meter = meter;
        this.durationHistogram = meter.createDoubleHistogram(METRIC_OPERATION_DURATION, "Duration of GenAI operations",
            METRIC_UNIT_SECONDS);
        this.tokenUsageHistogram = meter.createDoubleHistogram(METRIC_TOKEN_USAGE,
            "Number of input and output tokens used", METRIC_UNIT_TOKENS);
    }

    /**
     * @return whether any span or metric collection is active for this instrumentation.
     */
    public boolean isEnabled() {
        return tracer.isEnabled() || durationHistogram.isEnabled() || tokenUsageHistogram.isEnabled();
    }

    boolean isContentRecordingEnabled() {
        return captureContent;
    }

    Tracer tracer() {
        return tracer;
    }

    Meter meter() {
        return meter;
    }

    DoubleHistogram durationHistogram() {
        return durationHistogram;
    }

    DoubleHistogram tokenUsageHistogram() {
        return tokenUsageHistogram;
    }

    GenAiTracingScope startCreateAgent(String agentName) {
        return startScope(OPERATION_CREATE_AGENT, agentName);
    }

    GenAiTracingScope startInvokeAgent(String agentName) {
        return startScope(OPERATION_INVOKE_AGENT, agentName);
    }

    GenAiTracingScope startChat(String modelName) {
        return startScope(OPERATION_CHAT, modelName);
    }

    GenAiTracingScope startCreateConversation() {
        return startScope(OPERATION_CREATE_CONVERSATION, null);
    }

    private GenAiTracingScope startScope(String operationName, String spanNameSuffix) {
        if (!isEnabled()) {
            return null;
        }

        String spanName = spanNameSuffix != null ? operationName + " " + spanNameSuffix : operationName;
        String serverAddress = host != null ? host : "unknown";
        int serverPort = port > 0 ? port : DEFAULT_HTTPS_PORT;

        StartSpanOptions options
            = new StartSpanOptions(SpanKind.CLIENT).setAttribute(GEN_AI_OPERATION_NAME, operationName)
                .setAttribute(GEN_AI_SYSTEM, GEN_AI_SYSTEM_VALUE)
                .setAttribute(GEN_AI_PROVIDER_NAME, GEN_AI_PROVIDER_NAME_VALUE)
                .setAttribute(SERVER_ADDRESS, serverAddress);
        if (serverPort != DEFAULT_HTTPS_PORT) {
            options.setAttribute(SERVER_PORT, (long) serverPort);
        }

        Context spanContext = tracer.start(spanName, options, Context.NONE);
        return new GenAiTracingScope(this, spanContext, operationName, serverAddress, serverPort);
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
}
