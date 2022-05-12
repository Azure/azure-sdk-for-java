// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.metrics;

import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.MetricsOptions;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Metrics;

import java.util.Map;

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
    private static class MicrometerMeter implements AzureMeter {
        private final MeterRegistry registry;

        MicrometerMeter(String libraryName, String libraryVersion, MetricsOptions options) {
            // Check if metricsOptions contains MeterRegistry - this way it can be configured and passed to this code.
            // By default, global static singleton is used.
            Object providerObj = options == null ? null : options.getProvider();
            if (providerObj != null && MeterRegistry.class.isAssignableFrom(providerObj.getClass())) {
                registry = (MeterRegistry) providerObj;
            } else {
                registry = Metrics.globalRegistry;
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public AzureLongHistogram createLongHistogram(String name, String description, String unit, Map<String, Object> attributes) {
            DistributionSummary.Builder summaryBuilder = DistributionSummary.builder(name);
            if (attributes != null && !attributes.isEmpty()) {
                for (Map.Entry<String, Object> tag : attributes.entrySet()) {
                    summaryBuilder.tag(tag.getKey(), tag.getValue().toString());
                }
            }

            if (!CoreUtils.isNullOrEmpty(unit)) {
                summaryBuilder.baseUnit(unit);
            }

            DistributionSummary summary = summaryBuilder
                .description(description)
                .publishPercentileHistogram(true)
                .register(registry);

            return new MicrometerLongHistogram(summary);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public AzureLongCounter createLongCounter(String name, String description, String unit, Map<String, Object> attributes) {
            Counter.Builder counterBuilder = Counter.builder(name);
            if (attributes != null && !attributes.isEmpty()) {
                for (Map.Entry<String, Object> tag : attributes.entrySet()) {
                    counterBuilder.tag(tag.getKey(), tag.getValue().toString());
                }
            }

            if (!CoreUtils.isNullOrEmpty(unit)) {
                counterBuilder.baseUnit(unit);
            }

            Counter counter = counterBuilder
                .description(description)
                .register(registry);

            return new MicrometerLongCounter(counter);
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
        private static final class MicrometerLongCounter implements AzureLongCounter {

            private final Counter counter;

            MicrometerLongCounter(Counter counter) {
                this.counter = counter;
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public void add(long value, Context context) {
                counter.increment(value);
            }
        }

        /**
         * Simple wrapper over Micrometer {@link DistributionSummary}
         */
        private static final class MicrometerLongHistogram implements AzureLongHistogram {

            private final DistributionSummary summary;

            MicrometerLongHistogram(DistributionSummary summary) {
                this.summary = summary;
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public void record(long value, Context context) {
                summary.record(value);
            }
        }
    }
}
