package io.clientcore.core.http.pipeline;

import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.observability.LibraryObservabilityOptions;
import io.clientcore.core.observability.ObservabilityOptions;
import io.clientcore.core.observability.ObservabilityProvider;
import io.clientcore.core.observability.Scope;
import io.clientcore.core.observability.tracing.Span;
import io.clientcore.core.observability.tracing.SpanKind;
import io.clientcore.core.observability.tracing.Tracer;
import io.clientcore.core.util.Context;

import static io.clientcore.core.observability.tracing.Tracer.DISABLE_TRACING_KEY;

public class InstrumentationPolicy implements HttpPipelinePolicy {
    private final Tracer tracer;
    private static final LibraryObservabilityOptions LIBRARY_OPTIONS = new LibraryObservabilityOptions("clientcore")
        .setLibraryVersion("1.0.0")
        .setSchemaUrl("https://opentelemetry.io/schemas/1.29.0");

    private static final String HTTP_REQUEST_METHOD = "http.request.method";
    private static final String HTTP_RESPONSE_STATUS_CODE = "http.response.status_code";
    private static final String SERVER_ADDRESS = "server.address";
    private static final String SERVER_PORT = "server.port";
    private static final String URL_FULL = "url.full";

    public InstrumentationPolicy(ObservabilityOptions<?> options) {
        this.tracer = ObservabilityProvider.getInstance().getTracer(options, LIBRARY_OPTIONS);
    }

    @Override
    public Response<?> process(HttpRequest httpRequest, HttpPipelineNextPolicy next) {
        if (!isTracingEnabled(httpRequest)) {
            return next.process();
        }

        Span span = tracer.spanBuilder(httpRequest.getHttpMethod().toString())
            .setSpanKind(SpanKind.CLIENT)
            .setAttribute(HTTP_REQUEST_METHOD, httpRequest.getHttpMethod().toString())
            .setAttribute(URL_FULL, httpRequest.getUri().toString()) // TODO: sanitize
            .setAttribute(SERVER_ADDRESS, httpRequest.getUri().getHost())
            .setAttribute(SERVER_PORT, httpRequest.getUri().getPort() == -1 ? 443 : httpRequest.getUri().getPort())
            .startSpan();

        try(Scope scope = span.makeCurrent()) {
            Response<?> response = next.process();

            if (span.isRecording()) {
                span.setAttribute(HTTP_RESPONSE_STATUS_CODE, (long) response.getStatusCode());
                // TODO try count, user agent, template?
            }

            if (response.getStatusCode() >= 400) {
                span.setError(String.valueOf(response.getStatusCode()));
            }

            return response;
        } catch (Throwable t) {
            span.setError(unwrap(t));
            throw t;
        } finally {
            span.end();
        }
    }

    private boolean isTracingEnabled(HttpRequest httpRequest) {
        if (!tracer.isEnabled()) {
            return false;
        }

        Context context = httpRequest.getRequestOptions().getContext();
        Object disableTracing = context.get(DISABLE_TRACING_KEY);
        if (disableTracing instanceof Boolean) {
            return !((Boolean) disableTracing);
        }

        return true;
    }

    private Throwable unwrap(Throwable t) {
        while (t.getCause() != null) {
            t = t.getCause();
        }
        return t;
    }
}
