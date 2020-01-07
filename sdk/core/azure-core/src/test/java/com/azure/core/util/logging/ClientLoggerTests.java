// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.logging;

import com.azure.core.util.Configuration;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link ClientLogger}.
 */
public class ClientLoggerTests {
    private static final String PARAMETERIZED_TEST_NAME_TEMPLATE = "[" + ParameterizedTest.INDEX_PLACEHOLDER
        + "] " + ParameterizedTest.DISPLAY_NAME_PLACEHOLDER;

    private ByteArrayOutputStream logCaptureStream;
    private StreamHandler sh;

    private Logger mockLogger;
    private ClientLogger clientLogger;

    @BeforeEach
    public void setupLoggingConfiguration() throws Exception {
        InputStream stream = ClientLoggerTests.class.getClassLoader().
            getResourceAsStream("logging.properties");
        LogManager.getLogManager().readConfiguration(stream);
        new ClientLogger("test").verbose("I am talking");
        logCaptureStream = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(logCaptureStream);
        sh = new StreamHandler(ps, new SimpleFormatter());
        sh.setLevel(Level.FINEST);
        clientLogger = new ClientLogger(ClientLoggerTests.class);
        mockLogger = Logger.getLogger("test");
        mockLogger.addHandler(sh);
        Field f = ClientLogger.class.getDeclaredField("defaultLogger");
        f.setAccessible(true);
        f.set(clientLogger, mockLogger);
    }

    @AfterEach
    public void revertLoggingConfiguration() throws Exception {
        sh.close();
        logCaptureStream.close();
    }

    /**
     * Tests that logging at the same level as the environment logging level will log.
     *
     * @param logLevel Logging level to log a message
     */
    @ParameterizedTest(name = PARAMETERIZED_TEST_NAME_TEMPLATE)
    @ValueSource(ints = { 1, 2, 3, 4 })
    public void logAtSupportedLevelAtBinding(int logLevel) {
        setupLogLevel(logLevel);
        String logMessage = "This is a test";
        logMessage(clientLogger, logLevel, logMessage);

        sh.flush();
        String logValues = logCaptureStream.toString(StandardCharsets.UTF_8);
        assertTrue(logValues.contains(logMessage));
    }

    /**
     * Tests that logging at a level that is less than the environment logging level doesn't log anything.
     *
     * @param logLevel Logging level to log a message
     */
    @ParameterizedTest(name = PARAMETERIZED_TEST_NAME_TEMPLATE)
    @ValueSource(ints = {1, 2, 3 })
    public void logAtUnsupportedLevel(int logLevel) {
        String logMessage = "This is a test";

        setupLogLevel(logLevel + 1);
        logMessage(clientLogger, logLevel, logMessage);

        sh.flush();
        String logValues = new String(logCaptureStream.toByteArray(), StandardCharsets.UTF_8);
        assertTrue(logValues.isEmpty());
    }

    /**
     * Tests that logging when the environment log level is disabled nothing is logged.
     */
    @Test
    public void logWhenLoggingNotSet() {
        assertEquals(LogLevel.NOT_SET, LogLevel.fromString(null));
    }

    /**
     * Tests that logging an exception when the log level isn't VERBOSE only log the exception message.
     */
    @Test
    public void onlyLogExceptionMessage() {
        String logMessage = "This is an exception";
        String exceptionMessage = "An exception message";
        RuntimeException runtimeException = createRuntimeException(exceptionMessage);

        setupLogLevel(2);
        logMessage(clientLogger, 3, logMessage, runtimeException);
        sh.flush();
        String logValues = new String(logCaptureStream.toByteArray(), StandardCharsets.UTF_8);
        assertTrue(logValues.contains(logMessage + System.lineSeparator() + runtimeException.getMessage()));
    }

    /**
     * Tests that logging an exception when the log level is ERROR the stack trace is logged.
     */
    @Test
    public void logExceptionStackTraceWithNoLogLevel() {
        String logMessage = "This is an exception";
        String exceptionMessage = "An exception message";
        RuntimeException runtimeException = createRuntimeException(exceptionMessage);

        setupLogLevel(1);
        logMessage(clientLogger, 5, logMessage, runtimeException);

        String logValues = new String(logCaptureStream.toByteArray(), StandardCharsets.UTF_8);
        assertTrue(logValues.isEmpty());
    }

