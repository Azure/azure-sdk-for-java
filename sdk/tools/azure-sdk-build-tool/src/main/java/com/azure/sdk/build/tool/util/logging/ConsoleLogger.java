package com.azure.sdk.build.tool.util.logging;

/**
 * An implementation of {@link Logger} that logs messages to console.
 */
public class ConsoleLogger implements Logger {
    private static ConsoleLogger INSTANCE;

    public static Logger getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ConsoleLogger();
        }
        return INSTANCE;
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
