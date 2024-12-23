package io.clientcore.core.observability.tracing;

public interface SpanContext {
    String getTraceId();
    String getSpanId();

    boolean isSampled();

    //Object getTraceFlags();

    //Object getTraceState() ;

    boolean isRemote();
}
