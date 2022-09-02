/*
 * ApplicationInsights-Java
 * Copyright (c) Microsoft Corporation
 * All rights reserved.
 *
 * MIT License
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this
 * software and associated documentation files (the ""Software""), to deal in the Software
 * without restriction, including without limitation the rights to use, copy, modify, merge,
 * publish, distribute, sublicense, and/or sell copies of the Software, and to permit
 * persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED *AS IS*, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE
 * FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */

package com.azure.monitor.opentelemetry.exporter.implementation.utils;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.SpanId;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceId;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.logs.data.Body;
import io.opentelemetry.sdk.logs.data.LogData;
import io.opentelemetry.sdk.logs.data.Severity;
import io.opentelemetry.sdk.resources.Resource;

import java.time.Instant;

public class MockLogData implements LogData {

    @Override
    public Resource getResource() {
        return Resource.create(Attributes.empty());
    }

    @Override
    public InstrumentationScopeInfo getInstrumentationScopeInfo() {
        return InstrumentationScopeInfo.create("Testing Instrumentation", "1", null);
    }

    @Override
    public long getEpochNanos() {
        return Instant.now().getEpochSecond();
    }

    @Override
    public SpanContext getSpanContext() {
        return SpanContext.create(
            TraceId.fromLongs(10L, 2L),
            SpanId.fromLong(1),
            TraceFlags.getDefault(),
            TraceState.builder().build());
    }

    @Override
    public Severity getSeverity() {
        return Severity.DEBUG;
    }

    @Override
    public String getSeverityText() {
        return "DEBUG";
    }

    @Override
    public Body getBody() {
        return Body.string("testing log");
    }

    @Override
    public Attributes getAttributes() {
        return Attributes.builder()
            .put("one", "1")
            .put("two", 2L)
            .put("db.svc", "location")
            .put("operation", "get")
            .put("id", "1234")
            .build();
    }
}
