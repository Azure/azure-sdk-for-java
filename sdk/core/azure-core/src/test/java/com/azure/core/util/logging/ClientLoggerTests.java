// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.logging;

import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link ClientLogger}.
 */
public class ClientLoggerTests {
    private static final String PARAMETERIZED_TEST_NAME_TEMPLATE = "[" + ParameterizedTest.INDEX_PLACEHOLDER
        + "] " + ParameterizedTest.DISPLAY_NAME_PLACEHOLDER;

    private PrintStream originalSystemErr;
    private ByteArrayOutputStream logCaptureStream;

    @BeforeEach
    public void setupLoggingConfiguration() {
        /*
         * Indicate to SLF4J to enable trace level logging for a logger named
         * com.azure.core.util.logging.ClientLoggerTests. Trace is the maximum level of logging supported by the
         * ClientLogger.
         */
        System.setProperty("org.slf4j.simpleLogger.log.com.azure.core.util.logging.ClientLoggerTests", "trace");

        /*
         * The default configuration for SLF4J's SimpleLogger uses System.err to log. Inject a custom PrintStream to
         * log into for the duration of the test to capture the log messages.
         */
        originalSystemErr = System.err;
        logCaptureStream = new ByteArrayOutputStream();
        System.setErr(new PrintStream(logCaptureStream));
    }

    @AfterEach
    public void revertLoggingConfiguration() {
        System.clearProperty("org.slf4j.simpleLogger.log.com.azure.core.util.logging.ClientLoggerTests");
        System.setErr(originalSystemErr);
    }

    private void setPropertyToOriginalOrClear(String propertyName, String originalValue) {
        if (CoreUtils.isNullOrEmpty(originalValue)) {
            System.clearProperty(propertyName);
        } else {
            System.setProperty(propertyName, originalValue);
        }
    }

    /**
     * Tests that logging at the same level as the environment logging level will log.
     *
     * @param logLevel Logging level to log a message
     */
    @ParameterizedTest(name = PARAMETERIZED_TEST_NAME_TEMPLATE)
    @ValueSource(ints = { 1, 2, 3, 4 })
    public void logAtSupportedLevel(int logLevel) {
        String logMessage = "This is a test";

        String originalLogLevel = setupLogLevel(logLevel);
        logMessage(new ClientLogger(ClientLoggerTests.class), logLevel, logMessage);
        setPropertyToOriginalOrClear(Configuration.PROPERTY_AZURE_LOG_LEVEL, originalLogLevel);

        String logValues = new String(logCaptureStream.toByteArray(), StandardCharsets.UTF_8);
        assertTrue(logValues.contains(logMessage));
    }

    /**
     * Tests that logging at a level that is less than the environment logging level doesn't log anything.
     *
     * @param logLevel Logging level to log a message
     */
    @ParameterizedTest(name = PARAMETERIZED_TEST_NAME_TEMPLATE)
    @ValueSource(ints = { 1, 2, 3 })
    public void logAtUnsupportedLevel(int logLevel) {
        String logMessage = "This is a test";

        String originalLogLevel = setupLogLevel(logLevel + 1);
        logMessage(new ClientLogger(ClientLoggerTests.class), logLevel, logMessage);
        setPropertyToOriginalOrClear(Configuration.PROPERTY_AZURE_LOG_LEVEL, originalLogLevel);

        String logValues = new String(logCaptureStream.toByteArray(), StandardCharsets.UTF_8);
        assertFalse(logValues.contains(logMessage));
    }

    /**
     * Tests that logging when the environment log level is disabled nothing is logged.
     *
     * @param logLevel Logging level to log a message
     */
    @ParameterizedTest(name = PARAMETERIZED_TEST_NAME_TEMPLATE)
    @ValueSource(ints = { 1, 2, 3, 4 })
    public void logWhenLoggingDisabled(int logLevel) {
        String logMessage = "This is a test";

        String originalLogLevel = setupLogLevel(5);
        logMessage(new ClientLogger(ClientLoggerTests.class), logLevel, logMessage);
        setPropertyToOriginalOrClear(Configuration.PROPERTY_AZURE_LOG_LEVEL, originalLogLevel);

        String logValues = new String(logCaptureStream.toByteArray(), StandardCharsets.UTF_8);
        assertFalse(logValues.contains(logMessage));
    }

    /**
     * Tests that logging an exception when the log level isn't VERBOSE only log the exception message.
     */
    @Test
    public void onlyLogExceptionMessage() {
        String logMessage = "This is an exception";
        String exceptionMessage = "An exception message";
        RuntimeException runtimeException = createRuntimeException(exceptionMessage);

        String originalLogLevel = setupLogLevel(2);
        logMessage(new ClientLogger(ClientLoggerTests.class), 3, logMessage, runtimeException);
        setPropertyToOriginalOrClear(Configuration.PROPERTY_AZURE_LOG_LEVEL, originalLogLevel);

        String logValues = new String(logCaptureStream.toByteArray(), StandardCharsets.UTF_8);
        assertTrue(logValues.contains(logMessage + System.lineSeparator() + runtimeException.getMessage()));
        assertFalse(logValues.contains(runtimeException.getStackTrace()[0].toString()));
    }

