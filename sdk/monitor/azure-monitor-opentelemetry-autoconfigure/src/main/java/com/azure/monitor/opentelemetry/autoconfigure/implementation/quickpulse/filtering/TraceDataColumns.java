package com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.filtering;

import com.azure.monitor.opentelemetry.autoconfigure.implementation.models.MessageData;

public class TraceDataColumns extends TelemetryColumns{
    private String Message;
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
}
