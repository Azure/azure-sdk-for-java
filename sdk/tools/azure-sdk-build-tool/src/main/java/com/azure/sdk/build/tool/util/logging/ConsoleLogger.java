// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.sdk.build.tool.util.logging;

/**
 * An implementation of {@link Logger} that logs messages to console.
 */
public class ConsoleLogger implements Logger {
    private static ConsoleLogger instance;

    /**
     * Returns the singleton instance of {@link ConsoleLogger}.
     * @return The singleton instance of {@link ConsoleLogger}.
     */
    public static Logger getInstance() {
        if (instance == null) {
            instance = new ConsoleLogger();
        }
        return instance;
    }

    @Override
    public void info(String msg) {
        System.out.println(msg);
    }

    @Override
    public boolean isWarnEnabled() {
        return true;
    }

    @Override
    public void warn(String msg) {
        System.err.println(msg);
    }

    @Override
    public boolean isErrorEnabled() {
        return true;
    }

    @Override
    public void error(String msg) {
        System.err.println(msg);
    }

    @Override
    public boolean isVerboseEnabled() {
        return false;
    }

    @Override
    public void verbose(String msg) {
        System.out.println(msg);
    }
}
