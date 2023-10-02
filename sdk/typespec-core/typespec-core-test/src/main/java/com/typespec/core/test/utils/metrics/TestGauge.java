// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.core.test.utils.metrics;

import com.typespec.core.util.Context;
import com.typespec.core.util.TelemetryAttributes;
import com.typespec.core.util.metrics.LongGauge;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Test implementation of gauge allowing to verify what measurements were reported.
 */
public class TestGauge implements LongGauge {
    private final boolean isEnabled;

    private final ConcurrentLinkedQueue<Subscription> subscriptions = new ConcurrentLinkedQueue<>();

    TestGauge(boolean isEnabled) {
        this.isEnabled = isEnabled;
    }

    @Override
    public AutoCloseable registerCallback(Supplier<Long> valueSupplier, TelemetryAttributes attributes) {
        Subscription subscription = new Subscription(valueSupplier, attributes);
        subscriptions.add(subscription);
        return subscription;
    }

    @Override
    public boolean isEnabled() {
        return isEnabled;
    }

    /**
     * Gets all subscriptions requested from this instrument.
     *
     * @return list of all measurements
     */
    public List<Subscription> getSubscriptions() {
        return subscriptions.stream().collect(Collectors.toList());
    }

    /**
     * Test subscription, recording test measurements.
     */
    public class Subscription implements AutoCloseable {
        private final ConcurrentLinkedQueue<TestMeasurement<Long>> measurements = new ConcurrentLinkedQueue<>();
        private final Supplier<Long> valueSupplier;
        private final TelemetryAttributes attributes;
        private boolean closed = false;
        Subscription(Supplier<Long> value, TelemetryAttributes attributes) {
            this.valueSupplier = value;
            this.attributes = attributes;
        }

        /**
         * Records value. Tests should call into this method explicitly.
         */
        public void measure() {
            if (!closed) {
                measurements.add(new TestMeasurement<>(valueSupplier.get(), (TestTelemetryAttributes) attributes, Context.NONE));
            }
        }

        @Override
        public void close() {
            closed = true;
            subscriptions.remove(this);
        }

        /**
         * Gets all measurements reported with this subscription.
         *
         * @return list of all measurements
         */
        public List<TestMeasurement<Long>> getMeasurements() {
            return measurements.stream().collect(Collectors.toList());
        }
    }
}
