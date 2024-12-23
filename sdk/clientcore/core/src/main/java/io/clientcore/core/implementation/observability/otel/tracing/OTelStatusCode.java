package io.clientcore.core.implementation.observability.otel.tracing;

import io.clientcore.core.implementation.observability.otel.OTelInitializer;
import io.clientcore.core.observability.tracing.SpanKind;
import io.clientcore.core.util.ClientLogger;

import static io.clientcore.core.implementation.observability.otel.OTelInitializer.STATUS_CODE_CLASS;

public class OTelStatusCode {
    private static final ClientLogger LOGGER = new ClientLogger(OTelStatusCode.class);
    public final static OTelStatusCode ERROR;

    private final Object otelStatusCode;

    static {
        Object errorInstance = null;

        if (OTelInitializer.INSTANCE.isInitialized()) {
            try {
                errorInstance = STATUS_CODE_CLASS.getField("ERROR").get(null);
            } catch (Throwable t) {
                OTelInitializer.INSTANCE.initError(LOGGER, t);
            }
        }

        ERROR = new OTelStatusCode(errorInstance);
    }

    private OTelStatusCode(Object otelStatusCode) {
        this.otelStatusCode = otelStatusCode;
    }

    static Object getErrorStatusCode() {
        return ERROR.otelStatusCode;
    }
}
