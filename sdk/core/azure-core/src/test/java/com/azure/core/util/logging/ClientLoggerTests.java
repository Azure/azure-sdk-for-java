// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.logging;

import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests for {@link ClientLogger}.
 */
public class ClientLoggerTests {
    private static final String SLF4J_LOG_FILE_PROPERTY = "org.slf4j.simpleLogger.logFile";
    private static final String SLF4J_CACHED_STREAM_PROPERTY = "org.slf4j.simpleLogger.cacheOutputStream";
    private static final String SLF4J_TEST_LOG_FILE = "System.out";
    private static final String SLF4J_TEST_CACHED_STREAM = "false";

    private String originalLogFile;
    private String originalCachedOutputStream;
    private PrintStream originalSystemOut;

    private ByteArrayOutputStream logCaptureStream;

    @Before
    public void setupLoggingConfiguration() {
        originalLogFile = System.getProperty(SLF4J_LOG_FILE_PROPERTY);
        originalCachedOutputStream = System.getProperty(SLF4J_CACHED_STREAM_PROPERTY);
        System.setProperty(SLF4J_LOG_FILE_PROPERTY, SLF4J_TEST_LOG_FILE);
        System.setProperty(SLF4J_CACHED_STREAM_PROPERTY, SLF4J_TEST_CACHED_STREAM);

        originalSystemOut = System.out;
        logCaptureStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(logCaptureStream));
    }

    @After
    public void revertLoggingConfiguration() {
        setPropertyToOriginalOrClear(SLF4J_LOG_FILE_PROPERTY, originalLogFile);
        setPropertyToOriginalOrClear(SLF4J_CACHED_STREAM_PROPERTY, originalCachedOutputStream);

        System.setOut(originalSystemOut);
    }

    private void setPropertyToOriginalOrClear(String propertyName, String originalValue) {
        if (CoreUtils.isNullOrEmpty(originalValue)) {
            System.clearProperty(propertyName);
        } else {
            System.setProperty(propertyName, originalValue);
        }
    }

    private void logMessage(int logLevelToSet, int logLevelToLog, String logFormat, Object... arguments) {
        String originalLogLevel = Configuration.getGlobalConfiguration().get(Configuration.PROPERTY_AZURE_CLOUD);
        System.setProperty(Configuration.PROPERTY_AZURE_LOG_LEVEL, Integer.toString(logLevelToSet));

        ClientLogger logger = new ClientLogger(ClientLoggerTests.class);

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

        setPropertyToOriginalOrClear(Configuration.PROPERTY_AZURE_LOG_LEVEL, originalLogLevel);
    }

    /**
     * Tests that a simple log message can be logged.
     */
    @Test
    public void simpleLog() {
        String logMessage = "This is a test";
        logMessage(2, 2, logMessage);

        String logValues = new String(logCaptureStream.toByteArray(), StandardCharsets.UTF_8);
        assertTrue(logValues.contains(logMessage));
    }

    /**
     * Tests that logging at a level that isn't supported doesn't log anything.
     */
    @Test
    public void logAtUnsupportedLevel() {
        String logMessage = "This is a test";
        logMessage(2, 1, logMessage);

        String logValues = new String(logCaptureStream.toByteArray(), StandardCharsets.UTF_8);
        assertFalse(logValues.contains(logMessage));
    }

    /**
     * Tests that logging an exception when the log level isn't VERBOSE only log the exception message.
     */
    @Test
    public void onlyLogExceptionMessage() {
        String logMessage = "This is an exception";
        Throwable throwable = new Throwable("An exception message");
        StackTraceElement[] stackTraceElements = { new StackTraceElement("ClientLoggerTests", "onlyLogExceptionMessage",
            "ClientLoggerTests", 117) };
        throwable.setStackTrace(stackTraceElements);
        String stackTraceString = stackTraceElements[0].toString();

        logMessage(2, 3, logMessage, throwable);

        String logValues = new String(logCaptureStream.toByteArray(), StandardCharsets.UTF_8);
        assertTrue(logValues.contains(logMessage + throwable.getMessage()));
        assertFalse(logValues.contains(stackTraceString));
    }

    /**
     * Tests that logging an exception when the log level is VERBOSE the stack trace is logged.
     */
    @Test
    public void logExceptionStackTrace() {
        String logMessage = "This is an exception";
        Throwable throwable = new Throwable("An exception message");
        StackTraceElement[] stackTraceElements = { new StackTraceElement("ClientLoggerTests", "onlyLogExceptionMessage",
            "ClientLoggerTests", 117) };
        throwable.setStackTrace(stackTraceElements);
        String stackTraceString = stackTraceElements[0].toString();

        logMessage(1, 3, logMessage, throwable);

        String logValues = new String(logCaptureStream.toByteArray(), StandardCharsets.UTF_8);
        assertTrue(logValues.contains(logMessage + throwable.getMessage()));
        assertTrue(logValues.contains(stackTraceString));
    }
}
