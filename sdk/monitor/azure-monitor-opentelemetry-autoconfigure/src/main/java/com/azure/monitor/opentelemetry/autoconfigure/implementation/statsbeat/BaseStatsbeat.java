// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.autoconfigure.implementation.statsbeat;

import com.azure.monitor.opentelemetry.autoconfigure.implementation.builders.StatsbeatTelemetryBuilder;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.configuration.StatsbeatConnectionString;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.pipeline.TelemetryItemExporter;

abstract class BaseStatsbeat {

    private final CustomDimensions customDimensions;
    protected StatsbeatConnectionString connectionString;
    protected String instrumentationKey;

    protected BaseStatsbeat(CustomDimensions customDimensions) {
        this.customDimensions = customDimensions;
    }

    protected abstract void send(TelemetryItemExporter exporter);

    protected StatsbeatTelemetryBuilder createStatsbeatTelemetry(String name, double value) {

        StatsbeatTelemetryBuilder telemetryBuilder = StatsbeatTelemetryBuilder.create(name, value);

        if (connectionString != null) {
            // not sure if connectionString can be null in Azure Functions
            telemetryBuilder.setConnectionString(connectionString);
        }

        customDimensions.populateProperties(telemetryBuilder, instrumentationKey);

        return telemetryBuilder;
    }

    void setConnectionString(StatsbeatConnectionString connectionString) {
        this.connectionString = connectionString;
    }

    String getInstrumentationKey() {
        return instrumentationKey;
    }

    void setInstrumentationKey(String instrumentationKey) {
        this.instrumentationKey = instrumentationKey;
    }
}
