// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.handler;

import java.text.MessageFormat;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;
import java.util.Arrays;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.MissingResourceException;

/**
 * This is bridge/route all JUL log records to the SLF4J API.
 *
 * Essentially, the idea is to install on the root logger an instance of JUL2SLF4JHandler
 * as the sole JUL handler in the system.
 * Subsequently, the JUL2SLF4JHandler instance will redirect all JUL log records are redirected to
 * the SLF4J API based on the following mapping of levels:
 * FINEST -&gt; TRACE
 * FINER -&gt; DEBUG
 * FINE -&gt; DEBUG
 * INFO -&gt; INFO
 * WARNING -&gt; WARN
 * SEVERE -&gt; ERROR
 *
 * <p>Programmatic installation:</p>
 * Optionally remove existing handlers attached to j.u.l root logger
 * JUL2SLF4JHandler.removeHandlersForRootLogger()
 *
 * add JUL2SLF4JHandler to j.u.l's root logger, should be done once during
 * the initialization phase of your application
 * JUL2SLF4JHandler.install();
 *
 * <p>Installation via logging.properties configuration file:</p>
 * register JUL2SLF4JHandler as handler for the j.u.l. root logger
 * handlers = com.azure.core.handler.JUL2SLF4JHandler
 *
 * Once JUL2SLF4JHandler is installed, logging by j.u.l. loggers will be directed to SLF4J. Example:
 * import  java.util.logging.Logger;
 * ...
 * usual pattern: get a Logger and then log a message
 * Logger julLogger = Logger.getLogger("org.wombat");
 * julLogger.fine("hello world"); // this will get redirected to SLF4J
 *
 * Please note that translating a java.util.logging event into SLF4J incurs the cost of constructing LogRecord
 * instance regardless of whether the SLF4J logger is disabled for the given level.
 * Consequently, j.u.l. to SLF4J translation can seriously increase the cost of disabled logging statements
 * (60 fold or 6000% increase) and measurably impact the performance of enabled log statements (20% overall increase).
 * Please note that as of logback-version 0.9.25, it is possible to completely eliminate the 60 fold translation
 * overhead for disabled log statements with the help of <a href="http://logback.qos.ch/manual/configuration.html#LevelChangePropagator">LevelChangePropagator</a>.
 *
 */
public class JUL2SLF4JHandler extends java.util.logging.Handler {

    private static final int TRACE_LEVEL_THRESHOLD;
    private static final int DEBUG_LEVEL_THRESHOLD;
    private static final int INFO_LEVEL_THRESHOLD;
    private static final int WARN_LEVEL_THRESHOLD;

    private final Map<String, com.azure.core.util.logging.ClientLogger> clientLoggerMap = Collections.synchronizedMap(new HashMap<>());

    /**
     * Add JUL2SLF4JHandler to j.u.l's root logger, should be done once during the initialization phase of your application
     */
    public static void install() {
        java.util.logging.LogManager.getLogManager().getLogger("").addHandler(new JUL2SLF4JHandler());
    }

    /**
     * Get j.u.l's root logger
     */
    private static java.util.logging.Logger getRootLogger() {
        return java.util.logging.LogManager.getLogManager().getLogger("");
    }

    /**
     * Remove JUL2SLF4JHandler from j.u.l's root logger
     *
     * @throws SecurityException it is an error.
     */
    public static void uninstall() throws SecurityException {
        java.util.logging.Logger rootLogger = getRootLogger();
        java.util.logging.Handler[] handlers = rootLogger.getHandlers();
        Arrays.stream(handlers).parallel().forEach(handler -> {
            if (handler instanceof JUL2SLF4JHandler) {
                rootLogger.removeHandler(handler);
            }
        });
    }

    /**
     * Has been JUL2SLF4JHandler loaded into j.u.l's root logger
     *
     * @return Whether loaded the JUL2SLF4JHandler.
     * @throws SecurityException it is an error.
     */
    public static boolean isInstalled() throws SecurityException {
        java.util.logging.Logger rootLogger = getRootLogger();
        java.util.logging.Handler[] handlers = rootLogger.getHandlers();
        Optional<java.util.logging.Handler> optional =
            Arrays.stream(handlers).filter(handler -> handler instanceof JUL2SLF4JHandler).findFirst();
        if (optional.isPresent()) {
            return true;
        }
        return false;
    }

    /**
     * Remove any handler from j.u.l's root logger
     */
    public static void removeHandlersForRootLogger() {
        java.util.logging.Logger rootLogger = getRootLogger();
        java.util.logging.Handler[] handlers = rootLogger.getHandlers();
        Arrays.stream(handlers).parallel().forEach(handler -> rootLogger.removeHandler(handler));
    }

    /**
     * Create an empty JUL2SLF4JHandler instance.
     */
    public JUL2SLF4JHandler() {
    }

    /**
     * Close JUL2SLF4JHandler instance.
     */
    public void close() {
    }

    /**
     * Clear JUL2SLF4JHandler instance.
     */
    public void flush() {
    }

