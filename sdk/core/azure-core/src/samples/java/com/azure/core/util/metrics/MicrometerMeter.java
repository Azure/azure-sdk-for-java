// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.metrics;

import com.azure.core.util.AzureAttributeBuilder;
import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.MetricsOptions;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Tag;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Implements Micrometer version of {@link AzureMeter}
 */
class MicrometerMeter implements AzureMeter {
    private static final MicrometerTags EMPTY = new MicrometerTags();
    private static final Map<InstrumentInfo, Counter> COUNTER_CACHE = Collections.synchronizedMap(new WeakHashMap<>());
    private static final Map<InstrumentInfo, DistributionSummary> SUMMARY_CACHE = Collections.synchronizedMap(new WeakHashMap<>());

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
        public void record(long value, AzureAttributeBuilder attributes, Context context) {
            MicrometerTags tags = EMPTY;
            if (attributes instanceof MicrometerTags) {
                tags = ((MicrometerTags) attributes);
            }

            DistributionSummary summary = SUMMARY_CACHE.computeIfAbsent(new InstrumentInfo(name, tags), this::createSummary);
            summary.record(value);
        }

        private DistributionSummary createSummary(InstrumentInfo info) {
            DistributionSummary.Builder summaryBuilder = DistributionSummary.builder(info.getName())
                .description(description)
                .publishPercentileHistogram(true)
                .tags(info.getTags());

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
        public void add(long value, AzureAttributeBuilder attributes, Context context) {
            MicrometerTags tags = EMPTY;
            if (attributes instanceof MicrometerTags) {
                tags = ((MicrometerTags) attributes);
            }

            Counter counter = COUNTER_CACHE.computeIfAbsent(new InstrumentInfo(name, tags), this::createCounter);
            counter.increment(value);
        }

        private Counter createCounter(InstrumentInfo info) {
            Counter.Builder counterBuilder = Counter.builder(info.name)
                .description(description)
                .tags(info.getTags());

            if (!CoreUtils.isNullOrEmpty(unit)) {
                counterBuilder.baseUnit(unit);
            }

            return counterBuilder.register(registry);
        }
    }

    /**
     * Implements instrument cache key based on metric name and attribute set.
     */
    private static final class InstrumentInfo {
        private final String name;
        private final MicrometerTags tags;

        InstrumentInfo(String name, MicrometerTags tags) {
            this.name = name;
            this.tags = tags;
        }

        public String getName() {
            return name;
        }

        public Iterable<Tag> getTags() {
            return tags.get();
        }

        @Override
        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }

            if (!(o instanceof InstrumentInfo)) {
                return false;
            }

            InstrumentInfo other = (InstrumentInfo) o;

            return this.name.equals(other.name) && this.tags == other.tags;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 31 * hash + name.hashCode();
            hash = 31 * hash + tags.hashCode();
            return hash;
        }
    }
}
