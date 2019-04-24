package com.azure.common.implementation;

import org.slf4j.Logger;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.function.Supplier;

/**
 * Handles logging for a service.
 */
public final class ServiceLogger {
    private final Logger logger;

    /**
     * Constructs a ServiceLogger.
     * @param logger Logger to handle logging.
     */
    public ServiceLogger(Logger logger) {
        this.logger = logger;
    }

    /**
     * Handles evaluation for runnables that could throw exceptions, when exceptions happen it logs them if able.
     * @param loggable Runnable that could throw an exception.
     */
    public void evaluateLoggable(Runnable loggable) {
        try {
            loggable.run();
        } catch (Throwable ex) {
            logException(ex);
            throw ex;
        }
    }

    /**
     * Handles evaluation for suppliers that could throw exceptions, when exceptions happen it logs them if able.
     * @param loggable Supplier that could throw an exception.
     * @param <T> Return type of the supplier
     * @return Result of the supplier.
     */
    public <T> T evaluateLoggable(Supplier<T> loggable) {
        try {
            return loggable.get();
        } catch (Throwable ex) {
            logException(ex);
            throw ex;
        }
    }

    /**
     * Logs an informational message is able.
     * @param message Message to log.
     */
    public void logInformational(String message) {
        if (logger.isInfoEnabled()) {
            logger.info(message);
        }
    }

    private void logException(Throwable ex) {
        if (logger.isDebugEnabled()) {
            StringWriter stringWriter = new StringWriter();
            PrintWriter printWriter = new PrintWriter(stringWriter);
            ex.printStackTrace(printWriter);
            logger.warn(ex.toString() + '\n' + printWriter.toString());
        } else if (logger.isWarnEnabled()) {
            logger.warn(ex.toString());
        }
    }
}
