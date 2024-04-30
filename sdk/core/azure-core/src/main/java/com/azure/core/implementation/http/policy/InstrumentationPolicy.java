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
import com.azure.core.implementation.http.UrlSanitizer;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.tracing.SpanKind;
import com.azure.core.util.tracing.StartSpanOptions;
import com.azure.core.util.tracing.Tracer;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.InputStream;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

import static com.azure.core.http.HttpHeaderName.X_MS_CLIENT_REQUEST_ID;
import static com.azure.core.http.HttpHeaderName.X_MS_REQUEST_ID;
import static com.azure.core.http.policy.HttpLoggingPolicy.RETRY_COUNT_CONTEXT;
import static com.azure.core.implementation.logging.LoggingKeys.CANCELLED_ERROR_TYPE;
import static com.azure.core.util.tracing.Tracer.DISABLE_TRACING_KEY;

/**
 * Pipeline policy that initiates distributed tracing.
 */
public class InstrumentationPolicy implements HttpPipelinePolicy {

    // TODO (limolkova):
    // following attributes are kept for backward compatibility with current ApplicationInsights agent.
    // We'll need to update them to stable semconv attribute names (as an optimization) prior to tracing stability
    // and after new azure-core-tracing-opentelemetry is released and OTel/ApplicationInsights agents are updated to
    // used it.
    private static final String HTTP_METHOD = "http.method";
    private static final String HTTP_URL = "http.url";
    private static final String HTTP_STATUS_CODE = "http.status_code";
    private static final String SERVICE_REQUEST_ID_ATTRIBUTE = "serviceRequestId";
    private static final String CLIENT_REQUEST_ID_ATTRIBUTE = "requestId";

    // new attributes:
    private static final String HTTP_RESEND_COUNT = "http.request.resend_count";
    private static final String SERVER_ADDRESS = "server.address";
    private static final String SERVER_PORT = "server.port";
    private static final ClientLogger LOGGER = new ClientLogger(InstrumentationPolicy.class);

    // magic OpenTelemetry string that represents unknown error.
    private static final String OTHER_ERROR_TYPE = "_OTHER";
    private UrlSanitizer urlSanitizer;
    private Tracer tracer;

    /**
     * Creates an instance of {@link InstrumentationPolicy}.
     */
    public InstrumentationPolicy() {
    }

    /**
     * Initializes the policy with the {@link Tracer} instance.
     *
     * @param tracer the tracer instance.
     * @param urlSanitizer the url sanitizer instance.
     */
    public void initialize(Tracer tracer, UrlSanitizer urlSanitizer) {
        this.tracer = tracer;
        this.urlSanitizer = urlSanitizer;
    }

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        if (!isTracingEnabled(context)) {
            return next.process();
        }

        return Mono.using(() -> startSpan(context), span -> next.process().map(response -> {
            onResponseCode(response, span);
            // TODO: maybe we can optimize it? https://github.com/Azure/azure-sdk-for-java/issues/38228
            return TraceableResponse.create(response, tracer, span);
        })
            .doOnCancel(() -> tracer.end(CANCELLED_ERROR_TYPE, null, span))
            .doOnError(exception -> tracer.end(null, exception, span)), __ -> {
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
            return TraceableResponse.create(response, tracer, span);
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
        StartSpanOptions spanOptions = new StartSpanOptions(SpanKind.CLIENT).setAttribute(HTTP_METHOD, methodName)
            .setAttribute(HTTP_URL, urlSanitizer.getRedactedUrl(request.getUrl()))
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
        if (response != null && tracer.isRecording(span)) {
            int statusCode = response.getStatusCode();
            tracer.setAttribute(HTTP_STATUS_CODE, statusCode, span);
            String requestId = response.getHeaderValue(X_MS_REQUEST_ID);
            if (requestId != null) {
                tracer.setAttribute(SERVICE_REQUEST_ID_ATTRIBUTE, requestId, span);
            }
        }
    }

    private boolean isTracingEnabled(HttpPipelineCallContext context) {
        return tracer != null && tracer.isEnabled() && !((boolean) context.getData(DISABLE_TRACING_KEY).orElse(false));
    }

    private static final class TraceableResponse extends HttpResponse {
        private final HttpResponse response;
        private final Context span;
        private final Tracer tracer;
        private volatile int ended = 0;
        private static final AtomicIntegerFieldUpdater<TraceableResponse> ENDED_UPDATER
            = AtomicIntegerFieldUpdater.newUpdater(TraceableResponse.class, "ended");

        private TraceableResponse(HttpResponse response, Tracer tracer, Context span) {
            super(response.getRequest());
            this.response = response;
            this.span = span;
            this.tracer = tracer;
        }

        public static HttpResponse create(HttpResponse response, Tracer tracer, Context span) {
            if (tracer.isRecording(span)) {
                return new TraceableResponse(response, tracer, span);
            }

            // OTel does not need to end sampled-out spans, but let's do it just in case
            tracer.end(null, null, span);
            return response;
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
            return Flux.using(() -> span,
                s -> response.getBody()
                    .doOnError(e -> onError(null, e))
                    .doOnCancel(() -> onError(CANCELLED_ERROR_TYPE, null)),
                s -> endNoError());
        }

        @Override
        public Mono<byte[]> getBodyAsByteArray() {
            return endSpanWhen(response.getBodyAsByteArray());
        }

        @Override
        public Mono<String> getBodyAsString() {
            return endSpanWhen(response.getBodyAsString());
        }

        @Override
        public BinaryData getBodyAsBinaryData() {
            try {
                return response.getBodyAsBinaryData();
            } catch (Exception e) {
                onError(null, e);
                throw e;
            } finally {
                endNoError();
            }
        }

        @Override
        public Mono<String> getBodyAsString(Charset charset) {
            return endSpanWhen(response.getBodyAsString(charset));
        }

        @Override
        public Mono<InputStream> getBodyAsInputStream() {
            return endSpanWhen(response.getBodyAsInputStream());
        }

        @Override
        public void close() {
            response.close();
            endNoError();
        }

        private <T> Mono<T> endSpanWhen(Mono<T> publisher) {
            return Mono.using(() -> span,
                s -> publisher.doOnError(e -> onError(null, e)).doOnCancel(() -> onError(CANCELLED_ERROR_TYPE, null)),
                s -> endNoError());
        }

        private void onError(String errorType, Throwable error) {
            if (ENDED_UPDATER.compareAndSet(this, 0, 1)) {
                tracer.end(errorType, error, span);
            }
        }

        private void endNoError() {
            if (ENDED_UPDATER.compareAndSet(this, 0, 1)) {
                String errorType = null;
                if (response == null) {
                    errorType = OTHER_ERROR_TYPE;
                } else if (response.getStatusCode() >= 400) {
                    errorType = String.valueOf(response.getStatusCode());
                }

                tracer.end(errorType, null, span);
            }
        }
    }
}
