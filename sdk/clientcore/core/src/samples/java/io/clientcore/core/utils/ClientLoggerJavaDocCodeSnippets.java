// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.utils;

import io.clientcore.core.http.models.Response;
import io.clientcore.core.instrumentation.logging.ClientLogger;
import io.clientcore.core.instrumentation.logging.LogLevel;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Code snippets for {@link ClientLogger} javadocs
 */
public class ClientLoggerJavaDocCodeSnippets {

    /**
     * Code snippets to show usage of {@link ClientLogger} at all log levels
     */
    public void loggingSnippets() throws IOException {

        ClientLogger logger = new ClientLogger(ClientLoggerJavaDocCodeSnippets.class);
        String name = getName();

        // BEGIN: io.clientcore.core.util.logging.clientlogger.verbose
        logger.atVerbose().log("A log message");
        // END: io.clientcore.core.util.logging.clientlogger.verbose

        // BEGIN: io.clientcore.core.util.logging.clientlogger.verbose#string-object
        logger.atVerbose()
            .addKeyValue("hello", name)
            .log("A structured log message.");
        // END: io.clientcore.core.util.logging.clientlogger.verbose#string-object

        // BEGIN: io.clientcore.core.util.logging.clientlogger.info
        logger.atInfo().log("A log message");
        // END: io.clientcore.core.util.logging.clientlogger.info

        // BEGIN: io.clientcore.core.util.logging.clientlogger.info#string-object
        logger.atInfo()
            .addKeyValue("hello", name)
            .log("A structured log message.");
        // END: io.clientcore.core.util.logging.clientlogger.info#string-object

        // BEGIN: io.clientcore.core.util.logging.clientlogger.log
        logger.atVerbose()
            .addKeyValue("param1", 1)
            .addKeyValue("param2", 2)
            .addKeyValue("param3", 2)
            .log("A structured log message.");
        // END: io.clientcore.core.util.logging.clientlogger.log

        // BEGIN: io.clientcore.core.util.logging.clientlogger.log#throwable
        Throwable illegalArgumentException = new IllegalArgumentException("An invalid argument was encountered.");
        logger.atVerbose()
            .addKeyValue("param1", 1)
            .addKeyValue("param2", 2)
            .addKeyValue("param3", 2)
            .setThrowable(illegalArgumentException)
            .log("param3 is expected to be 3");
        // END: io.clientcore.core.util.logging.clientlogger.log#throwable

        // BEGIN: io.clientcore.core.util.logging.clientlogger.warning
        Throwable detailedException = new IllegalArgumentException("A exception with a detailed message");
        logger.atWarning().setThrowable(detailedException).log("A warning with exception.");
        // END: io.clientcore.core.util.logging.clientlogger.warning

        // BEGIN: io.clientcore.core.util.logging.clientlogger.warning#string-object
        Throwable exception = new IllegalArgumentException("An invalid argument was encountered.");
        logger.atWarning()
            .addKeyValue("hello", name)
            .setThrowable(exception)
            .log("A structured warning with exception.");
        // END: io.clientcore.core.util.logging.clientlogger.warning#string-object

        File resource = getFile();
        // BEGIN: io.clientcore.core.util.logging.clientlogger.error
        try {
            upload(resource);
        } catch (IOException ex) {
            throw logger.logThrowableAsError(ex);
        }
        // END: io.clientcore.core.util.logging.clientlogger.error

        // BEGIN: io.clientcore.core.util.logging.clientlogger.error#string-object
        try {
            upload(resource);
        } catch (IOException ex) {
            logger.atError()
                .addKeyValue("hello", name)
                .setThrowable(ex)
                .log();

            throw ex;
        }
        // END: io.clientcore.core.util.logging.clientlogger.error#string-object

        // BEGIN: io.clientcore.core.util.logging.clientlogger#globalcontext
        Map<String, Object> context = new HashMap<>();
        context.put("connectionId", "95a47cf");

        ClientLogger loggerWithContext = new ClientLogger(ClientLoggerJavaDocCodeSnippets.class, context);
        loggerWithContext.atInfo()
            .addKeyValue("hello", name)
            .log("A structured log with global and local contexts.");
        // END: io.clientcore.core.util.logging.clientlogger#globalcontext

        // BEGIN: io.clientcore.core.util.logging.clientlogger.atInfo
        logger.atInfo()
            .addKeyValue("key", "value")
            .addKeyValue("hello", name)
            .log("A structured log message.");
        // END: io.clientcore.core.util.logging.clientlogger.atInfo

        // BEGIN: io.clientcore.core.util.logging.clientlogger.atWarning
        logger.atWarning()
            .addKeyValue("key", "value")
            .setThrowable(exception)
            .log("A structured log message with exception.");
        // END: io.clientcore.core.util.logging.clientlogger.atWarning

        // BEGIN: io.clientcore.core.util.logging.clientlogger.atError#deffered-value
        try {
            upload(resource);
        } catch (IOException ex) {
            logger.atError()
                .addKeyValue("key", () -> "Expensive to calculate value")
                .setThrowable(ex)
                .log();

            throw ex;
        }
        // END: io.clientcore.core.util.logging.clientlogger.atError#deffered-value

        Response<Void> response = getResponse();
        // BEGIN: io.clientcore.core.util.logging.clientlogger.atLevel
        LogLevel level = response.getStatusCode() == 200 ? LogLevel.INFORMATIONAL : LogLevel.WARNING;
        logger.atLevel(level)
            .addKeyValue("key", "value")
            .log("message");
        // END: io.clientcore.core.util.logging.clientlogger.atLevel

        // BEGIN: io.clientcore.core.util.logging.clientlogger.atverbose.addKeyValue#primitive
        logger.atVerbose()
            .addKeyValue("key", 1L)
            .log("A structured log message.");
        // END: io.clientcore.core.util.logging.clientlogger.atverbose.addKeyValue#primitive

        // BEGIN: io.clientcore.core.util.logging.loggingeventbuilder
        logger.atInfo()
            .addKeyValue("key1", "value1")
            .addKeyValue("key2", true)
            .addKeyValue("key3", this::getName)
            .log("A structured log message.");
        // END: io.clientcore.core.util.logging.loggingeventbuilder

        // BEGIN: io.clientcore.core.util.logging.clientlogger.atverbose.addKeyValue#object
        logger.atVerbose()
            // equivalent to addKeyValue("key", () -> new LoggableObject("string representation").toString()
            .addKeyValue("key", new LoggableObject("string representation"))
            .log("A structured log message.");
        // END: io.clientcore.core.util.logging.clientlogger.atverbose.addKeyValue#object
    }

    private Response<Void> getResponse() {
        return new Response<>(null, 200, null, null);
    }

    /**
     * Implementation not provided
     *
     * @return {@code null}
     */
    private File getFile() {
        return null;
    }

    /**
     * Implementation not provided
     *
     * @return {@code null}
     */
    private String getName() {
        return null;
    }

    /**
     * Implementation not provided
     *
     * @param resource A file resource
     * @throws IOException if upload fails
     */
    private void upload(File resource) throws IOException {
        throw new IOException();
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
