// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.logging;

import com.azure.core.implementation.util.EnvironmentConfiguration;
import com.azure.core.util.CoreUtils;
import com.fasterxml.jackson.core.io.JsonStringEncoder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.api.parallel.Isolated;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.junit.jupiter.api.parallel.Resources;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static com.azure.core.util.Configuration.PROPERTY_AZURE_LOG_LEVEL;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link ClientLogger}.
 */
@Execution(ExecutionMode.SAME_THREAD)
@Isolated
@ResourceLock(Resources.SYSTEM_OUT)
public class ClientLoggerTests {
    private PrintStream originalSystemOut;
    private ByteArrayOutputStream logCaptureStream;
    private Map<String, Object> globalContext;

    @BeforeEach
    public void setupLoggingConfiguration() {
        /*
         * DefaultLogger uses System.out to log. Inject a custom PrintStream to log into for the duration of the test to
         * capture the log messages.
         */
        originalSystemOut = System.out;
        logCaptureStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(logCaptureStream));

        // preserve order
        globalContext = new LinkedHashMap<>();
        globalContext.put("connectionId", "foo");
        globalContext.put("linkName", 1);
        globalContext.put("anotherKey", new LoggableObject("hello world"));
    }

    @AfterEach
    public void revertLoggingConfiguration() {
        clearTestLogLevel();
        System.setOut(originalSystemOut);
    }

    /**
     * Test whether the logger supports a given log level based on its configured log level.
     */
    @ParameterizedTest
    @MethodSource("singleLevelCheckSupplier")
    public void canLogAtLevel(LogLevel logLevelToConfigure, LogLevel logLevelToValidate, boolean expected) {
        setupLogLevel(logLevelToConfigure.getLogLevel());
        assertEquals(expected, new ClientLogger(ClientLoggerTests.class).canLogAtLevel(logLevelToValidate));
    }

    /**
     * Test whether a log will be captured when the ClientLogger and message are configured to the passed log levels.
     */
    @ParameterizedTest
    @MethodSource("singleLevelCheckSupplier")
    public void logSimpleMessage(LogLevel logLevelToConfigure, LogLevel logLevelToUse, boolean logContainsMessage) {
        String logMessage = "This is a test";

        setupLogLevel(logLevelToConfigure.getLogLevel());
        logMessage(new ClientLogger(ClientLoggerTests.class), logLevelToUse, logMessage);

        String logValues = byteArraySteamToString(logCaptureStream);
        assertEquals(logContainsMessage, logValues.contains(logMessage));
    }

    /**
     * Test whether a log will be captured when the ClientLogger and message are configured to the passed log levels.
     */
    @ParameterizedTest
    @MethodSource("logMaliciousErrorSupplier")
    public void logMaliciousMessage(LogLevel logLevelToConfigure, LogLevel logLevelToUse) {
        String logMessage = "You have successfully authenticated, \r\n[INFO] User dummy was not"
            + " successfully authenticated.";

        String expectedMessage = "You have successfully authenticated, [INFO] User dummy was not"
            + " successfully authenticated.";

        setupLogLevel(logLevelToConfigure.getLogLevel());
        logMessage(new ClientLogger(ClientLoggerTests.class), logLevelToUse, logMessage);

        String logValues = byteArraySteamToString(logCaptureStream);
        System.out.println(logValues);
        assertTrue(logValues.contains(expectedMessage));
    }

    @ParameterizedTest
    @MethodSource("singleLevelCheckSupplier")
    public void logFormattedMessage(LogLevel logLevelToConfigure, LogLevel logLevelToUse, boolean logContainsMessage) {
        String logMessage = "This is a test";
        String logFormat = "{} is a {}";

        setupLogLevel(logLevelToConfigure.getLogLevel());
        logMessage(new ClientLogger(ClientLoggerTests.class), logLevelToUse, logFormat, "This", "test");

        String logValues = byteArraySteamToString(logCaptureStream);
        assertEquals(logContainsMessage, logValues.contains(logMessage));
    }

    /**
     * Tests whether a log will contain the exception message when the ClientLogger and message are configured to the
     * passed log levels.
     */
    @ParameterizedTest
    @MethodSource("multiLevelCheckSupplier")
    public void logException(LogLevel logLevelToConfigure, LogLevel logLevelToUse, boolean logContainsMessage,
        boolean logContainsStackTrace) {
        String logMessage = "This is an exception";
        String exceptionMessage = "An exception message";
        RuntimeException runtimeException = createIllegalStateException(exceptionMessage);

        setupLogLevel(logLevelToConfigure.getLogLevel());
        logMessage(new ClientLogger(ClientLoggerTests.class), logLevelToUse, logMessage, runtimeException);

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
    public void logExceptionAsWarning(LogLevel logLevelToConfigure, boolean logContainsMessage,
        boolean logContainsStackTrace) {
        String exceptionMessage = "An exception message";
        IllegalStateException illegalStateException = createIllegalStateException(exceptionMessage);

        setupLogLevel(logLevelToConfigure.getLogLevel());
        try {
            throw new ClientLogger(ClientLoggerTests.class).logExceptionAsWarning(illegalStateException);
        } catch (RuntimeException exception) {
            assertTrue(exception instanceof IllegalStateException, () -> "Expected IllegalStateException but got "
                + exception.getClass().getSimpleName() + ".");
        }

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
    public void logCheckedExceptionAsWarning(LogLevel logLevelToConfigure, boolean logContainsMessage,
        boolean logContainsStackTrace) {
        String exceptionMessage = "An exception message";
        IOException ioException = createIOException(exceptionMessage);

        setupLogLevel(logLevelToConfigure.getLogLevel());
        try {
            throw new ClientLogger(ClientLoggerTests.class).logThrowableAsWarning(ioException);
        } catch (Throwable throwable) {
            assertTrue(throwable instanceof IOException, () -> "Expected IOException but got "
                + throwable.getClass().getSimpleName() + ".");
        }

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
    public void logExceptionAsError(LogLevel logLevelToConfigure, boolean logContainsMessage,
        boolean logContainsStackTrace) throws UnsupportedEncodingException {
        String exceptionMessage = "An exception message";
        IllegalStateException illegalStateException = createIllegalStateException(exceptionMessage);

        setupLogLevel(logLevelToConfigure.getLogLevel());
        try {
            throw new ClientLogger(ClientLoggerTests.class).logExceptionAsError(illegalStateException);
        } catch (RuntimeException exception) {
            assertTrue(exception instanceof IllegalStateException, () -> "Expected IllegalStateException but got "
                + exception.getClass().getSimpleName() + ".");
        }

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
    public void logCheckedExceptionAsError(LogLevel logLevelToConfigure, boolean logContainsMessage,
        boolean logContainsStackTrace) {
        String exceptionMessage = "An exception message";
        IOException ioException = createIOException(exceptionMessage);

        setupLogLevel(logLevelToConfigure.getLogLevel());
        try {
            throw new ClientLogger(ClientLoggerTests.class).logThrowableAsError(ioException);
        } catch (Throwable throwable) {
            assertTrue(throwable instanceof IOException, () -> "Expected IOException but got "
                + throwable.getClass().getSimpleName() + ".");
        }

        String logValues = byteArraySteamToString(logCaptureStream);
        assertEquals(logContainsMessage, logValues.contains(exceptionMessage + System.lineSeparator()));
        assertEquals(logContainsStackTrace, logValues.contains(ioException.getStackTrace()[0].toString()));
    }

    /**
     * Tests that LogLevel.fromString returns the expected LogLevel enum based on the passed environment configuration.
     */
    @ParameterizedTest
    @MethodSource("validLogLevelSupplier")
    public void logLevelFromString(String environmentLogLevel, LogLevel expected) {
        assertEquals(expected, LogLevel.fromString(environmentLogLevel));
    }

    /**
     * Tests that LogLevel.fromString will throw an illegal argument exception when passed an environment configuration
     * it doesn't support.
     */
    @ParameterizedTest
    @ValueSource(strings = {"errs", "not_set", "12", "onlyErrorsPlease"})
    public void invalidLogLevelFromString(String environmentLogLevel) {
        assertThrows(IllegalArgumentException.class, () -> LogLevel.fromString(environmentLogLevel));
    }

    @ParameterizedTest
    @MethodSource("provideLogLevels")
    public void logWithSupplier(LogLevel logLevel) {
        setupLogLevel(logLevel.getLogLevel());
        Supplier<String> supplier = () -> String.format("Param 1: %s, Param 2: %s, Param 3: %s", "test1", "test2", "test3");
        ClientLogger logger = new ClientLogger(ClientLoggerTests.class);
        logHelper(() -> logger.log(logLevel, supplier), (args) -> logger.log(logLevel, supplier), supplier);

        String logValues = byteArraySteamToString(logCaptureStream);
        assertTrue(logValues.contains(supplier.get() + System.lineSeparator()));
    }

    @Test
    public void logWithNewLine() {
        setupLogLevel(LogLevel.INFORMATIONAL.getLogLevel());
        String message = String.format("Param 1: %s%s, Param 2: %s%s, Param 3: %s", "test1", System.lineSeparator(), "test2", System.lineSeparator(), "test3");
        ClientLogger logger = new ClientLogger(ClientLoggerTests.class);
        logger.log(LogLevel.INFORMATIONAL, () -> message);

        String logValues = byteArraySteamToString(logCaptureStream);
        assertTrue(logValues.contains("Param 1: test1, Param 2: test2, Param 3: test3"));
    }

    @ParameterizedTest
    @MethodSource("provideLogLevels")
    public void logWithNullSupplier(LogLevel logLevel) {
        setupLogLevel(logLevel.getLogLevel());
        ClientLogger logger = new ClientLogger(ClientLoggerTests.class);
        logHelper(() -> logger.log(logLevel, null), (args) -> logger.log(logLevel, null), new Object[]{null});

        String logValues = byteArraySteamToString(logCaptureStream);
        assertTrue(logValues.isEmpty());
    }

    @ParameterizedTest
    @MethodSource("provideLogLevels")
    public void logSupplierWithException(LogLevel logLevel) {
        NullPointerException exception = new NullPointerException();
        setupLogLevel(logLevel.getLogLevel());
        Supplier<String> supplier = () -> String.format("Param 1: %s, Param 2: %s, Param 3: %s", "test1", "test2", "test3");
        ClientLogger logger = new ClientLogger(ClientLoggerTests.class);
        logHelper(() -> logger.log(logLevel, supplier, exception), (args) -> logger.log(logLevel, supplier, exception), supplier);
        String logValues = byteArraySteamToString(logCaptureStream);

        assertTrue(logValues.contains(supplier.get() + System.lineSeparator()));
    }

    @ParameterizedTest
    @MethodSource("provideLogLevels")
    public void logShouldEvaluateSupplierWithNullException(LogLevel logLevel) {
        setupLogLevel(logLevel.getLogLevel());
        Supplier<String> supplier = () -> String.format("Param 1: %s, Param 2: %s, Param 3: %s", "test1", "test2", "test3");
        ClientLogger logger = new ClientLogger(ClientLoggerTests.class);
        logHelper(() -> logger.log(logLevel, supplier, null), (args) -> logger.log(logLevel, supplier, null), supplier);
        String logValues = byteArraySteamToString(logCaptureStream);

        assertTrue(logValues.contains(supplier.get() + System.lineSeparator()));
    }


    @Test
    public void testIsSupplierLogging() {
        Supplier<String> supplier = () -> String.format("Param 1: %s, Param 2: %s, Param 3: %s", "test1", "test2", "test3");
        ClientLogger logger = new ClientLogger(ClientLoggerTests.class);
        NullPointerException exception = new NullPointerException();
        Object[] args = {supplier, exception};

        assertTrue(logger.isSupplierLogging(args));
    }

    /**
     * Tests that logging with context of string message writes
     * log message and context in correct format and depending on the level.
     */
    @ParameterizedTest
    @MethodSource("provideLogLevels")
    public void logWithContext(LogLevel logLevelToConfigure) {
        setupLogLevel(logLevelToConfigure.getLogLevel());
        ClientLogger logger = new ClientLogger(ClientLoggerTests.class);

        String message = String.format("Param 1: %s, Param 2: %s, Param 3: %s", "test1", "test2", "test3");

        logger.atWarning()
            .addKeyValue("connectionId", "foo")
            .addKeyValue("linkName", 1)
            .log(message);

        assertMessage(
            "{\"az.sdk.message\":\"Param 1: test1, Param 2: test2, Param 3: test3\",\"connectionId\":\"foo\",\"linkName\":1}",
            byteArraySteamToString(logCaptureStream),
            logLevelToConfigure,
            LogLevel.WARNING);
    }

    /**
     * Tests that logging with context of string message writes
     * log message and global context in correct format and depending on the level.
     */
    @ParameterizedTest
    @MethodSource("provideLogLevels")
    public void logWithGlobalContext(LogLevel logLevelToConfigure) {
        setupLogLevel(logLevelToConfigure.getLogLevel());

        ClientLogger logger = new ClientLogger(ClientLoggerTests.class, globalContext);
        logger.warning("Param 1: {}, Param 2: {}, Param 3: {}", "test1", "test2", "test3");

        assertMessage(
            "{\"az.sdk.message\":\"Param 1: test1, Param 2: test2, Param 3: test3\",\"connectionId\":\"foo\",\"linkName\":1,\"anotherKey\":\"hello world\"}",
            byteArraySteamToString(logCaptureStream),
            logLevelToConfigure,
            LogLevel.WARNING);
    }

    /**
     * Tests that logging with context of string message writes
     * log message and global context in correct format at info level
     */
    @Test
    public void logInfoWithGlobalContext() {
        setupLogLevel(LogLevel.VERBOSE.getLogLevel());

        ClientLogger logger = new ClientLogger(ClientLoggerTests.class, globalContext);
        logger.info("message");

        assertMessage(
            "{\"az.sdk.message\":\"message\",\"connectionId\":\"foo\",\"linkName\":1,\"anotherKey\":\"hello world\"}",
            byteArraySteamToString(logCaptureStream),
            LogLevel.VERBOSE,
            LogLevel.VERBOSE);
    }

    /**
     * Tests that logging with context of string message writes
     * log message and global context in correct format at verbose level
     */
    @Test
    public void logVerboseWithGlobalContext() {
        setupLogLevel(LogLevel.VERBOSE.getLogLevel());

        ClientLogger logger = new ClientLogger(ClientLoggerTests.class, globalContext);
        logger.verbose("message");

        assertMessage(
            "{\"az.sdk.message\":\"message\",\"connectionId\":\"foo\",\"linkName\":1,\"anotherKey\":\"hello world\"}",
            byteArraySteamToString(logCaptureStream),
            LogLevel.VERBOSE,
            LogLevel.INFORMATIONAL);
    }

    /**
     * Tests that logging with context of string message writes
     * log message and global context in correct format at warning level
     */
    @Test
    public void logWarningWithGlobalContext() {
        setupLogLevel(LogLevel.VERBOSE.getLogLevel());

        new ClientLogger(ClientLoggerTests.class, globalContext)
            .warning("message");

        assertMessage(
            "{\"az.sdk.message\":\"message\",\"connectionId\":\"foo\",\"linkName\":1,\"anotherKey\":\"hello world\"}",
            byteArraySteamToString(logCaptureStream),
            LogLevel.VERBOSE,
            LogLevel.WARNING);
    }

    /**
     * Tests that logging with context of string message writes
     * log message and global context in correct format at error level
     */
    @Test
    public void logErrorWithGlobalContext() {
        setupLogLevel(LogLevel.VERBOSE.getLogLevel());

        new ClientLogger(ClientLoggerTests.class, globalContext)
            .error("message");

        assertMessage(
            "{\"az.sdk.message\":\"message\",\"connectionId\":\"foo\",\"linkName\":1,\"anotherKey\":\"hello world\"}",
            byteArraySteamToString(logCaptureStream),
            LogLevel.VERBOSE,
            LogLevel.ERROR);
    }

    /**
     * Tests empty global context
     */
    @Test
    public void logWithEmptyGlobalContext() {
        setupLogLevel(LogLevel.INFORMATIONAL.getLogLevel());

        ClientLogger logger = new ClientLogger(ClientLoggerTests.class, Collections.emptyMap());
        logger.warning("Param 1: {}, Param 2: {}, Param 3: {}", "test1", "test2", "test3");

        assertMessage(
            "Param 1: test1, Param 2: test2, Param 3: test3",
            byteArraySteamToString(logCaptureStream),
            LogLevel.INFORMATIONAL,
            LogLevel.WARNING);
    }

    /**
     * Tests null global context
     */
    @Test
    public void logWithNullGlobalContext() {
        setupLogLevel(LogLevel.INFORMATIONAL.getLogLevel());

        ClientLogger logger = new ClientLogger(ClientLoggerTests.class, null);
        logger.info("Param 1: {}, Param 2: {}, Param 3: {}", "test1", "test2", "test3");

        assertMessage(
            "Param 1: test1, Param 2: test2, Param 3: test3",
            byteArraySteamToString(logCaptureStream),
            LogLevel.INFORMATIONAL,
            LogLevel.INFORMATIONAL);
    }

    /**
     * Tests that logging with context of string message writes
     * log message and local and globacl context in correct format and depending on the level.
     */
    @ParameterizedTest
    @MethodSource("provideLogLevels")
    public void logWithGlobalAndLocalContext(LogLevel logLevelToConfigure) {
        setupLogLevel(logLevelToConfigure.getLogLevel());

        ClientLogger logger = new ClientLogger(ClientLoggerTests.class, globalContext);
        logger.atInfo()
            .addKeyValue("local", true)
            .addKeyValue("connectionId", "conflict")
            .log("Param 1: {}, Param 2: {}, Param 3: {}", "test1", "test2", "test3");

        assertMessage(
            "{\"az.sdk.message\":\"Param 1: test1, Param 2: test2, Param 3: test3\",\"connectionId\":\"foo\",\"linkName\":1,\"anotherKey\":\"hello world\",\"local\":true,\"connectionId\":\"conflict\"}",
            byteArraySteamToString(logCaptureStream),
            logLevelToConfigure,
            LogLevel.INFORMATIONAL);
    }

    /**
     * Tests that contextual logging without context of string message writes
     * log message and context in correct format and depending on the level.
     */
    @ParameterizedTest
    @MethodSource("provideLogLevels")
    public void contextualLogWithoutContext(LogLevel logLevelToConfigure) {
        setupLogLevel(logLevelToConfigure.getLogLevel());
        ClientLogger logger = new ClientLogger(ClientLoggerTests.class);

        logger.atWarning().log(String.format("Param 1: %s, Param 2: %s, Param 3: %s", "test1", "test2", "test3"));

        assertMessage(
            "{\"az.sdk.message\":\"Param 1: test1, Param 2: test2, Param 3: test3\"}",
            byteArraySteamToString(logCaptureStream),
            logLevelToConfigure,
            LogLevel.WARNING);
    }


    /**
     * Tests message supplier with local and global context.
     */
    @ParameterizedTest
    @MethodSource("provideLogLevels")
    public void logWithGlobalContextMessageSupplier(LogLevel logLevelToConfigure) {
        setupLogLevel(logLevelToConfigure.getLogLevel());
        ClientLogger logger = new ClientLogger(ClientLoggerTests.class, globalContext);

        logger.log(LogLevel.INFORMATIONAL, () -> String.format("Param 1: %s, Param 2: %s, Param 3: %s", "test1", "test2", "test3"));

        assertMessage(
            "{\"az.sdk.message\":\"Param 1: test1, Param 2: test2, Param 3: test3\",\"connectionId\":\"foo\",\"linkName\":1,\"anotherKey\":\"hello world\"}",
            byteArraySteamToString(logCaptureStream),
            logLevelToConfigure,
            LogLevel.INFORMATIONAL);
    }

    /**
     * Tests message supplier with context.
     */
    @ParameterizedTest
    @MethodSource("provideLogLevels")
    public void logWithContextMessageSupplier(LogLevel logLevelToConfigure) {
        setupLogLevel(logLevelToConfigure.getLogLevel());
        ClientLogger logger = new ClientLogger(ClientLoggerTests.class);

        String message = String.format("Param 1: %s, Param 2: %s, Param 3: %s", "test1", "test2", "test3");

        logger.atInfo()
            .addKeyValue("connectionId", "foo")
            .addKeyValue("linkName", "bar")
            .log(() -> message);

        assertMessage(
            "{\"az.sdk.message\":\"Param 1: test1, Param 2: test2, Param 3: test3\",\"connectionId\":\"foo\",\"linkName\":\"bar\"}",
            byteArraySteamToString(logCaptureStream),
            logLevelToConfigure,
            LogLevel.INFORMATIONAL);
    }

    /**
     * Tests that logging with context with null message does not throw.
     */
    @Test
    public void logWithContextNullMessage() {
        setupLogLevel(LogLevel.VERBOSE.getLogLevel());
        ClientLogger logger = new ClientLogger(ClientLoggerTests.class);

        logger.atVerbose()
            .addKeyValue("connectionId", "foo")
            .addKeyValue("linkName", true)
            .log((String) null);

        assertMessage(
            "{\"az.sdk.message\":\"\",\"connectionId\":\"foo\",\"linkName\":true}",
            byteArraySteamToString(logCaptureStream),
            LogLevel.VERBOSE,
            LogLevel.INFORMATIONAL);
    }

    /**
     * Tests that newline is escaped in message, keys and values.
     */
    @Test
    public void logWithContextNewLineIsEscaped() {
        setupLogLevel(LogLevel.VERBOSE.getLogLevel());
        ClientLogger logger = new ClientLogger(ClientLoggerTests.class);

        logger.atVerbose()
            .addKeyValue("connection\nId" + System.lineSeparator(), "foo")
            .addKeyValue("link\r\nName", "test" + System.lineSeparator() + "me")
            .log("multiline " + System.lineSeparator() + "message");

        String escapedNewLine = new String(JsonStringEncoder.getInstance().quoteAsString(System.lineSeparator()));

        assertMessage(
            "{\"az.sdk.message\":\"multiline " + escapedNewLine + "message\",\"connection\\nId" + escapedNewLine + "\":\"foo\",\"link\\r\\nName\":\"test" + escapedNewLine + "me\"}",
            byteArraySteamToString(logCaptureStream),
            LogLevel.VERBOSE,
            LogLevel.INFORMATIONAL);
    }

    /**
     * Tests that global context is escaped
     */
    @Test
    public void logWithGlobalContextIsEscaped() {
        setupLogLevel(LogLevel.VERBOSE.getLogLevel());

        // preserve order
        Map<String, Object> globalCtx = new LinkedHashMap<>();
        globalCtx.put("link\tName", 1);
        globalCtx.put("another\rKey\n", new LoggableObject("hello \"world\"\r\n"));

        ClientLogger logger = new ClientLogger(ClientLoggerTests.class, globalCtx);

        logger.verbose("\"message\"");

        assertMessage(
            "{\"az.sdk.message\":\"\\\"message\\\"\",\"link\\tName\":1,\"another\\rKey\\n\":\"hello \\\"world\\\"\\r\\n\"}",
            byteArraySteamToString(logCaptureStream),
            LogLevel.VERBOSE,
            LogLevel.INFORMATIONAL);
    }

    /**
     * Tests that logging with context with null message supplier does not throw.
     */
    @Test
    public void logWithContextNullSupplier() {
        setupLogLevel(LogLevel.INFORMATIONAL.getLogLevel());
        ClientLogger logger = new ClientLogger(ClientLoggerTests.class);

        Supplier<String> message = null;

        logger.atError()
            .addKeyValue("connectionId", "foo")
            .addKeyValue("linkName", (String) null)
            .log(message);

        assertMessage(
            "{\"az.sdk.message\":\"\",\"connectionId\":\"foo\",\"linkName\":null}",
            byteArraySteamToString(logCaptureStream),
            LogLevel.INFORMATIONAL,
            LogLevel.ERROR);
    }

    /**
     * Tests supplied context value.
     */
    @Test
    public void logWithContextValueSupplier() {
        setupLogLevel(LogLevel.INFORMATIONAL.getLogLevel());
        ClientLogger logger = new ClientLogger(ClientLoggerTests.class);

        logger.atWarning()
            // this is technically invalid, but we should not throw because of logging in runtime
            .addKeyValue("connectionId", (Supplier<String>) null)
            .addKeyValue("linkName", () -> String.format("complex value %s", 123))
            .log("test");

        assertMessage(
            "{\"az.sdk.message\":\"test\",\"connectionId\":null,\"linkName\":\"complex value 123\"}",
            byteArraySteamToString(logCaptureStream),
            LogLevel.INFORMATIONAL,
            LogLevel.WARNING);
    }

    /**
     * Tests supplied context value.
     */
    @Test
    public void logWithContextObject() {
        setupLogLevel(LogLevel.INFORMATIONAL.getLogLevel());
        ClientLogger logger = new ClientLogger(ClientLoggerTests.class);

        logger.atWarning()
            .addKeyValue("linkName", new LoggableObject("some complex object"))
            .log("test");

        assertMessage(
            "{\"az.sdk.message\":\"test\",\"linkName\":\"some complex object\"}",
            byteArraySteamToString(logCaptureStream),
            LogLevel.INFORMATIONAL,
            LogLevel.WARNING);
    }

    /**
     * Tests message with args and context.
     */
    @ParameterizedTest
    @MethodSource("provideLogLevels")
    public void logMessageAndArgsWithContext(LogLevel logLevelToConfigure) {
        setupLogLevel(logLevelToConfigure.getLogLevel());
        ClientLogger logger = new ClientLogger(ClientLoggerTests.class);

        logger.atWarning()
            .addKeyValue("connectionId", () -> null)
            .addKeyValue("linkName", "bar")
            .log("Param 1: {}, Param 2: {}, Param 3: {}", "test1", "test2", "test3");

        assertMessage(
            "{\"az.sdk.message\":\"Param 1: test1, Param 2: test2, Param 3: test3\",\"connectionId\":null,\"linkName\":\"bar\"}",
            byteArraySteamToString(logCaptureStream),
            logLevelToConfigure,
            LogLevel.WARNING);
    }

    /**
     * Tests logging with context when args have throwable (stack trace is only logged at debug)
     */
    @ParameterizedTest
    @MethodSource("provideLogLevels")
    public void logWithContextWithThrowableInArgs(LogLevel logLevelToConfigure) {
        setupLogLevel(logLevelToConfigure.getLogLevel());
        ClientLogger logger = new ClientLogger(ClientLoggerTests.class);

        String exceptionMessage = "An exception message";
        RuntimeException runtimeException = createIllegalStateException(exceptionMessage);

        logger.atWarning()
            .addKeyValue("connectionId", "foo")
            .addKeyValue("linkName", "bar")
            .log("hello {}", "world", runtimeException);

        String message = "{\"az.sdk.message\":\"hello world\",\"exception\":\"" + exceptionMessage + "\",\"connectionId\":\"foo\",\"linkName\":\"bar\"}";
        if (logLevelToConfigure.equals(LogLevel.VERBOSE)) {
            message += System.lineSeparator() + runtimeException.toString() + System.lineSeparator() + "\tat " + runtimeException.getStackTrace()[0].toString();
        }

        assertMessage(
            message,
            byteArraySteamToString(logCaptureStream),
            logLevelToConfigure,
            LogLevel.WARNING);
    }

    /**
     * Tests logging with context and supplied message when args have throwable (stack trace is only logged at debug)
     */
    @ParameterizedTest
    @MethodSource("provideLogLevels")
    public void logWithContextMessageSupplierAndThrowableInArgs(LogLevel logLevelToConfigure) {
        setupLogLevel(logLevelToConfigure.getLogLevel());
        ClientLogger logger = new ClientLogger(ClientLoggerTests.class);

        String exceptionMessage = "An exception message";
        IOException ioException = createIOException(exceptionMessage);

        logger.atWarning()
            .addKeyValue("connectionId", "foo")
            .addKeyValue("linkName", "bar")
            .log(() -> String.format("hello %s", "world"), ioException);

        String message = "{\"az.sdk.message\":\"hello world\",\"exception\":\"" + exceptionMessage + "\",\"connectionId\":\"foo\",\"linkName\":\"bar\"}";
        if (logLevelToConfigure.equals(LogLevel.VERBOSE)) {
            message += System.lineSeparator() + ioException.toString() + System.lineSeparator() + "\tat " + ioException.getStackTrace()[0].toString();
        }

        assertMessage(
            message,
            byteArraySteamToString(logCaptureStream),
            logLevelToConfigure,
            LogLevel.WARNING);
    }

    /**
     * Tests json escape in keys, values, message and exception message
     */
    @ParameterizedTest
    @MethodSource("provideLogLevels")
    public void logWithContextWithThrowableInArgsAndEscaping(LogLevel logLevelToConfigure) {
        setupLogLevel(logLevelToConfigure.getLogLevel());
        ClientLogger logger = new ClientLogger(ClientLoggerTests.class);

        String exceptionMessage = "An exception \tmessage with \"special characters\"\r\n";
        RuntimeException runtimeException = createIllegalStateException(exceptionMessage);

        logger.atWarning()
            .addKeyValue("connection\tId", "foo")
            .addKeyValue("linkName", "\rbar")
            .log("hello {}, \"and\" {more}", "world", runtimeException);


        String escapedExceptionMessage = "An exception \\tmessage with \\\"special characters\\\"\\r\\n";

        String expectedMessage = "{\"az.sdk.message\":\"hello world, \\\"and\\\" {more}\",\"exception\":\"" + escapedExceptionMessage + "\",\"connection\\tId\":\"foo\",\"linkName\":\"\\rbar\"}";
        if (logLevelToConfigure.equals(LogLevel.VERBOSE)) {
            expectedMessage += System.lineSeparator() + runtimeException.toString() + System.lineSeparator() + "\tat " + runtimeException.getStackTrace()[0].toString();
        }

        assertMessage(
            expectedMessage,
            byteArraySteamToString(logCaptureStream),
            logLevelToConfigure,
            LogLevel.WARNING);
    }

    /**
     * Tests logging with context when cause is set
     */
    @ParameterizedTest
    @MethodSource("provideLogLevels")
    public void logWithContextRuntimeException(LogLevel logLevelToConfigure) {
        setupLogLevel(logLevelToConfigure.getLogLevel());
        ClientLogger logger = new ClientLogger(ClientLoggerTests.class);

        String exceptionMessage = "An exception message";
        RuntimeException runtimeException = createIllegalStateException(exceptionMessage);

        assertSame(runtimeException, logger.atWarning()
            .addKeyValue("connectionId", "foo")
            .addKeyValue("linkName", "bar")
            .log(runtimeException));

        String message = "{\"az.sdk.message\":\"\",\"exception\":\"" + exceptionMessage + "\",\"connectionId\":\"foo\",\"linkName\":\"bar\"}";
        if (logLevelToConfigure.equals(LogLevel.VERBOSE)) {
            message += System.lineSeparator() + runtimeException.toString() + System.lineSeparator() + "\tat " + runtimeException.getStackTrace()[0].toString();
        }

        assertMessage(
            message,
            byteArraySteamToString(logCaptureStream),
            logLevelToConfigure,
            LogLevel.WARNING);
    }

    /**
     * Tests logging with context when cause is set
     */
    @ParameterizedTest
    @MethodSource("provideLogLevels")
    public void logWithContextThrowable(LogLevel logLevelToConfigure) {
        setupLogLevel(logLevelToConfigure.getLogLevel());
        ClientLogger logger = new ClientLogger(ClientLoggerTests.class);

        String exceptionMessage = "An exception message";
        IOException ioException = createIOException(exceptionMessage);

        assertSame(ioException, logger.atWarning()
            .addKeyValue("connectionId", "foo")
            .addKeyValue("linkName", "bar")
            .log(ioException));

        String message = "{\"az.sdk.message\":\"\",\"exception\":\"" + exceptionMessage + "\",\"connectionId\":\"foo\",\"linkName\":\"bar\"}";
        if (logLevelToConfigure.equals(LogLevel.VERBOSE)) {
            message += System.lineSeparator() + ioException.toString() + System.lineSeparator() + "\tat " + ioException.getStackTrace()[0].toString();
        }

        assertMessage(
            message,
            byteArraySteamToString(logCaptureStream),
            logLevelToConfigure,
            LogLevel.WARNING);
    }

    @Test
    public void testIsSupplierLoggingWithException() {
        Supplier<String> supplier = () -> String.format("Param 1: %s, Param 2: %s, Param 3: %s", "test1", "test2", "test3");
        ClientLogger logger = new ClientLogger(ClientLoggerTests.class);
        Object[] args = {supplier};

        assertTrue(logger.isSupplierLogging(args));
    }

    @Test
    public void testIsSupplierLoggingWithNullException() {
        Supplier<String> supplier = () -> String.format("Param 1: %s, Param 2: %s, Param 3: %s", "test1", "test2", "test3");
        ClientLogger logger = new ClientLogger(ClientLoggerTests.class);
        Object[] args = {supplier, null};

        assertTrue(logger.isSupplierLogging(args));
    }

    @Test
    public void testIsSupplierLoggingWithMoreParameters() {
        Supplier<String> supplier = () -> String.format("Param 1: %s, Param 2: %s, Param 3: %s", "test1", "test2", "test3");
        ClientLogger logger = new ClientLogger(ClientLoggerTests.class);
        Object[] args = {supplier, supplier, supplier};

        assertFalse(logger.isSupplierLogging(args));
    }

    @Test
    public void testIsSupplierGettingEvaluated() {
        Supplier<String> supplier = () -> String.format("Param 1: %s, Param 2: %s, Param 3: %s", "test1", "test2", "test3");
        ClientLogger logger = new ClientLogger(ClientLoggerTests.class);
        Object[] args = {supplier};

        assertEquals(supplier.get(), logger.evaluateSupplierArgument(args)[0]);
    }

    @Test
    public void logSupplierShouldLogExceptionOnVerboseLevel() {
        LogLevel logLevel = LogLevel.VERBOSE;
        NullPointerException exception = new NullPointerException();
        setupLogLevel(logLevel.getLogLevel());
        Supplier<String> supplier = () -> String.format("Param 1: %s, Param 2: %s, Param 3: %s", "test1", "test2", "test3");
        ClientLogger logger = new ClientLogger(ClientLoggerTests.class);
        String expectedStackTrace = stackTraceToString(exception);
        logHelper(() -> logger.log(logLevel, supplier, exception), (args) -> logger.log(logLevel, supplier, exception), supplier);

        String logValues = byteArraySteamToString(logCaptureStream);

        assertTrue(logValues.contains(supplier.get() + System.lineSeparator()));
        assertTrue(logValues.contains(expectedStackTrace));
    }

    @ParameterizedTest
    @MethodSource("provideLogLevels")
    public void logAtLevel(LogLevel level) {
        setupLogLevel(LogLevel.INFORMATIONAL.getLogLevel());
        ClientLogger logger = new ClientLogger(ClientLoggerTests.class);

        logger.atLevel(level)
            .addKeyValue("connectionId", "foo")
            .addKeyValue("linkName", "bar")
            .log("message");

        assertMessage(
            "{\"az.sdk.message\":\"message\",\"connectionId\":\"foo\",\"linkName\":\"bar\"}",
            byteArraySteamToString(logCaptureStream),
            LogLevel.INFORMATIONAL,
            level);
    }

    private String stackTraceToString(Throwable exception) {
        StringWriter stringWriter = new StringWriter();
        exception.printStackTrace(new PrintWriter(stringWriter));
        return stringWriter.toString();
    }

    private void setupLogLevel(int logLevelToSet) {
        EnvironmentConfiguration.getGlobalConfiguration().put(PROPERTY_AZURE_LOG_LEVEL, String.valueOf(logLevelToSet));
    }

    private void clearTestLogLevel() {
        EnvironmentConfiguration.getGlobalConfiguration().remove(PROPERTY_AZURE_LOG_LEVEL);
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

    private static String byteArraySteamToString(ByteArrayOutputStream stream) {
        try {
            return stream.toString(StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    private void assertMessage(String expectedMessage, String fullLog, LogLevel configuredLevel, LogLevel loggedLevel) {
        if (loggedLevel.compareTo(configuredLevel) >= 0) {
            // remove date/time/level/etc from fullMessage
            assertEquals(expectedMessage + System.lineSeparator(), fullLog.substring(fullLog.indexOf(" - ") + 3));
        } else {
            assertEquals("", fullLog);
        }
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

    private static Stream<Arguments> provideLogLevels() {
        return Stream.of(

            // Checking VERBOSE.
            Arguments.of(LogLevel.VERBOSE),

            // Checking WARNING.
            Arguments.of(LogLevel.WARNING),

            // Checking INFORMATIONAL.
            Arguments.of(LogLevel.INFORMATIONAL),

            // Checking ERROR.
            Arguments.of(LogLevel.ERROR)
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

    class LoggableObject {
        private final String str;
        LoggableObject(String str) {
            this.str = str;
        }

        @Override
        public String toString() {
            return str;
        }
    }
}
