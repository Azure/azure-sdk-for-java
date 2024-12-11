package io.clientcore.core.observability;

public class SpanKind {
    final static Class<?> OTEL_SPAN_KIND_CLASS;

    public final static SpanKind INTERNAL;
    public final static SpanKind SERVER;
    public final static SpanKind CLIENT;
    public final static SpanKind PRODUCER;
    public final static SpanKind CONSUMER;

    private final Object otelSpanKind;

    static {
        Class<?> otelSpanKindClass;

        Object internalInstance;
        Object serverInstance;
        Object clientInstance;
        Object producerInstance;
        Object consumerInstance;

        try {
            otelSpanKindClass = Class.forName("io.opentelemetry.api.trace.SpanKind", true, SpanBuilder.class.getClassLoader());
            internalInstance = otelSpanKindClass.getField("INTERNAL").get(null);
            serverInstance = otelSpanKindClass.getField("SERVER").get(null);
            clientInstance = otelSpanKindClass.getField("CLIENT").get(null);
            producerInstance = otelSpanKindClass.getField("PRODUCER").get(null);
            consumerInstance = otelSpanKindClass.getField("CONSUMER").get(null);
        } catch (Exception e) {
            otelSpanKindClass = null;
            internalInstance = null;
            serverInstance = null;
            clientInstance = null;
            producerInstance = null;
            consumerInstance = null;
        }

        INTERNAL = new SpanKind(internalInstance);
        SERVER = new SpanKind(serverInstance);
        CLIENT = new SpanKind(clientInstance);
        PRODUCER = new SpanKind(producerInstance);
        CONSUMER = new SpanKind(consumerInstance);
        OTEL_SPAN_KIND_CLASS = otelSpanKindClass;
    }

    private SpanKind(Object otelSpanKind) {
        this.otelSpanKind = otelSpanKind;
    }

    public Object getOtelSpanKind() {
        return otelSpanKind;
    }
}
