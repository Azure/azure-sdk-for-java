// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.metrics;

import com.azure.core.implementation.util.Providers;
import com.azure.core.util.MetricsOptions;
import com.azure.core.util.TelemetryAttributes;
import com.azure.core.util.LibraryTelemetryOptions;

import java.util.Objects;
import java.util.function.Supplier;

final class DefaultMeterProvider implements MeterProvider {
    private static final MeterProvider INSTANCE = new DefaultMeterProvider();
    private static final MetricsOptions DEFAULT_OPTIONS = new MetricsOptions();
    private static final AutoCloseable NOOP_CLOSEABLE = () -> {
    };

    private static final String NO_DEFAULT_PROVIDER = "A request was made to load the default MeterProvider provider "
        + "but one could not be found on the classpath. If you are using a dependency manager, consider including a "
        + "dependency on azure-core-metrics-opentelemetry or enabling instrumentation package.";

    private static final Providers<MeterProvider, Meter> METER_PROVIDER
        = new Providers<>(MeterProvider.class, null, NO_DEFAULT_PROVIDER);

    private DefaultMeterProvider() {
    }

    static MeterProvider getInstance() {
        return INSTANCE;
    }

    @Override
    public Meter createMeter(String libraryName, String libraryVersion, MetricsOptions options) {
        Objects.requireNonNull(libraryName, "'libraryName' cannot be null.");

        LibraryTelemetryOptions sdkOptions = new LibraryTelemetryOptions(libraryName).setLibraryVersion(libraryVersion);

        return createMeter(sdkOptions, options);
    }

    @Override
    public Meter createMeter(LibraryTelemetryOptions libraryOptions, MetricsOptions applicationOptions) {
        Objects.requireNonNull(libraryOptions, "'libraryOptions' cannot be null.");

        final MetricsOptions finalOptions = applicationOptions != null ? applicationOptions : DEFAULT_OPTIONS;

        return METER_PROVIDER.create(provider -> provider.createMeter(libraryOptions, finalOptions), NoopMeter.INSTANCE,
            finalOptions.getMeterProvider());
    }

    static final LongGauge NOOP_GAUGE = new LongGauge() {
        @Override
        public AutoCloseable registerCallback(Supplier<Long> valueSupplier, TelemetryAttributes attributes) {
            return NOOP_CLOSEABLE;
        }

        @Override
        public boolean isEnabled() {
            return false;
        }
    };
}
