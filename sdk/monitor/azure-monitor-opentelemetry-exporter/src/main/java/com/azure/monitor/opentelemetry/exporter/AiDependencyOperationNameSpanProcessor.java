// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.trace.ReadWriteSpan;
import io.opentelemetry.sdk.trace.ReadableSpan;
import io.opentelemetry.sdk.trace.SpanProcessor;

/**
 * SpanProcessor implementation to update operation name.
 */
// note: operation name for requests is handled during export so that it can use the updated span name from routing instrumentation
//       if we (only) set operation name on requests here, it would be based on span name at startSpan
public class AiDependencyOperationNameSpanProcessor implements SpanProcessor {
    public static final AttributeKey<String> AI_OPERATION_NAME_KEY =
        AttributeKey.stringKey("applicationinsights.internal.operation_name");

    @Override
    public void onStart(Context parentContext, ReadWriteSpan span) {
        // also check if we should copy parentSpan operation name if the child span already have operation name
        if (span.getAttribute(AI_OPERATION_NAME_KEY) != null) {
            return;
        }

        Span parentSpan = Span.fromContextOrNull(parentContext);
        if (parentSpan == null) {
            return;
        }
        if (!(parentSpan instanceof ReadableSpan)) {
            return;
        }

        span.setAttribute(AI_OPERATION_NAME_KEY, getOperationName((ReadableSpan) parentSpan));
    }

    @Override
    public boolean isStartRequired() {
        return true;
    }

    @Override
    public void onEnd(ReadableSpan span) {}

    @Override
    public boolean isEndRequired() {
        return false;
    }

    private static String getOperationName(ReadableSpan serverSpan) {
        String operationName = serverSpan.getAttribute(AI_OPERATION_NAME_KEY);
        if (operationName != null) {
            return operationName;
        }

        String spanName = serverSpan.getName();
        String httpMethod = serverSpan.getAttribute(SemanticAttributes.HTTP_METHOD);
        if (httpMethod == null || httpMethod.isEmpty()) {
            return spanName;
        }
        return httpMethod + " " + spanName;
    }
}
