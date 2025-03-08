// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.autoconfigure.implementation.logging;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.Instant;

import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.SpanId;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceId;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.logs.data.Body;
import io.opentelemetry.sdk.logs.data.LogRecordData;
import org.junit.jupiter.api.Test;

import com.azure.monitor.opentelemetry.autoconfigure.implementation.LogDataMapper;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.models.TelemetryEventData;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.models.TelemetryItem;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.logs.Severity;
import io.opentelemetry.sdk.resources.Resource;

@SuppressWarnings("checkstyle:IllegalImport")
class LogDataMapperTest {
    @Test
    void testCustomEventName() {
        LogRecordData logRecordData = new LogRecordData() {
            @Override
            public Resource getResource() {
                return Resource.empty();
            }

            @Override
            public Attributes getAttributes() {
                return Attributes.builder().put("microsoft.custom_event.name", "TestEvent").build();
            }

            @Override
            public InstrumentationScopeInfo getInstrumentationScopeInfo() {
                return InstrumentationScopeInfo.create("TestScope", null, null);
            }

            @Override
            public long getTimestampEpochNanos() {
                return Instant.now().toEpochMilli() * 1_000_000; // Convert millis to nanos
            }

            @Override
            public long getObservedTimestampEpochNanos() {
                return Instant.now().toEpochMilli() * 1_000_000;
            }

            @Override
            public SpanContext getSpanContext() {
                return SpanContext.create(TraceId.fromLongs(12345L, 67890L), SpanId.fromLong(12345L),
                    TraceFlags.getDefault(), TraceState.getDefault());
            }

            @Override
            public Severity getSeverity() {
                return Severity.INFO;
            }

            @Override
            public String getSeverityText() {
                return "INFO";
            }

            @Override
            public Body getBody() {
                return Body.string("Test log message");
            }

            @Override
            public int getTotalAttributeCount() {
                return 1;
            }
        };

        LogDataMapper logDataMapper = new LogDataMapper(true, true, (b, r) -> {
            // Initialize telemetry builder with resource
        });

        TelemetryItem result = logDataMapper.map(logRecordData, null, null);

        assertNotNull(result);
        assertEquals("Event", result.getName());

        // Convert result.getData().getBaseData() to TelemetryEventData and get the name property and validate against "TestEvent".
        TelemetryEventData eventData = (TelemetryEventData) result.getData().getBaseData();
        assertEquals("TestEvent", eventData.getName());
    }
}
