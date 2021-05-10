// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.handler;

import com.azure.core.util.Configuration;
import com.azure.core.util.logging.LogLevel;
import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.Optional;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.logging.LogRecord;
import java.util.logging.Level;
import java.util.logging.Handler;
import static com.azure.core.util.Configuration.PROPERTY_AZURE_LOG_LEVEL;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for {@link JUL2SLF4JHandler}.
 */
public class JUL2SLF4JHandlerTest {

    private static final Configuration configuration = Configuration.getGlobalConfiguration();
    private static Boolean isRootLogger = true;

    @Test
    public void jul2slf4jTest() {
        jul2slf4jTest4LogManager();
        jul2slf4jTest4Logger();
    }

    private void jul2slf4jTest4LogManager() {

        JUL2SLF4JHandler.removeHandlersForRootLogger();

        JUL2SLF4JHandler.install();

        LogManager.getLogManager().getLogger("").setLevel(Level.ALL);

        Logger julLogger = Logger.getLogger(JUL2SLF4JHandlerTest.class.getName());

        configuration.put(PROPERTY_AZURE_LOG_LEVEL,String.valueOf(LogLevel.VERBOSE.getLogLevel()));
        testDebugWithoutParameters(julLogger);
        testDebugWithParameters1(julLogger);
        testDebugWithParameters2(julLogger);

        configuration.put(PROPERTY_AZURE_LOG_LEVEL,String.valueOf(LogLevel.INFORMATIONAL.getLogLevel()));
        testInfoWithoutParameters(julLogger);
        testInfoWithParameters(julLogger);

        configuration.put(PROPERTY_AZURE_LOG_LEVEL,String.valueOf(LogLevel.WARNING.getLogLevel()));
        testWarningWithoutParameters(julLogger);
        testWarningWithParameters1(julLogger);
        testWarningWithParameters2(julLogger);

        configuration.put(PROPERTY_AZURE_LOG_LEVEL,String.valueOf(LogLevel.ERROR.getLogLevel()));
        testErrorWithoutParameters(julLogger);
        testErrorWithParameters1(julLogger);
        testErrorWithParameters2(julLogger);

        JUL2SLF4JHandler.uninstall();

    }

    private void jul2slf4jTest4Logger() {

        JUL2SLF4JHandler.removeHandlersForRootLogger();

        Logger julLogger = Logger.getLogger(JUL2SLF4JHandlerTest.class.getName());
        julLogger.setLevel(Level.ALL);

        JUL2SLF4JHandler jul2slf4jHandler = new JUL2SLF4JHandler();
        julLogger.addHandler(jul2slf4jHandler);

        isRootLogger = false;

        configuration.put(PROPERTY_AZURE_LOG_LEVEL,String.valueOf(LogLevel.VERBOSE.getLogLevel()));
        testDebugWithoutParameters(julLogger);
        testDebugWithParameters1(julLogger);
        testDebugWithParameters2(julLogger);

        configuration.put(PROPERTY_AZURE_LOG_LEVEL,String.valueOf(LogLevel.INFORMATIONAL.getLogLevel()));
        testInfoWithoutParameters(julLogger);
        testInfoWithParameters(julLogger);

        configuration.put(PROPERTY_AZURE_LOG_LEVEL,String.valueOf(LogLevel.WARNING.getLogLevel()));
        testWarningWithoutParameters(julLogger);
        testWarningWithParameters1(julLogger);
        testWarningWithParameters2(julLogger);

        configuration.put(PROPERTY_AZURE_LOG_LEVEL,String.valueOf(LogLevel.ERROR.getLogLevel()));
        testErrorWithoutParameters(julLogger);
        testErrorWithParameters1(julLogger);
        testErrorWithParameters2(julLogger);

        julLogger.removeHandler(jul2slf4jHandler);

    }

