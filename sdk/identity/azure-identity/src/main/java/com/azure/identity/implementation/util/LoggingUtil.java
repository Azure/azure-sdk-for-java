// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.implementation.util;

import com.azure.core.credential.TokenRequestContext;
import com.azure.core.util.logging.ClientLogger;

/**
 * Utilities to handle logging for credentials.
 */
public final class LoggingUtil {
    /**
     * Log a success message for a getToken() call.
     * @param logger the logger to output the log message
     * @param context the context of the getToken() request
     */
    public static void logTokenSuccess(ClientLogger logger, TokenRequestContext context) {
        logger.info("Azure Identity => getToken() result for scopes [{}]: SUCCESS",
                String.join(", ", context.getScopes()));
    }

    /**
     * Log an error message for a getToken() call.
     * @param logger the logger to output the log message
     * @param error the error thrown during getToken()
     */
    public static void logTokenError(ClientLogger logger, TokenRequestContext context, Throwable error) {
        logger.error("Azure Identity => ERROR in getToken() call for scopes [{}]: {}",
                String.join(", ", context.getScopes()), error == null ? "" : error.getMessage());
    }

    private LoggingUtil() {
    }
}
