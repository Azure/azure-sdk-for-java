// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation.quickpulse.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class QuickPulseExceptionDocument extends QuickPulseDocument {
    @JsonProperty(value = "Exception")
    private String exception;

    @JsonProperty(value = "ExceptionMessage")
    private String exceptionMessage;

    @JsonProperty(value = "ExceptionType")
    private String exceptionType;

    public String getException() {
        return exception;
    }

    public void setException(String exception) {
        this.exception = exception;
    }

    public String getExceptionMessage() {
        return exceptionMessage;
    }

    public void setExceptionMessage(String exceptionMessage) {
        this.exceptionMessage = exceptionMessage;
    }

    public String getExceptionType() {
        return exceptionType;
    }

    public void setExceptionType(String exceptionType) {
        this.exceptionType = exceptionType;
    }
}