    /**
     * Redirect all JUL log records are redirected to the SLF4J
     *
     * @param loggerName the name of j.u.l logger instance
     * @return The passed {@link com.azure.core.util.logging.ClientLogger}.
     */
    protected com.azure.core.util.logging.ClientLogger getClientLogger(String loggerName) {
        if (loggerName == null) {
            loggerName = "unknown.jul.logger";
        }
        if (!clientLoggerMap.containsKey(loggerName)) {
            com.azure.core.util.logging.ClientLogger logger = new com.azure.core.util.logging.ClientLogger(loggerName);
            clientLoggerMap.put(loggerName, logger);
            return logger;
        }

        return clientLoggerMap.get(loggerName);
    }

    /**
     * Output j.u.l logger by SLF4J
     *
     * @param logger The passed {@link com.azure.core.util.logging.ClientLogger}
     * @param record The passed {@link java.util.logging.LogRecord}
     */
    protected void callPlainSLF4JLogger(com.azure.core.util.logging.ClientLogger logger, java.util.logging.LogRecord record) {
        String i18nMessage = this.getMessageI18N(record);
        Object[] redefineParameters = redefineParameters(record);
        int julLevelValue = record.getLevel().intValue();
        if (julLevelValue <= TRACE_LEVEL_THRESHOLD) {
            if (redefineParameters != null && redefineParameters.length > 0) {
                logger.verbose(i18nMessage, redefineParameters);
            } else {
                logger.verbose(i18nMessage);
            }
        } else if (julLevelValue <= DEBUG_LEVEL_THRESHOLD) {
            if (redefineParameters != null && redefineParameters.length > 0) {
                logger.verbose(i18nMessage, redefineParameters);
            } else {
                logger.verbose(i18nMessage);
            }
        } else if (julLevelValue <= INFO_LEVEL_THRESHOLD) {
            if (redefineParameters != null && redefineParameters.length > 0) {
                logger.info(i18nMessage, redefineParameters);
            } else {
                logger.info(i18nMessage);
            }
        } else if (julLevelValue <= WARN_LEVEL_THRESHOLD) {
            if (redefineParameters != null && redefineParameters.length > 0) {
                logger.warning(i18nMessage, redefineParameters);
            } else {
                logger.warning(i18nMessage);
            }
        } else {
            if (redefineParameters != null && redefineParameters.length > 0) {
                logger.error(i18nMessage, redefineParameters);
            } else {
                logger.error(i18nMessage);
            }
        }

    }

    /**
     * Redefine message parameters for SLF4J
     *
     * @param record The passed {@link java.util.logging.LogRecord}
     * @return Object array of message parameters
     */
    private Object[] redefineParameters(java.util.logging.LogRecord record) {
        Object[] originalParameters = record.getParameters();
        Throwable throwable = record.getThrown();
        if (throwable != null && throwable.getMessage() != null && !throwable.getMessage().isEmpty()) {
            Object[] redefineParameters = null;
            if (originalParameters != null && originalParameters.length > 0) {
                redefineParameters = new Object[originalParameters.length + 1];
                for (int i = 0; i < originalParameters.length; i++) {
                    redefineParameters[i] = originalParameters[i];
                }
            } else {
                redefineParameters = new Object[1];
            }
            redefineParameters[redefineParameters.length - 1] = throwable;

            return redefineParameters;
        }
        return originalParameters;
    }

    /**
     * Multi-language support
     *
     * @param record The passed {@link java.util.logging.LogRecord}
     * @return Multi-language support processing and formatted messages
     */
    private String getMessageI18N(java.util.logging.LogRecord record) {
        String message = record.getMessage();
        if (message == null) {
            return null;
        } else {
            ResourceBundle bundle = record.getResourceBundle();
            if (bundle != null) {
                try {
                    message = bundle.getString(message);
                } catch (MissingResourceException var7) {
                }
            }

            Object[] params = record.getParameters();
            if (params != null && params.length > 0) {
                try {
                    message = MessageFormat.format(message, params);
                } catch (IllegalArgumentException var6) {
                    return message;
                }
            }

            return message;
        }
    }

    /**
     * Publish log message
     *
     * @param record The passed {@link java.util.logging.LogRecord}
     */
    @Override
    public void publish(java.util.logging.LogRecord record) {
        if (record != null) {
            com.azure.core.util.logging.ClientLogger logger = this.getClientLogger(record.getLoggerName());
            String message = record.getMessage();
            if (message == null) {
                message = "";
            }
            this.callPlainSLF4JLogger(logger, record);
        }
    }

    static {
        TRACE_LEVEL_THRESHOLD = java.util.logging.Level.FINEST.intValue();
        DEBUG_LEVEL_THRESHOLD = java.util.logging.Level.FINE.intValue();
        INFO_LEVEL_THRESHOLD = java.util.logging.Level.INFO.intValue();
        WARN_LEVEL_THRESHOLD = java.util.logging.Level.WARNING.intValue();
    }

}
