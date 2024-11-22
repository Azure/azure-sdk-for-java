// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.filtering;

import com.azure.monitor.opentelemetry.autoconfigure.implementation.models.MessageData;

public class TraceDataColumns extends TelemetryColumns {
    private final String message;

    public TraceDataColumns(MessageData traceData) {
        super();
        setCustomDimensions(traceData.getProperties(), traceData.getMeasurements());
        this.message = traceData.getMessage();
    }

    // to be used in tests only
    public TraceDataColumns(String message) {
        super();
        this.message = message;
    }

    @Override
    public Object getFieldValue(String fieldName) {
        if ("Message".equals(fieldName)) {
            return this.message;
        } else {
            return null;
        }
    }

    // To be used in tests only
    public String getMessage() {
        return this.message;
    }
}
