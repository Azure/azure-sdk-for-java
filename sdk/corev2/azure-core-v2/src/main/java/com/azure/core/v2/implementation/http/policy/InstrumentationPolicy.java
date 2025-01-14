// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.v2.implementation.http.policy;

import com.azure.core.v2.implementation.http.UrlSanitizer;
import com.azure.core.v2.util.CoreUtils;
import io.clientcore.core.util.ClientLogger;
import com.azure.core.v2.util.tracing.SpanKind;
import com.azure.core.v2.util.tracing.StartSpanOptions;
import com.azure.core.v2.util.tracing.Tracer;
import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpHeaders;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.http.pipeline.HttpPipelineNextPolicy;
import io.clientcore.core.http.pipeline.HttpPipelinePolicy;
import io.clientcore.core.util.Context;
import io.clientcore.core.util.binarydata.BinaryData;
import java.io.IOException;
import java.net.URI;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

import static com.azure.core.v2.util.tracing.Tracer.DISABLE_TRACING_KEY;
import static io.clientcore.core.http.models.HttpHeaderName.fromString;

/**
 * Pipeline policy that initiates distributed tracing.
 */
public class InstrumentationPolicy implements HttpPipelinePolicy {
    /**
     * Key for {@link Context} to pass request retry count metadata for logging.
     */
    private static final String RETRY_COUNT_CONTEXT = "requestRetryCount";
    private static final HttpHeaderName X_MS_CLIENT_REQUEST_ID = fromString("x-ms-client-request-id");
    private static final HttpHeaderName X_MS_REQUEST_ID = fromString("x-ms-request-id");
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

    @SuppressWarnings("deprecation")
    private Context startSpan(HttpRequest request) {

        // Build new child span representing this outgoing request.
        String methodName = request.getHttpMethod().toString();
        StartSpanOptions spanOptions = new StartSpanOptions(SpanKind.CLIENT).setAttribute(HTTP_METHOD, methodName)
            .setAttribute(HTTP_URL, urlSanitizer.getRedactedUrl(request.getUri()))
            .setAttribute(SERVER_ADDRESS, request.getUri().getHost())
            .setAttribute(SERVER_PORT, getPort(request.getUri()));
        Context span = tracer.start(methodName, spanOptions, request.getRequestOptions().getContext());

        addPostSamplingAttributes(span, request);

        // TODO (alzimmer): Add injectContext(BiConsumer<HttpHeaderName, String>, Context)
        tracer.injectContext((k, v) -> request.getHeaders().set(fromString(k), v), span);
        return span;
    }

    private static int getPort(URI uri) {
        int port = uri.getPort();
        if (port == -1) {
            // default port?
            port = uri.getPort();
        }
        return port;
    }

    private void addPostSamplingAttributes(Context span, HttpRequest request) {
        Object rawRetryCount = span.get(RETRY_COUNT_CONTEXT);
        if (rawRetryCount instanceof Integer && ((Integer) rawRetryCount) > 0) {
            tracer.setAttribute(HTTP_RESEND_COUNT, ((Integer) rawRetryCount).longValue(), span);
        }

        String requestId = request.getHeaders().getValue(X_MS_CLIENT_REQUEST_ID);
        if (!CoreUtils.isNullOrEmpty(requestId)) {
            tracer.setAttribute(CLIENT_REQUEST_ID_ATTRIBUTE, requestId, span);
        }
    }

    private void onResponseCode(Response<?> response, Context span) {
        if (response != null && tracer.isRecording(span)) {
            int statusCode = response.getStatusCode();
            tracer.setAttribute(HTTP_STATUS_CODE, statusCode, span);
            String requestId = response.getHeaders().getValue(X_MS_REQUEST_ID);
            if (requestId != null) {
                tracer.setAttribute(SERVICE_REQUEST_ID_ATTRIBUTE, requestId, span);
            }
        }
    }

    private boolean isTracingEnabled(Context context) {
        return tracer != null && tracer.isEnabled() && !((boolean) context.get(DISABLE_TRACING_KEY));
    }

    @Override
    @SuppressWarnings("try")
    public Response<?> process(HttpRequest httpRequest, HttpPipelineNextPolicy next) {
        if (!isTracingEnabled(httpRequest.getRequestOptions().getContext())) {
            return next.process();
        }

        Context span = startSpan(httpRequest);
        try (AutoCloseable ignored = tracer.makeSpanCurrent(span)) {
            Response<?> response = next.process();
            onResponseCode(response, span);
            // TODO: maybe we can optimize it? https://github.com/Azure/azure-sdk-for-java/issues/38228
            return TraceableResponse.create(response, tracer, span);
        } catch (RuntimeException ex) {
            tracer.end(null, ex, span);
            throw ex;
        } catch (Exception ex) {
            tracer.end(null, ex, span);
            throw LOGGER.logThrowableAsWarning(new RuntimeException(ex));
        }
    }

    private static final class TraceableResponse implements Response<String> {
        private final Response<?> response;
        private final Context span;
        private final Tracer tracer;
        private volatile int ended = 0;
        private static final AtomicIntegerFieldUpdater<TraceableResponse> ENDED_UPDATER
            = AtomicIntegerFieldUpdater.newUpdater(TraceableResponse.class, "ended");

        private TraceableResponse(Response<?> response, Tracer tracer, Context span) {
            this.response = response;
            this.span = span;
            this.tracer = tracer;
        }

        public static Response<?> create(Response<?> response, Tracer tracer, Context span) {
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

        @Override
        public HttpHeaders getHeaders() {
            return response.getHeaders();
        }

        @Override
        public HttpRequest getRequest() {
            return response.getRequest();
        }

        @Override
        public String getValue() {
            return null;
        }

        @Override
        public BinaryData getBody() {
            try {
                return response.getBody();
            } catch (Exception e) {
                onError(null, e);
                throw e;
            } finally {
                endNoError();
            }
        }

        @Override
        public void close() {
            try {
                response.close();
            } catch (IOException e) {
                onError(null, e);
                throw new RuntimeException(e);
            }
            endNoError();
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
