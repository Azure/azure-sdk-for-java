/**
 * Copyright Microsoft Corporation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.microsoft.azure.storage;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.slf4j.LoggerFactory;

import com.microsoft.azure.storage.TestRunners.CloudTests;
import com.microsoft.azure.storage.TestRunners.DevFabricTests;
import com.microsoft.azure.storage.TestRunners.DevStoreTests;
import com.microsoft.azure.storage.blob.BlobTestHelper;
import com.microsoft.azure.storage.blob.BlobType;
import com.microsoft.azure.storage.blob.CloudBlob;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.core.LogConstants;
import com.microsoft.azure.storage.core.Logger;

/*
 * If you'd like to use a different slf4j binding and/or not use Maven, you will need to add a different class path
 * dependency and set the properties for it accordingly. The dependency and the properties file will need to be put in
 * the appropriate locations for the logger implementation chosen.
 * 
 * The log implemention/properties must:
 * 1. Set the default/root log level to all.
 * 2. Set the log level for the logger "limited" to error only.
 * 3. Direct both loggers to log to System.err which these tests will redirect to a custom stream.
 * 
 * Then, you will need to modify the readAndCompareOutput method to parse the logs entries accordingly.
 */
@Category({ DevFabricTests.class, DevStoreTests.class, CloudTests.class })
public class LoggerTests {

    private final static String TRACE = "TRACE";
    private final static String DEBUG = "DEBUG";
    private final static String INFO = "INFO";
    private final static String WARN = "WARN";
    private final static String ERROR = "ERROR";

    private final static String ARG0 = "Test string";
    private final static String ARG1 = "Test string: arg1 = %s";
    private final static String ARG2 = "Test string: arg1 = %s; arg2 = %s";
    private final static String ARG3 = "Test string: arg1 = %s; arg2 = %s; arg3 = %s";

    private final static String ARG1_VAL = "arg1_val";
    private final static String ARG2_VAL1 = "arg2_val1";
    private final static String ARG2_VAL2 = "arg2_val2";
    private final static Object[] ARG3_VAL = { "arg3_val1", "arg3_val2", "arg3_val3" };

    private final static String LIMITED_LOGGER_NAME = "limited";

    private final static PrintStream old = System.err;
    private static ByteArrayOutputStream baos;

    @BeforeClass
    public synchronized static void loggerTestsClassSetup() {
        // redirect the error stream to a custom print stream
        baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        System.setErr(ps);
    }

    @AfterClass
    public synchronized static void loggerTestsClassTearDown() {
        // flush the current error stream
        System.err.flush();

        // reset the error stream to what it was originally
        System.setErr(old);
    }

    @Before
    public synchronized void loggerTestsMethodSetup() {
        // set default logging to off
        OperationContext.setLoggingEnabledByDefault(false);

        // clear the output stream
        baos.reset();
    }

    @Test
    public synchronized void testInheritedLogLevel() throws IOException {
        // in the configs file, the default logger logs all levels
        OperationContext.setLoggingEnabledByDefault(true);
        OperationContext ctx = new OperationContext();

        writeTraceLogs(ctx);
        readAndCompareOutput(TRACE, OperationContext.defaultLoggerName, ctx.getClientRequestID());

        writeDebugLogs(ctx);
        readAndCompareOutput(DEBUG, OperationContext.defaultLoggerName, ctx.getClientRequestID());

        writeInfoLogs(ctx);
        readAndCompareOutput(INFO, OperationContext.defaultLoggerName, ctx.getClientRequestID());

        writeWarnLogs(ctx);
        readAndCompareOutput(WARN, OperationContext.defaultLoggerName, ctx.getClientRequestID());

        writeErrorLogs(ctx);
        readAndCompareOutput(ERROR, OperationContext.defaultLoggerName, ctx.getClientRequestID());

        // in the configs file, the limited logger logs only errors
        ctx.setLogger(LoggerFactory.getLogger(LIMITED_LOGGER_NAME));

        writeTraceLogs(ctx);
        assertEquals(0, baos.toString().length());

        writeDebugLogs(ctx);
        assertEquals(0, baos.toString().length());

        writeInfoLogs(ctx);
        assertEquals(0, baos.toString().length());

        writeWarnLogs(ctx);
        assertEquals(0, baos.toString().length());

        writeErrorLogs(ctx);
        readAndCompareOutput(ERROR, LIMITED_LOGGER_NAME, ctx.getClientRequestID());
    }

