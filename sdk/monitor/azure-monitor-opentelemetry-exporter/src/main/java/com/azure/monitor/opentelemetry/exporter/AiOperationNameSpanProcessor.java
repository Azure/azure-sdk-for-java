package com.azure.monitor.opentelemetry.exporter;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.trace.ReadWriteSpan;
import io.opentelemetry.sdk.trace.ReadableSpan;
import io.opentelemetry.sdk.trace.SpanProcessor;

public class AiOperationNameSpanProcessor implements SpanProcessor {
    public static final AttributeKey<String> AI_OPERATION_NAME_KEY =
        AttributeKey.stringKey("applicationinsights.internal.operation_name");

    @Override
    public void onStart(Context parentContext, ReadWriteSpan span) {
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

    public static String getOperationName(ReadableSpan serverSpan) {

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
