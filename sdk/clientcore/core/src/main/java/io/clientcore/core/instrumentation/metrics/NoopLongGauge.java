// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.instrumentation.metrics;

import io.clientcore.core.instrumentation.InstrumentationAttributes;

import java.util.Objects;
import java.util.function.Supplier;

final class NoopLongGauge {
    static final LongGauge INSTANCE = new LongGauge() {
        @Override
        public AutoCloseable registerCallback(Supplier<Long> valueSupplier, InstrumentationAttributes attributes) {
            Objects.requireNonNull(valueSupplier, "'valueSupplier' cannot be null.");
            Objects.requireNonNull(attributes, "'attributes' cannot be null.");
            return () -> {
            };
        }

        @Override
        public boolean isEnabled() {
            return false;
        }
    };

    private NoopLongGauge() {
    }
}
