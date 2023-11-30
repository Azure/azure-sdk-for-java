// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.http.policy;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpPipelineNextSyncPolicy;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.tracing.SpanKind;
import com.azure.core.util.tracing.StartSpanOptions;
import com.azure.core.util.tracing.Tracer;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

<<<<<<< HEAD
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import static com.azure.core.http.HttpHeaderName.X_MS_CLIENT_REQUEST_ID;
import static com.azure.core.implementation.logging.LoggingKeys.CANCELLED_ERROR_TYPE;
=======
import java.net.URL;

import static com.azure.core.http.policy.HttpLoggingPolicy.RETRY_COUNT_CONTEXT;
>>>>>>> 5e9ba884c89 (update tracing semconv to 1.23 and stable http)
import static com.azure.core.util.tracing.Tracer.DISABLE_TRACING_KEY;

/**
 * Pipeline policy that initiates distributed tracing.
 */
public class InstrumentationPolicy implements HttpPipelinePolicy {
    private static final String HTTP_REQUEST_METHOD = "http.request.method";
    private static final String HTTP_RESEND_COUNT = "http.request.resend_count";
    private static final String URL_FULL = "url.full";
    private static final String SERVER_ADDRESS = "server.address";
    private static final String SERVER_PORT = "server.port";
    private static final String HTTP_RESPONSE_STATUS_CODE = "http.response.status_code";
    private static final String SERVICE_REQUEST_ID_ATTRIBUTE = "serviceRequestId";
    private static final String CLIENT_REQUEST_ID_ATTRIBUTE = "requestId";
    private static final String ERROR_TYPE_OTHER = "_OTHER";
    private static final HttpHeaderName SERVICE_REQUEST_ID_HEADER = HttpHeaderName.fromString("x-ms-request-id");
    private static final String LEGACY_OTEL_POLICY_NAME = "io.opentelemetry.javaagent.instrumentation.azurecore.v1_19.shaded.com.azure.core.tracing.opentelemetry.OpenTelemetryHttpPolicy";
    private static final ClientLogger LOGGER = new ClientLogger(InstrumentationPolicy.class);

    private Tracer tracer;
    private static boolean foundLegacyOTelPolicy;

    static {
        try {
            Class.forName(LEGACY_OTEL_POLICY_NAME, true, HttpPipelinePolicy.class.getClassLoader());
            foundLegacyOTelPolicy = true;
        } catch (ClassNotFoundException e) {
            foundLegacyOTelPolicy = false;
        }
    }

    static {
        try {
            Class.forName(LEGACY_OTEL_POLICY_NAME, true, HttpPipelinePolicy.class.getClassLoader());
            foundLegacyOTelPolicy = true;
        } catch (ClassNotFoundException e) {
            foundLegacyOTelPolicy = false;
        }
    }

    /**
     * Creates an InstrumentationPolicy.
     */
    public InstrumentationPolicy() {
    }

    /**
     * Initializes the policy with the {@link Tracer} instance.
     *
     * @param tracer the tracer instance.
     */
    public void initialize(Tracer tracer) {
        this.tracer = tracer;
    }

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        if (!isTracingEnabled(context)) {
            return next.process();
        }

