// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.autoconfigure.implementation.logging;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.Instant;
import java.util.Map;

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
import com.azure.monitor.opentelemetry.autoconfigure.implementation.models.MessageData;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.models.TelemetryEventData;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.models.TelemetryItem;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.semconv.CodeAttributes;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.semconv.incubating.CodeIncubatingAttributes;

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

    @Test
    void testStableCodeAttributesMappedToProperties() {
        Attributes attributes = Attributes.builder()
            .put(CodeAttributes.CODE_FILE_PATH, "/src/main/java/com/example/App.java")
            .put(CodeAttributes.CODE_FUNCTION_NAME, "com.example.App.process")
            .put(CodeAttributes.CODE_LINE_NUMBER, 42L)
            .build();

        LogDataMapper logDataMapper = new LogDataMapper(false, false, (b, r) -> {
        });

        TelemetryItem telemetryItem = logDataMapper.map(new SimpleLogRecordData(attributes), null, null);

        MessageData messageData = (MessageData) telemetryItem.getData().getBaseData();
        Map<String, String> properties = messageData.getProperties();

        assertNotNull(properties);
        assertEquals("/src/main/java/com/example/App.java", properties.get("FileName"));
        assertEquals("com.example.App", properties.get("ClassName"));
        assertEquals("process", properties.get("MethodName"));
        assertEquals("42", properties.get("LineNumber"));
    }

    @Test
    void testIncubatingCodeAttributesStillMapped() {
        Attributes attributes = Attributes.builder()
            .put(CodeIncubatingAttributes.CODE_FILEPATH, "/src/main/java/com/example/App.java")
            .put(CodeIncubatingAttributes.CODE_NAMESPACE, "com.example.App")
            .put(CodeIncubatingAttributes.CODE_FUNCTION, "process")
            .put(CodeIncubatingAttributes.CODE_LINENO, 42L)
            .build();

        LogDataMapper logDataMapper = new LogDataMapper(false, false, (b, r) -> {
        });

        TelemetryItem telemetryItem = logDataMapper.map(new SimpleLogRecordData(attributes), null, null);

        MessageData messageData = (MessageData) telemetryItem.getData().getBaseData();
        Map<String, String> properties = messageData.getProperties();

        assertNotNull(properties);
        assertEquals("/src/main/java/com/example/App.java", properties.get("FileName"));
        assertEquals("com.example.App", properties.get("ClassName"));
        assertEquals("process", properties.get("MethodName"));
        assertEquals("42", properties.get("LineNumber"));
    }

    @Test
    void testStableCodeFunctionWithoutNamespace() {
        Attributes attributes = Attributes.builder().put(CodeAttributes.CODE_FUNCTION_NAME, "fopen").build();

        LogDataMapper logDataMapper = new LogDataMapper(false, false, (b, r) -> {
        });

        TelemetryItem telemetryItem = logDataMapper.map(new SimpleLogRecordData(attributes), null, null);

        MessageData messageData = (MessageData) telemetryItem.getData().getBaseData();
        Map<String, String> properties = messageData.getProperties();

        assertNotNull(properties);
        assertEquals("fopen", properties.get("MethodName"));
        assertNull(properties.get("ClassName"));
    }

    private static final class SimpleLogRecordData implements LogRecordData {
        private final Attributes attributes;
        private final long timestamp;

        private SimpleLogRecordData(Attributes attributes) {
            this.attributes = attributes;
            this.timestamp = Instant.now().toEpochMilli() * 1_000_000;
        }

        @Override
        public Resource getResource() {
            return Resource.empty();
        }

        @Override
        public Attributes getAttributes() {
            return attributes;
        }

        @Override
        public InstrumentationScopeInfo getInstrumentationScopeInfo() {
            return InstrumentationScopeInfo.create("TestScope", null, null);
        }

        @Override
        public long getTimestampEpochNanos() {
            return timestamp;
        }

        @Override
        public long getObservedTimestampEpochNanos() {
            return timestamp;
        }

        @Override
        public SpanContext getSpanContext() {
            return SpanContext.getInvalid();
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
            return attributes.size();
        }
    }
}
