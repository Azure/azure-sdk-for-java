// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.metrics;

import com.azure.core.util.MetricsOptions;
import com.azure.core.util.LibraryTelemetryOptions;

/**
 * Resolves and provides {@link Meter} implementation.
 * <p>
 * This class is intended to be used by Azure client libraries and provides abstraction over different metrics
 * implementations.
 * Application developers should use metrics implementations such as OpenTelemetry or Micrometer directly.
 */
public interface MeterProvider {
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
     * @param applicationOptions instance of {@link MetricsOptions} provided by the application.
     * @return a meter instance.
     */
    Meter createMeter(String libraryName, String libraryVersion, MetricsOptions applicationOptions);

    /**
     * Creates meter instance.
     *
     * @param libraryOptions Azure SDK telemetry options.
     * @param applicationOptions instance of {@link MetricsOptions} provided by the application.
     * @return a meter instance.
     */
    default Meter createMeter(LibraryTelemetryOptions libraryOptions, MetricsOptions applicationOptions) {
        return createMeter(libraryOptions.getLibraryName(), libraryOptions.getLibraryVersion(), applicationOptions);
    }

    /**
     * Returns default implementation of {@code MeterProvider} that uses SPI to resolve metrics implementation.
     * @return an instance of {@code MeterProvider}
     */
    static MeterProvider getDefaultProvider() {
        return DefaultMeterProvider.getInstance();
    }
}
