// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.util.logging;

import com.generic.core.implementation.util.EnvironmentConfiguration;
import com.generic.core.util.ClientLogger;
import com.generic.core.util.ClientLogger.LogLevel;
import com.generic.json.implementation.jackson.core.io.JsonStringEncoder;
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

import static com.generic.core.util.configuration.Configuration.PROPERTY_LOG_LEVEL;
import static org.junit.jupiter.api.Assertions.assertEquals;
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
        setupLogLevel(logLevelToConfigure.toString());
        assertEquals(expected, new ClientLogger(ClientLoggerTests.class).canLogAtLevel(logLevelToValidate));
    }

    /**
     * Test whether a log will be captured when the ClientLogger and message are configured to the passed log levels.
     */
    @ParameterizedTest
    @MethodSource("singleLevelCheckSupplier")
    public void logSimpleMessage(LogLevel logLevelToConfigure, LogLevel logLevelToUse, boolean logContainsMessage) {
        String logMessage = "This is a test";

        setupLogLevel(logLevelToConfigure.toString());
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

        setupLogLevel(logLevelToConfigure.toString());
        logMessage(new ClientLogger(ClientLoggerTests.class), logLevelToUse, logMessage);

        String logValues = byteArraySteamToString(logCaptureStream);
        System.out.println(logValues);
        // not removing new lines from log message
        assertTrue(logValues.contains(expectedMessage));
    }

    @ParameterizedTest
    @MethodSource("singleLevelCheckSupplier")
    public void logFormattedMessage(LogLevel logLevelToConfigure, LogLevel logLevelToUse, boolean logContainsMessage) {
        String logMessage = "This is a test";
        String logFormat = "{} is a {}";

        setupLogLevel(logLevelToConfigure.toString());
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

        setupLogLevel(logLevelToConfigure.toString());
        logMessage(new ClientLogger(ClientLoggerTests.class), logLevelToUse, logMessage, runtimeException);

        String logValues = byteArraySteamToString(logCaptureStream);
        assertEquals(logContainsMessage, logValues.contains("{\"message\":" + "\"" + logMessage + "\"" + ",\"exception\":" + "\"" + runtimeException.getMessage() + "\"" + "}"));
        assertEquals(logContainsStackTrace, logValues.contains(runtimeException.getStackTrace()[0].toString()));
    }

    /**
     * Tests that logging a RuntimeException as warning will log a message and stack trace appropriately based on the
     * configured log level.
     */
    @ParameterizedTest
    @MethodSource("logExceptionAsWarningSupplier")
    public void logThrowableAsWarning(LogLevel logLevelToConfigure, boolean logContainsMessage,
        boolean logContainsStackTrace) {
        String exceptionMessage = "An exception message";
        IOException ioException = createIOException(exceptionMessage);

        setupLogLevel(logLevelToConfigure.toString());
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
     * Tests that logging a Throwable as warning will log a message and stack trace appropriately based on the
     * configured log level.
     */
    @ParameterizedTest
    @MethodSource("logExceptionAsWarningSupplier")
    public void logCheckedExceptionAsWarning(LogLevel logLevelToConfigure, boolean logContainsMessage,
        boolean logContainsStackTrace) {
        String exceptionMessage = "An exception message";
        IOException ioException = createIOException(exceptionMessage);

        setupLogLevel(logLevelToConfigure.toString());
        try {
            throw new ClientLogger(ClientLoggerTests.class).logThrowableAsWarning(ioException);
        } catch (Throwable throwable) {
            assertTrue(throwable instanceof IOException, () -> "Expected IOException but got "
                + throwable.getClass().getSimpleName() + ".");
        }

        String logValues = byteArraySteamToString(logCaptureStream);
        assertEquals(logContainsMessage, logValues.contains(exceptionMessage));
        assertEquals(logContainsStackTrace, logValues.contains(ioException.getStackTrace()[0].toString()));
    }

    /**
     * Tests that logging a RuntimeException as error will log a message and stack trace appropriately based on the
     * configured log level.
     */
    @ParameterizedTest
    @MethodSource("logExceptionAsErrorSupplier")
    public void logThrowableAsError(LogLevel logLevelToConfigure, boolean logContainsMessage,
        boolean logContainsStackTrace) {
        String exceptionMessage = "An exception message";
        IllegalStateException illegalStateException = createIllegalStateException(exceptionMessage);

        setupLogLevel(logLevelToConfigure.toString());
        try {
            throw new ClientLogger(ClientLoggerTests.class).logThrowableAsError(illegalStateException);
        } catch (RuntimeException exception) {
            assertTrue(exception instanceof IllegalStateException, () -> "Expected IllegalStateException but got "
                + exception.getClass().getSimpleName() + ".");
        }

        String logValues = byteArraySteamToString(logCaptureStream);
        assertEquals(logContainsMessage, logValues.contains(exceptionMessage));
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

        setupLogLevel(logLevelToConfigure.toString());
        try {
            throw new ClientLogger(ClientLoggerTests.class).logThrowableAsError(ioException);
        } catch (Throwable throwable) {
            assertTrue(throwable instanceof IOException, () -> "Expected IOException but got "
                + throwable.getClass().getSimpleName() + ".");
        }

        String logValues = byteArraySteamToString(logCaptureStream);
        assertEquals(logContainsMessage, logValues.contains(exceptionMessage));
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
        setupLogLevel(logLevel.toString());
        Supplier<String> supplier = () -> String.format("Param 1: %s, Param 2: %s, Param 3: %s", "test1", "test2", "test3");
        ClientLogger logger = new ClientLogger(ClientLoggerTests.class);
        if (logLevel.equals(LogLevel.ERROR)) {
            logHelper(()-> logger.atError().log(supplier), (args) -> logger.atError().log(supplier), supplier);
        } else if (logLevel.equals(LogLevel.WARNING)) {
            logHelper(()-> logger.atWarning().log(supplier), (args) -> logger.atWarning().log(supplier), supplier);
        } else if (logLevel.equals(LogLevel.INFORMATIONAL)) {
            logHelper(()-> logger.atInfo().log(supplier), (args) -> logger.atInfo().log(supplier), supplier);
        } else if (logLevel.equals(LogLevel.VERBOSE)) {
            logHelper(()-> logger.atVerbose().log(supplier), (args) -> logger.atVerbose().log(supplier), supplier);
        } else {
            throw new IllegalArgumentException("Unknown log level: " + logLevel);
        }

        String logValues = byteArraySteamToString(logCaptureStream);
        assertTrue(logValues.contains(supplier.get()));
    }

    @Test
    public void logWithNewLine() {
        setupLogLevel(LogLevel.INFORMATIONAL.toString());
        String message = String.format("Param 1: %s%s, Param 2: %s%s, Param 3: %s", "test1", System.lineSeparator(), "test2", System.lineSeparator(), "test3");
        ClientLogger logger = new ClientLogger(ClientLoggerTests.class);
        logger.atInfo().log(() -> message);

        String logValues = byteArraySteamToString(logCaptureStream);
        assertTrue(logValues.contains("{\"message\":Param 1: test1, Param 2: test2, Param 3: test3"));
    }

    @ParameterizedTest
    @MethodSource("provideLogLevels")
    public void logWithNullSupplier(LogLevel logLevel) {
        setupLogLevel(logLevel.toString());
        ClientLogger logger = new ClientLogger(ClientLoggerTests.class);
        if (logLevel.equals(LogLevel.ERROR)) {
            logHelper(()-> logger.atError().log((Supplier<String>) null), (args) -> logger.atError().log((Supplier<String>) null), new Object[]{null});
        } else if (logLevel.equals(LogLevel.WARNING)) {
            logHelper(()-> logger.atWarning().log((Supplier<String>) null), (args) -> logger.atWarning().log((Supplier<String>) null), new Object[]{null});
        } else if (logLevel.equals(LogLevel.INFORMATIONAL)) {
            logHelper(()-> logger.atInfo().log((Supplier<String>) null), (args) -> logger.atInfo().log((Supplier<String>) null), new Object[]{null});
        } else if (logLevel.equals(LogLevel.VERBOSE)) {
            logHelper(()-> logger.atVerbose().log((Supplier<String>) null), (args) -> logger.atVerbose().log((Supplier<String>) null), new Object[]{null});
        } else {
            throw new IllegalArgumentException("Unknown log level: " + logLevel);
        }

        String logValues = byteArraySteamToString(logCaptureStream);
        assertTrue(logValues.isEmpty());
    }

    @ParameterizedTest
    @MethodSource("provideLogLevels")
    public void logSupplierWithException(LogLevel logLevel) {
        NullPointerException exception = new NullPointerException();
        setupLogLevel(logLevel.toString());
        Supplier<String> supplier = () -> String.format("Param 1: %s, Param 2: %s, Param 3: %s", "test1", "test2", "test3");
        ClientLogger logger = new ClientLogger(ClientLoggerTests.class);
        if (logLevel.equals(LogLevel.ERROR)) {
            logHelper(()-> logger.atError().log(supplier, exception), (args) -> logger.atError().log(supplier, exception), supplier);
        } else if (logLevel.equals(LogLevel.WARNING)) {
            logHelper(()-> logger.atWarning().log(supplier, exception), (args) -> logger.atWarning().log(supplier, exception), supplier);
        } else if (logLevel.equals(LogLevel.INFORMATIONAL)) {
            logHelper(()-> logger.atInfo().log(supplier, exception), (args) -> logger.atInfo().log(supplier, exception), supplier);
        } else if (logLevel.equals(LogLevel.VERBOSE)) {
            logHelper(()-> logger.atVerbose().log(supplier, exception), (args) -> logger.atVerbose().log(supplier, exception), supplier);
        } else {
            throw new IllegalArgumentException("Unknown log level: " + logLevel);
        }
        String logValues = byteArraySteamToString(logCaptureStream);

        assertTrue(logValues.contains(supplier.get()));
    }

    @ParameterizedTest
    @MethodSource("provideLogLevels")
    public void logShouldEvaluateSupplierWithNullException(LogLevel logLevel) {
        setupLogLevel(logLevel.toString());
        Supplier<String> supplier = () -> String.format("Param 1: %s, Param 2: %s, Param 3: %s", "test1", "test2", "test3");
        ClientLogger logger = new ClientLogger(ClientLoggerTests.class);
        if (logLevel.equals(LogLevel.ERROR)) {
            logHelper(()-> logger.atError().log(supplier, null), (args) -> logger.atError().log(supplier, null), supplier);
        } else if (logLevel.equals(LogLevel.WARNING)) {
            logHelper(()-> logger.atWarning().log(supplier, null), (args) -> logger.atWarning().log(supplier, null), supplier);
        } else if (logLevel.equals(LogLevel.INFORMATIONAL)) {
            logHelper(()-> logger.atInfo().log(supplier, null), (args) -> logger.atInfo().log(supplier, null), supplier);
        } else if (logLevel.equals(LogLevel.VERBOSE)) {
            logHelper(()-> logger.atVerbose().log(supplier, null), (args) -> logger.atVerbose().log(supplier, null), supplier);
        } else {
            throw new IllegalArgumentException("Unknown log level: " + logLevel);
        }
        String logValues = byteArraySteamToString(logCaptureStream);

        assertTrue(logValues.contains(supplier.get()));
    }

//    @Test
//    public void testIsSupplierLogging() {
//        Supplier<String> supplier = () -> String.format("Param 1: %s, Param 2: %s, Param 3: %s", "test1", "test2", "test3");
//        ClientLogger logger = new ClientLogger(ClientLoggerTests.class);
//        NullPointerException exception = new NullPointerException();
//        Object[] args = {supplier, exception};
//
//        assertTrue(logger.isSupplierLogging(args));
//    }

    /**
     * Tests that logging with context of string message writes
     * log message and context in correct format and depending on the level.
     */
    @ParameterizedTest
    @MethodSource("provideLogLevels")
    public void logWithContext(LogLevel logLevelToConfigure) {
        setupLogLevel(logLevelToConfigure.toString());
        ClientLogger logger = new ClientLogger(ClientLoggerTests.class);

        String message = String.format("Param 1: %s, Param 2: %s, Param 3: %s", "test1", "test2", "test3");

        logger.atWarning()
            .addKeyValue("connectionId", "foo")
            .addKeyValue("linkName", 1)
            .log(message);

        assertMessage(
            "{\"message\":\"Param 1: test1, Param 2: test2, Param 3: test3\",\"connectionId\":\"foo\",\"linkName\":1}",
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
        setupLogLevel(logLevelToConfigure.toString());

        ClientLogger logger = new ClientLogger(ClientLoggerTests.class, globalContext);
        logger.atWarning().log(() ->
            String.format("Param 1: %s, Param 2: %s, Param 3: %s", "test1", "test2", "test3"));

        assertMessage(
            "{\"message\":\"Param 1: test1, Param 2: test2, Param 3: test3\",\"connectionId\":\"foo\",\"linkName\":1,\"anotherKey\":\"hello world\"}",
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
        setupLogLevel(LogLevel.VERBOSE.toString());

        ClientLogger logger = new ClientLogger(ClientLoggerTests.class, globalContext);
        logger.atInfo().log(() -> "message");

        assertMessage(
            "{\"message\":\"message\",\"connectionId\":\"foo\",\"linkName\":1,\"anotherKey\":\"hello world\"}",
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
        setupLogLevel(LogLevel.VERBOSE.toString());

        ClientLogger logger = new ClientLogger(ClientLoggerTests.class, globalContext);
        logger.atVerbose().log(() -> "message");

        assertMessage(
            "{\"message\":\"message\",\"connectionId\":\"foo\",\"linkName\":1,\"anotherKey\":\"hello world\"}",
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
        setupLogLevel(LogLevel.VERBOSE.toString());

        new ClientLogger(ClientLoggerTests.class, globalContext)
            .atWarning().log(() -> "message");;

        assertMessage(
            "{\"message\":\"message\",\"connectionId\":\"foo\",\"linkName\":1,\"anotherKey\":\"hello world\"}",
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
        setupLogLevel(LogLevel.VERBOSE.toString());

        new ClientLogger(ClientLoggerTests.class, globalContext)
            .atError().log(() -> "message");

        assertMessage(
            "{\"message\":\"message\",\"connectionId\":\"foo\",\"linkName\":1,\"anotherKey\":\"hello world\"}",
            byteArraySteamToString(logCaptureStream),
            LogLevel.VERBOSE,
            LogLevel.ERROR);
    }

    /**
     * Tests empty global context
     */
    @Test
    public void logWithEmptyGlobalContext() {
        setupLogLevel(LogLevel.INFORMATIONAL.toString());

        ClientLogger logger = new ClientLogger(ClientLoggerTests.class, Collections.emptyMap());
        logger.atWarning().log(() -> "Param 1: {}, Param 2: {}, Param 3: {}", "test1", "test2", "test3");

        assertMessage(
            "{\"message\":\"Param 1: test1, Param 2: test2, Param 3: test3\"}",
            byteArraySteamToString(logCaptureStream),
            LogLevel.INFORMATIONAL,
            LogLevel.WARNING);
    }

    /**
     * Tests null global context
     */
    @Test
    public void logWithNullGlobalContext() {
        setupLogLevel(LogLevel.INFORMATIONAL.toString());

        ClientLogger logger = new ClientLogger(ClientLoggerTests.class, null);
        logger.atInfo().log(
            () -> String.format("Param 1: %s, Param 2: %s, Param 3: %s", "test1", "test2", "test3"));

        assertMessage(
            "{\"message\":\"Param 1: test1, Param 2: test2, Param 3: test3\"}",
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
        setupLogLevel(logLevelToConfigure.toString());

        ClientLogger logger = new ClientLogger(ClientLoggerTests.class, globalContext);
        logger.atInfo()
            .addKeyValue("local", true)
            .addKeyValue("connectionId", "conflict")
            .log(() -> "Param 1: {}, Param 2: {}, Param 3: {}", "test1", "test2", "test3");

        assertMessage(
            "{\"message\":\"Param 1: test1, Param 2: test2, Param 3: test3\",\"connectionId\":\"foo\",\"linkName\":1,\"anotherKey\":\"hello world\",\"local\":true,\"connectionId\":\"conflict\"}",
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
        setupLogLevel(logLevelToConfigure.toString());
        ClientLogger logger = new ClientLogger(ClientLoggerTests.class);

        logger.atWarning().log(String.format("Param 1: %s, Param 2: %s, Param 3: %s", "test1", "test2", "test3"));

        assertMessage(
            "{\"message\":\"Param 1: test1, Param 2: test2, Param 3: test3\"}",
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
        setupLogLevel(logLevelToConfigure.toString());
        ClientLogger logger = new ClientLogger(ClientLoggerTests.class, globalContext);

        logger.atInfo().log(() -> String.format("Param 1: %s, Param 2: %s, Param 3: %s", "test1", "test2", "test3"));

        assertMessage(
            "{\"message\":\"Param 1: test1, Param 2: test2, Param 3: test3\",\"connectionId\":\"foo\",\"linkName\":1,\"anotherKey\":\"hello world\"}",
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
        setupLogLevel(logLevelToConfigure.toString());
        ClientLogger logger = new ClientLogger(ClientLoggerTests.class);

        String message = String.format("Param 1: %s, Param 2: %s, Param 3: %s", "test1", "test2", "test3");

        logger.atInfo()
            .addKeyValue("connectionId", "foo")
            .addKeyValue("linkName", "bar")
            .log(() -> message);

        assertMessage(
            "{\"message\":\"Param 1: test1, Param 2: test2, Param 3: test3\",\"connectionId\":\"foo\",\"linkName\":\"bar\"}",
            byteArraySteamToString(logCaptureStream),
            logLevelToConfigure,
            LogLevel.INFORMATIONAL);
    }

    /**
     * Tests that logging with context with null message does not throw.
     */
    @Test
    public void logWithContextNullMessage() {
        setupLogLevel(LogLevel.VERBOSE.toString());
        ClientLogger logger = new ClientLogger(ClientLoggerTests.class);

        logger.atVerbose()
            .addKeyValue("connectionId", "foo")
            .addKeyValue("linkName", true)
            .log((String) null);

        assertMessage(
            "{\"message\":\"\",\"connectionId\":\"foo\",\"linkName\":true}",
            byteArraySteamToString(logCaptureStream),
            LogLevel.VERBOSE,
            LogLevel.INFORMATIONAL);
    }

    /**
     * Tests that newline is escaped in message, keys and values.
     */
    @Test
    public void logWithContextNewLineIsEscaped() {
        setupLogLevel(LogLevel.VERBOSE.toString());
        ClientLogger logger = new ClientLogger(ClientLoggerTests.class);

        logger.atVerbose()
            .addKeyValue("connection\nId" + System.lineSeparator(), "foo")
            .addKeyValue("link\r\nName", "test" + System.lineSeparator() + "me")
            .log(() -> "multiline " + System.lineSeparator() + "message");

        String escapedNewLine = new String(JsonStringEncoder.getInstance().quoteAsString(System.lineSeparator()));

        assertMessage(
            "{\"message\":\"multiline " + escapedNewLine + "message\",\"connection\\nId" + escapedNewLine + "\":\"foo\",\"link\\r\\nName\":\"test" + escapedNewLine + "me\"}",
            byteArraySteamToString(logCaptureStream),
            LogLevel.VERBOSE,
            LogLevel.INFORMATIONAL);
    }

    /**
     * Tests that global context is escaped
     */
    @Test
    public void logWithGlobalContextIsEscaped() {
        setupLogLevel(LogLevel.VERBOSE.toString());

        // preserve order
        Map<String, Object> globalCtx = new LinkedHashMap<>();
        globalCtx.put("link\tName", 1);
        globalCtx.put("another\rKey\n", new LoggableObject("hello \"world\"\r\n"));

        ClientLogger logger = new ClientLogger(ClientLoggerTests.class, globalCtx);

        logger.atVerbose().log(() -> "\"message\"");

        assertMessage(
            "{\"message\":\"\\\"message\\\"\",\"link\\tName\":1,\"another\\rKey\\n\":\"hello \\\"world\\\"\\r\\n\"}",
            byteArraySteamToString(logCaptureStream),
            LogLevel.VERBOSE,
            LogLevel.INFORMATIONAL);
    }

    /**
     * Tests that logging with context with null message supplier does not throw.
     */
    @Test
    public void logWithContextNullSupplier() {
        setupLogLevel(LogLevel.INFORMATIONAL.toString());
        ClientLogger logger = new ClientLogger(ClientLoggerTests.class);

        Supplier<String> message = null;

        logger.atError()
            .addKeyValue("connectionId", "foo")
            .addKeyValue("linkName", (String) null)
            .log(message);

        assertMessage(
            "{\"message\":\"\",\"connectionId\":\"foo\",\"linkName\":null}",
            byteArraySteamToString(logCaptureStream),
            LogLevel.INFORMATIONAL,
            LogLevel.ERROR);
    }

    /**
     * Tests supplied context value.
     */
    @Test
    public void logWithContextValueSupplier() {
        setupLogLevel(LogLevel.INFORMATIONAL.toString());
        ClientLogger logger = new ClientLogger(ClientLoggerTests.class);

        logger.atWarning()
            // this is technically invalid, but we should not throw because of logging in runtime
            .addKeyValue("connectionId", (Supplier<String>) null)
            .addKeyValue("linkName", () -> String.format("complex value %s", 123))
            .log(() -> "test");

        assertMessage(
            "{\"message\":\"test\",\"connectionId\":null,\"linkName\":\"complex value 123\"}",
            byteArraySteamToString(logCaptureStream),
            LogLevel.INFORMATIONAL,
            LogLevel.WARNING);
    }

    /**
     * Tests supplied context value.
     */
    @Test
    public void logWithContextObject() {
        setupLogLevel(LogLevel.INFORMATIONAL.toString());
        ClientLogger logger = new ClientLogger(ClientLoggerTests.class);

        logger.atWarning()
            .addKeyValue("linkName", new LoggableObject("some complex object"))
            .log(() -> "test");

        assertMessage(
            "{\"message\":\"test\",\"linkName\":\"some complex object\"}",
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
        setupLogLevel(logLevelToConfigure.toString());
        ClientLogger logger = new ClientLogger(ClientLoggerTests.class);

        logger.atWarning()
            .addKeyValue("connectionId", () -> null)
            .addKeyValue("linkName", "bar")
            .log(() -> "Param 1: {}, Param 2: {}, Param 3: {}", "test1", "test2", "test3");

        assertMessage(
            "{\"message\":\"Param 1: test1, Param 2: test2, Param 3: test3\",\"connectionId\":null,\"linkName\":\"bar\"}",
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
        setupLogLevel(logLevelToConfigure.toString());
        ClientLogger logger = new ClientLogger(ClientLoggerTests.class);

        String exceptionMessage = "An exception message";
        RuntimeException runtimeException = createIllegalStateException(exceptionMessage);

        logger.atWarning()
            .addKeyValue("connectionId", "foo")
            .addKeyValue("linkName", "bar")
            .log(() -> "hello {}", "world", runtimeException);

        String message = "{\"message\":\"hello world\",\"exception\":\"" + exceptionMessage + "\",\"connectionId\":\"foo\",\"linkName\":\"bar\"}";
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
        setupLogLevel(logLevelToConfigure.toString());
        ClientLogger logger = new ClientLogger(ClientLoggerTests.class);

        String exceptionMessage = "An exception message";
        IOException ioException = createIOException(exceptionMessage);

        logger.atWarning()
            .addKeyValue("connectionId", "foo")
            .addKeyValue("linkName", "bar")
            .log(() -> String.format("hello %s", "world"), ioException);

        String message = "{\"message\":\"hello world\",\"exception\":\"" + exceptionMessage + "\",\"connectionId\":\"foo\",\"linkName\":\"bar\"}";
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
        setupLogLevel(logLevelToConfigure.toString());
        ClientLogger logger = new ClientLogger(ClientLoggerTests.class);

        String exceptionMessage = "An exception \tmessage with \"special characters\"\r\n";
        RuntimeException runtimeException = createIllegalStateException(exceptionMessage);

        logger.atWarning()
            .addKeyValue("connection\tId", "foo")
            .addKeyValue("linkName", "\rbar")
            .log(() -> "hello {}, \"and\" {more}", "world", runtimeException);


        String escapedExceptionMessage = "An exception \\tmessage with \\\"special characters\\\"\\r\\n";

        String expectedMessage = "{\"message\":\"hello world, \\\"and\\\" {more}\",\"exception\":\"" + escapedExceptionMessage + "\",\"connection\\tId\":\"foo\",\"linkName\":\"\\rbar\"}";
        if (logLevelToConfigure.equals(LogLevel.VERBOSE)) {
            expectedMessage += System.lineSeparator() + runtimeException + System.lineSeparator() + "\tat " + runtimeException.getStackTrace()[0].toString();
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
        setupLogLevel(logLevelToConfigure.toString());
        ClientLogger logger = new ClientLogger(ClientLoggerTests.class);

        String exceptionMessage = "An exception message";
        RuntimeException runtimeException = createIllegalStateException(exceptionMessage);

        assertSame(runtimeException, logger.atWarning()
            .addKeyValue("connectionId", "foo")
            .addKeyValue("linkName", "bar")
            .log(() -> RuntimeException));

        String message = "{\"message\":\"\",\"exception\":\"" + exceptionMessage + "\",\"connectionId\":\"foo\",\"linkName\":\"bar\"}";
        if (logLevelToConfigure.equals(LogLevel.VERBOSE)) {
            message += System.lineSeparator() + runtimeException + System.lineSeparator() + "\tat " + runtimeException.getStackTrace()[0].toString();
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
        setupLogLevel(logLevelToConfigure.toString());
        ClientLogger logger = new ClientLogger(ClientLoggerTests.class);

        String exceptionMessage = "An exception message";
        IOException ioException = createIOException(exceptionMessage);

        assertSame(ioException, logger.atWarning()
            .addKeyValue("connectionId", "foo")
            .addKeyValue("linkName", "bar")
            .log(ioException));

        String message = "{\"message\":\"\",\"exception\":\"" + exceptionMessage + "\",\"connectionId\":\"foo\",\"linkName\":\"bar\"}";
        if (logLevelToConfigure.equals(LogLevel.VERBOSE)) {
            message += System.lineSeparator() + ioException + System.lineSeparator() + "\tat " + ioException.getStackTrace()[0].toString();
        }

        assertMessage(
            message,
            byteArraySteamToString(logCaptureStream),
            logLevelToConfigure,
            LogLevel.WARNING);
    }

//    @Test
//    public void testIsSupplierLoggingWithException() {
//        Supplier<String> supplier = () -> String.format("Param 1: %s, Param 2: %s, Param 3: %s", "test1", "test2", "test3");
//        ClientLogger logger = new ClientLogger(ClientLoggerTests.class);
//        Object[] args = {supplier};
//
//        assertTrue(logger.isSupplierLogging(args));
//    }
//
//    @Test
//    public void testIsSupplierLoggingWithNullException() {
//        Supplier<String> supplier = () -> String.format("Param 1: %s, Param 2: %s, Param 3: %s", "test1", "test2", "test3");
//        ClientLogger logger = new ClientLogger(ClientLoggerTests.class);
//        Object[] args = {supplier, null};
//
//        assertTrue(logger.isSupplierLogging(args));
//    }

//    @Test
//    public void testIsSupplierLoggingWithMoreParameters() {
//        Supplier<String> supplier = () -> String.format("Param 1: %s, Param 2: %s, Param 3: %s", "test1", "test2", "test3");
//        ClientLogger logger = new ClientLogger(ClientLoggerTests.class);
//        Object[] args = {supplier, supplier, supplier};
//
//        assertFalse(logger.isSupplierLogging(args));
//    }

//    @Test
//    public void testIsSupplierGettingEvaluated() {
//        Supplier<String> supplier = () -> String.format("Param 1: %s, Param 2: %s, Param 3: %s", "test1", "test2", "test3");
//        ClientLogger logger = new ClientLogger(ClientLoggerTests.class);
//        Object[] args = {supplier};
//
//        assertEquals(supplier.get(), logger.evaluateSupplierArgument(args)[0]);
//    }

    @Test
    public void logSupplierShouldLogExceptionOnVerboseLevel() {
        LogLevel logLevel = LogLevel.VERBOSE;
        NullPointerException exception = new NullPointerException();
        setupLogLevel(logLevel.toString());
        Supplier<String> supplier = () -> String.format("Param 1: %s, Param 2: %s, Param 3: %s", "test1", "test2", "test3");
        ClientLogger logger = new ClientLogger(ClientLoggerTests.class);
        String expectedStackTrace = stackTraceToString(exception);
        logHelper(() -> logger.atVerbose().log(supplier, exception), (args) -> logger.atVerbose().log(supplier, exception), supplier);

        String logValues = byteArraySteamToString(logCaptureStream);

        assertTrue(logValues.contains("{\"message\":\"" + supplier.get()));
        assertTrue(logValues.contains(expectedStackTrace));
    }

    @ParameterizedTest
    @MethodSource("provideLogLevels")
    public void logAtLevel(LogLevel level) {
        setupLogLevel(LogLevel.INFORMATIONAL.toString());
        ClientLogger logger = new ClientLogger(ClientLoggerTests.class);

        logger.atLevel(level)
            .addKeyValue("connectionId", "foo")
            .addKeyValue("linkName", "bar")
            .log(() -> "message");

        assertMessage(
            "{\"message\":\"message\",\"connectionId\":\"foo\",\"linkName\":\"bar\"}",
            byteArraySteamToString(logCaptureStream),
            LogLevel.INFORMATIONAL,
            level);
    }

    private String stackTraceToString(Throwable exception) {
        StringWriter stringWriter = new StringWriter();
        exception.printStackTrace(new PrintWriter(stringWriter));
        return stringWriter.toString();
    }

    private void setupLogLevel(String logLevelToSet) {
        EnvironmentConfiguration.getGlobalConfiguration().put(PROPERTY_LOG_LEVEL, String.valueOf(logLevelToSet));
    }

    private void clearTestLogLevel() {
        EnvironmentConfiguration.getGlobalConfiguration().remove(PROPERTY_LOG_LEVEL);
    }

    private void logMessage(ClientLogger logger, LogLevel logLevel, String logFormat, Object... arguments) {
        if (logLevel == null) {
            return;
        }

        switch (logLevel) {
            case VERBOSE:
                logHelper(() -> logger.atVerbose().log(logFormat), (args) -> logger.atVerbose().log(logFormat, args), arguments);
                break;
            case INFORMATIONAL:
                logHelper(() -> logger.atInfo().log(logFormat), (args) -> logger.atInfo().log(logFormat, args), arguments);
                break;
            case WARNING:
                logHelper(() -> logger.atWarning().log(logFormat), (args) -> logger.atWarning().log(logFormat, args), arguments);
                break;
            case ERROR:
                logHelper(() -> logger.atError().log(logFormat), (args) -> logger.atError().log(logFormat, args), arguments);
                break;
            default:
                break;
        }
    }

    private static void logHelper(Runnable simpleLog, Consumer<Object[]> formatLog, Object... args) {
        if (args == null || args.length == 0) {
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
            Arguments.of(LogLevel.VERBOSE, LogLevel.NOTSET, false),

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
            Arguments.of(LogLevel.INFORMATIONAL, LogLevel.NOTSET, false),

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
            Arguments.of(LogLevel.WARNING, LogLevel.NOTSET, false),

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
            Arguments.of(LogLevel.VERBOSE, LogLevel.NOTSET, false),

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
            Arguments.of(LogLevel.VERBOSE, LogLevel.NOTSET, false, false),

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
            Arguments.of(LogLevel.INFORMATIONAL, LogLevel.NOTSET, false, false),

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
            Arguments.of(LogLevel.WARNING, LogLevel.NOTSET, false, false),

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
            Arguments.of(LogLevel.VERBOSE, LogLevel.NOTSET, false, false),

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
            Arguments.of(LogLevel.NOTSET, false, false)
        );
    }

    private static Stream<Arguments> logExceptionAsErrorSupplier() {
        return Stream.of(
            Arguments.of(LogLevel.VERBOSE, true, true),
            Arguments.of(LogLevel.INFORMATIONAL, true, false),
            Arguments.of(LogLevel.WARNING, true, false),
            Arguments.of(LogLevel.ERROR, true, false),
            Arguments.of(LogLevel.NOTSET, false, false)
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
            Arguments.of("0", LogLevel.NOTSET),
            Arguments.of(null, LogLevel.NOTSET)
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
