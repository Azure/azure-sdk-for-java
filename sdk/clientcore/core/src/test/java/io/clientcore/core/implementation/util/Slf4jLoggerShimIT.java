// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package io.clientcore.core.implementation.util;

import io.clientcore.core.util.ClientLogger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.api.parallel.Isolated;
import org.slf4j.Logger;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration tests for {@link Slf4jLoggerShim} verifying the reflective invocation behaviors.
 */
@Isolated("Mutates System.properties")
@Execution(ExecutionMode.SAME_THREAD)
public class Slf4jLoggerShimIT {
    private static final String LOG_LEVEL_PREFIX = "org.slf4j.simpleLogger.log.";
    private String slf4jLoggerShimITLogLevel;
    private String slf4jLoggerShimLogLevel;

    @BeforeEach
    public void setupLogLevels() {
        slf4jLoggerShimITLogLevel = System.setProperty(
            LOG_LEVEL_PREFIX + "io.clientcore.core.implementation.util.Slf4jLoggerShimIT", "debug");
        slf4jLoggerShimLogLevel = System.setProperty(
            LOG_LEVEL_PREFIX + "io.clientcore.core.implementation.util.Slf4jLoggerShim", "info");
    }

    @AfterEach
    public void resetLogLevels() {
        if (slf4jLoggerShimITLogLevel == null) {
            System.clearProperty(LOG_LEVEL_PREFIX + "io.clientcore.core.implementation.util.Slf4jLoggerShimIT");
        } else {
            System.setProperty(LOG_LEVEL_PREFIX + "io.clientcore.core.implementation.util.Slf4jLoggerShimIT",
                slf4jLoggerShimITLogLevel);
        }

        if (slf4jLoggerShimLogLevel == null) {
            System.clearProperty(LOG_LEVEL_PREFIX + "io.clientcore.core.implementation.util.Slf4jLoggerShim");
        } else {
            System.setProperty(LOG_LEVEL_PREFIX + "io.clientcore.core.implementation.util.Slf4jLoggerShim",
                slf4jLoggerShimLogLevel);
        }
    }

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

        shim = new Slf4jLoggerShim(Slf4jLoggerShim.class);

        // But the default log level should be INFO for everything else.
        assertFalse(shim.canLogAtLevel(ClientLogger.LogLevel.VERBOSE));
        assertTrue(shim.canLogAtLevel(ClientLogger.LogLevel.INFORMATIONAL));
        assertTrue(shim.canLogAtLevel(ClientLogger.LogLevel.WARNING));
        assertTrue(shim.canLogAtLevel(ClientLogger.LogLevel.ERROR));
    }
}
