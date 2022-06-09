package com.azure.sdk.build.tool.util.logging;

import com.azure.sdk.build.tool.mojo.AzureSdkMojo;

/**
 * A simple logger interface to support enable logging for the Azure SDK build tool.
 */
public interface Logger {

    static Logger getInstance() {
        if (AzureSdkMojo.MOJO == null) {
            return ConsoleLogger.getInstance();
        } else {
            return MojoLogger.getInstance();
        }
    }

    /**
     * Logs message at info level.
     * @param msg The message to log.
     */
    void info(String msg);

    /**
     * Returns true if logging at warning level is enabled.
     * @return {@code true} if logging at warning level is enabled.
     */
    boolean isWarnEnabled();

    /**
     * Logs the message at warning level.
     * @param msg The message to log.
     */
    void warn(String msg);

    /**
     * Returns true if logging at error level is enabled.
     * @return {@code true} if logging at error level is enabled.
     */
    boolean isErrorEnabled();

    /**
     * Logs the message at error level.
     * @param msg The message to log.
     */
    void error(String msg);


    /**
     * Returns true if logging at verbose level is enabled.
     * @return {@code true} if logging at verbose level is enabled.
     */
    boolean isVerboseEnabled();

    /**
     * Logs the message at verbose level.
     * @param msg The message to log.
     */
    void verbose(String msg);
}
