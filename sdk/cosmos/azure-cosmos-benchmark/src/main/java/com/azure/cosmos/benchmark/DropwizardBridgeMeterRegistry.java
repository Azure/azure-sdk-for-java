// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.benchmark;

import com.codahale.metrics.MetricRegistry;
import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.dropwizard.DropwizardConfig;
import io.micrometer.core.instrument.dropwizard.DropwizardMeterRegistry;
import io.micrometer.core.instrument.util.HierarchicalNameMapper;

/**
 * A Micrometer {@link io.micrometer.core.instrument.MeterRegistry} that bridges meters
 * to a Dropwizard {@link MetricRegistry}.
 *
 * <p>Any Micrometer meter registered on this registry (directly or via a composite) is
 * automatically converted to an equivalent Dropwizard meter in the underlying
 * {@link MetricRegistry}. The Dropwizard registry can then be consumed by standard
 * Dropwizard reporters (CSV, Console, Graphite, etc.).</p>
 */
public class DropwizardBridgeMeterRegistry extends DropwizardMeterRegistry {

    public DropwizardBridgeMeterRegistry() {
        super(createConfig(), new MetricRegistry(), HierarchicalNameMapper.DEFAULT, Clock.SYSTEM);
    }

    @Override
    protected Double nullGaugeValue() {
        return Double.NaN;
    }

    private static DropwizardConfig createConfig() {
        return new DropwizardConfig() {
            @Override
            public String get(String key) {
                return null;
            }

            @Override
            public String prefix() {
                return "benchmark";
            }
        };
    }
}