    @Test
    public synchronized void testDefaultLogging() throws IOException {
        // doesn't write logs by default
        OperationContext ctx = new OperationContext();
        writeErrorLogs(ctx);
        assertEquals(0, baos.toString().length());

        // set logging on by default and make sure a previous created context now logs
        OperationContext.setLoggingEnabledByDefault(true);
        writeErrorLogs(ctx);
        readAndCompareOutput(ERROR, OperationContext.defaultLoggerName, ctx.getClientRequestID());

        // with logging set on by default, make sure a new context starts logging
        OperationContext ctx2 = new OperationContext();
        writeErrorLogs(ctx2);
        readAndCompareOutput(ERROR, OperationContext.defaultLoggerName, ctx2.getClientRequestID());

        // set logging off by default and make sure a previously created context stops logging
        OperationContext.setLoggingEnabledByDefault(false);
        writeErrorLogs(ctx);
        assertEquals(0, baos.toString().length());

        // with logging set off by default, make sure a new context doesn't logging
        ctx2 = new OperationContext();
        writeErrorLogs(ctx2);
        assertEquals(0, baos.toString().length());
    }

    @Test
    public synchronized void testRequestLevelLogging() throws IOException {
        // enabling for an individual request works with default logger
        OperationContext ctx = new OperationContext();
        ctx.setLoggingEnabled(true);
        writeErrorLogs(ctx);
        readAndCompareOutput(ERROR, OperationContext.defaultLoggerName, ctx.getClientRequestID());

        // enabling for a previous request doesn't enable for newly created contexts
        OperationContext ctx2 = new OperationContext();
        writeErrorLogs(ctx2);
        assertEquals(0, baos.toString().length());

        // enabling for an individual request works with default logger
        ctx.setLogger(LoggerFactory.getLogger(LIMITED_LOGGER_NAME));
        writeErrorLogs(ctx);
        readAndCompareOutput(ERROR, LIMITED_LOGGER_NAME, ctx.getClientRequestID());

        // changing logger for one request doesn't change logger for old request
        ctx2.setLoggingEnabled(true);
        writeErrorLogs(ctx2);
        readAndCompareOutput(ERROR, OperationContext.defaultLoggerName, ctx2.getClientRequestID());

        // changing logger for one request doen't change logger for new requests
        ctx2 = new OperationContext();
        ctx2.setLoggingEnabled(true);
        writeErrorLogs(ctx2);
        readAndCompareOutput(ERROR, OperationContext.defaultLoggerName, ctx2.getClientRequestID());

        // turning logging off for a context doesn't change it's logger and doesn't log
        ctx.setLoggingEnabled(false);
        assertEquals(LIMITED_LOGGER_NAME, ctx.getLogger().getName());
        writeErrorLogs(ctx);
        assertEquals(0, baos.toString().length());

        // turning logging on overall and off for a request works
        OperationContext.setLoggingEnabledByDefault(true);
        ctx.setLoggingEnabled(false);
        writeErrorLogs(ctx);
        assertEquals(0, baos.toString().length());
    }

    @Test
    public synchronized void testTrace() throws IOException {
        OperationContext ctx = new OperationContext();
        ctx.setLoggingEnabled(true);
        writeTraceLogs(ctx);
        readAndCompareOutput(TRACE, OperationContext.defaultLoggerName, ctx.getClientRequestID());
    }

    @Test
    public synchronized void testDebug() throws IOException {
        OperationContext ctx = new OperationContext();
        ctx.setLoggingEnabled(true);
        writeDebugLogs(ctx);
        readAndCompareOutput(DEBUG, OperationContext.defaultLoggerName, ctx.getClientRequestID());
    }

    @Test
    public synchronized void testInfo() throws IOException {
        OperationContext ctx = new OperationContext();
        ctx.setLoggingEnabled(true);
        writeInfoLogs(ctx);
        readAndCompareOutput(INFO, OperationContext.defaultLoggerName, ctx.getClientRequestID());
    }

    @Test
    public synchronized void testWarn() throws IOException {
        OperationContext ctx = new OperationContext();
        ctx.setLoggingEnabled(true);
        writeWarnLogs(ctx);
        readAndCompareOutput(WARN, OperationContext.defaultLoggerName, ctx.getClientRequestID());
    }

    @Test
    public synchronized void testError() throws IOException {
        OperationContext ctx = new OperationContext();
        ctx.setLoggingEnabled(true);
        writeErrorLogs(ctx);
        readAndCompareOutput(ERROR, OperationContext.defaultLoggerName, ctx.getClientRequestID());
    }

