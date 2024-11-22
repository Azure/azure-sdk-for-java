// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.filtering;

public class UnexpectedFilterCreateException extends Exception {
    public UnexpectedFilterCreateException(String message) {
        super(message);
    }
}
