// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.implementation.instrumentation;

import io.clientcore.core.instrumentation.logging.LogLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.NOPLogger;

import static io.clientcore.core.instrumentation.logging.LogLevel.ERROR;
import static io.clientcore.core.instrumentation.logging.LogLevel.INFORMATIONAL;
import static io.clientcore.core.instrumentation.logging.LogLevel.VERBOSE;
import static io.clientcore.core.instrumentation.logging.LogLevel.WARNING;

public class Slf4jLoggerShim {
    private static final DefaultLogger DEFAULT_LOGGER = new DefaultLogger(Slf4jLoggerShim.class);
    private static final boolean SLF4J_AVAILABLE = isSlf4jAvailable();
    private static boolean slf4jErrorLogged = false;
    private final DefaultLogger defaultLogger;

    private volatile Object slf4jLogger;
    private boolean isVerboseEnabled;
    private boolean isInfoEnabled;
    private boolean isWarnEnabled;
    private boolean isErrorEnabled;

    private final boolean isVerboseEnabledForDefault;
    private final boolean isInfoEnabledForDefault;
    private final boolean isWarnEnabledForDefault;
    private final boolean isErrorEnabledForDefault;

    private static boolean isSlf4jAvailable() {
        try {
            Class.forName("org.slf4j.LoggerFactory", false, Slf4jLoggerShim.class.getClassLoader());
            Slf4jAdapter.initialize();
            return true;
        } catch (ClassNotFoundException | LinkageError | RuntimeException e) {
            DEFAULT_LOGGER.log(VERBOSE, "Failed to initialize Slf4jLoggerShim.", e);
            return false;
        }
    }

    public Slf4jLoggerShim(DefaultLogger defaultLogger) {
        this(null, defaultLogger);
    }

    public Slf4jLoggerShim(String className) {
        this(className, new DefaultLogger(className));
    }

    public Slf4jLoggerShim(Class<?> clazz) {
        this(clazz.getName(), new DefaultLogger(clazz));
    }

    private Slf4jLoggerShim(String className, DefaultLogger defaultLogger) {
        Object localSlf4jLogger = createLogger(className);
        this.slf4jLogger = localSlf4jLogger;
        this.defaultLogger = defaultLogger;

        // Check if SLF4J was found on the classpath.
        if (localSlf4jLogger != null) {
            try {
                // If so, attempt to set logging levels to what is enabled for the SLF4J Logger.
                isVerboseEnabled = Slf4jAdapter.isDebugEnabled(localSlf4jLogger);
                isInfoEnabled = Slf4jAdapter.isInfoEnabled(localSlf4jLogger);
                isWarnEnabled = Slf4jAdapter.isWarnEnabled(localSlf4jLogger);
                isErrorEnabled = Slf4jAdapter.isErrorEnabled(localSlf4jLogger);
            } catch (Throwable e) {
                writeSlf4jDisabledError(VERBOSE, "Failed to check if SLF4J log level is enabled", e);
                slf4jLogger = null;
            }
        }

        // Always track what the DefaultLogger has enabled if we need to use it as the logging path or if we need to
        // fall back to it in case of issue with calling the SLF4J Logger.
        this.isVerboseEnabledForDefault = defaultLogger.isEnabled(VERBOSE);
        this.isInfoEnabledForDefault = defaultLogger.isEnabled(INFORMATIONAL);
        this.isWarnEnabledForDefault = defaultLogger.isEnabled(WARNING);
        this.isErrorEnabledForDefault = defaultLogger.isEnabled(ERROR);
    }

