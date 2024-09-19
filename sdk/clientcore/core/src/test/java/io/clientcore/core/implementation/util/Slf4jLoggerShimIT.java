// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package io.clientcore.core.implementation.util;

import io.clientcore.core.util.ClientLogger;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration tests for {@link Slf4jLoggerShim} verifying the reflective invocation behaviors.
 */
public class Slf4jLoggerShimIT {
    @Test
    public void slf4jOnClasspathEnablesNonDefaultLogger() {
        Object logger = Slf4jLoggerShim.createLogger(Slf4jLoggerShimIT.class.getName());

        // Should return an instance of SLF4J Logger.
        assertNotNull(logger);
        assertInstanceOf(Logger.class, logger);
    }

    @Test
    public void slf4jLoggingLevelsAreInspectedCorrectly() {
        Slf4jLoggerShim shim = new Slf4jLoggerShim(Slf4jLoggerShimIT.class.getName());

        // All logging levels should be enabled for Slf4jLoggerShimIT based on the simplelogger.properties in resources.
        assertTrue(shim.canLogAtLevel(ClientLogger.LogLevel.VERBOSE));
        assertTrue(shim.canLogAtLevel(ClientLogger.LogLevel.INFORMATIONAL));
        assertTrue(shim.canLogAtLevel(ClientLogger.LogLevel.WARNING));
        assertTrue(shim.canLogAtLevel(ClientLogger.LogLevel.ERROR));

        shim = new Slf4jLoggerShim(Object.class);

        // But the default log level should be INFO for everything else.
        assertFalse(shim.canLogAtLevel(ClientLogger.LogLevel.VERBOSE));
        assertTrue(shim.canLogAtLevel(ClientLogger.LogLevel.INFORMATIONAL));
        assertTrue(shim.canLogAtLevel(ClientLogger.LogLevel.WARNING));
        assertTrue(shim.canLogAtLevel(ClientLogger.LogLevel.ERROR));
    }
}
