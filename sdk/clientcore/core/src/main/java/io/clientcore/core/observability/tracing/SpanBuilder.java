package io.clientcore.core.observability.tracing;

import io.clientcore.core.observability.Attributes;
import io.clientcore.core.util.Context;

public interface SpanBuilder {
    SpanBuilder setParent(Context context);

    SpanBuilder addLink(SpanContext spanContext, Attributes attributes);

    SpanBuilder setAttribute(String key, Object value);

    SpanBuilder setSpanKind(SpanKind spanKind);

    Span startSpan();
}
