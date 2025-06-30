// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.agents.persistent.implementation;

import com.azure.core.http.rest.RequestOptions;
import com.azure.core.util.Configuration;
import com.azure.core.util.ConfigurationProperty;
import com.azure.core.util.ConfigurationPropertyBuilder;
import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.metrics.DoubleHistogram;
import com.azure.core.util.metrics.LongCounter;
import com.azure.core.util.metrics.Meter;
import com.azure.core.util.serializer.JsonSerializerProviders;
import com.azure.core.util.serializer.TypeReference;
import com.azure.core.util.tracing.SpanKind;
import com.azure.core.util.tracing.StartSpanOptions;
import com.azure.core.util.tracing.Tracer;
import com.azure.json.JsonProviders;
import com.azure.json.JsonWriter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class ClientTracer {
    public static final String OTEL_SCHEMA_URL = "https://opentelemetry.io/schemas/1.27.0";

    /**
     * Reference to the operation performing the actual call.
     */
    @FunctionalInterface
    public interface Operation<T> {
        T invoke(RequestOptions requestOptions);
    }

    @FunctionalInterface
    public interface TraceBeforeInvocation {
        void invoke(Context span);
    }

    @FunctionalInterface
    public interface TraceAfterInvocation<T> {
        void invoke(Context span, Map<String, Object> traceAttributes, T result);
    }

    @FunctionalInterface
    public interface TraceAfterInvocationAsync<T> {
        Mono<Void> invoke(Context span, Map<String, Object> traceAttributes, T result);
    }

    private static final ClientLogger LOGGER = new ClientLogger(ClientTracer.class);

    protected static final StartSpanOptions START_SPAN_OPTIONS = new StartSpanOptions(SpanKind.CLIENT);
    protected static final ConfigurationProperty<Boolean> TRACE_CONTENT
        = ConfigurationPropertyBuilder.ofBoolean("azure.tracing.gen_ai.content_recording_enabled")
            .environmentVariableName("AZURE_TRACING_GEN_AI_CONTENT_RECORDING_ENABLED")
            .systemPropertyName("azure.tracing.gen_ai.content_recording_enabled")
            .shared(true)
            .defaultValue(false)
            .build();

    // OpenTelemetry constants - Based on OpenTelemetryConstants.cs
    protected static final String ERROR_TYPE_KEY = "error.type";
    protected static final String ERROR_MESSAGE_KEY = "error.message";
    protected static final String AZ_NAMESPACE_KEY = "az.namespace";
    protected static final String SERVER_ADDRESS_KEY = "server.address";
    protected static final String SERVER_PORT_KEY = "server.port";
    protected static final String GEN_AI_SYSTEM_VALUE = "az.ai.agents";
    protected static final String GEN_AI_OPERATION_NAME_KEY = "gen_ai.operation.name";
    protected static final String GEN_AI_SYSTEM_KEY = "gen_ai.system";
    protected static final String GEN_AI_EVENT_CONTENT = "gen_ai.event.content";
    protected static final String EVENT_NAME_SYSTEM_MESSAGE = "gen_ai.system.message";
    protected static final String GEN_AI_CLIENT_OPERATION_DURATION_METRIC_NAME = "gen_ai.client.operation.duration";
    protected static final String GEN_AI_CLIENT_TOKEN_USAGE_METRIC_NAME = "gen_ai.client.token.usage";

    protected static final String AZURE_RP_NAMESPACE_VALUE = "Microsoft.CognitiveServices";

    protected static final Configuration GLOBAL_CONFIG = Configuration.getGlobalConfiguration();

    protected final String host;
    protected final int port;
    protected final boolean traceContent;
    protected final Tracer tracer;
    protected final Meter meter;
    protected final DoubleHistogram durationHistogram;
    protected final LongCounter tokensCounter;

    protected Function<Context, Mono<Void>> getAsyncComplete() {
        return ((span) -> {
            tracer.end(null, null, span);
            return Mono.empty();
        });
    }

    protected BiFunction<Context, Throwable, Mono<Void>> getAsyncError() {
        return (span, throwable) -> {
            if (tracer.isRecording(span)) {
                traceErrorAttributes(span, throwable);
            }
            tracer.end(null, throwable, span);
            return Mono.empty();
        };
    }

    protected Function<Context, Mono<Void>> getAsyncCancel() {
        return span -> {
            tracer.end("cancelled", null, span);
            return Mono.empty();
        };
    }

    /**
     * Creates BaseClientTracer.
     *
     * @param endpoint the service endpoint.
     * @param configuration the {@link Configuration} instance to check if message content needs to be captured,
     *     if {@code null} is passed then {@link Configuration#getGlobalConfiguration()} will be used.
     * @param tracer the Tracer instance.
     */
    protected ClientTracer(String endpoint, Configuration configuration, Tracer tracer, Meter meter) {
        final URL url = parse(endpoint);
        if (url != null) {
            this.host = url.getHost();
            this.port = url.getPort() == -1 ? url.getDefaultPort() : url.getPort();
        } else {
            this.host = null;
            this.port = -1;
        }
        this.traceContent = configuration == null ? GLOBAL_CONFIG.get(TRACE_CONTENT) : configuration.get(TRACE_CONTENT);
        this.tracer = tracer;
        this.meter = meter;
        this.durationHistogram = meter.createDoubleHistogram(GEN_AI_CLIENT_OPERATION_DURATION_METRIC_NAME,
            "Measures GenAI operation duration.", "s");
        this.tokensCounter = meter.createLongCounter(GEN_AI_CLIENT_TOKEN_USAGE_METRIC_NAME,
            "Measures the number of input and output token used.", "{token}");
    }

    private Map<String, Object> traceCommonAttributes(Context span, String operationName) {
        Map<String, Object> commonAttributes = new HashMap<>();
        commonAttributes.put(GEN_AI_SYSTEM_KEY, GEN_AI_SYSTEM_VALUE);
        commonAttributes.put(GEN_AI_OPERATION_NAME_KEY, operationName);
        commonAttributes.put(AZ_NAMESPACE_KEY, AZURE_RP_NAMESPACE_VALUE);

        // set server attributes
        if (host != null) {
            commonAttributes.put(SERVER_ADDRESS_KEY, host);
            if (port != -1 && port != 443) {
                commonAttributes.put(SERVER_PORT_KEY, port);
            }
        }
        for (Map.Entry<String, Object> entry : commonAttributes.entrySet()) {
            tracer.setAttribute(entry.getKey(), entry.getValue(), span);
        }
        return commonAttributes;
    }

    @SuppressWarnings("try")
    protected <T> T traceSyncOperation(String operationName, Operation<T> operation, RequestOptions requestOptions,
        TraceBeforeInvocation traceBeforeInvocation, TraceAfterInvocation<T> traceAfterInvocation) {
        if (!tracer.isEnabled()) {
            return operation.invoke(requestOptions);
        }
        final Context span = tracer.start(operationName, START_SPAN_OPTIONS, parentSpan(requestOptions));
        Instant startTime = Instant.now();
        Map<String, Object> traceAttributes = this.traceCommonAttributes(span, operationName);
        if (tracer.isRecording(span)) {
            safeInvoke(() -> traceBeforeInvocation.invoke(span));
        }

        try (AutoCloseable ignored = tracer.makeSpanCurrent(span)) {
            final T result = operation.invoke(requestOptions.setContext(span));

            if (tracer.isRecording(span) && result != null) {
                safeInvoke(() -> traceAfterInvocation.invoke(span, traceAttributes, result));
            }
            recordDuration(span, startTime, traceAttributes);
            tracer.end(null, null, span);
            return result;
        } catch (Exception e) {
            if (tracer.isRecording(span)) {
                traceErrorAttributes(span, e);
            }
            traceAttributes.put(ERROR_TYPE_KEY, e.getClass().getName());
            recordDuration(span, startTime, traceAttributes);
            tracer.end(null, e, span);
            sneakyThrows(e);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    protected <T extends Mono<R>, R> T traceAsyncMonoOperation(String spanName, Operation<T> operation,
        RequestOptions requestOptions, TraceBeforeInvocation traceBeforeInvocation,
        TraceAfterInvocationAsync<T> traceAfterInvocation) {
        if (!tracer.isEnabled()) {
            return operation.invoke(requestOptions);
        }

        final Mono<Context> resourceSupplier
            = Mono.fromSupplier(() -> tracer.start(spanName, START_SPAN_OPTIONS, parentSpan(requestOptions)));

        final Function<Context, Mono<R>> resourceClosure = span -> {
            final Map<String, Object> traceAttributes = this.traceCommonAttributes(span, spanName);
            final Instant startTime = Instant.now();

            if (tracer.isRecording(span)) {
                safeInvoke(() -> traceBeforeInvocation.invoke(span));
            }
            T response = operation.invoke(requestOptions.setContext(span));
            return (T) response.doOnSuccess((result) -> {
                if (tracer.isRecording(span)) {
                    traceAfterInvocation.invoke(span, traceAttributes, response).doFinally(signalType -> {
                        recordDuration(span, startTime, traceAttributes);
                    }).onErrorResume(error -> {
                        LOGGER.verbose("Error in traceAfterInvocation", error);
                        return Mono.empty();
                    }).subscribe();
                } else {
                    recordDuration(span, startTime, traceAttributes);
                }
            }).doOnError(error -> {
                if (tracer.isRecording(span)) {
                    traceErrorAttributes(span, error);
                }
                traceAttributes.put(ERROR_TYPE_KEY, error.getClass().getName());
                recordDuration(span, startTime, traceAttributes);
            });
        };

        return (T) Mono.usingWhen(resourceSupplier, resourceClosure, getAsyncComplete(), getAsyncError(),
            getAsyncCancel());
    }

    @SuppressWarnings("unchecked")
    protected <T extends Flux<R>, R> T traceAsyncFluxOperation(String spanName, Operation<T> operation,
        RequestOptions requestOptions, TraceBeforeInvocation traceBeforeInvocation,
        TraceAfterInvocationAsync<T> traceAfterInvocation) {
        if (!tracer.isEnabled()) {
            return operation.invoke(requestOptions);
        }

        final Mono<Context> resourceSupplier
            = Mono.fromSupplier(() -> tracer.start(spanName, START_SPAN_OPTIONS, parentSpan(requestOptions)));

        final Function<Context, T> resourceClosure = span -> {
            final Map<String, Object> traceAttributes = this.traceCommonAttributes(span, spanName);
            final Instant startTime = Instant.now();

            if (tracer.isRecording(span)) {
                safeInvoke(() -> traceBeforeInvocation.invoke(span));
            }

            T response = operation.invoke(requestOptions.setContext(span));
            return (T) response.doOnComplete(() -> {
                if (tracer.isRecording(span)) {
                    traceAfterInvocation.invoke(span, traceAttributes, response).doFinally(signalType -> {
                        recordDuration(span, startTime, traceAttributes);
                    }).onErrorResume(error -> {
                        LOGGER.verbose("Error in traceAfterInvocation", error);
                        return Mono.empty();
                    }).subscribe();
                } else {
                    recordDuration(span, startTime, traceAttributes);
                }
            }).doOnError(error -> {
                if (tracer.isRecording(span)) {
                    traceErrorAttributes(span, error);
                }
                traceAttributes.put(ERROR_TYPE_KEY, error.getClass().getName());
                recordDuration(span, startTime, traceAttributes);
            });
        };

        return (T) Flux.usingWhen(resourceSupplier, resourceClosure, getAsyncComplete(), getAsyncError(),
            getAsyncCancel());
    }

    // Helper method to record duration metrics
    private void recordDuration(Context span, Instant startTime, Map<String, Object> traceAttributes) {
        if (startTime != null) {
            durationHistogram.record(Instant.now().getEpochSecond() - startTime.getEpochSecond(),
                meter.createAttributes(traceAttributes), span);
        }
    }

    protected void setAttributeIfNotNull(String key, Object value, Context span) {
        if (value != null) {
            tracer.setAttribute(key, value.toString(), span);
        }
    }

    protected void setAttributeIfNotNullOrEmpty(String key, CharSequence value, Context span) {
        if (!CoreUtils.isNullOrEmpty(value)) {
            tracer.setAttribute(key, value, span);
        }
    }

    /**
     * Records error attributes on the span.
     *
     * @param span The current span context.
     * @param e The exception that occurred.
     */
    protected void traceErrorAttributes(Context span, Throwable e) {
        if (e != null) {
            tracer.setAttribute(ERROR_TYPE_KEY, e.getClass().getName(), span);
            this.setAttributeIfNotNull(ERROR_MESSAGE_KEY, e.getMessage(), span);
        }
    }

    //<editor-fold desc="Static utility methods">

    /**
     * Serializes an object to JSON string.
     *
     * @param obj The object to serialize.
     * @return A JSON representation of the object.
     */
    protected static String toJsonString(Object obj) {
        try (ByteArrayOutputStream stream = new ByteArrayOutputStream();
            JsonWriter writer = JsonProviders.createWriter(stream)) {

            if (obj instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> map = (Map<String, Object>) obj;
                writer.writeStartObject();
                for (Map.Entry<String, Object> entry : map.entrySet()) {
                    writeJsonValue(writer, entry.getKey(), entry.getValue());
                }
                writer.writeEndObject();
            } else {
                writer.writeStartObject();
                writer.writeEndObject();
            }

            writer.flush();
            return new String(stream.toByteArray(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            LOGGER.verbose("Object serialization error", e);
        }
        return null;
    }

    /**
     * Helper method to write a value to JSON.
     *
     * @param writer The JSON writer.
     * @param key The key name.
     * @param value The value to write.
     * @throws IOException If an I/O error occurs.
     */
    protected static void writeJsonValue(JsonWriter writer, String key, Object value) throws IOException {
        if (value == null) {
            writer.writeNullField(key);
        } else if (value instanceof String) {
            writer.writeStringField(key, (String) value);
        } else if (value instanceof Number) {
            if (value instanceof Integer) {
                writer.writeIntField(key, (Integer) value);
            } else if (value instanceof Long) {
                writer.writeLongField(key, (Long) value);
            } else if (value instanceof Double) {
                writer.writeDoubleField(key, (Double) value);
            } else if (value instanceof Float) {
                writer.writeFloatField(key, (Float) value);
            } else {
                writer.writeNumberField(key, (Number) value);
            }
        } else if (value instanceof Boolean) {
            writer.writeBooleanField(key, (Boolean) value);
        } else if (value instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) value;
            writer.writeStartObject(key);
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                writeJsonValue(writer, entry.getKey(), entry.getValue());
            }
            writer.writeEndObject();
        } else {
            writer.writeStringField(key, value.toString());
        }
    }

    /**
     * Parses an endpoint string into a URL.
     *
     * @param endpoint The endpoint string to parse.
     * @return The parsed URL, or null if invalid.
     */
    protected static URL parse(String endpoint) {
        if (CoreUtils.isNullOrEmpty(endpoint)) {
            return null;
        }
        try {
            final URI uri = new URI(endpoint);
            return uri.toURL();
        } catch (MalformedURLException | URISyntaxException e) {
            LOGGER.atWarning().log("service endpoint uri parse error.", e);
        }
        return null;
    }

    /**
     * Utility method for "sneaky throws" pattern.
     *
     * @param e The exception to throw.
     * @param <E> The type of exception.
     * @throws E The exception.
     */
    @SuppressWarnings("unchecked")
    protected static <E extends Throwable> void sneakyThrows(Throwable e) throws E {
        throw (E) e;
    }

    protected static void putIfNotNullOrEmpty(Map<String, Object> map, String key, String value) {
        if (!CoreUtils.isNullOrEmpty(value)) {
            map.put(key, value);
        }
    }

    protected static void putIfNotNull(Map<String, Object> map, String key, Object value) {
        if (value != null) {
            map.put(key, value);
        }
    }

    protected static <T> void putIfNotNullOrEmpty(Map<String, Object> map, String key, List<T> value) {
        if (value != null && !value.isEmpty()) {
            map.put(key, value);
        }
    }

    protected static void safeInvoke(Runnable action) {
        try {
            if (action != null) {
                action.run();
            }
        } catch (Exception e) {
            LOGGER.verbose("Error during invocation.", e);
        }
    }

    protected static <T> T safeInvoke(Supplier<T> supplier) {
        try {
            if (supplier != null) {
                return supplier.get();
            }
            return null;
        } catch (Exception e) {
            LOGGER.verbose("Error during invocation.", e);
            return null;
        }
    }

    /**
     * Gets the parent span from request options.
     *
     * @param requestOptions The request options.
     * @return The parent span context.
     */
    protected static Context parentSpan(RequestOptions requestOptions) {
        return requestOptions.getContext() == null ? Context.NONE : requestOptions.getContext();
    }

    @SuppressWarnings("unchecked")
    protected static Map<String, Object> parseJsonString(String jsonString) {
        try {
            return CoreUtils.isNullOrEmpty(jsonString)
                ? Collections.emptyMap()
                : JsonSerializerProviders.createInstance()
                    .deserialize(new ByteArrayInputStream(jsonString.getBytes()),
                        TypeReference.createInstance(HashMap.class));
        } catch (Exception e) {
            LOGGER.warning("Failed to parse JSON arguments: {}", e.getMessage());
            return Collections.emptyMap();
        }
    }

    protected static <T> Map<String, Object> convertObjectToMap(T object) {
        try {
            String json = toJsonString(object);
            return parseJsonString(json);
        } catch (Exception e) {
            LOGGER.warning("Failed to convert tool call attributes: {}", e.getMessage());
            return Collections.emptyMap();
        }
    }
    //</editor-fold>
}
