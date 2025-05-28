// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.identity.implementation.util;

import com.azure.v2.core.credentials.TokenRequestContext;
import io.clientcore.core.instrumentation.logging.ClientLogger;
import io.clientcore.core.instrumentation.logging.LogLevel;
import io.clientcore.core.utils.CoreUtils;
import io.clientcore.core.utils.configuration.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

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
        logger.atVerbose()
            .addKeyValue("scopes", () -> CoreUtils.stringJoin(", ", context.getScopes()))
            .addKeyValue("event.name", "azure.identity.get_token.success")
            .log();
    }

    /**
     * Log an error message for a getToken() call.
     * @param logger the logger to output the log message
     * @param context the context of the getToken() request
     * @param cause the error thrown during getToken()
     * @param factory a factory to create the exception to be thrown
     * @param <T> the type of the exception to be thrown
     * @return the exception to be thrown
     */
    public static <T extends Throwable> T logAndThrowTokenError(ClientLogger logger, TokenRequestContext context,
        Throwable cause, BiFunction<String, Throwable, T> factory) {
        return logAndThrowTokenError(logger, cause.getMessage(), context, cause, factory);
    }

    /**
     * Log an error message for a getToken() call.
     * @param logger the logger to output the log message
     * @param message the short message
     * @param context the context of the getToken() request
     * @param cause the error thrown during getToken()
     * @param factory a factory to create the exception to be thrown
     * @param <T> the type of the exception to be thrown
     * @return the exception to be thrown
     */
    public static <T extends Throwable> T logAndThrowTokenError(ClientLogger logger, String message,
        TokenRequestContext context, Throwable cause, BiFunction<String, Throwable, T> factory) {
        return logger.throwableAtError()
            .addKeyValue("scopes", CoreUtils.stringJoin(", ", context.getScopes()))
            .addKeyValue("event.name", "azure.identity.get_token.error")
            .log(message, cause, factory);
    }

    private LoggingUtil() {
    }

    /**
     * Log the names of the currently available environment variables among a list of useful environment variables for
     * Azure Identity authentications.
     *
     * @param logger the logger to output the log message
     * @param configuration the configuration store
     */
    public static void logAvailableEnvironmentVariables(ClientLogger logger, Configuration configuration) {
        if (!logger.canLogAtLevel(LogLevel.VERBOSE)) {
            return;
        }

        String clientId = configuration.get(IdentityUtil.PROPERTY_AZURE_CLIENT_ID);
        String tenantId = configuration.get(IdentityUtil.PROPERTY_AZURE_TENANT_ID);
        String clientSecret = configuration.get(IdentityUtil.PROPERTY_AZURE_CLIENT_SECRET);
        String certPath = configuration.get(IdentityUtil.PROPERTY_AZURE_CLIENT_CERTIFICATE_PATH);
        List<String> envVars = new ArrayList<>();
        if (clientId != null) {
            envVars.add(IdentityUtil.PROPERTY_AZURE_CLIENT_ID);
        }
        if (tenantId != null) {
            envVars.add(IdentityUtil.PROPERTY_AZURE_TENANT_ID);
        }
        if (clientSecret != null) {
            envVars.add(IdentityUtil.PROPERTY_AZURE_CLIENT_SECRET);
        }
        if (certPath != null) {
            envVars.add(IdentityUtil.PROPERTY_AZURE_CLIENT_CERTIFICATE_PATH);
        }
        logger.atVerbose()
            .addKeyValue("providedEnvVars", String.join(", ", envVars))
            .log("Azure Identity => Environment variables");
    }

}
