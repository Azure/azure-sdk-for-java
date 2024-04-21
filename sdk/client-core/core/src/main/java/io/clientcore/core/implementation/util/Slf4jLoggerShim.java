// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.implementation.util;

import io.clientcore.core.util.ClientLogger;

import java.lang.reflect.Method;

public class Slf4jLoggerShim {
    private static final Method LOGGER_FACTORY_GET_LOGGER_METHOD;
    private static final Method LOGGER_VERBOSE_METHOD;
    private static final Method LOGGER_INFO_METHOD;
    private static final Method LOGGER_WARN_METHOD;
    private static final Method LOGGER_ERROR_METHOD;
    private static final Method LOGGER_IS_VERBOSE_ENABLED_METHOD;
    private static final Method LOGGER_IS_INFO_ENABLED_METHOD;
    private static final Method LOGGER_IS_WARN_ENABLED_METHOD;
    private static final Method LOGGER_IS_ERROR_ENABLED_METHOD;
    private static final Class<?> NOP_LOGGER_CLASS;
    private final DefaultLogger defaultLogger;

    private Object logger;
    private boolean isVerboseEnabled;
    private boolean isInfoEnabled;
    private boolean isWarnEnabled;
    private boolean isErrorEnabled;

    static {
        Method getLoggerMethod;
        Class<?> nopLoggerClass;
        Method logVerboseMethod;
        Method logInfoMethod;
        Method logWarnMethod;
        Method logErrorMethod;

        Method isVerboseEnabledMethod;
        Method isInfoEnabledMethod;
        Method isWarnEnabledMethod;
        Method isErrorEnabledMethod;

        try {
            Class<?> loggerFactoryClass = Class.forName("org.slf4j.LoggerFactory", true, Slf4jLoggerShim.class.getClassLoader());
            getLoggerMethod = loggerFactoryClass.getMethod("getLogger", String.class);
            Class<?> loggerClass = Class.forName("org.slf4j.Logger", true, Slf4jLoggerShim.class.getClassLoader());
            nopLoggerClass = Class.forName("org.slf4j.helpers.NOPLogger", true, Slf4jLoggerShim.class.getClassLoader());

            logVerboseMethod = loggerClass.getMethod("debug", String.class, Throwable.class);
            logInfoMethod = loggerClass.getMethod("info", String.class, Throwable.class);
            logWarnMethod = loggerClass.getMethod("warn", String.class, Throwable.class);
            logErrorMethod = loggerClass.getMethod("error", String.class, Throwable.class);

            isVerboseEnabledMethod = loggerClass.getMethod("isDebugEnabled");
            isInfoEnabledMethod = loggerClass.getMethod("isInfoEnabled");
            isWarnEnabledMethod = loggerClass.getMethod("isWarnEnabled");
            isErrorEnabledMethod = loggerClass.getMethod("isErrorEnabled");
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            nopLoggerClass = null;
            getLoggerMethod = null;
            logVerboseMethod = null;
            logInfoMethod = null;
            logWarnMethod = null;
            logErrorMethod = null;
            isVerboseEnabledMethod = null;
            isInfoEnabledMethod = null;
            isWarnEnabledMethod = null;
            isErrorEnabledMethod = null;
        }

        LOGGER_FACTORY_GET_LOGGER_METHOD = getLoggerMethod;
        NOP_LOGGER_CLASS = nopLoggerClass;
        LOGGER_VERBOSE_METHOD = logVerboseMethod;
        LOGGER_INFO_METHOD = logInfoMethod;
        LOGGER_WARN_METHOD = logWarnMethod;
        LOGGER_ERROR_METHOD = logErrorMethod;
        LOGGER_IS_VERBOSE_ENABLED_METHOD = isVerboseEnabledMethod;
        LOGGER_IS_INFO_ENABLED_METHOD = isInfoEnabledMethod;
        LOGGER_IS_WARN_ENABLED_METHOD = isWarnEnabledMethod;
        LOGGER_IS_ERROR_ENABLED_METHOD = isErrorEnabledMethod;
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
            isVerboseEnabled = isSlf4JEnabledAtLevel(ClientLogger.LogLevel.VERBOSE) | defaultLogger.isEnabled(ClientLogger.LogLevel.VERBOSE);
            isInfoEnabled = isSlf4JEnabledAtLevel(ClientLogger.LogLevel.INFORMATIONAL) | defaultLogger.isEnabled(ClientLogger.LogLevel.INFORMATIONAL);
            isWarnEnabled = isSlf4JEnabledAtLevel(ClientLogger.LogLevel.WARNING) | defaultLogger.isEnabled(ClientLogger.LogLevel.WARNING);
            isErrorEnabled = isSlf4JEnabledAtLevel(ClientLogger.LogLevel.ERROR) | defaultLogger.isEnabled(ClientLogger.LogLevel.ERROR);
        } catch (ReflectiveOperationException e) {
            defaultLogger.log(DefaultLogger.ERROR, "Failed to initialize Slf4jLoggerShim " + e.getMessage());
            logger = null;
            isVerboseEnabled = defaultLogger.isEnabled(ClientLogger.LogLevel.VERBOSE);
            isInfoEnabled = defaultLogger.isEnabled(ClientLogger.LogLevel.INFORMATIONAL);
            isWarnEnabled = defaultLogger.isEnabled(ClientLogger.LogLevel.WARNING);
            isErrorEnabled = defaultLogger.isEnabled(ClientLogger.LogLevel.ERROR);
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

        performLoggingDefaultLogger(logLevel, message, throwable);

        if (logger == null) {
            return;
        }
        try {
            switch (logLevel) {
                case VERBOSE:
                    LOGGER_VERBOSE_METHOD.invoke(logger, message, throwable);
                    break;
                case INFORMATIONAL:
                    LOGGER_INFO_METHOD.invoke(logger, message, throwable);
                    break;
                case WARNING:
                    LOGGER_WARN_METHOD.invoke(logger, message, throwable);
                    break;
                case ERROR:
                    LOGGER_ERROR_METHOD.invoke(logger, message, throwable);
                    break;
                default:
                    // Don't do anything, this state shouldn't be possible.
                    break;
            }
        } catch (ReflectiveOperationException e) {
            defaultLogger.log(DefaultLogger.ERROR, "Failed to log message " + e.getMessage());
            logger = null;
        }
    }

