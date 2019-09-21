module com.azure.tracing.opentelemetry {
    requires com.azure.core;

    requires opencensus.api;

    exports com.azure.tracing.opentelemetry;

    provides com.azure.core.util.tracing.Tracer
        with com.azure.tracing.opentelemetry.OpenTelemetryTracer;
    provides com.azure.core.http.policy.AfterRetryPolicyProvider
        with com.azure.tracing.opentelemetry.OpenTelemetryHttpPolicy;
}
