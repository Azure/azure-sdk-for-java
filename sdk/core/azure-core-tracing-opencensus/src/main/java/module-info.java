module com.azure.core.tracing.opencensus {
    requires transitive com.azure.core;
    requires opencensus.api;

    exports com.azure.core.tracing.opencensus;

    provides com.azure.core.util.tracing.Tracer
        with com.azure.core.tracing.opencensus.OpenCensusTracer;
    provides com.azure.core.http.policy.AfterRetryPolicyProvider
        with com.azure.core.tracing.opencensus.OpenCensusHttpPolicy;
}
