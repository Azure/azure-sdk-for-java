package com.azure.identity.implementation;

import com.azure.core.util.logging.LogLevel;

public class IdentityLogOptions {
    private LogLevel runtimeExceptionLogLevel;

    public IdentityLogOptions() {
        runtimeExceptionLogLevel = LogLevel.ERROR;
    }

    public IdentityLogOptions(boolean isUnderCredentialChainScope) {
        runtimeExceptionLogLevel = isUnderCredentialChainScope ? LogLevel.VERBOSE : LogLevel.ERROR;
    }

    public LogLevel getRuntimeExceptionLogLevel() {
        return runtimeExceptionLogLevel;
    }
}
