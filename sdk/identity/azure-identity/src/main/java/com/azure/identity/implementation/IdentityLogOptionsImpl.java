// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.implementation;

import com.azure.core.util.logging.LogLevel;

public class IdentityLogOptionsImpl {
    private LogLevel runtimeExceptionLogLevel;
    private boolean allowAccountIdentifierLogs;

    public IdentityLogOptionsImpl() {
        this(false);
    }

    public IdentityLogOptionsImpl(boolean isUnderCredentialChainScope) {
        runtimeExceptionLogLevel = isUnderCredentialChainScope ? LogLevel.VERBOSE : LogLevel.ERROR;
    }

    public LogLevel getRuntimeExceptionLogLevel() {
        return runtimeExceptionLogLevel;
    }

    public IdentityLogOptionsImpl setLoggingAccountIdentifiersAllowed(boolean allowAccountIdentifierLogs) {
        this.allowAccountIdentifierLogs = allowAccountIdentifierLogs;
        return this;
    }

    public boolean isLoggingAccountIdentifiersAllowed() {
        return this.allowAccountIdentifierLogs;
    }
}