    public boolean canLogAtLevel(LogLevel logLevel) {
        if (logLevel == null) {
            return false;
        }

        boolean slf4jLoggerAvailable = this.slf4jLogger != null;
        switch (logLevel) {
            case VERBOSE:
                return (slf4jLoggerAvailable && isVerboseEnabled)
                    || (!slf4jLoggerAvailable && isVerboseEnabledForDefault);

            case INFORMATIONAL:
                return (slf4jLoggerAvailable && isInfoEnabled) || (!slf4jLoggerAvailable && isInfoEnabledForDefault);

            case WARNING:
                return (slf4jLoggerAvailable && isWarnEnabled) || (!slf4jLoggerAvailable && isWarnEnabledForDefault);

            case ERROR:
                return (slf4jLoggerAvailable && isErrorEnabled) || (!slf4jLoggerAvailable && isErrorEnabledForDefault);

            default:
                return false;
        }
    }

    public void performLogging(LogLevel logLevel, String message, Throwable throwable) {
        if (!canLogAtLevel(logLevel)) {
            return;
        }

        // We'll keep a reference to the current logger
        // since it can be set to null if an error occurs to turn SLF4J logging off.
        Object localSlf4jLogger = this.slf4jLogger;

        // Again, attempt to use the SLF4J Logger if it was on the classpath.
        if (localSlf4jLogger != null) {
            try {
                Slf4jAdapter.log(localSlf4jLogger, logLevel, message, throwable);
            } catch (Throwable e) {
                writeSlf4jDisabledError(VERBOSE, "Failed to log message with SLF4J", e);
                slf4jLogger = null;

                // But if it were to fail, fallback to the DefaultLogger again.
                defaultLogger.log(logLevel, message, null);
            }
        } else {
            // Otherwise, if SLF4J wasn't on the classpath, just use the DefaultLogger.
            defaultLogger.log(logLevel, message, null);
        }
    }

    static Object createLogger(String className) {
        if (!SLF4J_AVAILABLE) {
            return null;
        }

        try {
            Object logger = Slf4jAdapter.createLogger(className);

            if (logger == null) {
                writeSlf4jDisabledError(VERBOSE, "Resolved NOPLogger", null);
                return null;
            }

            return logger;
        } catch (Throwable e) {
            writeSlf4jDisabledError(WARNING, "Failed to create SLF4J logger", e);
            return null;
        }
    }

    private static void writeSlf4jDisabledError(LogLevel level, String message, Throwable throwable) {
        if (!slf4jErrorLogged) {
            slf4jErrorLogged = true;
            DEFAULT_LOGGER.log(level, String.format("[DefaultLogger]: %s. SLF4J logging will be disabled.", message),
                throwable);
        }
    }

    // Keep optional SLF4J types behind the availability check.
    private static final class Slf4jAdapter {
        private static void initialize() {
            // Complete provider discovery before concurrent callers request loggers.
            LoggerFactory.getILoggerFactory();
        }

        private static Object createLogger(String className) {
            Logger logger = LoggerFactory.getLogger(className);
            return logger instanceof NOPLogger ? null : logger;
        }

        private static boolean isDebugEnabled(Object logger) {
            return ((Logger) logger).isDebugEnabled();
        }

        private static boolean isInfoEnabled(Object logger) {
            return ((Logger) logger).isInfoEnabled();
        }

        private static boolean isWarnEnabled(Object logger) {
            return ((Logger) logger).isWarnEnabled();
        }

        private static boolean isErrorEnabled(Object logger) {
            return ((Logger) logger).isErrorEnabled();
        }

        private static void log(Object slf4jLogger, LogLevel logLevel, String message, Throwable throwable) {
            Logger logger = (Logger) slf4jLogger;
            // Keep raw line breaks from creating additional log records. Structured messages are already escaped.
            String singleLineMessage = message == null ? null : message.replace('\r', ' ').replace('\n', ' ');
            switch (logLevel) {
                case VERBOSE:
                    logger.debug(singleLineMessage, throwable);

                    break;

                case INFORMATIONAL:
                    logger.info(singleLineMessage, throwable);

                    break;

                case WARNING:
                    logger.warn(singleLineMessage, throwable);

                    break;

                case ERROR:
                    logger.error(singleLineMessage, throwable);

                    break;

                default:
                    // Don't do anything, this state shouldn't be possible.
                    break;
            }
        }
    }
}
