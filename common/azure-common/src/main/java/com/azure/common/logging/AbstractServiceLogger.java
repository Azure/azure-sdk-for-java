// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.common.logging;

public abstract class AbstractServiceLogger implements ServiceLoggerAPI {
    public abstract ServiceLoggerAPI asInformational();

    public abstract ServiceLoggerAPI asWarning();

    public abstract ServiceLoggerAPI asError();

    public abstract ServiceLoggerAPI asDebug();

    public abstract ServiceLoggerAPI asTrace();

    public abstract ServiceLoggerAPI withStackTrace(Throwable throwable);

    public abstract void log(String s);

    public abstract void log(String s, Object val);
}
