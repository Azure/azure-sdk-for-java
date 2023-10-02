// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.core.implementation.http.policy;

import com.typespec.core.http.HttpHeaderName;
import com.typespec.core.http.HttpPipelineCallContext;
import com.typespec.core.http.HttpPipelineNextPolicy;
import com.typespec.core.http.HttpPipelineNextSyncPolicy;
import com.typespec.core.http.HttpRequest;
import com.typespec.core.http.HttpResponse;
import com.typespec.core.http.policy.HttpPipelinePolicy;
import com.typespec.core.util.Context;
import com.typespec.core.util.CoreUtils;
import com.typespec.core.util.logging.ClientLogger;
import com.typespec.core.util.tracing.SpanKind;
import com.typespec.core.util.tracing.StartSpanOptions;
import com.typespec.core.util.tracing.Tracer;
import reactor.core.publisher.Mono;

import static com.typespec.core.util.tracing.Tracer.DISABLE_TRACING_KEY;

public class InstrumentationPolicy implements HttpPipelinePolicy {
    private static final String HTTP_USER_AGENT = "http.user_agent";
    private static final String HTTP_METHOD = "http.method";
    private static final String HTTP_URL = "http.url";
    private static final String HTTP_STATUS_CODE = "http.status_code";
    private static final String SERVICE_REQUEST_ID_ATTRIBUTE = "serviceRequestId";
    private static final String CLIENT_REQUEST_ID_ATTRIBUTE = "requestId";
    private static final String REACTOR_HTTP_TRACE_CONTEXT_KEY = "instrumentation-context-key";
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
    public InstrumentationPolicy() {
    }

    public void initialize(Tracer tracer) {
        this.tracer = tracer;
    }

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        if (!isTracingEnabled(context)) {
            return next.process();
        }

        // OpenTelemetry reactor instrumentation needs a bit of help
        // to pick up Azure SDK context. While we're working on explicit
        // context propagation, ScalarPropagatingMono.INSTANCE is the workaround
        return Mono.defer(() -> {
            Context span = startSpan(context);
            return next.process()
                .doOnSuccess(response -> endSpan(response, span))
                .doOnCancel(() -> tracer.end("cancel", null, span))
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
            endSpan(response, span);
            return response;
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
            .setAttribute(HTTP_METHOD, methodName)
            .setAttribute(HTTP_URL, request.getUrl().toString());
        Context span = tracer.start("HTTP " + methodName, spanOptions, azContext.getContext());

        addPostSamplingAttributes(span, request);

        // TODO (alzimmer): Add injectContext(BiConsumer<HttpHeaderName, String>, Context)
        tracer.injectContext((k, v) -> request.getHeaders().set(k, v), span);
        return span;
    }

    private void addPostSamplingAttributes(Context span, HttpRequest request) {
        String userAgent = request.getHeaders().getValue(HttpHeaderName.USER_AGENT);
        if (!CoreUtils.isNullOrEmpty(userAgent)) {
            tracer.setAttribute(HTTP_USER_AGENT, userAgent, span);
        }

        String requestId = request.getHeaders().getValue(HttpHeaderName.X_MS_CLIENT_REQUEST_ID);
        if (!CoreUtils.isNullOrEmpty(requestId)) {
            tracer.setAttribute(CLIENT_REQUEST_ID_ATTRIBUTE, requestId, span);
        }
    }

    private void endSpan(HttpResponse response, Context span) {
        if (response != null) {
            int statusCode = response.getStatusCode();
            tracer.setAttribute(HTTP_STATUS_CODE, statusCode, span);
            String requestId = response.getHeaderValue(SERVICE_REQUEST_ID_HEADER);
            if (requestId != null) {
                tracer.setAttribute(SERVICE_REQUEST_ID_ATTRIBUTE, requestId, span);
            }

            tracer.end((statusCode >= 400) ? "error" : null, null, span);
        }

        tracer.end("", null, span);
    }

    private boolean isTracingEnabled(HttpPipelineCallContext context) {
        return tracer != null && tracer.isEnabled() && !foundLegacyOTelPolicy
            && !((boolean) context.getData(DISABLE_TRACING_KEY).orElse(false));
    }
}
