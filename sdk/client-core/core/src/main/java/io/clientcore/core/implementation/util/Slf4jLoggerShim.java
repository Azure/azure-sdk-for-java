// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.implementation.util;

import io.clientcore.core.util.ClientLogger;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

import static io.clientcore.core.util.ClientLogger.LogLevel.ERROR;
import static io.clientcore.core.util.ClientLogger.LogLevel.INFORMATIONAL;
import static io.clientcore.core.util.ClientLogger.LogLevel.VERBOSE;
import static io.clientcore.core.util.ClientLogger.LogLevel.WARNING;

public class Slf4jLoggerShim {
    private static final DefaultLogger DEFAULT_LOGGER = new DefaultLogger(Slf4jLoggerShim.class);
    private static final MethodHandle LOGGER_FACTORY_GET_LOGGER_METHOD_HANDLE;
    private static final MethodHandle LOGGER_VERBOSE_METHOD_HANDLE;
    private static final MethodHandle LOGGER_INFO_METHOD_HANDLE;
    private static final MethodHandle LOGGER_WARN_METHOD_HANDLE;
    private static final MethodHandle LOGGER_ERROR_METHOD_HANDLE;
    private static final MethodHandle LOGGER_IS_VERBOSE_ENABLED_METHOD_HANDLE;
    private static final MethodHandle LOGGER_IS_INFO_ENABLED_METHOD_HANDLE;
    private static final MethodHandle LOGGER_IS_WARN_ENABLED_METHOD_HANDLE;
    private static final MethodHandle LOGGER_IS_ERROR_ENABLED_METHOD_HANDLE;
    private static final Class<?> NOP_LOGGER_CLASS;
    private final DefaultLogger defaultLogger;

    private Object logger;
    private boolean isVerboseEnabled;
    private boolean isInfoEnabled;
    private boolean isWarnEnabled;
    private boolean isErrorEnabled;

