package com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.filtering;

import com.azure.monitor.opentelemetry.autoconfigure.implementation.models.TelemetryExceptionData;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.models.TelemetryExceptionDetails;
import java.util.List;

// Casing of private fields is to match the names of fields passed down via filtering configuration
public class ExceptionDataColumns extends TelemetryColumns{
    private String Message;
    private String StackTrace;

    public ExceptionDataColumns(TelemetryExceptionData exceptionData) {
        super();
        setCustomDimensions(exceptionData.getProperties(), exceptionData.getMeasurements());
        List<TelemetryExceptionDetails> details = exceptionData.getExceptions();
        if(details != null & !details.isEmpty()) {
            this.Message = details.get(0).getMessage();
            this.StackTrace = details.get(0).getStack();
        } else {
            this.Message = "";
            this.StackTrace = "";
        }
    }
}
