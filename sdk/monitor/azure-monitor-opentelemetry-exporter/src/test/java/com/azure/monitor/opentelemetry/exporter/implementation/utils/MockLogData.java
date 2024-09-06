// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation.utils;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.logs.Severity;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.SpanId;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceId;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.logs.data.Body;
import io.opentelemetry.sdk.logs.data.LogRecordData;
import io.opentelemetry.sdk.resources.Resource;

import java.time.Instant;

public class MockLogData implements LogRecordData {

    @Override
    public Resource getResource() {
        return Resource.create(Attributes.empty());
    }

    @Override
    public InstrumentationScopeInfo getInstrumentationScopeInfo() {
        return InstrumentationScopeInfo.create("Testing Instrumentation", "1", null);
    }

    @Override
    public long getTimestampEpochNanos() {
        return Instant.now().getEpochSecond();
    }

    @Override
    public long getObservedTimestampEpochNanos() {
        return getTimestampEpochNanos();
    }

    @Override
    public SpanContext getSpanContext() {
        return SpanContext.create(TraceId.fromLongs(10L, 2L), SpanId.fromLong(1), TraceFlags.getDefault(),
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

    @Override
    public int getTotalAttributeCount() {
        return getAttributes().size();
    }
}
