package com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.filtering;

import com.azure.monitor.opentelemetry.autoconfigure.implementation.models.TelemetryExceptionData;

public class ExceptionDataColumns {
    private String message;
    private String stackTrace;

    public ExceptionDataColumns(TelemetryExceptionData exceptionData) {

    }
}
