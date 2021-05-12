// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.handler;

import com.azure.core.util.Configuration;
import com.azure.core.util.logging.LogLevel;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import java.util.Arrays;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.logging.LogRecord;
import java.util.logging.Level;
import java.util.logging.Handler;
import static com.azure.core.util.Configuration.PROPERTY_AZURE_LOG_LEVEL;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for {@link JUL2SLF4JHandler}.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class JUL2SLF4JHandler4Logger {

    private static JUL2SLF4JHandler jUL2SLF4JHandler = null;
    private static Configuration configuration = null;
    private static Logger julLogger = null;

    @BeforeAll
    static void beforeAll() {
        JUL2SLF4JHandler.removeHandlersForRootLogger();
        configuration = Configuration.getGlobalConfiguration();
        jUL2SLF4JHandler = new JUL2SLF4JHandler();
        julLogger = Logger.getLogger(JUL2SLF4JHandler4Logger.class.getName());
        julLogger.setLevel(Level.ALL);
        julLogger.addHandler(jUL2SLF4JHandler);
    }

    @AfterAll
    static void afterAll() {
        julLogger.removeHandler(jUL2SLF4JHandler);
    }

    @Test
    void testDebugWithoutParameters() {
        configuration.put(PROPERTY_AZURE_LOG_LEVEL, String.valueOf(LogLevel.VERBOSE.getLogLevel()));
        LogRecord infoLogRecord = new LogRecord(Level.FINEST, "Debug message without parameters and throwable");
        infoLogRecord.setLoggerName(julLogger.getName());
        julLogger.log(infoLogRecord);
        Optional<Handler> optionalHandler = Arrays.stream(julLogger.getHandlers()).filter(handler -> handler instanceof JUL2SLF4JHandler).findFirst();
        assertEquals(optionalHandler.isPresent(), true);
    }

    @Test
    void testDebugWithParameters1() {
        configuration.put(PROPERTY_AZURE_LOG_LEVEL, String.valueOf(LogLevel.VERBOSE.getLogLevel()));
        LogRecord infoLogRecord = new LogRecord(Level.FINEST, "Debug message with parameters {0}/{1}/{2}/{3}, and without throwable");
        infoLogRecord.setParameters(new Object[]{"Parameter 1", "Parameter 2", "Parameter 3", "Parameter 4"});
        infoLogRecord.setLoggerName(julLogger.getName());
        julLogger.log(infoLogRecord);
        Optional<Handler> optionalHandler = Arrays.stream(julLogger.getHandlers()).filter(handler -> handler instanceof JUL2SLF4JHandler).findFirst();
        assertEquals(optionalHandler.isPresent(), true);
    }

    @Test
    void testDebugWithParameters2() {
        configuration.put(PROPERTY_AZURE_LOG_LEVEL, String.valueOf(LogLevel.VERBOSE.getLogLevel()));
        LogRecord infoLogRecord = new LogRecord(Level.FINEST, "Debug message with parameters {0}/{1}/{2}/{3}, and throwable");
        infoLogRecord.setParameters(new Object[]{"Parameter 1", "Parameter 2", "Parameter 3", "Parameter 4"});
        infoLogRecord.setThrown(new Throwable("Test throwable message"));
        infoLogRecord.setLoggerName(julLogger.getName());
        julLogger.log(infoLogRecord);
        Optional<Handler> optionalHandler = Arrays.stream(julLogger.getHandlers()).filter(handler -> handler instanceof JUL2SLF4JHandler).findFirst();
        assertEquals(optionalHandler.isPresent(), true);
    }

    @Test
    void testInfoWithoutParameters() {
        configuration.put(PROPERTY_AZURE_LOG_LEVEL, String.valueOf(LogLevel.INFORMATIONAL.getLogLevel()));
        LogRecord infoLogRecord = new LogRecord(Level.INFO, "Info message without parameters and throwable");
        infoLogRecord.setLoggerName(julLogger.getName());
        julLogger.log(infoLogRecord);
        Optional<Handler> optionalHandler = Arrays.stream(julLogger.getHandlers()).filter(handler -> handler instanceof JUL2SLF4JHandler).findFirst();
        assertEquals(optionalHandler.isPresent(), true);
    }

    @Test
    void testInfoWithParameters() {
        configuration.put(PROPERTY_AZURE_LOG_LEVEL, String.valueOf(LogLevel.INFORMATIONAL.getLogLevel()));
        LogRecord infoLogRecord = new LogRecord(Level.INFO, "Info message with parameters {0}/{1}/{2}/{3}, but without throwable");
        infoLogRecord.setLoggerName(julLogger.getName());
        infoLogRecord.setParameters(new Object[]{"Parameter 1", "Parameter 2", "Parameter 3", "Parameter 4"});
        julLogger.log(infoLogRecord);
        Optional<Handler> optionalHandler = Arrays.stream(julLogger.getHandlers()).filter(handler -> handler instanceof JUL2SLF4JHandler).findFirst();
        assertEquals(optionalHandler.isPresent(), true);
    }

    @Test
    void testWarningWithoutParameters() {
        configuration.put(PROPERTY_AZURE_LOG_LEVEL, String.valueOf(LogLevel.WARNING.getLogLevel()));
        LogRecord infoLogRecord = new LogRecord(Level.WARNING, "Warning message without parameters and throwable");
        infoLogRecord.setLoggerName(julLogger.getName());
        julLogger.log(infoLogRecord);
        Optional<Handler> optionalHandler = Arrays.stream(julLogger.getHandlers()).filter(handler -> handler instanceof JUL2SLF4JHandler).findFirst();
        assertEquals(optionalHandler.isPresent(), true);
    }

    @Test
    void testWarningWithParameters1() {
        configuration.put(PROPERTY_AZURE_LOG_LEVEL, String.valueOf(LogLevel.WARNING.getLogLevel()));
        LogRecord infoLogRecord = new LogRecord(Level.WARNING, "Warning message with parameters {0}/{1}/{2}/{3}, and without throwable");
        infoLogRecord.setParameters(new Object[]{"Parameter 1", "Parameter 2", "Parameter 3", "Parameter 4"});
        infoLogRecord.setLoggerName(julLogger.getName());
        julLogger.log(infoLogRecord);
        Optional<Handler> optionalHandler = Arrays.stream(julLogger.getHandlers()).filter(handler -> handler instanceof JUL2SLF4JHandler).findFirst();
        assertEquals(optionalHandler.isPresent(), true);
    }

    @Test
    void testWarningWithParameters2() {
        configuration.put(PROPERTY_AZURE_LOG_LEVEL, String.valueOf(LogLevel.WARNING.getLogLevel()));
        LogRecord infoLogRecord = new LogRecord(Level.WARNING, "Warning message with parameters {0}/{1}/{2}/{3}, and throwable");
        infoLogRecord.setParameters(new Object[]{"Parameter 1", "Parameter 2", "Parameter 3", "Parameter 4", new Throwable("Test throwable message")});
        infoLogRecord.setThrown(new Throwable("Test throwable message"));
        infoLogRecord.setLoggerName(julLogger.getName());
        julLogger.log(infoLogRecord);
        Optional<Handler> optionalHandler = Arrays.stream(julLogger.getHandlers()).filter(handler -> handler instanceof JUL2SLF4JHandler).findFirst();
        assertEquals(optionalHandler.isPresent(), true);
    }


    @Test
    void testErrorWithoutParameters() {
        configuration.put(PROPERTY_AZURE_LOG_LEVEL, String.valueOf(LogLevel.ERROR.getLogLevel()));
        LogRecord infoLogRecord = new LogRecord(Level.SEVERE, "Error message without parameters and throwable");
        infoLogRecord.setLoggerName(julLogger.getName());
        julLogger.log(infoLogRecord);
        Optional<Handler> optionalHandler = Arrays.stream(julLogger.getHandlers()).filter(handler -> handler instanceof JUL2SLF4JHandler).findFirst();
        assertEquals(optionalHandler.isPresent(), true);
    }

    @Test
    void testErrorWithParameters1() {
        configuration.put(PROPERTY_AZURE_LOG_LEVEL, String.valueOf(LogLevel.ERROR.getLogLevel()));
        LogRecord infoLogRecord = new LogRecord(Level.SEVERE, "Error message with parameters {0}/{1}/{2}/{3}, and without throwable");
        infoLogRecord.setParameters(new Object[]{"Parameter 1", "Parameter 2", "Parameter 3", "Parameter 4"});
        infoLogRecord.setLoggerName(julLogger.getName());
        julLogger.log(infoLogRecord);
        Optional<Handler> optionalHandler = Arrays.stream(julLogger.getHandlers()).filter(handler -> handler instanceof JUL2SLF4JHandler).findFirst();
        assertEquals(optionalHandler.isPresent(), true);
    }

    @Test
    void testErrorWithParameters2() {
        configuration.put(PROPERTY_AZURE_LOG_LEVEL, String.valueOf(LogLevel.ERROR.getLogLevel()));
        LogRecord infoLogRecord = new LogRecord(Level.SEVERE, "Error message with parameters {0}/{1}/{2}/{3}, and throwable");
        infoLogRecord.setParameters(new Object[]{"Parameter 1", "Parameter 2", "Parameter 3", "Parameter 4"});
        infoLogRecord.setThrown(new Throwable("Test throwable message"));
        infoLogRecord.setLoggerName(julLogger.getName());
        julLogger.log(infoLogRecord);
        Optional<Handler> optionalHandler = Arrays.stream(julLogger.getHandlers()).filter(handler -> handler instanceof JUL2SLF4JHandler).findFirst();
        assertEquals(optionalHandler.isPresent(), true);
    }
}
