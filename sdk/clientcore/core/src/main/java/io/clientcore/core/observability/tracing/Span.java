package io.clientcore.core.observability.tracing;

import io.clientcore.core.observability.Attributes;
import io.clientcore.core.observability.Scope;

public interface Span {
    Span setAttribute(String key, Object value);
    Span setError(Throwable error);
    Span setError(String errorType);

    Span addLink(SpanContext spanContext, Attributes attributes);

    void end();

    SpanContext getSpanContext() ;

    boolean isRecording();

    Scope makeCurrent();
}
