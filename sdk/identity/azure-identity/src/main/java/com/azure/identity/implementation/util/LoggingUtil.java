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
     * @param credential the credential class
     * @param logger the logger to output the log message
     * @param context the context of the getToken() request
     */
    public static void getTokenSuccess(Class<?> credential, ClientLogger logger, TokenRequestContext context) {
        logger.info("Azure Identity => getToken() result for {}: SUCCESS", credential.getSimpleName());
        logger.verbose("Azure Identity => Scopes: [{}]", String.join(", ", context.getScopes()));
    }

    /**
     * Log an error message for a getToken() call.
     * @param credential the credential class
     * @param logger the logger to output the log message
     * @param error the error thrown during getToken()
     */
    public static void getTokenError(Class<?> credential, ClientLogger logger, Throwable error) {
        logger.error("Azure Identity => ERROR in getToken() call for {}: {}",
            credential.getSimpleName(), error == null ? "" : error.getMessage());
    }

    private LoggingUtil() {
    }
}
