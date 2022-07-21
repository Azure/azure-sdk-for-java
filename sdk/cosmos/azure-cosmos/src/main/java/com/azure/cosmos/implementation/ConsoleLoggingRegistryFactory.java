// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation;

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.MetricRegistry;
import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.config.NamingConvention;
import io.micrometer.core.instrument.dropwizard.DropwizardConfig;
import io.micrometer.core.instrument.dropwizard.DropwizardMeterRegistry;
import io.micrometer.core.instrument.util.HierarchicalNameMapper;
import io.micrometer.core.lang.Nullable;

import java.util.concurrent.TimeUnit;

public final class ConsoleLoggingRegistryFactory {
    public static MeterRegistry create(final int step) {

        final MetricRegistry dropwizardRegistry = new MetricRegistry();

        ConsoleReporter consoleReporter = ConsoleReporter
            .forRegistry(dropwizardRegistry)
            .convertRatesTo(TimeUnit.SECONDS)
            .convertDurationsTo(TimeUnit.MILLISECONDS)
            .build();

        consoleReporter.start(step, TimeUnit.SECONDS);

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

        final MeterRegistry consoleLoggingRegistry = new DropwizardMeterRegistry(
            dropwizardConfig, dropwizardRegistry, HierarchicalNameMapper.DEFAULT, Clock.SYSTEM) {
            @Override
            protected Double nullGaugeValue() {
                return Double.NaN;
            }
        };

        consoleLoggingRegistry.config().namingConvention(NamingConvention.dot);
        return consoleLoggingRegistry;
    }
}
