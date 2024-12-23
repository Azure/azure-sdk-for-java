package io.clientcore.core.implementation.observability.otel.tracing;

import io.clientcore.core.implementation.observability.otel.OTelInitializer;
import io.clientcore.core.observability.tracing.SpanKind;
import io.clientcore.core.util.ClientLogger;

import static io.clientcore.core.implementation.observability.otel.OTelInitializer.SPAN_KIND_CLASS;

public class OTelSpanKind {
    private static final ClientLogger LOGGER = new ClientLogger(OTelSpanKind.class);
    public final static OTelSpanKind INTERNAL;
    public final static OTelSpanKind SERVER;
    public final static OTelSpanKind CLIENT;
    public final static OTelSpanKind PRODUCER;
    public final static OTelSpanKind CONSUMER;

    private final Object otelSpanKind;

    static {
        Object internalInstance = null;
        Object serverInstance = null;
        Object clientInstance = null;
        Object producerInstance = null;
        Object consumerInstance = null;

        if (OTelInitializer.INSTANCE.isInitialized()) {
            try {
                internalInstance = SPAN_KIND_CLASS.getField("INTERNAL").get(null);
                serverInstance = SPAN_KIND_CLASS.getField("SERVER").get(null);
                clientInstance = SPAN_KIND_CLASS.getField("CLIENT").get(null);
                producerInstance = SPAN_KIND_CLASS.getField("PRODUCER").get(null);
                consumerInstance = SPAN_KIND_CLASS.getField("CONSUMER").get(null);
            } catch (Throwable t) {
                OTelInitializer.INSTANCE.initError(LOGGER, t);
            }
        }

        INTERNAL = new OTelSpanKind(internalInstance);
        SERVER = new OTelSpanKind(serverInstance);
        CLIENT = new OTelSpanKind(clientInstance);
        PRODUCER = new OTelSpanKind(producerInstance);
        CONSUMER = new OTelSpanKind(consumerInstance);
    }

    private OTelSpanKind(Object otelSpanKind) {
        this.otelSpanKind = otelSpanKind;
    }

    static OTelSpanKind fromSpanKind(SpanKind spanKind) {
        switch (spanKind) {
            case SERVER:
                return SERVER;
            case CLIENT:
                return CLIENT;
            case PRODUCER:
                return PRODUCER;
            case CONSUMER:
                return CONSUMER;
            default:
                return INTERNAL;
        }
    }

    static Object getOtelSpanKind(SpanKind spanKind) {
        switch (spanKind) {
            case SERVER:
                return SERVER.otelSpanKind;
            case CLIENT:
                return CLIENT.otelSpanKind;
            case PRODUCER:
                return PRODUCER.otelSpanKind;
            case CONSUMER:
                return CONSUMER.otelSpanKind;
            default:
                return INTERNAL.otelSpanKind;
        }
    }
}
