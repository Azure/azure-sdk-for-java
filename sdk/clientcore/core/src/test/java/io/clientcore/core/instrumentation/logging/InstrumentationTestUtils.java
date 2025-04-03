// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.instrumentation.logging;

import io.clientcore.core.implementation.AccessibleByteArrayOutputStream;
import io.clientcore.core.implementation.instrumentation.DefaultLogger;
import io.clientcore.core.instrumentation.InstrumentationContext;
import io.clientcore.core.instrumentation.tracing.Span;
import io.clientcore.core.serialization.json.JsonReader;
import io.clientcore.core.utils.CoreUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class InstrumentationTestUtils {
    public static void assertValidSpanId(String spanId) {
        assertNotNull(spanId);
        assertTrue(spanId.matches("[0-9a-f]{16}"));
    }

    public static void assertValidTraceId(String traceId) {
        assertNotNull(traceId);
        assertTrue(traceId.matches("[0-9a-f]{32}"));
    }

    public static ClientLogger setupLogLevelAndGetLogger(LogLevel logLevelToSet, OutputStream logCaptureStream) {
        DefaultLogger logger
            = new DefaultLogger(ClientLogger.class.getName(), new PrintStream(logCaptureStream), logLevelToSet);

        return new ClientLogger(logger, null);
    }

    public static List<Map<String, Object>> parseLogMessages(AccessibleByteArrayOutputStream logCaptureStream) {
        String fullLog = logCaptureStream.toString(StandardCharsets.UTF_8);
        // Changing this from String.lines() has a slight runtime difference where lines() won't have any output if the
        // String is empty where as String.split("\\R") will have an empty string in the array.
        // Filter out any empty lines.
        return Arrays.stream(fullLog.split("\\R"))
            .filter(line -> !CoreUtils.isNullOrEmpty(line)) // Filter out empty lines
            .map(InstrumentationTestUtils::parseLogLine)
            .collect(Collectors.toList());
    }

    private static Map<String, Object> parseLogLine(String logLine) {
        String messageJson = logLine.substring(logLine.indexOf(" - ") + 3);
        System.out.println(messageJson);
        try (JsonReader reader = JsonReader.fromString(messageJson)) {
            return reader.readMap(JsonReader::readUntyped);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static TestInstrumentationContext createRandomInstrumentationContext() {
        String randomTraceId = UUID.randomUUID().toString().toLowerCase(Locale.ROOT).replace("-", "");
        String randomSpanId = UUID.randomUUID().toString().toLowerCase(Locale.ROOT).replace("-", "").substring(0, 16);
        return new TestInstrumentationContext(randomTraceId, randomSpanId, "01", true);
    }

    public static TestInstrumentationContext createInstrumentationContext(String traceId, String spanId) {
        return new TestInstrumentationContext(traceId, spanId, "00", true);
    }

    public static TestInstrumentationContext createInvalidInstrumentationContext() {
        return new TestInstrumentationContext("00000000000000000000000000000000", "0000000000000000", "00", false);
    }

    public static class TestInstrumentationContext implements InstrumentationContext {
        private final String traceId;
        private final String spanId;
        private final String traceFlags;
        private final boolean isValid;

        public TestInstrumentationContext(String traceId, String spanId, String traceFlags, boolean isValid) {
            this.traceId = traceId;
            this.spanId = spanId;
            this.traceFlags = traceFlags;
            this.isValid = isValid;
        }

        @Override
        public String getTraceId() {
            return traceId;
        }

        @Override
        public String getSpanId() {
            return spanId;
        }

        @Override
        public String getTraceFlags() {
            return traceFlags;
        }

        @Override
        public boolean isValid() {
            return isValid;
        }

        @Override
        public Span getSpan() {
            return Span.noop();
        }
    }

    private InstrumentationTestUtils() {
    }
}
