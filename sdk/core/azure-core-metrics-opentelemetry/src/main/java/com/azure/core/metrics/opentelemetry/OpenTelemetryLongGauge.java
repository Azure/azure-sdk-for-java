// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.metrics.opentelemetry;

import io.opentelemetry.api.metrics.ObservableLongGauge;

/**
 * {@inheritDoc}
 */
class OpenTelemetryLongGauge implements AutoCloseable {
    private final ObservableLongGauge gauge;

    OpenTelemetryLongGauge( ObservableLongGauge gauge) {
        this.gauge = gauge;
    }

    @Override
    public void close() {
        gauge.close();
    }
}
