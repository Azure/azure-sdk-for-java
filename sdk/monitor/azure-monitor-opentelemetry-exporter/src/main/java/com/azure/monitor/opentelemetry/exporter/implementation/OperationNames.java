// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation;

import io.opentelemetry.sdk.trace.ReadableSpan;

public final class OperationNames {

    public static String getOperationName(ReadableSpan span) {
        String operationName = span.getAttribute(AiSemanticAttributes.OPERATION_NAME);
        if (operationName != null) {
            return operationName;
        }

        String spanName = span.getName();
        String httpMethod = span.getAttribute(SemanticAttributes.HTTP_METHOD);
        if (httpMethod != null && !httpMethod.isEmpty() && spanName.startsWith("/")) {
            return httpMethod + " " + spanName;
        }
        return spanName;
    }

    private OperationNames() {
    }
}
