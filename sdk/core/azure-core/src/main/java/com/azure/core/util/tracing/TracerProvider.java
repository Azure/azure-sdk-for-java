// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.tracing;

import com.azure.core.util.MetricsOptions;
import com.azure.core.util.TracingOptions;

/**
 * Resolves and provides {@link Tracer} implementation.
 *
 * This class is intended to be used by Azure client libraries and provides abstraction over possible tracing implementations.
 * Application developers should use tracing libraries such as OpenTelemetry or Spring tracing.
 */
public interface TracerProvider {
    /**
     * Creates named and versioned meter instance.
     *
     * <!-- src_embed com.azure.core.util.metrics.MeterProvider.createMeter -->
     * <pre>
     * MetricsOptions metricsOptions = new MetricsOptions&#40;&#41;;
     *
     * Meter meter = MeterProvider.getDefaultProvider&#40;&#41;.createMeter&#40;&quot;azure-core&quot;, &quot;1.0.0&quot;, metricsOptions&#41;;
     * </pre>
     * <!-- end com.azure.core.util.metrics.MeterProvider.createMeter -->
     *
     * @param libraryName Azure client library package name
     * @param libraryVersion Azure client library version
     * @param azNamespace Azure Resource Provider namespace.
     * @param options instance of {@link MetricsOptions}
     * @return a meter instance.
     */
    Tracer createTracer(String libraryName, String libraryVersion, String azNamespace, TracingOptions options);

    /**
     * Returns default implementation of {@code TracerProvider} that uses SPI to resolve metrics implementation.
     * @return an instance of {@code MeterProvider}
     */
    static TracerProvider getDefaultProvider() {
        return DefaultTracerProvider.getInstance();
    }
}