    @Test
    public synchronized void testStringToSign()
            throws IOException, InvalidKeyException, StorageException, URISyntaxException {
        
        OperationContext.setLoggingEnabledByDefault(true);
        final CloudBlobContainer cont = BlobTestHelper.getRandomContainerReference();
        
        try {
            cont.create();
            final CloudBlob blob = BlobTestHelper.uploadNewBlob(cont, BlobType.BLOCK_BLOB, "", 0, null);
            
            // Test logging for SAS access
            baos.reset();
            blob.generateSharedAccessSignature(null, null);
            baos.flush();
    
            String log = baos.toString();
            String[] logEntries = log.split("[\\r\\n]+");
    
            assertEquals(1, logEntries.length);
            
            // example log entry: TRACE ROOT - {0b902691-1a8e-41da-ab60-5b912df186a6}: {Test string}
            String[] segment = logEntries[0].split("\\{");
            assertEquals(3, segment.length);
            assertTrue(segment[1].startsWith("*"));
            assertTrue(segment[2].startsWith(String.format(LogConstants.SIGNING, Constants.EMPTY_STRING)));
            baos.reset();
    
            // Test logging for Shared Key access
            OperationContext ctx = new OperationContext();
            blob.downloadAttributes(null, null, ctx);
            
            baos.flush();
            log = baos.toString();
            logEntries = log.split("[\\r\\n]+");
            assertNotEquals(0, logEntries.length);
            
            // Select correct log entry
            for (int n = 0; n < logEntries.length; n++) {
                if (logEntries[n].startsWith(LoggerTests.TRACE)) {
                    segment = logEntries[n].split("\\{");
                    assertEquals(3, segment.length);
                    assertTrue(segment[1].startsWith(ctx.getClientRequestID()));
                    assertTrue(segment[2].startsWith(String.format(LogConstants.SIGNING, Constants.EMPTY_STRING)));
                    return;
                }
            }
            
            // If this line is reached then the log entry was not found
            fail();
        }
        finally {
            cont.deleteIfExists();
        }
    }

    private void writeTraceLogs(OperationContext ctx) {
        Logger.trace(ctx, ARG0);
        Logger.trace(ctx, ARG1, ARG1_VAL);
        Logger.trace(ctx, ARG2, ARG2_VAL1, ARG2_VAL2);
        Logger.trace(ctx, ARG3, ARG3_VAL);
    }

    private void writeDebugLogs(OperationContext ctx) {
        Logger.debug(ctx, ARG0);
        Logger.debug(ctx, ARG1, ARG1_VAL);
        Logger.debug(ctx, ARG2, ARG2_VAL1, ARG2_VAL2);
        Logger.debug(ctx, ARG3, ARG3_VAL);
    }

    private void writeInfoLogs(OperationContext ctx) {
        Logger.info(ctx, ARG0);
        Logger.info(ctx, ARG1, ARG1_VAL);
        Logger.info(ctx, ARG2, ARG2_VAL1, ARG2_VAL2);
        Logger.info(ctx, ARG3, ARG3_VAL);
    }

    private void writeWarnLogs(OperationContext ctx) {
        Logger.warn(ctx, ARG0);
        Logger.warn(ctx, ARG1, ARG1_VAL);
        Logger.warn(ctx, ARG2, ARG2_VAL1, ARG2_VAL2);
        Logger.warn(ctx, ARG3, ARG3_VAL);
    }

    private void writeErrorLogs(OperationContext ctx) {
        Logger.error(ctx, ARG0);
        Logger.error(ctx, ARG1, ARG1_VAL);
        Logger.error(ctx, ARG2, ARG2_VAL1, ARG2_VAL2);
        Logger.error(ctx, ARG3, ARG3_VAL);
    }

    private void readAndCompareOutput(final String logLevel, final String loggerName, final String id)
            throws IOException {
        baos.flush();

        String log = baos.toString();
        String[] logEntries = log.split("[\\r\\n]+");

        assertEquals(4, logEntries.length);

        // example log entry: DEBUG ROOT - {0b902691-1a8e-41da-ab60-5b912df186a6}: {Test string}       
        String format = String.format("%s %s - {%s}: {%s}", logLevel, loggerName, id, "%s");
        String log0 = ARG0;
        String log1 = String.format(ARG1, ARG1_VAL);
        String log2 = String.format(ARG2, ARG2_VAL1, ARG2_VAL2);
        String log3 = String.format(ARG3, ARG3_VAL);

        assertEquals(String.format(format, log0), logEntries[0]);
        assertEquals(String.format(format, log1), logEntries[1]);
        assertEquals(String.format(format, log2), logEntries[2]);
        assertEquals(String.format(format, log3), logEntries[3]);

        baos.reset();
    }
}