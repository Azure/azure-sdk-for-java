// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.sdk.build.tool.util.logging;

import com.azure.sdk.build.tool.mojo.AzureSdkMojo;
import org.apache.maven.plugin.logging.Log;

/**
 * An implementation of {@link Logger} that uses the Maven plugin logger.
 */
public final class MojoLogger implements Logger {
    private static MojoLogger instance;
    private final Log mojoLog;

    /**
     * Returns the singleton instance of {@link MojoLogger}.
     * @return The singleton instance of {@link MojoLogger}.
     */
    public static Logger getInstance() {
        if (instance == null) {
            instance = new MojoLogger(AzureSdkMojo.getMojo().getLog());
        }
        return instance;
    }

    private MojoLogger(Log mojoLog) {
        this.mojoLog = mojoLog;
    }

    @Override
    public void info(String msg) {
        mojoLog.info(msg);
    }

    @Override
    public boolean isWarnEnabled() {
        return mojoLog.isWarnEnabled();
    }

    @Override
    public void warn(String msg) {
        mojoLog.warn(msg);
    }

    @Override
    public boolean isErrorEnabled() {
        return mojoLog.isErrorEnabled();
    }

    @Override
    public void error(String msg) {
        mojoLog.error(msg);
    }

    @Override
    public boolean isVerboseEnabled() {
        return mojoLog.isDebugEnabled();
    }

    @Override
    public void verbose(String msg) {
        mojoLog.debug(msg);
    }
}