    /**
     * Tests that logging an exception when the log level is VERBOSE the stack trace is logged.
     */
    @Test
    public void logExceptionStackTrace() {
        String logMessage = "This is an exception";
        String exceptionMessage = "An exception message";
        RuntimeException runtimeException = createRuntimeException(exceptionMessage);

        String originalLogLevel = setupLogLevel(1);
        logMessage(new ClientLogger(ClientLoggerTests.class), 3, logMessage, runtimeException);
        setPropertyToOriginalOrClear(Configuration.PROPERTY_AZURE_LOG_LEVEL, originalLogLevel);

        String logValues = new String(logCaptureStream.toByteArray(), StandardCharsets.UTF_8);
        assertTrue(logValues.contains(logMessage + System.lineSeparator() + runtimeException.getMessage()));
        assertTrue(logValues.contains(runtimeException.getStackTrace()[0].toString()));
    }

    /**
     * Tests that logging an exception as warning won't include the stack trace when the environment log level isn't
     * VERBOSE. Additionally, this tests that the exception message isn't logged twice as logging an exception uses
     * the exception message as the format string.
     */
    @Test
    public void logExceptionAsWarningOnlyExceptionMessage() {
        String exceptionMessage = "An exception message";
        RuntimeException runtimeException = createRuntimeException(exceptionMessage);

        String originalLogLevel = setupLogLevel(2);
        new ClientLogger(ClientLoggerTests.class).logExceptionAsWarning(runtimeException);
        setPropertyToOriginalOrClear(Configuration.PROPERTY_AZURE_LOG_LEVEL, originalLogLevel);

        String logValues = new String(logCaptureStream.toByteArray(), StandardCharsets.UTF_8);
        assertTrue(logValues.contains(exceptionMessage + System.lineSeparator()));
        assertFalse(logValues.contains(runtimeException.getStackTrace()[0].toString()));
    }

    /**
     * Tests that logging an exception as warning will include the stack trace when the environment log level is set to
     * VERBOSE.
     */
    @Test
    public void logExceptionAsWarningStackTrace() {
        String exceptionMessage = "An exception message";
        RuntimeException runtimeException = createRuntimeException(exceptionMessage);

        String originalLogLevel = setupLogLevel(1);
        new ClientLogger(ClientLoggerTests.class).logExceptionAsWarning(runtimeException);
        setPropertyToOriginalOrClear(Configuration.PROPERTY_AZURE_LOG_LEVEL, originalLogLevel);

        String logValues = new String(logCaptureStream.toByteArray(), StandardCharsets.UTF_8);
        assertTrue(logValues.contains(exceptionMessage + System.lineSeparator()));
        assertTrue(logValues.contains(runtimeException.getStackTrace()[0].toString()));
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

        String originalLogLevel = setupLogLevel(2);
        new ClientLogger(ClientLoggerTests.class).logExceptionAsError(runtimeException);
        setPropertyToOriginalOrClear(Configuration.PROPERTY_AZURE_LOG_LEVEL, originalLogLevel);

        String logValues = new String(logCaptureStream.toByteArray(), StandardCharsets.UTF_8);
        assertTrue(logValues.contains(exceptionMessage + System.lineSeparator()));
        assertFalse(logValues.contains(runtimeException.getStackTrace()[0].toString()));
    }

    /**
     * Tests that logging an exception as error will include the stack trace when the environment log level is set to
     * VERBOSE.
     */
    @Test
    public void logExceptionAsErrorStackTrace() {
        String exceptionMessage = "An exception message";
        RuntimeException runtimeException = createRuntimeException(exceptionMessage);

        String originalLogLevel = setupLogLevel(1);
        new ClientLogger(ClientLoggerTests.class).logExceptionAsError(runtimeException);
        setPropertyToOriginalOrClear(Configuration.PROPERTY_AZURE_LOG_LEVEL, originalLogLevel);

        String logValues = new String(logCaptureStream.toByteArray(), StandardCharsets.UTF_8);
        assertTrue(logValues.contains(exceptionMessage + System.lineSeparator()));
        assertTrue(logValues.contains(runtimeException.getStackTrace()[0].toString()));
    }

    private String setupLogLevel(int logLevelToSet) {
        String originalLogLevel = Configuration.getGlobalConfiguration().get(Configuration.PROPERTY_AZURE_CLOUD);
        System.setProperty(Configuration.PROPERTY_AZURE_LOG_LEVEL, Integer.toString(logLevelToSet));

        return originalLogLevel;
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
