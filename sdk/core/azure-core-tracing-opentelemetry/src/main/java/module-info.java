import com.azure.core.tracing.opentelemetry.OpenTelemetryHttpPolicy;
import com.azure.core.tracing.opentelemetry.OpenTelemetryTracer;

module com.azure.core.tracing.opentelemetry {
    requires transitive com.azure.core;

    requires io.opentelemetry.api;
    requires io.opentelemetry.context;

    opens com.azure.core.tracing.opentelemetry to com.fasterxml.jackson.databind;

    exports com.azure.core.tracing.opentelemetry;

    provides com.azure.core.util.tracing.Tracer
        with OpenTelemetryTracer;
    provides com.azure.core.http.policy.AfterRetryPolicyProvider
        with OpenTelemetryHttpPolicy;
}
