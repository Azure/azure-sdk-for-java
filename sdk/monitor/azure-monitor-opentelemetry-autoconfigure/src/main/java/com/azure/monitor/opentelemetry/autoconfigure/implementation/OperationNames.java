// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.autoconfigure.implementation;

import io.opentelemetry.sdk.trace.ReadableSpan;

public final class OperationNames {

    public static String getOperationName(ReadableSpan span) {
        String operationName = span.getAttribute(AiSemanticAttributes.OPERATION_NAME);
        if (operationName != null) {
            return operationName;
        }
        return span.getName();
    }

    private OperationNames() {
    }
}
