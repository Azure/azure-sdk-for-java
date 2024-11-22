// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.filtering;

import com.azure.monitor.opentelemetry.autoconfigure.implementation.models.TelemetryExceptionData;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.models.TelemetryExceptionDetails;
import java.util.List;

// Casing of private fields is to match the names of fields passed down via filtering configuration
public class ExceptionDataColumns extends TelemetryColumns {
    private final String message;
    private final String stackTrace;

    public ExceptionDataColumns(TelemetryExceptionData exceptionData) {
        super();
        setCustomDimensions(exceptionData.getProperties(), exceptionData.getMeasurements());
        List<TelemetryExceptionDetails> details = exceptionData.getExceptions();
        if (details != null && !details.isEmpty()) {
            this.message = details.get(0).getMessage();
            this.stackTrace = details.get(0).getStack();
        } else {
            this.message = "";
            this.stackTrace = "";
        }
    }

    // To be used in tests only
    public ExceptionDataColumns(String message, String stackTrace) {
        super();
        this.message = message;
        this.stackTrace = stackTrace;
    }

    @Override
    public Object getFieldValue(String fieldName) {
        if ("Exception.Message".equals(fieldName)) {
            return this.message;
        } else if ("Exception.StackTrace".equals(fieldName)) {
            return this.stackTrace;
        } else {
            return null;
        }
    }

    // To be used in tests only
    public String getMessage() {
        return this.message;
    }

    // To be used in tests only
    public String getStackTrace() {
        return this.stackTrace;
    }
}
