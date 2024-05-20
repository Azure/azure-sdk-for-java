// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.implementation.util;

import io.clientcore.core.util.ClientLogger;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

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
    private static boolean slf4jErrorLogged = false;
    private final DefaultLogger defaultLogger;

    private Object slf4jLogger;
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

            getLoggerMethodHandle = lookup.unreflect(loggerFactoryClass.getMethod("getLogger", String.class));
            logVerboseMethodHandle = lookup.unreflect(loggerClass.getMethod("debug", String.class, Throwable.class));
            logInfoMethodHandle = lookup.unreflect(loggerClass.getMethod("info", String.class, Throwable.class));
            logWarnMethodHandle = lookup.unreflect(loggerClass.getMethod("warn", String.class, Throwable.class));
            logErrorMethodHandle = lookup.unreflect(loggerClass.getMethod("error", String.class, Throwable.class));

            isVerboseEnabledMethodHandle = lookup.unreflect(loggerClass.getMethod("isDebugEnabled"));
            isInfoEnabledMethodHandle = lookup.unreflect(loggerClass.getMethod("isInfoEnabled"));
            isWarnEnabledMethodHandle = lookup.unreflect(loggerClass.getMethod("isWarnEnabled"));
            isErrorEnabledMethodHandle = lookup.unreflect(loggerClass.getMethod("isErrorEnabled"));
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

    public Slf4jLoggerShim(Class<?> clazz) {
        this(clazz.getName(), new DefaultLogger(clazz));
    }

    private Slf4jLoggerShim(String className, DefaultLogger defaultLogger) {
        this.slf4jLogger = createLogger(className);
        this.defaultLogger = defaultLogger;

        try {
            if (slf4jLogger != null) {
                isVerboseEnabled |= (Boolean) LOGGER_IS_VERBOSE_ENABLED_METHOD_HANDLE.invoke(slf4jLogger, VERBOSE);
                isInfoEnabled |= (Boolean) LOGGER_IS_INFO_ENABLED_METHOD_HANDLE.invoke(slf4jLogger, INFORMATIONAL);
                isWarnEnabled |= (Boolean) LOGGER_IS_WARN_ENABLED_METHOD_HANDLE.invoke(slf4jLogger, WARNING);
                isErrorEnabled |= (Boolean) LOGGER_IS_ERROR_ENABLED_METHOD_HANDLE.invoke(slf4jLogger, ERROR);
            }
        } catch (Throwable e) {
            writeSlf4jDisabledError(VERBOSE, "Failed to check if SLF4J log level is enabled", e);
            slf4jLogger = null;
        }

        isVerboseEnabled |= defaultLogger.isEnabled(VERBOSE);
        isInfoEnabled |= defaultLogger.isEnabled(INFORMATIONAL);
        isWarnEnabled |= defaultLogger.isEnabled(WARNING);
        isErrorEnabled |= defaultLogger.isEnabled(ERROR);
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

        // We've already included the exception stacktrace in the message, so there's no need to pass it again to
        // the default logger. We'll still pass it to the SLF4J logger in case the provider wants to do something
        // else with it.
        // NOTE: If default logger is enabled, it should log things regardless of SLF4J configuration.
        defaultLogger.log(logLevel, message, null);

        // We'll keep a reference to the current logger
        // since it can be set to null if an error occurs to turn SLF4J logging off.
        Object slf4jLoggerCopy = this.slf4jLogger;
        if (slf4jLoggerCopy == null) {
            return;
        }

        try {
            switch (logLevel) {
                case VERBOSE:
                    LOGGER_VERBOSE_METHOD_HANDLE.invoke(slf4jLoggerCopy, message, throwable);

                    break;
                case INFORMATIONAL:
                    LOGGER_INFO_METHOD_HANDLE.invoke(slf4jLoggerCopy, message, throwable);

                    break;
                case WARNING:
                    LOGGER_WARN_METHOD_HANDLE.invoke(slf4jLoggerCopy, message, throwable);

                    break;
                case ERROR:
                    LOGGER_ERROR_METHOD_HANDLE.invoke(slf4jLoggerCopy, message, throwable);

                    break;
                default:
                    // Don't do anything, this state shouldn't be possible.
                    break;
            }
        } catch (Throwable e) {
            writeSlf4jDisabledError(VERBOSE, "Failed to log message with SLF4J", e);
            slf4jLogger = null;
        }
    }

    private static Object createLogger(String className) {
        if (LOGGER_FACTORY_GET_LOGGER_METHOD_HANDLE == null || NOP_LOGGER_CLASS == null) {
            return null;
        }

        try {
            Object logger = LOGGER_FACTORY_GET_LOGGER_METHOD_HANDLE.invoke(className);

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
