// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.v2.implementation.util;

import com.azure.identity.v2.CredentialUnavailableException;
import com.azure.v2.core.credentials.TokenRequestContext;
import com.azure.v2.core.utils.CoreUtils;
import io.clientcore.core.instrumentation.logging.ClientLogger;
import io.clientcore.core.instrumentation.logging.LogLevel;

import java.util.function.Supplier;

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
        logger.atLevel(LogLevel.VERBOSE).log(String.format("Azure Identity => getToken() result for scopes [%s]: SUCCESS",
            CoreUtils.stringJoin(", ", context.getScopes())));
    }

    /**
     * Log an error message for a getToken() call.
     * @param logger the logger to output the log message
     * @param context the context of the getToken() request
     * @param error the error thrown during getToken()
     */
    public static void logTokenError(ClientLogger logger, TokenRequestContext context,
                                     Throwable error) {
        logger.atLevel(LogLevel.ERROR).log(String.format("Azure Identity => ERROR in getToken() call for scopes [%s]: %s",
            CoreUtils.stringJoin(", ", context.getScopes()), error == null ? "" : error.getMessage()));
    }


    private LoggingUtil() {
    }

    public static CredentialUnavailableException logCredentialUnavailableException(ClientLogger logger, CredentialUnavailableException exception) {
        logger.logThrowableAsError(exception);
        return exception;
    }

    public static void logError(ClientLogger logger, String message) {
        logger.atLevel(LogLevel.ERROR).log(message);
    }

    public static void logError(ClientLogger logger, Supplier<String> messageSupplier) {
        logger.atLevel(LogLevel.ERROR).log(messageSupplier.get());
    }
}