    private void performLoggingDefaultLogger(ClientLogger.LogLevel logLevel, String message, Throwable throwable) {
        if (!defaultLogger.isEnabled(logLevel)) {
            return;
        }

        switch (logLevel) {
            case VERBOSE:
                defaultLogger.log(DefaultLogger.TRACE, message);
                break;
            case INFORMATIONAL:
                defaultLogger.log(DefaultLogger.INFO, message);
                break;
            case WARNING:
                defaultLogger.log(DefaultLogger.WARN, message);
                break;
            case ERROR:
                defaultLogger.log(DefaultLogger.ERROR, message);
                break;
            default:
                // Don't do anything, this state shouldn't be possible.
                break;
        }
    }

    private static Object createLogger(String className) {
        if (LOGGER_FACTORY_GET_LOGGER_METHOD == null || NOP_LOGGER_CLASS == null) {
            return null;
        }
        try {
            Object logger = LOGGER_FACTORY_GET_LOGGER_METHOD.invoke(null, className);
            if (NOP_LOGGER_CLASS.isAssignableFrom(logger.getClass())) {
                return null;
            }
            return logger;
        } catch (ReflectiveOperationException e) {
            return null;
        }
    }

    private boolean isSlf4JEnabledAtLevel(ClientLogger.LogLevel logLevel) throws ReflectiveOperationException {
        if (logger == null) {
            return false;
        }

        switch (logLevel) {
            case VERBOSE:
                return (boolean) LOGGER_IS_VERBOSE_ENABLED_METHOD.invoke(logger);
            case INFORMATIONAL:
                return (boolean) LOGGER_IS_INFO_ENABLED_METHOD.invoke(logger);
            case WARNING:
                return (boolean) LOGGER_IS_WARN_ENABLED_METHOD.invoke(logger);
            case ERROR:
                return (boolean) LOGGER_IS_ERROR_ENABLED_METHOD.invoke(logger);
            default:
                return false;
        }
    }
}
