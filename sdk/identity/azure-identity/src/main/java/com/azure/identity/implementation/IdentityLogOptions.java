// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.implementation;

import com.azure.core.util.logging.LogLevel;

public class IdentityLogOptions {
    private LogLevel runtimeExceptionLogLevel;

    public IdentityLogOptions() {
        this(false);
    }

    public IdentityLogOptions(boolean isUnderCredentialChainScope) {
        runtimeExceptionLogLevel = isUnderCredentialChainScope ? LogLevel.VERBOSE : LogLevel.ERROR;
    }

    public LogLevel getRuntimeExceptionLogLevel() {
        return runtimeExceptionLogLevel;
    }
}
