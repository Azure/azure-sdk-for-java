// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.logging;

import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static com.azure.core.util.Configuration.PROPERTY_AZURE_LOG_LEVEL;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link ClientLogger}.
 */
public class ClientLoggerTests {
    private PrintStream originalSystemOut;
    private ByteArrayOutputStream logCaptureStream;

    @BeforeEach
    public void setupLoggingConfiguration() {
        /*
         * DefaultLogger uses System.out to log. Inject a custom PrintStream to log into for the duration of the test to
         * capture the log messages.
         */
        originalSystemOut = System.out;
        logCaptureStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(logCaptureStream));
    }

    @AfterEach
    public void revertLoggingConfiguration() throws Exception {
        System.setOut(originalSystemOut);
        logCaptureStream.close();
    }

    /**
     * Test whether the logger supports a given log level based on its configured log level.
     */
    @ParameterizedTest
    @MethodSource("singleLevelCheckSupplier")
    @ResourceLock("SYSTEM_OUT")
    public void canLogAtLevel(LogLevel logLevelToConfigure, LogLevel logLevelToValidate, boolean expected) {
        String originalLogLevel = setupLogLevel(logLevelToConfigure.getLogLevel());
        assertEquals(expected, new ClientLogger(ClientLoggerTests.class).canLogAtLevel(logLevelToValidate));
        setPropertyToOriginalOrClear(originalLogLevel);
    }

    /**
     * Test whether a log will be captured when the ClientLogger and message are configured to the passed log levels.
     */
    @ParameterizedTest
    @MethodSource("singleLevelCheckSupplier")
    @ResourceLock("SYSTEM_OUT")
    public void logSimpleMessage(LogLevel logLevelToConfigure, LogLevel logLevelToUse, boolean logContainsMessage)
        throws UnsupportedEncodingException {
        String logMessage = "This is a test";

        String originalLogLevel = setupLogLevel(logLevelToConfigure.getLogLevel());
        logMessage(new ClientLogger(ClientLoggerTests.class), logLevelToUse, logMessage);

        setPropertyToOriginalOrClear(originalLogLevel);

        String logValues = byteArraySteamToString(logCaptureStream);
        assertEquals(logContainsMessage, logValues.contains(logMessage));
    }

    /**
     * Test whether a log will be captured when the ClientLogger and message are configured to the passed log levels.
     */
    @ParameterizedTest
    @MethodSource("logMaliciousErrorSupplier")
    @ResourceLock("SYSTEM_OUT")
    public void logMaliciousMessage(LogLevel logLevelToConfigure, LogLevel logLevelToUse)
        throws UnsupportedEncodingException {
        String logMessage = "You have successfully authenticated, \r\n[INFO] User dummy was not"
                                + " successfully authenticated.";

        String expectedMessage = "You have successfully authenticated, [INFO] User dummy was not"
                                     + " successfully authenticated.";

        String originalLogLevel = setupLogLevel(logLevelToConfigure.getLogLevel());
        logMessage(new ClientLogger(ClientLoggerTests.class), logLevelToUse, logMessage);

        setPropertyToOriginalOrClear(originalLogLevel);

        String logValues = byteArraySteamToString(logCaptureStream);
        System.out.println(logValues);
        assertTrue(logValues.contains(expectedMessage));
    }

    @ParameterizedTest
    @MethodSource("singleLevelCheckSupplier")
    @ResourceLock("SYSTEM_OUT")
    public void logFormattedMessage(LogLevel logLevelToConfigure, LogLevel logLevelToUse, boolean logContainsMessage)
        throws UnsupportedEncodingException {
        String logMessage = "This is a test";
        String logFormat = "{} is a {}";

        String originalLogLevel = setupLogLevel(logLevelToConfigure.getLogLevel());
        logMessage(new ClientLogger(ClientLoggerTests.class), logLevelToUse, logFormat, "This", "test");

        setPropertyToOriginalOrClear(originalLogLevel);

        String logValues = byteArraySteamToString(logCaptureStream);
        assertEquals(logContainsMessage, logValues.contains(logMessage));
    }

    /**
     * Tests whether a log will contain the exception message when the ClientLogger and message are configured to the
     * passed log levels.
     */
    @ParameterizedTest
    @MethodSource("multiLevelCheckSupplier")
    @ResourceLock("SYSTEM_OUT")
    public void logException(LogLevel logLevelToConfigure, LogLevel logLevelToUse, boolean logContainsMessage,
        boolean logContainsStackTrace) throws UnsupportedEncodingException {
        String logMessage = "This is an exception";
        String exceptionMessage = "An exception message";
        RuntimeException runtimeException = createIllegalStateException(exceptionMessage);

        String originalLogLevel = setupLogLevel(logLevelToConfigure.getLogLevel());
        logMessage(new ClientLogger(ClientLoggerTests.class), logLevelToUse, logMessage, runtimeException);
        setPropertyToOriginalOrClear(originalLogLevel);

        String logValues = byteArraySteamToString(logCaptureStream);
        assertEquals(logContainsMessage, logValues.contains(logMessage + System.lineSeparator() + runtimeException.getMessage()));
        assertEquals(logContainsStackTrace, logValues.contains(runtimeException.getStackTrace()[0].toString()));
    }

    /**
     * Tests that logging a RuntimeException as warning will log a message and stack trace appropriately based on the
     * configured log level.
     */
    @ParameterizedTest
    @MethodSource("logExceptionAsWarningSupplier")
    @ResourceLock("SYSTEM_OUT")
    public void logExceptionAsWarning(LogLevel logLevelToConfigure, boolean logContainsMessage,
        boolean logContainsStackTrace) throws UnsupportedEncodingException {
        String exceptionMessage = "An exception message";
        IllegalStateException illegalStateException = createIllegalStateException(exceptionMessage);

        String originalLogLevel = setupLogLevel(logLevelToConfigure.getLogLevel());
        try {
            throw new ClientLogger(ClientLoggerTests.class).logExceptionAsWarning(illegalStateException);
        } catch (RuntimeException exception) {
            assertTrue(exception instanceof IllegalStateException);
        }
        setPropertyToOriginalOrClear(originalLogLevel);

        String logValues = byteArraySteamToString(logCaptureStream);
        assertEquals(logContainsMessage, logValues.contains(exceptionMessage + System.lineSeparator()));
        assertEquals(logContainsStackTrace, logValues.contains(illegalStateException.getStackTrace()[0].toString()));
    }

    /**
     * Tests that logging a Throwable as warning will log a message and stack trace appropriately based on the
     * configured log level.
     */
    @ParameterizedTest
    @MethodSource("logExceptionAsWarningSupplier")
    @ResourceLock("SYSTEM_OUT")
    public void logCheckedExceptionAsWarning(LogLevel logLevelToConfigure, boolean logContainsMessage,
        boolean logContainsStackTrace) throws UnsupportedEncodingException {
        String exceptionMessage = "An exception message";
        IOException ioException = createIOException(exceptionMessage);

        String originalLogLevel = setupLogLevel(logLevelToConfigure.getLogLevel());
        try {
            throw new ClientLogger(ClientLoggerTests.class).logThrowableAsWarning(ioException);
        } catch (Throwable throwable) {
            assertTrue(throwable instanceof IOException);
        }
        setPropertyToOriginalOrClear(originalLogLevel);

        String logValues = byteArraySteamToString(logCaptureStream);
        assertEquals(logContainsMessage, logValues.contains(exceptionMessage + System.lineSeparator()));
        assertEquals(logContainsStackTrace, logValues.contains(ioException.getStackTrace()[0].toString()));
    }

    /**
     * Tests that logging a RuntimeException as error will log a message and stack trace appropriately based on the
     * configured log level.
     */
    @ParameterizedTest
    @MethodSource("logExceptionAsErrorSupplier")
    @ResourceLock("SYSTEM_OUT")
    public void logExceptionAsError(LogLevel logLevelToConfigure, boolean logContainsMessage,
        boolean logContainsStackTrace) throws UnsupportedEncodingException {
        String exceptionMessage = "An exception message";
        IllegalStateException illegalStateException = createIllegalStateException(exceptionMessage);

        String originalLogLevel = setupLogLevel(logLevelToConfigure.getLogLevel());
        try {
            throw new ClientLogger(ClientLoggerTests.class).logExceptionAsError(illegalStateException);
        } catch (RuntimeException exception) {
            assertTrue(exception instanceof IllegalStateException);
        }
        setPropertyToOriginalOrClear(originalLogLevel);

        String logValues = byteArraySteamToString(logCaptureStream);
        assertEquals(logContainsMessage, logValues.contains(exceptionMessage + System.lineSeparator()));
        assertEquals(logContainsStackTrace, logValues.contains(illegalStateException.getStackTrace()[0].toString()));
    }

    /**
     * Tests that logging a Throwable as error will log a message and stack trace appropriately based on the configured
     * log level.
     */
    @ParameterizedTest
    @MethodSource("logExceptionAsErrorSupplier")
    @ResourceLock("SYSTEM_OUT")
    public void logCheckedExceptionAsError(LogLevel logLevelToConfigure, boolean logContainsMessage,
        boolean logContainsStackTrace) throws UnsupportedEncodingException {
        String exceptionMessage = "An exception message";
        IOException ioException = createIOException(exceptionMessage);

        String originalLogLevel = setupLogLevel(logLevelToConfigure.getLogLevel());
        try {
            throw new ClientLogger(ClientLoggerTests.class).logThrowableAsError(ioException);
        } catch (Throwable throwable) {
            assertTrue(throwable instanceof IOException);
        }
        setPropertyToOriginalOrClear(originalLogLevel);

        String logValues = byteArraySteamToString(logCaptureStream);
        assertEquals(logContainsMessage, logValues.contains(exceptionMessage + System.lineSeparator()));
        assertEquals(logContainsStackTrace, logValues.contains(ioException.getStackTrace()[0].toString()));
    }

    /**
     * Tests that LogLevel.fromString returns the expected LogLevel enum based on the passed environment configuration.
     */
    @ParameterizedTest
    @MethodSource("validLogLevelSupplier")
    @ResourceLock("SYSTEM_OUT")
    public void logLevelFromString(String environmentLogLevel, LogLevel expected) {
        assertEquals(expected, LogLevel.fromString(environmentLogLevel));
    }

    /**
     * Tests that LogLevel.fromString will throw an illegal argument exception when passed an environment configuration
     * it doesn't support.
     */
    @ParameterizedTest
    @ValueSource(strings = {"errs", "not_set", "12", "onlyerrorsplease"})
    @ResourceLock("SYSTEM_OUT")
    public void invalidLogLevelFromString(String environmentLogLevel) {
        assertThrows(IllegalArgumentException.class, () -> LogLevel.fromString(environmentLogLevel));
    }

    private String setupLogLevel(int logLevelToSet) {
        String originalLogLevel = Configuration.getGlobalConfiguration().get(PROPERTY_AZURE_LOG_LEVEL);
        Configuration.getGlobalConfiguration().put(PROPERTY_AZURE_LOG_LEVEL, String.valueOf(logLevelToSet));
        return originalLogLevel;
    }

    private void setPropertyToOriginalOrClear(String originalValue) {
        if (CoreUtils.isNullOrEmpty(originalValue)) {
            Configuration.getGlobalConfiguration().remove(PROPERTY_AZURE_LOG_LEVEL);
        } else {
            Configuration.getGlobalConfiguration().put(PROPERTY_AZURE_LOG_LEVEL, originalValue);
        }
    }

    private void logMessage(ClientLogger logger, LogLevel logLevel, String logFormat, Object... arguments) {
        if (logLevel == null) {
            return;
        }

        switch (logLevel) {
            case VERBOSE:
                logHelper(() -> logger.verbose(logFormat), (args) -> logger.verbose(logFormat, args), arguments);
                break;
            case INFORMATIONAL:
                logHelper(() -> logger.info(logFormat), (args) -> logger.info(logFormat, args), arguments);
                break;
            case WARNING:
                logHelper(() -> logger.warning(logFormat), (args) -> logger.warning(logFormat, args), arguments);
                break;
            case ERROR:
                logHelper(() -> logger.error(logFormat), (args) -> logger.error(logFormat, args), arguments);
                break;
            default:
                break;
        }
    }

    private static void logHelper(Runnable simpleLog, Consumer<Object[]> formatLog, Object... args) {
        if (CoreUtils.isNullOrEmpty(args)) {
            simpleLog.run();
        } else {
            formatLog.accept(args);
        }
    }

    private static IllegalStateException createIllegalStateException(String message) {
        return fillInStackTrace(new IllegalStateException(message));
    }

    private static IOException createIOException(String message) {
        return fillInStackTrace(new IOException(message));
    }

    private static <T extends Throwable> T fillInStackTrace(T throwable) {
        StackTraceElement[] stackTraceElements = {new StackTraceElement("ClientLoggerTests", "onlyLogExceptionMessage",
            "ClientLoggerTests", 117)};
        throwable.setStackTrace(stackTraceElements);

        return throwable;
    }

    private static String byteArraySteamToString(ByteArrayOutputStream stream) throws UnsupportedEncodingException {
        return stream.toString(StandardCharsets.UTF_8.name());
    }

    private static Stream<Arguments> singleLevelCheckSupplier() {
        return Stream.of(
            // Supported logging level set to VERBOSE.
            // Checking VERBOSE.
            Arguments.of(LogLevel.VERBOSE, LogLevel.VERBOSE, true),

            // Checking INFORMATIONAL.
            Arguments.of(LogLevel.VERBOSE, LogLevel.INFORMATIONAL, true),

            // Checking WARNING.
            Arguments.of(LogLevel.VERBOSE, LogLevel.WARNING, true),

            // Checking ERROR.
            Arguments.of(LogLevel.VERBOSE, LogLevel.ERROR, true),

            // Checking NOT_SET.
            Arguments.of(LogLevel.VERBOSE, LogLevel.NOT_SET, false),

            // Checking null.
            Arguments.of(LogLevel.VERBOSE, null, false),

            // Supported logging level set to INFORMATIONAL.
            // Checking VERBOSE.
            Arguments.of(LogLevel.INFORMATIONAL, LogLevel.VERBOSE, false),

            // Checking INFORMATIONAL.
            Arguments.of(LogLevel.INFORMATIONAL, LogLevel.INFORMATIONAL, true),

            // Checking WARNING.
            Arguments.of(LogLevel.INFORMATIONAL, LogLevel.WARNING, true),

            // Checking ERROR.
            Arguments.of(LogLevel.INFORMATIONAL, LogLevel.ERROR, true),

            // Checking NOT_SET.
            Arguments.of(LogLevel.INFORMATIONAL, LogLevel.NOT_SET, false),

            // Checking null.
            Arguments.of(LogLevel.INFORMATIONAL, null, false),

            // Supported logging level set to WARNING.
            // Checking VERBOSE.
            Arguments.of(LogLevel.WARNING, LogLevel.VERBOSE, false),

            // Checking INFORMATIONAL.
            Arguments.of(LogLevel.WARNING, LogLevel.INFORMATIONAL, false),

            // Checking WARNING.
            Arguments.of(LogLevel.WARNING, LogLevel.WARNING, true),

            // Checking ERROR.
            Arguments.of(LogLevel.WARNING, LogLevel.ERROR, true),

            // Checking NOT_SET.
            Arguments.of(LogLevel.WARNING, LogLevel.NOT_SET, false),

            // Checking null.
            Arguments.of(LogLevel.WARNING, null, false),

            // Supported logging level set to ERROR.
            // Checking VERBOSE.
            Arguments.of(LogLevel.ERROR, LogLevel.VERBOSE, false),

            // Checking INFORMATIONAL.
            Arguments.of(LogLevel.ERROR, LogLevel.INFORMATIONAL, false),

            // Checking WARNING.
            Arguments.of(LogLevel.ERROR, LogLevel.WARNING, false),

            // Checking ERROR.
            Arguments.of(LogLevel.ERROR, LogLevel.ERROR, true),

            // Checking NOT_SET.
            Arguments.of(LogLevel.VERBOSE, LogLevel.NOT_SET, false),

            // Checking null.
            Arguments.of(LogLevel.VERBOSE, null, false)
        );
    }

    private static Stream<Arguments> multiLevelCheckSupplier() {
        return Stream.of(
            // Supported logging level set to VERBOSE.
            // Checking VERBOSE.
            Arguments.of(LogLevel.VERBOSE, LogLevel.VERBOSE, false, true),

            // Checking INFORMATIONAL.
            Arguments.of(LogLevel.VERBOSE, LogLevel.INFORMATIONAL, false, true),

            // Checking WARNING.
            Arguments.of(LogLevel.VERBOSE, LogLevel.WARNING, true, true),

            // Checking ERROR.
            Arguments.of(LogLevel.VERBOSE, LogLevel.ERROR, true, true),

            // Checking NOT_SET.
            Arguments.of(LogLevel.VERBOSE, LogLevel.NOT_SET, false, false),

            // Checking null.
            Arguments.of(LogLevel.VERBOSE, null, false, false),

            // Supported logging level set to INFORMATIONAL.
            // Checking VERBOSE.
            Arguments.of(LogLevel.INFORMATIONAL, LogLevel.VERBOSE, false, false),

            // Checking INFORMATIONAL.
            Arguments.of(LogLevel.INFORMATIONAL, LogLevel.INFORMATIONAL, false, false),

            // Checking WARNING.
            Arguments.of(LogLevel.INFORMATIONAL, LogLevel.WARNING, true, false),

            // Checking ERROR.
            Arguments.of(LogLevel.INFORMATIONAL, LogLevel.ERROR, true, false),

            // Checking NOT_SET.
            Arguments.of(LogLevel.INFORMATIONAL, LogLevel.NOT_SET, false, false),

            // Checking null.
            Arguments.of(LogLevel.INFORMATIONAL, null, false, false),

            // Supported logging level set to WARNING.
            // Checking VERBOSE.
            Arguments.of(LogLevel.WARNING, LogLevel.VERBOSE, false, false),

            // Checking INFORMATIONAL.
            Arguments.of(LogLevel.WARNING, LogLevel.INFORMATIONAL, false, false),

            // Checking WARNING.
            Arguments.of(LogLevel.WARNING, LogLevel.WARNING, true, false),

            // Checking ERROR.
            Arguments.of(LogLevel.WARNING, LogLevel.ERROR, true, false),

            // Checking NOT_SET.
            Arguments.of(LogLevel.WARNING, LogLevel.NOT_SET, false, false),

            // Checking null.
            Arguments.of(LogLevel.WARNING, null, false, false),

            // Supported logging level set to ERROR.
            // Checking VERBOSE.
            Arguments.of(LogLevel.ERROR, LogLevel.VERBOSE, false, false),

            // Checking INFORMATIONAL.
            Arguments.of(LogLevel.ERROR, LogLevel.INFORMATIONAL, false, false),

            // Checking WARNING.
            Arguments.of(LogLevel.ERROR, LogLevel.WARNING, false, false),

            // Checking ERROR.
            Arguments.of(LogLevel.ERROR, LogLevel.ERROR, true, false),

            // Checking NOT_SET.
            Arguments.of(LogLevel.VERBOSE, LogLevel.NOT_SET, false, false),

            // Checking null.
            Arguments.of(LogLevel.VERBOSE, null, false, false)
        );
    }

    private static Stream<Arguments> logMaliciousErrorSupplier() {
        return Stream.of(
            // Supported logging level set to VERBOSE.
            // Checking VERBOSE.
            Arguments.of(LogLevel.VERBOSE, LogLevel.VERBOSE, true),

            // Checking INFORMATIONAL.
            Arguments.of(LogLevel.VERBOSE, LogLevel.INFORMATIONAL, true),

            // Checking WARNING.
            Arguments.of(LogLevel.VERBOSE, LogLevel.WARNING, true),

            // Checking ERROR.
            Arguments.of(LogLevel.VERBOSE, LogLevel.ERROR, true),

            // Checking INFORMATIONAL.
            Arguments.of(LogLevel.INFORMATIONAL, LogLevel.INFORMATIONAL, true),

            // Checking WARNING.
            Arguments.of(LogLevel.INFORMATIONAL, LogLevel.WARNING, true),

            // Checking ERROR.
            Arguments.of(LogLevel.INFORMATIONAL, LogLevel.ERROR, true),

            // Checking WARNING.
            Arguments.of(LogLevel.WARNING, LogLevel.WARNING, true),

            // Checking ERROR.
            Arguments.of(LogLevel.WARNING, LogLevel.ERROR, true),

            // Checking ERROR.
            Arguments.of(LogLevel.ERROR, LogLevel.ERROR, true)
        );
    }

    private static Stream<Arguments> logExceptionAsWarningSupplier() {
        return Stream.of(
            Arguments.of(LogLevel.VERBOSE, true, true),
            Arguments.of(LogLevel.INFORMATIONAL, true, false),
            Arguments.of(LogLevel.WARNING, true, false),
            Arguments.of(LogLevel.ERROR, false, false),
            Arguments.of(LogLevel.NOT_SET, false, false)
        );
    }

    private static Stream<Arguments> logExceptionAsErrorSupplier() {
        return Stream.of(
            Arguments.of(LogLevel.VERBOSE, true, true),
            Arguments.of(LogLevel.INFORMATIONAL, true, false),
            Arguments.of(LogLevel.WARNING, true, false),
            Arguments.of(LogLevel.ERROR, true, false),
            Arguments.of(LogLevel.NOT_SET, false, false)
        );
    }

    private static Stream<Arguments> validLogLevelSupplier() {
        return Stream.of(
            // Valid VERBOSE environment variables.
            Arguments.of("1", LogLevel.VERBOSE),
            Arguments.of("verbose", LogLevel.VERBOSE),
            Arguments.of("debug", LogLevel.VERBOSE),
            Arguments.of("deBUG", LogLevel.VERBOSE),

            // Valid INFORMATIONAL environment variables.
            Arguments.of("2", LogLevel.INFORMATIONAL),
            Arguments.of("info", LogLevel.INFORMATIONAL),
            Arguments.of("information", LogLevel.INFORMATIONAL),
            Arguments.of("informational", LogLevel.INFORMATIONAL),
            Arguments.of("InForMATiONaL", LogLevel.INFORMATIONAL),

            // Valid WARNING environment variables.
            Arguments.of("3", LogLevel.WARNING),
            Arguments.of("warn", LogLevel.WARNING),
            Arguments.of("warning", LogLevel.WARNING),
            Arguments.of("WARniNg", LogLevel.WARNING),

            // Valid ERROR environment variables.
            Arguments.of("4", LogLevel.ERROR),
            Arguments.of("err", LogLevel.ERROR),
            Arguments.of("error", LogLevel.ERROR),
            Arguments.of("ErRoR", LogLevel.ERROR),

            // Valid NOT_SET environment variables.
            Arguments.of("5", LogLevel.NOT_SET),
            Arguments.of(null, LogLevel.NOT_SET)
        );
    }
}
