package com.azure.core.util;

import com.azure.core.util.logging.ClientLogger;

import java.time.Duration;
import java.util.function.Function;

public class ConfigurationProperty<T> {
    private static final String[] EMPTY_LIST = new String[0];

    private final String name;
    private final String[] aliases;
    private final String environmentVariableName;
    private final ValueProcessor<T> converter;
    private final T defaultValue;
    private final boolean isLocal;
    private final ClientLogger callerLogger;

    // todo (configuration)
    //  - required flag?
    //  - range

    public ConfigurationProperty(String name, ValueProcessor<T> converter, boolean isLocal, String environmentVariableName, T defaultValue, String[] aliases, ClientLogger callerLogger) {
        this.name = name;
        this.environmentVariableName = environmentVariableName;
        this.aliases = aliases == null ? EMPTY_LIST : aliases;
        this.converter = converter;
        this.defaultValue = defaultValue;
        this.isLocal = isLocal;
        this.callerLogger = callerLogger;
    }

    public boolean isLocal() {
        return isLocal;
    }

    public String getName() {
        return name;
    }

    // let's pick one: convert or getconverter
    public T convert(String value) {
        return converter.processAndConvert(value, this.defaultValue, this.getName(), this.callerLogger);
    }

    public Function<String, T> getConverter() {
        return value -> converter.processAndConvert(value, this.defaultValue, this.getName(), this.callerLogger);
    }

    public T getDefaultValue() {
        return defaultValue;
    }

    public String[] getAliases() {
        return aliases;
    }

    public String getEnvironmentVariableName() {
        return environmentVariableName;
    }

    public static ConfigurationProperty<String> stringLocalProperty(String name, String defaultValue, ClientLogger logger) {
        return new ConfigurationProperty<>(name, STRING_CONVERTER, true, null, defaultValue, EMPTY_LIST, logger);
    }

    public static ConfigurationProperty<String> stringProperty(String name, String environmentVariableName, String defaultValue, ClientLogger logger) {
        return new ConfigurationProperty<>(name, STRING_CONVERTER, false, environmentVariableName, defaultValue, EMPTY_LIST, logger);
    }

    public static ConfigurationProperty<String> stringProperty(String name, String environmentVariableName, String defaultValue, ClientLogger logger, String... aliases) {
        return new ConfigurationProperty<>(name, STRING_CONVERTER, false, environmentVariableName, defaultValue, aliases, logger);
    }


    public static ConfigurationProperty<Boolean> booleanProperty(String name, String environmentVariableName, boolean defaultValue, ClientLogger logger) {
        return new ConfigurationProperty<>(name, BOOLEAN_CONVERTER, false, environmentVariableName, defaultValue, EMPTY_LIST, logger);
    }

    public static ConfigurationProperty<Integer> integerProperty(String name, String environmentVariableName, Integer defaultValue, ClientLogger logger) {
        return new ConfigurationProperty<>(name, INTEGER_CONVERTER, false, environmentVariableName, defaultValue, EMPTY_LIST, logger);
    }

    public static ConfigurationProperty<Long> longProperty(String name, String environmentVariableName, Long defaultValue, ClientLogger logger) {
        return new ConfigurationProperty<>(name, LONG_CONVERTER, false, environmentVariableName, defaultValue, EMPTY_LIST, logger);
    }

    public static ConfigurationProperty<Duration> durationProperty(String name, String environmentVariableName, Duration defaultValue, ClientLogger logger) {
        return new ConfigurationProperty<>(name, DURATION_CONVERTER, false, environmentVariableName, defaultValue, EMPTY_LIST, logger);
    }

    public interface ValueProcessor<T> {
        // TODO range, required
        T processAndConvert(String value, T defaultValue, String propertyName, ClientLogger logger);
    }

    public static ValueProcessor<String> STRING_CONVERTER = new ValueProcessor<String>() {
        @Override
        public String processAndConvert(String value, String defaultValue, String propertyName, ClientLogger logger) {
            logger.atVerbose()
                .addKeyValue("property", propertyName)
                .addKeyValue("value", value)
                .log("Read property.");

            return value == null ? defaultValue : value;
        }
    };

    public static ValueProcessor<Duration> DURATION_CONVERTER = new ValueProcessor<Duration>() {
        @Override
        public Duration processAndConvert(String value, Duration defaultValue, String propertyName, ClientLogger logger) {
            logger.atVerbose()
                .addKeyValue("property", propertyName)
                .addKeyValue("value", value)
                .log("Read property.");

            if (CoreUtils.isNullOrEmpty(value)) {
                return defaultValue;
            }

            try {
                long duration = Long.parseLong(value);
                if (duration < 0) {
                    throw logger.atError()
                        .addKeyValue("property", propertyName)
                        .addKeyValue("value", value)
                        .log(new IllegalArgumentException("Duration cannot have negative value."));
                }

                return Duration.ofMillis(duration);
            } catch (NumberFormatException ex) {
                throw logger.atError()
                    .addKeyValue("property", propertyName)
                    .addKeyValue("value", value)
                    .log(ex);
            }
        }
    };

    public static ValueProcessor<Long> LONG_CONVERTER = new ValueProcessor<Long>() {
        @Override
        public
        Long processAndConvert(String value, Long defaultValue, String propertyName, ClientLogger logger) {
            logger.atVerbose()
                .addKeyValue("property", propertyName)
                .addKeyValue("value", value)
                .log("Read property.");

            if (CoreUtils.isNullOrEmpty(value)) {
                return defaultValue;
            }

            try {
                return Long.parseLong(value);
            } catch (NumberFormatException ex) {
                throw logger.atError()
                    .addKeyValue("property", propertyName)
                    .addKeyValue("value", value)
                    .log(ex);
            }
        }
    };

    public static ValueProcessor<Integer> INTEGER_CONVERTER = new ValueProcessor<Integer>() {
        @Override
        public
        Integer processAndConvert(String value, Integer defaultValue, String propertyName, ClientLogger logger) {
            logger.atVerbose()
                .addKeyValue("property", propertyName)
                .addKeyValue("value", value)
                .log("Read property.");

            if (CoreUtils.isNullOrEmpty(value)) {
                return defaultValue;
            }

            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException ex) {
                throw logger.atError()
                    .addKeyValue("property", propertyName)
                    .addKeyValue("value", value)
                    .log(ex);
            }
        }
    };

    public static ValueProcessor<Boolean> BOOLEAN_CONVERTER = new ValueProcessor<Boolean>() {
        @Override
        public
        Boolean processAndConvert(String value, Boolean defaultValue, String propertyName, ClientLogger logger) {
            logger.atVerbose()
                .addKeyValue("property", propertyName)
                .addKeyValue("value", value)
                .log("Read property.");

            if (CoreUtils.isNullOrEmpty(value)) {
                return defaultValue;
            }

            return Boolean.parseBoolean(value);
        }
    };
}
