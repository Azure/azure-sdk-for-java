package com.azure.core.util.logging;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LoggingUtilsTests {
    private static final String NEW_LINE = System.lineSeparator();
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"hello", "hello world\t123"})
    public void noNewLine(String message) {
        assertEquals(message, LoggingUtils.sanitizeLogMessageInput(message));
    }

    @ParameterizedTest
    @MethodSource("messagesWithNewLine")
    public void newLine(String message) {
        assertEquals("hello", LoggingUtils.sanitizeLogMessageInput(message));
    }

    @Test
    public void newLineOnly() {
        assertEquals("", LoggingUtils.sanitizeLogMessageInput(NEW_LINE + NEW_LINE + NEW_LINE));
    }

    @Test
    public void withCL() {
        assertEquals("\r\thello\rworld", LoggingUtils.sanitizeLogMessageInput("\r\thello\r" + NEW_LINE + "world"));
    }

    private static Stream<String> messagesWithNewLine() {
        return Stream.of(
            "hello" + NEW_LINE + NEW_LINE,
            NEW_LINE + "hello" + NEW_LINE,
            NEW_LINE + NEW_LINE + "hello",
            NEW_LINE + "he" + NEW_LINE + "l" + NEW_LINE + "lo");
    }
}