    private void testInfoWithoutParameters(Logger julLogger) {
        LogRecord infoLogRecord = new LogRecord(Level.INFO, "Info message without parameters and throwable");
        infoLogRecord.setLoggerName(julLogger.getName());
        julLogger.log(infoLogRecord);
        if (isRootLogger) {
            assertEquals(JUL2SLF4JHandler.isInstalled(),true);
        } else {
            Optional<Handler> optionalHandler = Arrays.stream(julLogger.getHandlers()).filter(handler -> handler instanceof JUL2SLF4JHandler).findFirst();
            assertEquals(optionalHandler.isPresent(), true);
        }
    }

    private void testInfoWithParameters(Logger julLogger) {
        LogRecord infoLogRecord = new LogRecord(Level.INFO, "Info message with parameters {0}/{1}/{2}/{3}, but without throwable");
        infoLogRecord.setLoggerName(julLogger.getName());
        infoLogRecord.setParameters(new Object[]{"Parameter 1", "Parameter 2", "Parameter 3", "Parameter 4"});
        julLogger.log(infoLogRecord);
        if (isRootLogger) {
            assertEquals(JUL2SLF4JHandler.isInstalled(),true);
        } else {
            Optional<Handler> optionalHandler = Arrays.stream(julLogger.getHandlers()).filter(handler -> handler instanceof JUL2SLF4JHandler).findFirst();
            assertEquals(optionalHandler.isPresent(), true);
        }
    }

    private void testWarningWithoutParameters(Logger julLogger) {
        LogRecord infoLogRecord = new LogRecord(Level.WARNING, "Warning message without parameters and throwable");
        infoLogRecord.setLoggerName(julLogger.getName());
        julLogger.log(infoLogRecord);
        if (isRootLogger) {
            assertEquals(JUL2SLF4JHandler.isInstalled(),true);
        } else {
            Optional<Handler> optionalHandler = Arrays.stream(julLogger.getHandlers()).filter(handler -> handler instanceof JUL2SLF4JHandler).findFirst();
            assertEquals(optionalHandler.isPresent(), true);
        }
    }

    private void testWarningWithParameters1(Logger julLogger) {
        LogRecord infoLogRecord = new LogRecord(Level.WARNING, "Warning message with parameters {0}/{1}/{2}/{3}, and without throwable");
        infoLogRecord.setParameters(new Object[]{"Parameter 1", "Parameter 2", "Parameter 3", "Parameter 4"});
        infoLogRecord.setLoggerName(julLogger.getName());
        julLogger.log(infoLogRecord);
        if (isRootLogger) {
            assertEquals(JUL2SLF4JHandler.isInstalled(),true);
        } else {
            Optional<Handler> optionalHandler = Arrays.stream(julLogger.getHandlers()).filter(handler -> handler instanceof JUL2SLF4JHandler).findFirst();
            assertEquals(optionalHandler.isPresent(), true);
        }
    }

    private void testWarningWithParameters2(Logger julLogger) {
        LogRecord infoLogRecord = new LogRecord(Level.WARNING, "Warning message with parameters {0}/{1}/{2}/{3}, and throwable");
        infoLogRecord.setParameters(new Object[]{"Parameter 1", "Parameter 2", "Parameter 3", "Parameter 4", new Throwable("Test throwable message")});
        infoLogRecord.setThrown(new Throwable("Test throwable message"));
        infoLogRecord.setLoggerName(julLogger.getName());
        julLogger.log(infoLogRecord);
        if (isRootLogger) {
            assertEquals(JUL2SLF4JHandler.isInstalled(),true);
        } else {
            Optional<Handler> optionalHandler = Arrays.stream(julLogger.getHandlers()).filter(handler -> handler instanceof JUL2SLF4JHandler).findFirst();
            assertEquals(optionalHandler.isPresent(), true);
        }
    }

    private void testDebugWithoutParameters(Logger julLogger) {
        LogRecord infoLogRecord = new LogRecord(Level.FINEST, "Debug message without parameters and throwable");
        infoLogRecord.setLoggerName(julLogger.getName());
        julLogger.log(infoLogRecord);
        if (isRootLogger) {
            assertEquals(JUL2SLF4JHandler.isInstalled(),true);
        } else {
            Optional<Handler> optionalHandler = Arrays.stream(julLogger.getHandlers()).filter(handler -> handler instanceof JUL2SLF4JHandler).findFirst();
            assertEquals(optionalHandler.isPresent(), true);
        }
    }

