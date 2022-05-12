// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.metrics;

import com.azure.core.util.AttributesBuilder;
import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.MetricsOptions;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Metrics;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

/**
 * Implements Micrometer version of {@link Meter}
 */
class MicrometerMeter implements Meter {
    private static final MicrometerTags EMPTY = new MicrometerTags();
    private static final Map<io.micrometer.core.instrument.Meter.Id, DistributionSummary> SUMMARY_CACHE = Collections.synchronizedMap(new WeakHashMap<>());
    private static final Map<io.micrometer.core.instrument.Meter.Id, Counter> COUNTER_CACHE = Collections.synchronizedMap(new WeakHashMap<>());
    private static final Map<io.micrometer.core.instrument.Meter.Id, GaugeWrapper> SETTABLE_GAUGE_CACHE = Collections.synchronizedMap(new WeakHashMap<>());

    private final MeterRegistry registry;

    MicrometerMeter(String libraryName, String libraryVersion, MetricsOptions options) {
        // Check if metricsOptions contains MeterRegistry - this way it can be configured and passed to this code.
        // By default, global static singleton is used.
        if (options != null && options.isEnabled() && options instanceof MicrometerMetricsOptions) {
            registry = ((MicrometerMetricsOptions) options).getRegistry();
        } else {
            registry = Metrics.globalRegistry;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LongHistogram createLongHistogram(String name, String description, String unit) {
        return new MicrometerLongHistogram(name, description, unit);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LongCounter createLongCounter(String name, String description, String unit) {
        return new MicrometerLongCounter(name, description, unit);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LongCounter createLongUpDownCounter(String name, String description, String unit) {
        return new MicrometerLongUpDownLongCounter(name, description, unit);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AttributesBuilder createAttributesBuilder() {
        return new MicrometerTags();
    }

    @Override
    public void close() {
        SUMMARY_CACHE.values().forEach(io.micrometer.core.instrument.Meter::close);
        COUNTER_CACHE.values().forEach(io.micrometer.core.instrument.Meter::close);
        SETTABLE_GAUGE_CACHE.values().forEach(GaugeWrapper::close);
    }

    /**
     * Simple wrapper over Micrometer {@link DistributionSummary}
     */
    private final class MicrometerLongHistogram implements LongHistogram {
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
        public void record(long value, AttributesBuilder attributes, Context context) {
            MicrometerTags tags = EMPTY;
            if (attributes instanceof MicrometerTags) {
                tags = ((MicrometerTags) attributes);
            }

            io.micrometer.core.instrument.Meter.Id id = new io.micrometer.core.instrument.Meter.Id(name, tags.get(), unit, description, io.micrometer.core.instrument.Meter.Type.DISTRIBUTION_SUMMARY);
            DistributionSummary summary = SUMMARY_CACHE.computeIfAbsent(id, this::createSummary);
            summary.record(value);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isEnabled() {
            return true;
        }

        private DistributionSummary createSummary(io.micrometer.core.instrument.Meter.Id id) {
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
    private final class MicrometerLongCounter implements LongCounter {
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
        public void add(long value, AttributesBuilder attributes, Context context) {
            MicrometerTags tags = EMPTY;
            if (attributes instanceof MicrometerTags) {
                tags = ((MicrometerTags) attributes);
            }
            io.micrometer.core.instrument.Meter.Id id = new io.micrometer.core.instrument.Meter.Id(name, tags.get(), unit, description, io.micrometer.core.instrument.Meter.Type.COUNTER);
            Counter counter = COUNTER_CACHE.computeIfAbsent(id, this::createCounter);
            counter.increment(value);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isEnabled() {
            return true;
        }

        private Counter createCounter(io.micrometer.core.instrument.Meter.Id id) {
            Counter.Builder counterBuilder = Counter.builder(id.getName())
                .description(description)
                .tags(id.getTags());

            if (!CoreUtils.isNullOrEmpty(unit)) {
                counterBuilder.baseUnit(unit);
            }

            return counterBuilder.register(registry);
        }
    }

    /**
     * Micrometer-based adapter for UpDownCounter over {@link Gauge}
     */
    private final class MicrometerLongUpDownLongCounter implements LongCounter {
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
        public void add(long value, AttributesBuilder attributes, Context context) {
            MicrometerTags tags = EMPTY;
            if (attributes instanceof MicrometerTags) {
                tags = ((MicrometerTags) attributes);
            }

            io.micrometer.core.instrument.Meter.Id id = new io.micrometer.core.instrument.Meter.Id(name, tags.get(), unit, description, io.micrometer.core.instrument.Meter.Type.COUNTER);
            GaugeWrapper gauge = SETTABLE_GAUGE_CACHE.computeIfAbsent(id, i -> new GaugeWrapper(i));
            gauge.add(value);
        }

        @Override
        public boolean isEnabled() {
            return true;
        }
    }

    /**
     * Wrapper over Micrometer {@link Gauge} that allows to record value change.
     */
    private final class GaugeWrapper {
        private final AtomicLong value = new AtomicLong();
        private final Gauge gauge;

        GaugeWrapper(io.micrometer.core.instrument.Meter.Id id) {
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

        public void close() {
            gauge.close();
        }
    }
}
