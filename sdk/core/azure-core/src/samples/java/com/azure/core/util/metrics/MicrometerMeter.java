// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.metrics;

import com.azure.core.util.AzureAttributeCollection;
import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.MetricsOptions;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Metrics;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

/**
 * Implements Micrometer version of {@link AzureMeter}
 */
class MicrometerMeter implements AzureMeter {
    private static final MicrometerTags EMPTY = new MicrometerTags();
    private static final Map<Meter.Id, DistributionSummary> SUMMARY_CACHE = Collections.synchronizedMap(new WeakHashMap<>());
    private static final Map<Meter.Id, Counter> COUNTER_CACHE = Collections.synchronizedMap(new WeakHashMap<>());
    private static final Map<Meter.Id, SettableGauge> SETTABLE_GAUGE_CACHE = Collections.synchronizedMap(new WeakHashMap<>());

    private final MeterRegistry registry;

    MicrometerMeter(String libraryName, String libraryVersion, MetricsOptions options) {
        // Check if metricsOptions contains MeterRegistry - this way it can be configured and passed to this code.
        // By default, global static singleton is used.
        Object providerObj = options == null ? null : options.getProvider();
        if (providerObj instanceof MeterRegistry) {
            registry = (MeterRegistry) providerObj;
        } else {
            registry = Metrics.globalRegistry;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AzureLongHistogram createLongHistogram(String name, String description, String unit) {
        return new MicrometerLongHistogram(name, description, unit);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AzureLongCounter createLongCounter(String name, String description, String unit) {
        return new MicrometerLongCounter(name, description, unit);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AzureLongCounter createLongUpDownCounter(String name, String description, String unit) {
        return new MicrometerLongUpDownLongCounter(name, description, unit);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AzureAttributeCollection createAttributeBuilder() {
        return new MicrometerTags();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEnabled() {
        return true;
    }

    private final class MicrometerLongHistogram implements AzureLongHistogram {
        private final String name;
        private final String description;
        private final String unit;

        MicrometerLongHistogram(String name, String description, String unit) {
            this.name = name;
            this.description = description;
            this.unit = unit;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void record(long value, AzureAttributeCollection attributes, Context context) {
            MicrometerTags tags = EMPTY;
            if (attributes instanceof MicrometerTags) {
                tags = ((MicrometerTags) attributes);
            }

            Meter.Id id = new Meter.Id(name, tags.get(), unit, description, Meter.Type.DISTRIBUTION_SUMMARY);
            DistributionSummary summary = SUMMARY_CACHE.computeIfAbsent(id, this::createSummary);
            summary.record(value);
        }

        private DistributionSummary createSummary(Meter.Id id) {
            DistributionSummary.Builder summaryBuilder = DistributionSummary.builder(id.getName())
                .description(description)
                .publishPercentileHistogram(true)
                .tags(id.getTags());

            if (!CoreUtils.isNullOrEmpty(unit)) {
                summaryBuilder.baseUnit(unit);
            }

            return summaryBuilder.register(registry);
        }
    }

    /**
     * Simple wrapper over Micrometer {@link Counter}
     */
    private final class MicrometerLongCounter implements AzureLongCounter {
        private final String name;
        private final String description;
        private final String unit;

        MicrometerLongCounter(String name, String description, String unit) {
            this.name = name;
            this.description = description;
            this.unit = unit;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void add(long value, AzureAttributeCollection attributes, Context context) {
            MicrometerTags tags = EMPTY;
            if (attributes instanceof MicrometerTags) {
                tags = ((MicrometerTags) attributes);
            }
            Meter.Id id = new Meter.Id(name, tags.get(), unit, description, Meter.Type.COUNTER);
            Counter counter = COUNTER_CACHE.computeIfAbsent(id, this::createCounter);
            counter.increment(value);
        }

        private Counter createCounter(Meter.Id id) {
            Counter.Builder counterBuilder = Counter.builder(id.getName())
                .description(description)
                .tags(id.getTags());

            if (!CoreUtils.isNullOrEmpty(unit)) {
                counterBuilder.baseUnit(unit);
            }

            return counterBuilder.register(registry);
        }
    }

    private final class SettableGauge implements AutoCloseable {
        private final AtomicLong value = new AtomicLong();
        private final Gauge gauge;

        public SettableGauge(Meter.Id id) {
            Gauge.Builder<Supplier<Number>> gaugeBuilder = Gauge.builder(id.getName(), value::get)
                .description(id.getDescription())
                .tags(id.getTags());

            if (!CoreUtils.isNullOrEmpty(id.getBaseUnit())) {
                gaugeBuilder.baseUnit(id.getBaseUnit());
            }

            this.gauge = gaugeBuilder.register(registry);
        }

        public void add(long delta) {
            this.value.addAndGet(delta);
        }

        @Override
        public void close()  {
            this.gauge.close();
        }
    }

    /**
     * Simple wrapper over Micrometer {@link Counter}
     */
    private final class MicrometerLongUpDownLongCounter implements AzureLongCounter {
        private final String name;
        private final String description;
        private final String unit;

        MicrometerLongUpDownLongCounter(String name, String description, String unit) {
            this.name = name;
            this.description = description;
            this.unit = unit;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void add(long value, AzureAttributeCollection attributes, Context context) {
            MicrometerTags tags = EMPTY;
            if (attributes instanceof MicrometerTags) {
                tags = ((MicrometerTags) attributes);
            }

            Meter.Id id = new Meter.Id(name, tags.get(), unit, description, Meter.Type.COUNTER);
            SettableGauge gauge = SETTABLE_GAUGE_CACHE.computeIfAbsent(id, i -> new SettableGauge(i));
            gauge.add(value);
        }
    }

    /*
    private final class MicrometerGauge implements AutoCloseable {
        private final Gauge gauge;

        MicrometerGauge(String name, String description, String unit, Supplier<GaugePoint<Long>> valueSupplier) {
            MicrometerTags tags = EMPTY;

            // Micrometer does not support gauges with dynamic attributes
            AzureAttributeBuilder attributes = valueSupplier.get().getAttributes();
            if (attributes instanceof MicrometerTags) {
                tags = ((MicrometerTags) attributes);
            }
            Meter.Id id = new Meter.Id(name, tags.get(), unit, description, Meter.Type.GAUGE);
            this.gauge = GAUGE_CACHE.computeIfAbsent(id, i -> createGauge(i, () -> valueSupplier.get().getValue()));
        }

        private Gauge createGauge(Meter.Id id, Supplier<Number> supplier) {
            Gauge.Builder<Supplier<Number>> summaryBuilder = Gauge.builder(id.getName(), supplier)
                .description(id.getDescription())
                .tags(id.getTags());

            if (!CoreUtils.isNullOrEmpty(id.getBaseUnit())) {
                summaryBuilder.baseUnit(id.getBaseUnit());
            }

            return summaryBuilder.register(registry);
        }

        @Override
        public void close() {
            this.gauge.close();
        }
    }*/
}
