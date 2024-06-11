// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation.pipeline;

public final class ResponseError {
    private final int index;
    private final int statusCode;
    private final String message;

    public ResponseError(int index, int statusCode, String message) {
        this.index = index;
        this.statusCode = statusCode;
        this.message = message;
    }

    public int getIndex() {
        return index;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getMessage() {
        return message;
    }

    public String toString() {
        return "ErrorResponse{" +
            "index=" + index +
            ", statusCode=" + statusCode +
            ", message='" + message + '}';
    }
}
