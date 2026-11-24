// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.autoconfigure.implementation;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.sdk.trace.ReadableSpan;
import io.opentelemetry.sdk.trace.data.SpanData;

import java.util.function.Function;

import static com.azure.monitor.opentelemetry.autoconfigure.implementation.AiSemanticAttributes.JOB_SYSTEM;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.semconv.incubating.MessagingIncubatingAttributes;

public final class RequestChecker {

    private RequestChecker() {
    }

    public static boolean isRequest(SpanData span) {
        return isRequest(span.getKind(), span.getParentSpanContext(), span.getAttributes()::get);
    }

    public static boolean isRequest(ReadableSpan span) {
        return isRequest(span.getKind(), span.getParentSpanContext(), span::getAttribute);
    }

    public static boolean isRequest(SpanKind kind, SpanContext parentSpanContext,
        Function<AttributeKey<String>, String> attrFn) {
        if (kind == SpanKind.INTERNAL) {
            // INTERNAL scheduled job spans with no parent are mapped to requests
            return attrFn.apply(JOB_SYSTEM) != null && !parentSpanContext.isValid();
        } else if (kind == SpanKind.CLIENT || kind == SpanKind.PRODUCER) {
            return false;
        } else if (kind == SpanKind.CONSUMER
            && "receive".equals(attrFn.apply(MessagingIncubatingAttributes.MESSAGING_OPERATION))) {
            return false;
        } else if (kind == SpanKind.SERVER || kind == SpanKind.CONSUMER) {
            return true;
        } else {
            throw new UnsupportedOperationException(kind.name());
        }
    }
}