    /**
     * Tests that logging an exception when the log level is ERROR the stack trace is logged.
     */
    @Test
    public void logExceptionWithInvalidLogLevel() {
        String logMessage = "This is an exception";
        Object runtimeException = new Object();

        setupLogLevel(1);
        logMessage(clientLogger, 3, logMessage, runtimeException);
        sh.flush();

        String logValues = new String(logCaptureStream.toByteArray(), StandardCharsets.UTF_8);
        assertTrue(logValues.contains(logMessage));
    }

//    @Test
//    public void testNoBindingNoEnv() {
//        String logMessage = "This is an exception";
//
//        System.clearProperty(Configuration.PROPERTY_AZURE_LOG_LEVEL);
//        logMessage(clientLogger, 4, logMessage);
//        sh.flush();
//
//        String logValues = new String(logCaptureStream.toByteArray(), StandardCharsets.UTF_8);
//        assertTrue(logValues.isEmpty());
//    }

    /**
     * Tests that logging an exception as warning won't include the stack trace when the environment log level isn't
     * VERBOSE. Additionally, this tests that the exception message isn't logged twice as logging an exception uses
     * the exception message as the format string.
     */
    @Test
    public void logExceptionAsWarningOnlyExceptionMessage() {
        String exceptionMessage = "An exception message";
        RuntimeException runtimeException = createRuntimeException(exceptionMessage);

        setupLogLevel(1);
        clientLogger.logExceptionAsWarning(runtimeException);
        sh.flush();

        String logValues = new String(logCaptureStream.toByteArray(), StandardCharsets.UTF_8);
        assertTrue(logValues.contains(exceptionMessage + System.lineSeparator()));
    }

    /**
     * Tests that logging an exception as error won't include the stack trace when the environment log level isn't
     * VERBOSE. Additionally, this tests that the exception message isn't logged twice as logging an exception uses
     * the exception message as the format string.
     */
    @Test
    public void logExceptionAsErrorOnlyExceptionMessage() {
        String exceptionMessage = "An exception message";
        RuntimeException runtimeException = createRuntimeException(exceptionMessage);

        setupLogLevel(2);
        clientLogger.logExceptionAsError(runtimeException);
        sh.flush();

        String logValues = new String(logCaptureStream.toByteArray(), StandardCharsets.UTF_8);
        assertTrue(logValues.contains(exceptionMessage + System.lineSeparator()));
        assertFalse(logValues.contains(runtimeException.getStackTrace()[0].toString()));
    }

    @ParameterizedTest(name = "{index} from logLevelToConfigure = {0}, logLevelToValidate = {1}, expected = {2}")
    @CsvSource({"1, 1, true", "1, 2, true", "1, 3, true", "1, 4, true", "2, 1, false", "1, VERBOSE, true", "1, info, true", "1, warning, true", "1, error, true", "2, verbose, false"})
    public void canLogAtLevel(int logLevelToConfigure, String logLevelToValidate, boolean expected) {
        setupLogLevel(logLevelToConfigure);

        LogLevel logLevel = LogLevel.fromString(logLevelToValidate);
        assertEquals(new ClientLogger(UUID.randomUUID().toString()).canLogAtLevel(logLevel), expected);
    }

    @Test
    public void canLogAtLevelInvalid() {
        assertThrows(IllegalArgumentException.class, () -> LogLevel.fromString("5"));
    }

    private void setupLogLevel(int logLevelToSet) {
        System.setProperty(Configuration.PROPERTY_AZURE_LOG_LEVEL, Integer.toString(logLevelToSet));
    }

    private void logMessage(ClientLogger logger, int logLevelToLog, String logFormat, Object... arguments) {
        switch (logLevelToLog) {
            case 1:
                logger.verbose(logFormat, arguments);
                break;
            case 2:
                logger.info(logFormat, arguments);
                break;
            case 3:
                logger.warning(logFormat, arguments);
                break;
            case 4:
                logger.error(logFormat, arguments);
                break;
            default:
                break;
        }
    }

    private RuntimeException createRuntimeException(String message) {
        RuntimeException runtimeException = new RuntimeException(message);
        StackTraceElement[] stackTraceElements = { new StackTraceElement("ClientLoggerTests", "onlyLogExceptionMessage",
            "ClientLoggerTests", 117) };
        runtimeException.setStackTrace(stackTraceElements);

        return runtimeException;
    }

}
