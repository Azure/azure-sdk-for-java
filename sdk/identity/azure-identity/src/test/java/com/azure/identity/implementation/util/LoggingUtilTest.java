// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.implementation.util;

import com.azure.core.credential.TokenRequestContext;
import com.azure.core.exception.ClientAuthenticationException;
import com.azure.core.util.Configuration;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.logging.LogLevel;
import com.azure.identity.implementation.IdentityClientOptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LoggingUtilTest {

    private static final ClientLogger LOGGER = new ClientLogger(LoggingUtilTest.class);

    @Test
    public void testLogTokenErrorWithInnerException() {
        // Create a nested exception to simulate the "see inner exception" scenario
        Exception innerException = new RuntimeException("Inner exception details");
        ClientAuthenticationException outerException = new ClientAuthenticationException(
            "Managed Identity authentication failed, see inner exception for more information.", null, innerException);

        // Create a token request context
        TokenRequestContext context = new TokenRequestContext().addScopes("https://management.azure.com/.default");

        // Create identity client options
        IdentityClientOptions options = new IdentityClientOptions();

        // Verify that calling logTokenError doesn't throw an exception
        // and that it properly handles the nested exception
        assertDoesNotThrow(() -> LoggingUtil.logTokenError(LOGGER, options, context, outerException));
    }

    @Test
    public void testLogTokenErrorWithNullException() {
        // Test that the method handles null exceptions gracefully
        TokenRequestContext context = new TokenRequestContext().addScopes("https://management.azure.com/.default");
        IdentityClientOptions options = new IdentityClientOptions();

        assertDoesNotThrow(() -> LoggingUtil.logTokenError(LOGGER, options, context, null));
    }

    @Test
    public void testAuthenticationFailureIsLoggedAtConfiguredErrorLevel() {
        IdentityClientOptions options = new IdentityClientOptions();
        ClientAuthenticationException failure = new ClientAuthenticationException("Invalid client secret.", null);

        assertEquals(options.getIdentityLogOptionsImpl().getRuntimeExceptionLogLevel(),
            LoggingUtil.tokenErrorLogLevel(options, failure));
        assertEquals(LogLevel.ERROR, LoggingUtil.tokenErrorLogLevel(options, failure));
        assertFalse(IdentityUtil.isInterruption(failure));
    }

    @Test
    public void testInterruptionIsLoggedAtVerboseLevel() {
        IdentityClientOptions options = new IdentityClientOptions();
        // What getTokenSync sees when the calling thread is interrupted while waiting for the MSAL future.
        RuntimeException interruption = new RuntimeException("The token request was interrupted before it completed.",
            new InterruptedException());
        TokenRequestContext context = new TokenRequestContext().addScopes("https://graph.microsoft.com/.default");

        assertTrue(IdentityUtil.isInterruption(interruption));
        assertEquals(LogLevel.VERBOSE, LoggingUtil.tokenErrorLogLevel(options, interruption));
        assertDoesNotThrow(() -> LoggingUtil.logTokenError(LOGGER, options, context, interruption));
    }

    @Test
    public void testNestedInterruptionIsLoggedAtVerboseLevel() {
        IdentityClientOptions options = new IdentityClientOptions();
        // ChainedTokenCredential and friends wrap the credential's exception one more time.
        ClientAuthenticationException wrapped = new ClientAuthenticationException("Tried all credentials.", null,
            new RuntimeException("The token request was interrupted before it completed.", new InterruptedException()));

        assertTrue(IdentityUtil.isInterruption(wrapped));
        assertEquals(LogLevel.VERBOSE, LoggingUtil.tokenErrorLogLevel(options, wrapped));
    }

    @Test
    public void testJvmShutdownSignalIsLoggedAtVerboseLevel() {
        IdentityClientOptions options = new IdentityClientOptions();
        // What Runtime.addShutdownHook raises once JVM shutdown has begun, surfaced through azure-core's shared
        // executor and MSAL as the token request's failure.
        RuntimeException shutdown
            = new RuntimeException("MSAL request failed", new IllegalStateException("Shutdown in progress"));
        TokenRequestContext context = new TokenRequestContext().addScopes("https://graph.microsoft.com/.default");

        assertTrue(IdentityUtil.isShutdownSignal(shutdown));
        assertFalse(IdentityUtil.isInterruption(shutdown));
        assertEquals(LogLevel.VERBOSE, LoggingUtil.tokenErrorLogLevel(options, shutdown));
        assertDoesNotThrow(() -> LoggingUtil.logTokenError(LOGGER, options, context, shutdown));
    }

    @Test
    public void testOtherIllegalStateIsStillLoggedAtErrorLevel() {
        IdentityClientOptions options = new IdentityClientOptions();
        IllegalStateException failure = new IllegalStateException("Received token is close to expiry.");

        assertFalse(IdentityUtil.isShutdownSignal(failure));
        assertEquals(LogLevel.ERROR, LoggingUtil.tokenErrorLogLevel(options, failure));
    }

    @Test
    @Timeout(10)
    public void testCausalTraversalTerminatesOnCyclicCauses() {
        // initCause allows a chain to loop back on itself; walking it must still terminate.
        RuntimeException a = new RuntimeException("a");
        RuntimeException b = new RuntimeException("b", a);
        a.initCause(b);

        assertFalse(IdentityUtil.isInterruption(a));
        assertFalse(IdentityUtil.isShutdownSignal(a));
        assertFalse(IdentityUtil.isInterruption(null));
        assertFalse(IdentityUtil.isShutdownSignal(null));
    }

    @Test
    @Timeout(10)
    public void testCausalTraversalFindsACauseInACyclicChain() {
        RuntimeException a = new RuntimeException("a");
        RuntimeException b = new RuntimeException("b", a);
        a.initCause(b);
        RuntimeException head = new RuntimeException("head", new RuntimeException("mid", new InterruptedException()));

        assertTrue(IdentityUtil.isInterruption(head));
        // A deep chain is followed to its end rather than to a fixed depth.
        Throwable deep = new InterruptedException();
        for (int i = 0; i < 64; i++) {
            deep = new RuntimeException("layer " + i, deep);
        }
        assertTrue(IdentityUtil.isInterruption(deep));
        assertTrue(IdentityUtil.isShutdownSignal(deep));
    }

    @Test
    public void testLogTokenSuccess() {
        TokenRequestContext context = new TokenRequestContext().addScopes("https://management.azure.com/.default");

        assertDoesNotThrow(() -> LoggingUtil.logTokenSuccess(LOGGER, context));
    }

    @Test
    public void testLogAvailableEnvironmentVariables() {
        Configuration configuration = Configuration.getGlobalConfiguration();

        assertDoesNotThrow(() -> LoggingUtil.logAvailableEnvironmentVariables(LOGGER, configuration));
    }
}
