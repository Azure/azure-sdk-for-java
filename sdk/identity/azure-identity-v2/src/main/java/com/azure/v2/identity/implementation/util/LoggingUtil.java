// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.identity.implementation.util;

import com.azure.v2.identity.exceptions.CredentialUnavailableException;
import com.azure.v2.core.credentials.TokenRequestContext;
import io.clientcore.core.instrumentation.logging.ClientLogger;
import io.clientcore.core.instrumentation.logging.LogLevel;
import io.clientcore.core.utils.CoreUtils;
import io.clientcore.core.utils.configuration.Configuration;

import java.util.ArrayList;
import java.util.List;
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
        logger.atLevel(LogLevel.VERBOSE)
            .log(String.format("Azure Identity => SUCCESS getToken() result for scopes [%s]",
                CoreUtils.stringJoin(", ", context.getScopes())));
    }

    /**
     * Log an error message for a getToken() call.
     * @param logger the logger to output the log message
     * @param context the context of the getToken() request
     * @param error the error thrown during getToken()
     */
    public static void logTokenError(ClientLogger logger, TokenRequestContext context, Throwable error) {
        logger.atLevel(LogLevel.ERROR)
            .log(String.format("Azure Identity => ERROR in getToken() call for scopes [%s]: %s",
                CoreUtils.stringJoin(", ", context.getScopes()), error == null ? "" : error.getMessage()));
    }

    private LoggingUtil() {
    }

    /**
     * Logs {@link CredentialUnavailableException} as ERROR.
     *
     * @param logger the logger to be used for logging
     * @param exception the cred unavailable exception
     * @return the logged exception
     */
    public static CredentialUnavailableException logCredentialUnavailableException(ClientLogger logger,
        CredentialUnavailableException exception) {
        throw logger.logThrowableAsError(exception);
    }

    /**
     * Logs the message at ERROR level.
     *
     * @param logger the logger to be used for logging
     * @param message the error message to be logged
     */
    public static void logError(ClientLogger logger, String message) {
        logger.atLevel(LogLevel.ERROR).log(message);
    }

    /**
     * Logs the message at ERROR level.
     *
     * @param logger the logger to be used for logging
     * @param messageSupplier the error message supplier
     */
    public static void logError(ClientLogger logger, Supplier<String> messageSupplier) {
        logger.atLevel(LogLevel.ERROR).log(messageSupplier.get());
    }

    /**
     * Log the names of the currently available environment variables among a list of useful environment variables for
     * Azure Identity authentications.
     *
     * @param logger the logger to output the log message
     * @param configuration the configuration store
     */
    public static void logAvailableEnvironmentVariables(ClientLogger logger, Configuration configuration) {
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
        logger.atLevel(LogLevel.VERBOSE)
            .log(String.format("Azure Identity => Found the following environment variables: {}",
                String.join(", ", envVars)));
    }

}
