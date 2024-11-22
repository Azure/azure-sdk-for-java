// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.filtering;

import com.azure.monitor.opentelemetry.autoconfigure.implementation.models.MessageData;

public class TraceDataColumns extends TelemetryColumns {
    private final String Message;

    public TraceDataColumns(MessageData traceData) {
        super();
        setCustomDimensions(traceData.getProperties(), traceData.getMeasurements());
        this.Message = traceData.getMessage();
    }

    // to be used in tests only
    public TraceDataColumns(String message) {
        super();
        this.Message = message;
    }

    // To be used in tests only
    public String getMessage() {
        return this.Message;
    }
}