        return Mono.defer(() -> {
            Context span = startSpan(context);
            return next.process()
                .doOnSuccess(response -> onResponseCode(response, span))
                // TODO: maybe we can optimize it? https://github.com/Azure/azure-sdk-for-java/issues/38228
                .map(response -> new TraceableResponse(response, span))
                .doOnCancel(() -> tracer.end(CANCELLED_ERROR_TYPE, null, span))
                .doOnError(exception -> tracer.end(null, exception, span));
        });
    }

    @SuppressWarnings("try")
    @Override
    public HttpResponse processSync(HttpPipelineCallContext context, HttpPipelineNextSyncPolicy next) {
        if (!isTracingEnabled(context)) {
            return next.processSync();
        }

        Context span = startSpan(context);
        try (AutoCloseable scope = tracer.makeSpanCurrent(span)) {
            HttpResponse response = next.processSync();
            onResponseCode(response, span);
            // TODO: maybe we can optimize it? https://github.com/Azure/azure-sdk-for-java/issues/38228
            return new TraceableResponse(response, span);
        } catch (RuntimeException ex) {
            tracer.end(null, ex, span);
            throw ex;
        } catch (Exception ex) {
            tracer.end(null, ex, span);
            throw LOGGER.logExceptionAsWarning(new RuntimeException(ex));
        }
    }

    @SuppressWarnings("deprecation")
    private Context startSpan(HttpPipelineCallContext azContext) {
        HttpRequest request = azContext.getHttpRequest();

        // Build new child span representing this outgoing request.
        String methodName = request.getHttpMethod().toString();
        StartSpanOptions spanOptions = new StartSpanOptions(SpanKind.CLIENT)
            .setAttribute(HTTP_REQUEST_METHOD, methodName)
            .setAttribute(URL_FULL, request.getUrl().toString())
            .setAttribute(SERVER_ADDRESS, request.getUrl().getHost())
            .setAttribute(SERVER_PORT, getPort(request.getUrl()));
        Context span = tracer.start(methodName, spanOptions, azContext.getContext());

        addPostSamplingAttributes(span, request);

        // TODO (alzimmer): Add injectContext(BiConsumer<HttpHeaderName, String>, Context)
        tracer.injectContext((k, v) -> request.getHeaders().set(k, v), span);
        return span;
    }

    private static int getPort(URL url) {
        int port = url.getPort();
        if (port == -1) {
            port = url.getDefaultPort();
        }
        return port;
    }

    private void addPostSamplingAttributes(Context span, HttpRequest request) {
        Object rawRetryCount = span.getData(RETRY_COUNT_CONTEXT).orElse(null);
        if (rawRetryCount instanceof Integer && ((Integer) rawRetryCount) > 0) {
            tracer.setAttribute(HTTP_RESEND_COUNT, ((Integer) rawRetryCount).longValue(), span);
        }

        String requestId = request.getHeaders().getValue(X_MS_CLIENT_REQUEST_ID);
        if (!CoreUtils.isNullOrEmpty(requestId)) {
            tracer.setAttribute(CLIENT_REQUEST_ID_ATTRIBUTE, requestId, span);
        }
    }

    private void onResponseCode(HttpResponse response, Context span) {
        if (response != null) {
            int statusCode = response.getStatusCode();
            tracer.setAttribute(HTTP_RESPONSE_STATUS_CODE, statusCode, span);
            String requestId = response.getHeaderValue(SERVICE_REQUEST_ID_HEADER);
            if (requestId != null) {
                tracer.setAttribute(SERVICE_REQUEST_ID_ATTRIBUTE, requestId, span);
            }
        }
    }

    private boolean isTracingEnabled(HttpPipelineCallContext context) {
        return tracer != null && tracer.isEnabled() && !foundLegacyOTelPolicy
            && !((boolean) context.getData(DISABLE_TRACING_KEY).orElse(false));
    }

    private final class TraceableResponse extends HttpResponse {
        private final HttpResponse response;
        private final Context span;
        private Throwable exception;
        private String errorType;
        TraceableResponse(HttpResponse response, Context span) {
            super(response.getRequest());
            this.response = response;
            this.span = span;
        }

        @Override
        public int getStatusCode() {
            return response.getStatusCode();
        }

        @Deprecated
        @Override
        public String getHeaderValue(String name) {
            return response.getHeaderValue(name);
        }

        @Override
        public String getHeaderValue(HttpHeaderName headerName) {
            return response.getHeaderValue(headerName);
        }

        @Override
        public HttpHeaders getHeaders() {
            return response.getHeaders();
        }

        @Override
        public Flux<ByteBuffer> getBody() {
            return response.getBody()
                    .doOnError(e -> exception = e)
                    .doOnCancel(() -> errorType = CANCELLED_ERROR_TYPE);
        }

        @Override
        public Mono<byte[]> getBodyAsByteArray() {
            return response.getBodyAsByteArray()
                    .doOnError(e -> exception = e)
                    .doOnCancel(() -> errorType = CANCELLED_ERROR_TYPE);
        }

        @Override
        public Mono<String> getBodyAsString() {
            return response.getBodyAsString()
                    .doOnError(e -> exception = e)
                    .doOnCancel(() -> errorType = CANCELLED_ERROR_TYPE);
        }

        @Override
        public BinaryData getBodyAsBinaryData() {
            try {
                return response.getBodyAsBinaryData();
            } catch (Exception e) {
                exception = e;
                throw e;
            }
        }

        @Override
        public Mono<String> getBodyAsString(Charset charset) {
            return response.getBodyAsString(charset)
                    .doOnError(e -> exception = e)
                    .doOnCancel(() -> errorType = CANCELLED_ERROR_TYPE);
        }

        @Override
        public Mono<InputStream> getBodyAsInputStream() {
            return response.getBodyAsInputStream()
                    .doOnError(e -> exception = e)
                    .doOnCancel(() -> errorType = CANCELLED_ERROR_TYPE);
        }

        @Override
        public void close() {
            response.close();
            int statusCode = response.getStatusCode();

            if (errorType == null && statusCode >= 400) {
                errorType = String.valueOf(statusCode);
            }
            tracer.end(null, exception, span);
        }
    }
}
