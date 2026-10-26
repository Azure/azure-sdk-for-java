// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.autoconfigure.implementation.builders;

import reactor.util.annotation.Nullable;

public interface CustomMeasurementsTelemetryBuilder {
    void addMeasurement(@Nullable String key, Double value);
}
