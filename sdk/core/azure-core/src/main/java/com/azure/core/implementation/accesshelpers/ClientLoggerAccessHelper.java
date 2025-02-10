// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.implementation.accesshelpers;

import com.azure.core.util.logging.ClientLogger;
import org.slf4j.Logger;

import java.util.Map;

/**
 * This class is used to access the package-private methods of {@link ClientLogger}.
 */
public final class ClientLoggerAccessHelper {
    private static ClientLoggerAccessor accessor;

    /**
     * Interface defining the methods that access the package-private methods of {@link ClientLogger}.
     */
    public interface ClientLoggerAccessor {
        /**
         * Creates a new instance of {@link ClientLogger}.
         *
         * @param logger The {@link Logger} to use for logging.
         * @param globalContext The global context to include in log messages.
         * @return A new instance of {@link ClientLogger}.
         */
        ClientLogger createClientLogger(Logger logger, Map<String, Object> globalContext);
    }

    /**
     * Sets the accessor.
     *
     * @param accessor The accessor.
     */
    public static void setAccessor(ClientLoggerAccessor accessor) {
        ClientLoggerAccessHelper.accessor = accessor;
    }

    /**
     * Creates a new instance of {@link ClientLogger}.
     *
     * @param logger The {@link Logger} to use for logging.
     * @param globalContext The global context to include in log messages.
     * @return A new instance of {@link ClientLogger}.
     */
    public static ClientLogger createClientLogger(Logger logger, Map<String, Object> globalContext) {
        if (accessor == null) {
            new ClientLogger(ClientLoggerAccessHelper.class);
        }

        assert accessor != null;
        return accessor.createClientLogger(logger, globalContext);
    }

    private ClientLoggerAccessHelper() {
    }
}
