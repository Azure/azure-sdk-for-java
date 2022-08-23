import com.azure.core.metrics.opentelemetry.OpenTelemetryMeterProvider;
import com.azure.core.util.metrics.MeterProvider;

module com.azure.core.metrics.opentelemetry {
    requires transitive com.azure.core;

    requires io.opentelemetry.api;
    requires io.opentelemetry.context;

    opens com.azure.core.metrics.opentelemetry to com.fasterxml.jackson.databind;

    exports com.azure.core.metrics.opentelemetry;

    provides MeterProvider
        with OpenTelemetryMeterProvider;
}
