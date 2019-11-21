import com.azure.core.tracing.opentelemetry.OpenTelemetryHttpPolicy;
import com.azure.core.tracing.opentelemetry.OpenTelemetryTracer;

module com.azure.core.tracing.opentelemetry {
    requires transitive com.azure.core;
    requires opentelemetry.api;

    exports com.azure.core.tracing.opentelemetry;

    provides com.azure.core.util.tracing.Tracer
        with OpenTelemetryTracer;
    provides com.azure.core.http.policy.AfterRetryPolicyProvider
        with OpenTelemetryHttpPolicy;
}
