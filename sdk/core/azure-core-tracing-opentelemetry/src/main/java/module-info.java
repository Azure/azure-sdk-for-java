import com.azure.core.tracing.opentelemetry.OpenTelemetryTracer;
import com.azure.core.tracing.opentelemetry.OpenTelemetryTracerProvider;

module com.azure.core.tracing.opentelemetry {
    requires transitive com.azure.core;

    requires io.opentelemetry.api;
    requires io.opentelemetry.context;

    opens com.azure.core.tracing.opentelemetry to com.fasterxml.jackson.databind;

    exports com.azure.core.tracing.opentelemetry;
    exports com.azure.core.tracing.opentelemetry.implementation;
    opens com.azure.core.tracing.opentelemetry.implementation to com.fasterxml.jackson.databind;

    provides com.azure.core.util.tracing.Tracer
        with OpenTelemetryTracer;

    provides com.azure.core.util.tracing.TracerProvider
        with OpenTelemetryTracerProvider;
}
