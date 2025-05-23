// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation;

    import com.codahale.metrics.Slf4jReporter;
    import com.codahale.metrics.MetricRegistry;
    import io.micrometer.core.instrument.Clock;
    import io.micrometer.core.instrument.MeterRegistry;
    import io.micrometer.core.instrument.config.NamingConvention;
    import io.micrometer.core.instrument.dropwizard.DropwizardConfig;
    import io.micrometer.core.instrument.dropwizard.DropwizardMeterRegistry;
    import io.micrometer.core.instrument.util.HierarchicalNameMapper;
    import io.micrometer.core.lang.Nullable;

    import java.util.concurrent.TimeUnit;

public class Slf4jLoggingRegistryFactory {
    public static MeterRegistry create(final int step, final String loggingLevel) {

        final MetricRegistry dropwizardRegistry = new MetricRegistry();

        final Slf4jReporter slf4jReporter = Slf4jReporter
            .forRegistry(dropwizardRegistry)
            .convertRatesTo(TimeUnit.SECONDS)
            .convertDurationsTo(TimeUnit.MILLISECONDS)
            .withLoggingLevel(Slf4jReporter.LoggingLevel.valueOf(loggingLevel))
            .build();

        slf4jReporter.start(step, TimeUnit.SECONDS);

        DropwizardConfig dropwizardConfig = new DropwizardConfig() {

            @Override
            public String get(@Nullable String key) {
                return null;
            }

            @Override
            public String prefix() {
                return "console";
            }

        };

        final DropwizardMeterRegistry slf4jLoggingRegistry = new DropwizardMeterRegistry(
            dropwizardConfig, dropwizardRegistry, HierarchicalNameMapper.DEFAULT, Clock.SYSTEM) {
            @Override
            protected Double nullGaugeValue() {
                return Double.NaN;
            }

            @Override
            public void close() {
                super.close();
                slf4jReporter.stop();
                slf4jReporter.close();
            }
        };

        slf4jLoggingRegistry.config().namingConvention(NamingConvention.dot);
        return slf4jLoggingRegistry;
    }
}
