// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.implementation.util;

import io.clientcore.core.implementation.ReflectionUtils;
import io.clientcore.core.implementation.ReflectiveInvoker;
import io.clientcore.core.util.ClientLogger;

import static io.clientcore.core.util.ClientLogger.LogLevel.ERROR;
import static io.clientcore.core.util.ClientLogger.LogLevel.INFORMATIONAL;
import static io.clientcore.core.util.ClientLogger.LogLevel.VERBOSE;
import static io.clientcore.core.util.ClientLogger.LogLevel.WARNING;

public class Slf4jLoggerShim {
    private static final DefaultLogger DEFAULT_LOGGER = new DefaultLogger(Slf4jLoggerShim.class);
    private static final ReflectiveInvoker LOGGER_FACTORY_GET_LOGGER;
    private static final ReflectiveInvoker LOGGER_VERBOSE;
    private static final ReflectiveInvoker LOGGER_INFO;
    private static final ReflectiveInvoker LOGGER_WARN;
    private static final ReflectiveInvoker LOGGER_ERROR;
    private static final ReflectiveInvoker LOGGER_IS_VERBOSE_ENABLED;
    private static final ReflectiveInvoker LOGGER_IS_INFO_ENABLED;
    private static final ReflectiveInvoker LOGGER_IS_WARN_ENABLED;
    private static final ReflectiveInvoker LOGGER_IS_ERROR_ENABLED;
    private static final Class<?> NOP_LOGGER_CLASS;
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

