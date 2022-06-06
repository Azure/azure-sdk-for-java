// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.metrics;

import com.azure.core.util.AttributeBuilder;
import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.MetricsOptions;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.ImmutableTag;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Tag;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Sample implementation of Micrometer {@link AzureMeterProvider}. Should be resolved using SPI and registered in provider
 * configuration file.
 */
public class MicrometerMeterProvider implements AzureMeterProvider {

    /**
     * Default constructor for {@link java.util.ServiceLoader#load(Class, ClassLoader)}
     */
    public MicrometerMeterProvider() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AzureMeter createMeter(String libraryName, String libraryVersion, MetricsOptions options) {
        return new MicrometerMeter(libraryName, libraryVersion, options);
    }

    /**
     * Implements Micrometer version of {@link AzureMeter}
     */
    private class MicrometerMeter extends AzureMeter {
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
        public AttributeBuilder createAttributesBuilder() {
            return new MicrometerTags();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isEnabled() {
            return true;
        }

        /**
         * Simple wrapper over Micrometer {@link Counter}
         */
        private final class MicrometerLongCounter implements AzureLongCounter {
            private final static Map<InstrumentInfo, Counter> counterCache = Collections.synchronizedMap(new WeakHashMap<>());
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
            public void add(long value, AttributeBuilder attributes, Context context) {
                MicrometerTags tags = MicrometerTags.EMPTY;
                if (attributes instanceof MicrometerTags) {
                    tags = ((MicrometerTags) attributes);
                }

                Counter counter = counterCache.computeIfAbsent(new InstrumentInfo(name, tags), this::createCounter);
                counter.increment(value);
            }

            private Counter createCounter(InstrumentInfo info) {
                Counter.Builder counterBuilder = Counter.builder(info.name)
                    .description(description)
                    .tags(info.tags.getAttributes());

                if (!CoreUtils.isNullOrEmpty(unit)) {
                    counterBuilder.baseUnit(unit);
                }

                return counterBuilder.register(registry);
            }
        }

        /**
         * Simple wrapper over Micrometer {@link DistributionSummary}
         */
        private final class MicrometerLongHistogram implements AzureLongHistogram {
            private final static Map<InstrumentInfo, DistributionSummary> summaryCache = Collections.synchronizedMap(new WeakHashMap<>());
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
            public void record(long value, AttributeBuilder attributes, Context context) {
                MicrometerTags tags = MicrometerTags.EMPTY;
                if (attributes instanceof MicrometerTags) {
                    tags = ((MicrometerTags) attributes);
                }

                DistributionSummary summary = summaryCache.computeIfAbsent(new InstrumentInfo(name, tags), this::createSummary);
                summary.record(value);
            }

            private DistributionSummary createSummary(InstrumentInfo info) {
                DistributionSummary.Builder summaryBuilder = DistributionSummary.builder(info.name)
                    .description(description)
                    .publishPercentileHistogram(true)
                    .tags(info.tags.getAttributes());

                if (!CoreUtils.isNullOrEmpty(unit)) {
                    summaryBuilder.baseUnit(unit);
                }

                return summaryBuilder.register(registry);
            }
        }

        private static class InstrumentInfo {
            public String name;
            public MicrometerTags tags;

            public InstrumentInfo(String name, MicrometerTags tags) {
                this.name = name;
                this.tags = tags;
            }

            @Override
            public boolean equals(Object o) {
                if (o == this) {
                    return true;
                }

                if (!(o instanceof  InstrumentInfo)) {
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

        private static class MicrometerTags implements AttributeBuilder<Iterable<Tag>> {
            private static final MicrometerTags EMPTY = new MicrometerTags();
            private List<Tag> tags;
            public MicrometerTags() {
                tags = new ArrayList<>();
            }

            @Override
            public AttributeBuilder addAttribute(String key, String value) {
                tags.add(new ImmutableTag(key, value));

                return this;
            }

            @Override
            public AttributeBuilder addAttribute(String key, long value) {
                tags.add(new ImmutableTag(key, String.valueOf(value)));
                return this;
            }

            @Override
            public AttributeBuilder addAttribute(String key, double value) {
                tags.add(new ImmutableTag(key, String.valueOf(value)));
                return this;
            }

            @Override
            public AttributeBuilder addAttribute(String key, boolean value) {
                tags.add(new ImmutableTag(key, String.valueOf(value)));
                return this;
            }

            public Iterable<Tag> getAttributes() {
                return tags;
            }
        }
    }
}
