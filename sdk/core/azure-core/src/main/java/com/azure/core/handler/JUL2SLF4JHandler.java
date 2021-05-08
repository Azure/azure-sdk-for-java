// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.handler;

import com.azure.core.util.logging.ClientLogger;
import org.slf4j.Marker;
import org.slf4j.spi.LocationAwareLogger;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.MissingResourceException;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Handler;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Ths is bridge/route all JUL log records to the SLF4J API.
 *
 * Essentially, the idea is to install on the root logger an instance of JUL2SLF4JHandler
 * as the sole JUL handler in the system.
 * Subsequently, the JUL2SLF4JHandler instance will redirect all JUL log records are redirected to
 * the SLF4J API based on the following mapping of levels:
 * FINEST -> TRACE
 * FINER -> DEBUG
 * FINE -> DEBUG
 * INFO -> INFO
 * WARNING -> WARN
 * SEVERE -> ERROR
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
 * overhead for disabled log statements with the help of <a link="http://logback.qos.ch/manual/configuration.html#LevelChangePropagator">LevelChangePropagator</a>.
 *
 */
public class JUL2SLF4JHandler extends Handler {

    private static final String FQCN = Logger.class.getName();
    private static final int TRACE_LEVEL_THRESHOLD;
    private static final int DEBUG_LEVEL_THRESHOLD;
    private static final int INFO_LEVEL_THRESHOLD;
    private static final int WARN_LEVEL_THRESHOLD;

    private final ConcurrentHashMap<String, ClientLogger> concurrentHashMap = new ConcurrentHashMap<String, ClientLogger>();

    /**
     * Add JUL2SLF4JHandler to j.u.l's root logger, should be done once during the initialization phase of your application
     */
    public static void install() {
        LogManager.getLogManager().getLogger("").addHandler(new JUL2SLF4JHandler());
    }

    /**
     * Get j.u.l's root logger
     */
    private static Logger getRootLogger() {
        return LogManager.getLogManager().getLogger("");
    }

    /**
     * Remove JUL2SLF4JHandler from j.u.l's root logger
     */
    public static void uninstall() throws SecurityException {
        Logger rootLogger = getRootLogger();
        Handler[] handlers = rootLogger.getHandlers();
        Arrays.stream(handlers).parallel().forEach(handler -> {
            if (handler instanceof JUL2SLF4JHandler) {
                rootLogger.removeHandler(handler);
            }
        });
    }

    /**
     * Has JUL2SLF4JHandler loaded into j.u.l's root logger
     */
    public static boolean isInstalled() throws SecurityException {
        Logger rootLogger = getRootLogger();
        Handler[] handlers = rootLogger.getHandlers();
        Optional<Handler> optional =
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
        Logger rootLogger = getRootLogger();
        Handler[] handlers = rootLogger.getHandlers();
        Arrays.stream(handlers).parallel().forEach(handler -> rootLogger.removeHandler(handler));
    }

    public JUL2SLF4JHandler() {
    }

    public void close() {
    }

    public void flush() {
    }

    /**
     *  Redirect all JUL log records are redirected to the SLF4J
     */
    protected ClientLogger getClientLogger(LogRecord record) {
        String name = record.getLoggerName();
        if (name == null) {
            name = "unknown.jul.logger";
        }
        if (!concurrentHashMap.containsKey(name)) {
            concurrentHashMap.put(name, new ClientLogger(name));
        }

        return concurrentHashMap.get(name);
    }

    /**
     *  Print j.u.l logger by SLF4J
     */
    protected void callLocationAwareLogger(LocationAwareLogger lal, LogRecord record) {
        int julLevelValue = record.getLevel().intValue();
        byte slf4jLevel;
        if (julLevelValue <= TRACE_LEVEL_THRESHOLD) {
            slf4jLevel = 0;
        } else if (julLevelValue <= DEBUG_LEVEL_THRESHOLD) {
            slf4jLevel = 10;
        } else if (julLevelValue <= INFO_LEVEL_THRESHOLD) {
            slf4jLevel = 20;
        } else if (julLevelValue <= WARN_LEVEL_THRESHOLD) {
            slf4jLevel = 30;
        } else {
            slf4jLevel = 40;
        }

        String i18nMessage = this.getMessageI18N(record);
        lal.log((Marker)null, FQCN, slf4jLevel, i18nMessage, redefineParameters(record), record.getThrown());
    }

    /**
     *  Output j.u.l logger by SLF4J
     */
    protected void callPlainSLF4JLogger(ClientLogger clientLogger, LogRecord record) {
        String i18nMessage = this.getMessageI18N(record);
        Object[] redefineParameters = redefineParameters(record);
        int julLevelValue = record.getLevel().intValue();
        if (julLevelValue <= TRACE_LEVEL_THRESHOLD) {
            if (redefineParameters != null && redefineParameters.length > 0) {
                clientLogger.verbose(i18nMessage, redefineParameters);
            } else {
                clientLogger.verbose(i18nMessage);
            }
        } else if (julLevelValue <= DEBUG_LEVEL_THRESHOLD) {
            if (redefineParameters != null && redefineParameters.length > 0) {
                clientLogger.verbose(i18nMessage, redefineParameters);
            } else {
                clientLogger.verbose(i18nMessage);
            }
        } else if (julLevelValue <= INFO_LEVEL_THRESHOLD) {
            if (redefineParameters != null && redefineParameters.length > 0) {
                clientLogger.info(i18nMessage, redefineParameters);
            } else {
                clientLogger.info(i18nMessage);
            }
        } else if (julLevelValue <= WARN_LEVEL_THRESHOLD) {
            if (redefineParameters != null && redefineParameters.length > 0) {
                clientLogger.warning(i18nMessage, redefineParameters);
            } else {
                clientLogger.warning(i18nMessage);
            }
        } else {
            if (redefineParameters != null && redefineParameters.length > 0) {
                clientLogger.error(i18nMessage, redefineParameters);
            } else {
                clientLogger.error(i18nMessage);
            }
        }

    }

    /**
     *  Redefine message parameters for SLF4J
     */
    private Object[] redefineParameters(LogRecord record) {
        Object[] originalParameters = record.getParameters();
        Throwable throwable = record.getThrown();
        if (throwable != null && throwable.getMessage() != null && !throwable.getMessage().isBlank()) {
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
     *  Multi-language support
     */
    private String getMessageI18N(LogRecord record) {
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
     *  Publish log message
     */
    public void publish(LogRecord record) {
        if (record != null) {
            ClientLogger clientLogger = this.getClientLogger(record);
            String message = record.getMessage();
            if (message == null) {
                message = "";
            }

            if (clientLogger.getLogger() instanceof LocationAwareLogger) {
                this.callLocationAwareLogger((LocationAwareLogger)clientLogger.getLogger(), record);
            } else {
                this.callPlainSLF4JLogger(clientLogger, record);
            }
        }
    }

    static {
        TRACE_LEVEL_THRESHOLD = Level.FINEST.intValue();
        DEBUG_LEVEL_THRESHOLD = Level.FINE.intValue();
        INFO_LEVEL_THRESHOLD = Level.INFO.intValue();
        WARN_LEVEL_THRESHOLD = Level.WARNING.intValue();
    }

}
