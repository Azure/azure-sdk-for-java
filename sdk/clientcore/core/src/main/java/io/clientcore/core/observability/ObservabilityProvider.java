package io.clientcore.core.observability;

import io.clientcore.core.implementation.observability.otel.OTelInitializer;
import io.clientcore.core.implementation.observability.otel.tracing.OTelTracerProvider;
import io.clientcore.core.observability.tracing.Tracer;

import static io.clientcore.core.observability.NoopObservabilityProvider.NOOP_PROVIDER;

public interface ObservabilityProvider {
    //AttributesBuilder createAttributesBuilder();

    Tracer getTracer(ObservabilityOptions<?> applicationOptions, LibraryObservabilityOptions libraryOptions);

    static ObservabilityProvider getInstance() {
        if (OTelInitializer.INSTANCE.isInitialized()) {
            return new OTelTracerProvider();
        } else {
            return NOOP_PROVIDER;
        }
    }
}
