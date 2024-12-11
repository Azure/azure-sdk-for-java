package io.clientcore.core.observability;

public class StatusCode {
    final static Class<?> OTEL_STATUS_CODE_CLASS;

    public final static StatusCode UNSET;
    public final static StatusCode ERROR;

    private final Object otelStatusCode;

    static {
        Class<?> otelStatusCodeClass;

        Object unsetInstance;
        Object errorInstance;

        try {
            otelStatusCodeClass = Class.forName("io.opentelemetry.api.trace.StatusCode", true, Span.class.getClassLoader());
            unsetInstance = otelStatusCodeClass.getField("UNSET").get(null);
            errorInstance = otelStatusCodeClass.getField("ERROR").get(null);
        } catch (Exception e) {
            otelStatusCodeClass = null;
            unsetInstance = null;
            errorInstance = null;
        }

        UNSET = new StatusCode(unsetInstance);
        ERROR = new StatusCode(errorInstance);
        OTEL_STATUS_CODE_CLASS = otelStatusCodeClass;
    }

    private StatusCode(Object otelStatusCode) {
        this.otelStatusCode = otelStatusCode;
    }

    public Object getOtelStatusCode() {
        return otelStatusCode;
    }
}