    static {
        Class<?> nopLoggerClass;
        ReflectiveInvoker getLoggerMethodHandle;
        ReflectiveInvoker logVerboseMethodHandle;
        ReflectiveInvoker logInfoMethodHandle;
        ReflectiveInvoker logWarnMethodHandle;
        ReflectiveInvoker logErrorMethodHandle;

        ReflectiveInvoker isVerboseEnabledMethodHandle;
        ReflectiveInvoker isInfoEnabledMethodHandle;
        ReflectiveInvoker isWarnEnabledMethodHandle;
        ReflectiveInvoker isErrorEnabledMethodHandle;

        try {
            nopLoggerClass = Class.forName("org.slf4j.helpers.NOPLogger", true, Slf4jLoggerShim.class.getClassLoader());

            Class<?> loggerFactoryClass = Class.forName("org.slf4j.LoggerFactory", true,
                Slf4jLoggerShim.class.getClassLoader());
            Class<?> loggerClass = Class.forName("org.slf4j.Logger", true, Slf4jLoggerShim.class.getClassLoader());

            getLoggerMethodHandle = ReflectionUtils.getMethodInvoker(loggerFactoryClass,
                loggerFactoryClass.getMethod("getLogger", String.class));
            logVerboseMethodHandle = ReflectionUtils.getMethodInvoker(loggerClass,
                loggerClass.getMethod("debug", String.class, Throwable.class));
            logInfoMethodHandle = ReflectionUtils.getMethodInvoker(loggerClass,
                loggerClass.getMethod("info", String.class, Throwable.class));
            logWarnMethodHandle = ReflectionUtils.getMethodInvoker(loggerClass,
                loggerClass.getMethod("warn", String.class, Throwable.class));
            logErrorMethodHandle = ReflectionUtils.getMethodInvoker(loggerClass,
                loggerClass.getMethod("error", String.class, Throwable.class));

            isVerboseEnabledMethodHandle = ReflectionUtils.getMethodInvoker(loggerClass,
                loggerClass.getMethod("isDebugEnabled"));
            isInfoEnabledMethodHandle = ReflectionUtils.getMethodInvoker(loggerClass,
                loggerClass.getMethod("isInfoEnabled"));
            isWarnEnabledMethodHandle = ReflectionUtils.getMethodInvoker(loggerClass,
                loggerClass.getMethod("isWarnEnabled"));
            isErrorEnabledMethodHandle = ReflectionUtils.getMethodInvoker(loggerClass,
                loggerClass.getMethod("isErrorEnabled"));
        } catch (Exception e) {
            DEFAULT_LOGGER.log(VERBOSE, "Failed to initialize Slf4jLoggerShim.", e);

            nopLoggerClass = null;

            getLoggerMethodHandle = null;

            logVerboseMethodHandle = null;
            logInfoMethodHandle = null;
            logWarnMethodHandle = null;
            logErrorMethodHandle = null;

            isVerboseEnabledMethodHandle = null;
            isInfoEnabledMethodHandle = null;
            isWarnEnabledMethodHandle = null;
            isErrorEnabledMethodHandle = null;
        }

        LOGGER_FACTORY_GET_LOGGER = getLoggerMethodHandle;
        NOP_LOGGER_CLASS = nopLoggerClass;
        LOGGER_VERBOSE = logVerboseMethodHandle;
        LOGGER_INFO = logInfoMethodHandle;
        LOGGER_WARN = logWarnMethodHandle;
        LOGGER_ERROR = logErrorMethodHandle;
        LOGGER_IS_VERBOSE_ENABLED = isVerboseEnabledMethodHandle;
        LOGGER_IS_INFO_ENABLED = isInfoEnabledMethodHandle;
        LOGGER_IS_WARN_ENABLED = isWarnEnabledMethodHandle;
        LOGGER_IS_ERROR_ENABLED = isErrorEnabledMethodHandle;
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
                isVerboseEnabled = (boolean) LOGGER_IS_VERBOSE_ENABLED.invokeWithArguments(localSlf4jLogger);
                isInfoEnabled = (boolean) LOGGER_IS_INFO_ENABLED.invokeWithArguments(localSlf4jLogger);
                isWarnEnabled = (boolean) LOGGER_IS_WARN_ENABLED.invokeWithArguments(localSlf4jLogger);
                isErrorEnabled = (boolean) LOGGER_IS_ERROR_ENABLED.invokeWithArguments(localSlf4jLogger);
            } catch (Throwable e) {
                writeSlf4jDisabledError(VERBOSE, "Failed to check if SLF4J log level is enabled", e);
                slf4jLogger = null;
            }
        }

        // Always track what the DefaultLogger has enabled if we need to use it as the logging path or if we need to
        // fall back to it in case of issue with reflectively invoking the SLF4J Logger.
        this.isVerboseEnabledForDefault = defaultLogger.isEnabled(VERBOSE);
        this.isInfoEnabledForDefault = defaultLogger.isEnabled(INFORMATIONAL);
        this.isWarnEnabledForDefault = defaultLogger.isEnabled(WARNING);
        this.isErrorEnabledForDefault = defaultLogger.isEnabled(ERROR);
    }

    public boolean canLogAtLevel(ClientLogger.LogLevel logLevel) {
        if (logLevel == null) {
            return false;
        }

        boolean slf4jLoggerAvailable = this.slf4jLogger != null;
        switch (logLevel) {
            case VERBOSE:
                return (slf4jLoggerAvailable && isVerboseEnabled)
                    || (!slf4jLoggerAvailable && isVerboseEnabledForDefault);
            case INFORMATIONAL:
                return (slf4jLoggerAvailable && isInfoEnabled)
                    || (!slf4jLoggerAvailable && isInfoEnabledForDefault);
            case WARNING:
                return (slf4jLoggerAvailable && isWarnEnabled)
                    || (!slf4jLoggerAvailable && isWarnEnabledForDefault);
            case ERROR:
                return (slf4jLoggerAvailable && isErrorEnabled)
                    || (!slf4jLoggerAvailable && isErrorEnabledForDefault);
            default:
                return false;
        }
    }

    public void performLogging(ClientLogger.LogLevel logLevel, String message, Throwable throwable) {
        if (!canLogAtLevel(logLevel)) {
            return;
        }

        // We'll keep a reference to the current logger
        // since it can be set to null if an error occurs to turn SLF4J logging off.
        Object localSlf4jLogger = this.slf4jLogger;

        // Again, attempt to use the SLF4J Logger if it was on the classpath.
        if (localSlf4jLogger != null) {
            try {
                switch (logLevel) {
                    case VERBOSE:
                        LOGGER_VERBOSE.invokeWithArguments(localSlf4jLogger, message, throwable);

                        break;
                    case INFORMATIONAL:
                        LOGGER_INFO.invokeWithArguments(localSlf4jLogger, message, throwable);

                        break;
                    case WARNING:
                        LOGGER_WARN.invokeWithArguments(localSlf4jLogger, message, throwable);

                        break;
                    case ERROR:
                        LOGGER_ERROR.invokeWithArguments(localSlf4jLogger, message, throwable);

                        break;
                    default:
                        // Don't do anything, this state shouldn't be possible.
                        break;
                }
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
        if (LOGGER_FACTORY_GET_LOGGER == null || NOP_LOGGER_CLASS == null) {
            return null;
        }

        try {
            Object logger = LOGGER_FACTORY_GET_LOGGER.invokeStatic(className);

            if (NOP_LOGGER_CLASS.isAssignableFrom(logger.getClass())) {
                writeSlf4jDisabledError(VERBOSE, "Resolved NOPLogger", null);
                return null;
            }

            return logger;
        } catch (Throwable e) {
            writeSlf4jDisabledError(WARNING, "Failed to create SLF4J logger", e);
            return null;
        }
    }

    private static void writeSlf4jDisabledError(ClientLogger.LogLevel level, String message, Throwable throwable) {
        if (!slf4jErrorLogged) {
            slf4jErrorLogged = true;
            DEFAULT_LOGGER.log(level, String.format("[DefaultLogger]: %s. SLF4J logging will be disabled.", message),
                throwable);
        }
    }
}
