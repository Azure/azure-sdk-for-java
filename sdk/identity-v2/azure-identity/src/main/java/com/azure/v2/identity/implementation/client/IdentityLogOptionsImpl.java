// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.identity.implementation.client;

import io.clientcore.core.instrumentation.logging.LogLevel;

/**
 * Represents Internal Identity Log Options.
 */
public class IdentityLogOptionsImpl {
    private final LogLevel runtimeExceptionLogLevel;
    private boolean allowAccountIdentifierLogs;

    /**
     * Creates instance of Identity Log Options.
     *
     * @param isUnderCredentialChainScope The boolean flag indicating whether credential is chained or not.
     */
    public IdentityLogOptionsImpl(boolean isUnderCredentialChainScope) {
        runtimeExceptionLogLevel = isUnderCredentialChainScope ? LogLevel.VERBOSE : LogLevel.ERROR;
    }

    /**
     * Gets the runtime exception log level.
     *
     * @return the runtime exception log level
     */
    public LogLevel getRuntimeExceptionLogLevel() {
        return runtimeExceptionLogLevel;
    }

    /**
     * Sets the boolean flag indicating whether Logging Account Identifiers is allowed or not.
     *
     * @param allowAccountIdentifierLogs the boolean flag to indicate logging account identifiers
     * @return the updated options
     */
    public IdentityLogOptionsImpl setLoggingAccountIdentifiersAllowed(boolean allowAccountIdentifierLogs) {
        this.allowAccountIdentifierLogs = allowAccountIdentifierLogs;
        return this;
    }

    /**
     * Checks whether account identifier logging is allowed or not.
     *
     * @return the boolean flag indicating whether account identifier logging is allowed or not.
     */
    public boolean isLoggingAccountIdentifiersAllowed() {
        return this.allowAccountIdentifierLogs;
    }
}
