// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.tracing.opentelemetry;

import io.opencensus.trace.SpanContext;

final class DiagnosticIdConversionUtil {
    private DiagnosticIdConversionUtil() { }

    /**
     * Parse OpenTelemetry Status from HTTP response status code.
     *
     * <p>This method serves a default routine to map HTTP status code to Open Census Status. The
     * mapping is defined in <a
     * href="https://github.com/googleapis/googleapis/blob/master/google/rpc/code.proto">Google API
     * canonical error code</a>, and the behavior is defined in <a
     * href="https://github.com/census-instrumentation/opencensus-specs/blob/master/trace/HTTP.md">OpenTelemetry
     * Specs</a>.
     *
     * @param spanContext the span context.
     * @return the corresponding OpenTelemetry {@code Status}.
     */
    public static String getDiagnosticId(SpanContext spanContext) {
        char[] chars = new char[55];
        chars[0] = '0';
        chars[1] = '0';
        chars[2] = '-';
        spanContext.getTraceId().copyLowerBase16To(chars, 3);
        chars[35] = '-';
        spanContext.getSpanId().copyLowerBase16To(chars, 36);
        chars[52] = '-';
        spanContext.getTraceOptions().copyLowerBase16To(chars, 53);
        return new String(chars);
    }
}