    static {
        Class<?> nopLoggerClass;
        MethodHandle getLoggerMethodHandle;
        MethodHandle logVerboseMethodHandle;
        MethodHandle logInfoMethodHandle;
        MethodHandle logWarnMethodHandle;
        MethodHandle logErrorMethodHandle;

        MethodHandle isVerboseEnabledMethodHandle;
        MethodHandle isInfoEnabledMethodHandle;
        MethodHandle isWarnEnabledMethodHandle;
        MethodHandle isErrorEnabledMethodHandle;

        try {
            nopLoggerClass = Class.forName("org.slf4j.helpers.NOPLogger", true, Slf4jLoggerShim.class.getClassLoader());

            Class<?> loggerFactoryClass = Class.forName("org.slf4j.LoggerFactory", true,
                Slf4jLoggerShim.class.getClassLoader());
            Class<?> loggerClass = Class.forName("org.slf4j.Logger", true, Slf4jLoggerShim.class.getClassLoader());

            MethodHandles.Lookup lookup = MethodHandles.publicLookup();

            getLoggerMethodHandle = lookup.findStatic(loggerFactoryClass, "getLogger",
                MethodType.methodType(loggerClass, String.class));

            logVerboseMethodHandle = lookup.findVirtual(loggerClass, "debug",
                MethodType.methodType(void.class, String.class, Throwable.class));
            logInfoMethodHandle = lookup.findVirtual(loggerClass, "info",
                MethodType.methodType(void.class, String.class, Throwable.class));
            logWarnMethodHandle = lookup.findVirtual(loggerClass, "warn",
                MethodType.methodType(void.class, String.class, Throwable.class));
            logErrorMethodHandle = lookup.findVirtual(loggerClass, "error",
                MethodType.methodType(void.class, String.class, Throwable.class));

            isVerboseEnabledMethodHandle = lookup.findVirtual(loggerClass, "isDebugEnabled",
                MethodType.methodType(boolean.class));
            isInfoEnabledMethodHandle = lookup.findVirtual(loggerClass, "isInfoEnabled",
                MethodType.methodType(boolean.class));
            isWarnEnabledMethodHandle = lookup.findVirtual(loggerClass, "isWarnEnabled",
                MethodType.methodType(boolean.class));
            isErrorEnabledMethodHandle = lookup.findVirtual(loggerClass, "isErrorEnabled",
                MethodType.methodType(boolean.class));
        } catch (ClassNotFoundException | IllegalAccessException | NoSuchMethodException e) {
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

        LOGGER_FACTORY_GET_LOGGER_METHOD_HANDLE = getLoggerMethodHandle;
        NOP_LOGGER_CLASS = nopLoggerClass;
        LOGGER_VERBOSE_METHOD_HANDLE = logVerboseMethodHandle;
        LOGGER_INFO_METHOD_HANDLE = logInfoMethodHandle;
        LOGGER_WARN_METHOD_HANDLE = logWarnMethodHandle;
        LOGGER_ERROR_METHOD_HANDLE = logErrorMethodHandle;
        LOGGER_IS_VERBOSE_ENABLED_METHOD_HANDLE = isVerboseEnabledMethodHandle;
        LOGGER_IS_INFO_ENABLED_METHOD_HANDLE = isInfoEnabledMethodHandle;
        LOGGER_IS_WARN_ENABLED_METHOD_HANDLE = isWarnEnabledMethodHandle;
        LOGGER_IS_ERROR_ENABLED_METHOD_HANDLE = isErrorEnabledMethodHandle;
    }

    public Slf4jLoggerShim(DefaultLogger defaultLogger) {
        this(null, defaultLogger);
    }

    public Slf4jLoggerShim(String className) {
        this(className, new DefaultLogger(className));
    }

    private Slf4jLoggerShim(String className, DefaultLogger defaultLogger) {
        this.logger = createLogger(className);
        this.defaultLogger = defaultLogger;

        try {
            isVerboseEnabled = isSlf4JEnabledAtLevel(logger, VERBOSE) | defaultLogger.isEnabled(VERBOSE);
            isInfoEnabled = isSlf4JEnabledAtLevel(logger, INFORMATIONAL) | defaultLogger.isEnabled(INFORMATIONAL);
            isWarnEnabled = isSlf4JEnabledAtLevel(logger, WARNING) | defaultLogger.isEnabled(WARNING);
            isErrorEnabled = isSlf4JEnabledAtLevel(logger, ERROR) | defaultLogger.isEnabled(ERROR);
        } catch (ReflectiveOperationException e) {
            logger = null;

            DEFAULT_LOGGER.log(WARNING, "Failed to initialize Slf4jLoggerShim.", e);

            isVerboseEnabled = defaultLogger.isEnabled(VERBOSE);
            isInfoEnabled = defaultLogger.isEnabled(INFORMATIONAL);
            isWarnEnabled = defaultLogger.isEnabled(WARNING);
            isErrorEnabled = defaultLogger.isEnabled(ERROR);
        }
    }

    public boolean canLogAtLevel(ClientLogger.LogLevel logLevel) {
        if (logLevel == null) {
            return false;
        }

        switch (logLevel) {
            case VERBOSE:
                return isVerboseEnabled;
            case INFORMATIONAL:
                return isInfoEnabled;
            case WARNING:
                return isWarnEnabled;
            case ERROR:
                return isErrorEnabled;
            default:
                return false;
        }
    }

    public void performLogging(ClientLogger.LogLevel logLevel, String message, Throwable throwable) {
        if (!canLogAtLevel(logLevel)) {
            return;
        }

        // We've already included the exception stacktrace in the message, so there's no need to pass it again to the
        // default logger. We'll still pass it to the SLF4J logger in case the provider wants to do something else
        // with it.
        defaultLogger.log(logLevel, message, null);

        if (logger == null) {
            return;
        }

        try {
            switch (logLevel) {
                case VERBOSE:
                    LOGGER_VERBOSE_METHOD_HANDLE.invoke(message, throwable);

                    break;
                case INFORMATIONAL:
                    LOGGER_INFO_METHOD_HANDLE.invoke(message, throwable);

                    break;
                case WARNING:
                    LOGGER_WARN_METHOD_HANDLE.invoke(message, throwable);

                    break;
                case ERROR:
                    LOGGER_ERROR_METHOD_HANDLE.invoke(message, throwable);

                    break;
                default:
                    // Don't do anything, this state shouldn't be possible.
                    break;
            }
        } catch (Throwable e) {
            defaultLogger.log(WARNING, "Failed to log message, SLF4J logging will be disabled.", e);

            logger = null;
        }
    }

    private static Object createLogger(String className) {
        if (LOGGER_FACTORY_GET_LOGGER_METHOD_HANDLE == null || NOP_LOGGER_CLASS == null) {
            return null;
        }

        try {
            Object logger = LOGGER_FACTORY_GET_LOGGER_METHOD_HANDLE.invoke(className);

            if (NOP_LOGGER_CLASS.isAssignableFrom(logger.getClass())) {
                DEFAULT_LOGGER.log(VERBOSE, "Resolved NOPLogger, SLF4J logging will be disabled.", null);

                return null;
            }

            return logger;
        } catch (Throwable e) {
            DEFAULT_LOGGER.log(WARNING, "Failed to create SLF4J logger, SLF4J logging will be disabled.", e);

            return null;
        }
    }

    private static boolean isSlf4JEnabledAtLevel(Object logger, ClientLogger.LogLevel logLevel)
        throws ReflectiveOperationException {

        if (logger == null) {
            return false;
        }

        try {
            switch (logLevel) {
                case VERBOSE:
                    return (boolean) LOGGER_IS_VERBOSE_ENABLED_METHOD_HANDLE.invoke();
                case INFORMATIONAL:
                    return (boolean) LOGGER_IS_INFO_ENABLED_METHOD_HANDLE.invoke();
                case WARNING:
                    return (boolean) LOGGER_IS_WARN_ENABLED_METHOD_HANDLE.invoke();
                case ERROR:
                    return (boolean) LOGGER_IS_ERROR_ENABLED_METHOD_HANDLE.invoke();
                default:
                    return false;
            }
        } catch (Throwable e) {
            DEFAULT_LOGGER.log(WARNING, "Failed to check if log level is enabled, SLF4J logging will be disabled.", e);

            return false;
        }
    }
}
