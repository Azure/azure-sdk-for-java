// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.filtering;

public class DuplicateMetricIdException extends Exception {
    public DuplicateMetricIdException(String message) {
        super(message);
    }
}
