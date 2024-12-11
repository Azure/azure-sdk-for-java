package io.clientcore.core.http.pipeline;

import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.observability.AttributeKey;
import io.clientcore.core.observability.Span;
import io.clientcore.core.observability.SpanKind;
import io.clientcore.core.observability.StatusCode;
import io.clientcore.core.observability.Tracer;

public class InstrumentationPolicy implements HttpPipelinePolicy {
    private final Tracer tracer;

    private static final AttributeKey<String> HTTP_REQUEST_METHOD = AttributeKey.stringKey("http.request.method");
    private static final AttributeKey<String> ERROR_TYPE = AttributeKey.stringKey("error.type");
    private static final AttributeKey<Long> HTTP_RESPONSE_STATUS_CODE = AttributeKey.longKey("http.response.status_code");

    public
    InstrumentationPolicy(Tracer tracer) {
        this.tracer = tracer;
    }

    @Override
    public Response<?> process(HttpRequest httpRequest, HttpPipelineNextPolicy next) {
        Span span = tracer.spanBuilder("http-request")
            .setSpanKind(SpanKind.CLIENT)
            .setAttribute(HTTP_REQUEST_METHOD, httpRequest.getHttpMethod().toString())
            .startSpan();

        // todo make current
        try {
            Response<?> response = next.process();

            if (span.isRecording()) {
                span.setAttribute(HTTP_RESPONSE_STATUS_CODE, (long) response.getStatusCode());
            }

            return response;
        } catch (Throwable e) {
            // TODO log

            if (span.isRecording()) {
                span.setAttribute(ERROR_TYPE, e.getClass().getCanonicalName());
            }
            span.setStatus(StatusCode.ERROR, e.getMessage());

            throw e;
        } finally {
            span.end();
        }
    }
}
