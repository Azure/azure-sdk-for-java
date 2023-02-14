// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.metrics;

import com.azure.core.implementation.util.Providers;
import com.azure.core.util.MetricsOptions;
import com.azure.core.util.TelemetryAttributes;

import java.util.Objects;
import java.util.function.Supplier;

final class DefaultMeterProvider implements MeterProvider {
    private static final MeterProvider INSTANCE = new DefaultMeterProvider();
    private static final AutoCloseable NOOP_CLOSEABLE = () -> {};

    private static final String NO_DEFAULT_PROVIDER = "A request was made to load the default MeterProvider provider "
        + "but one could not be found on the classpath. If you are using a dependency manager, consider including a "
        + "dependency on azure-core-metrics-opentelemetry or enabling instrumentation package.";

    private static final Providers<MeterProvider, Meter> METER_PROVIDER = new Providers<>(MeterProvider.class, null,
        NO_DEFAULT_PROVIDER);

    private DefaultMeterProvider() {}

    static MeterProvider getInstance() {
        return INSTANCE;
    }

    public Meter createMeter(String libraryName, String libraryVersion, MetricsOptions options) {
        Objects.requireNonNull(libraryName, "'libraryName' cannot be null.");

        return METER_PROVIDER
            .create(provider -> provider.createMeter(libraryName, libraryVersion, options), NoopMeter.INSTANCE, null);
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