    private void testDebugWithParameters1(Logger julLogger) {
        LogRecord infoLogRecord = new LogRecord(Level.FINEST, "Debug message with parameters {0}/{1}/{2}/{3}, and without throwable");
        infoLogRecord.setParameters(new Object[]{"Parameter 1", "Parameter 2", "Parameter 3", "Parameter 4"});
        infoLogRecord.setLoggerName(julLogger.getName());
        julLogger.log(infoLogRecord);
        if (isRootLogger) {
            assertEquals(JUL2SLF4JHandler.isInstalled(),true);
        } else {
            Optional<Handler> optionalHandler = Arrays.stream(julLogger.getHandlers()).filter(handler -> handler instanceof JUL2SLF4JHandler).findFirst();
            assertEquals(optionalHandler.isPresent(), true);
        }
    }

    private void testDebugWithParameters2(Logger julLogger) {
        LogRecord infoLogRecord = new LogRecord(Level.FINEST, "Debug message with parameters {0}/{1}/{2}/{3}, and throwable");
        infoLogRecord.setParameters(new Object[]{"Parameter 1", "Parameter 2", "Parameter 3", "Parameter 4"});
        infoLogRecord.setThrown(new Throwable("Test throwable message"));
        infoLogRecord.setLoggerName(julLogger.getName());
        julLogger.log(infoLogRecord);
        if (isRootLogger) {
            assertEquals(JUL2SLF4JHandler.isInstalled(),true);
        } else {
            Optional<Handler> optionalHandler = Arrays.stream(julLogger.getHandlers()).filter(handler -> handler instanceof JUL2SLF4JHandler).findFirst();
            assertEquals(optionalHandler.isPresent(), true);
        }
    }

    private void testErrorWithoutParameters(Logger julLogger) {
        LogRecord infoLogRecord = new LogRecord(Level.SEVERE, "Error message without parameters and throwable");
        infoLogRecord.setLoggerName(julLogger.getName());
        julLogger.log(infoLogRecord);
        if (isRootLogger) {
            assertEquals(JUL2SLF4JHandler.isInstalled(),true);
        } else {
            Optional<Handler> optionalHandler = Arrays.stream(julLogger.getHandlers()).filter(handler -> handler instanceof JUL2SLF4JHandler).findFirst();
            assertEquals(optionalHandler.isPresent(), true);
        }
    }

    private void testErrorWithParameters1(Logger julLogger) {
        LogRecord infoLogRecord = new LogRecord(Level.SEVERE, "Error message with parameters {0}/{1}/{2}/{3}, and without throwable");
        infoLogRecord.setParameters(new Object[]{"Parameter 1", "Parameter 2", "Parameter 3", "Parameter 4"});
        infoLogRecord.setLoggerName(julLogger.getName());
        julLogger.log(infoLogRecord);
        if (isRootLogger) {
            assertEquals(JUL2SLF4JHandler.isInstalled(),true);
        } else {
            Optional<Handler> optionalHandler = Arrays.stream(julLogger.getHandlers()).filter(handler -> handler instanceof JUL2SLF4JHandler).findFirst();
            assertEquals(optionalHandler.isPresent(), true);
        }
    }

    private void testErrorWithParameters2(Logger julLogger) {
        LogRecord infoLogRecord = new LogRecord(Level.SEVERE, "Error message with parameters {0}/{1}/{2}/{3}, and throwable");
        infoLogRecord.setParameters(new Object[]{"Parameter 1", "Parameter 2", "Parameter 3", "Parameter 4"});
        infoLogRecord.setThrown(new Throwable("Test throwable message"));
        infoLogRecord.setLoggerName(julLogger.getName());
        julLogger.log(infoLogRecord);
        if (isRootLogger) {
            assertEquals(JUL2SLF4JHandler.isInstalled(),true);
        } else {
            Optional<Handler> optionalHandler = Arrays.stream(julLogger.getHandlers()).filter(handler -> handler instanceof JUL2SLF4JHandler).findFirst();
            assertEquals(optionalHandler.isPresent(), true);
        }
    }
}
