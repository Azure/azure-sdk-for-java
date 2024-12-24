package io.clientcore.core.observability;

import io.clientcore.core.implementation.observability.otel.OTelInitializer;
import io.clientcore.core.implementation.observability.otel.OTelObservabilityProvider;
import io.clientcore.core.observability.tracing.Tracer;

import static io.clientcore.core.observability.NoopObservabilityProvider.NOOP_PROVIDER;

public interface ObservabilityProvider {
    String DISABLE_TRACING_KEY = "disable-tracing";
    String TRACE_CONTEXT_KEY = "trace-context";

    Tracer getTracer(ObservabilityOptions<?> applicationOptions, LibraryObservabilityOptions libraryOptions);

    static ObservabilityProvider getInstance() {
        if (OTelInitializer.INSTANCE.isInitialized()) {
            return new OTelObservabilityProvider();
        } else {
            return NOOP_PROVIDER;
        }
    }
}
