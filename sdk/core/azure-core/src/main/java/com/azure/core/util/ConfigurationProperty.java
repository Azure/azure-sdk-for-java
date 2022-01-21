package com.azure.core.util;

import java.time.Duration;
import java.util.function.Function;

public class ConfigurationProperty<T> {
    private static final Function<String, Boolean> BOOLEAN_CONVERTER = (value) -> Boolean.parseBoolean(value);
    private static final Function<String, Duration> DURATION_CONVERTER = (value) ->  {
        long timeoutMillis = Long.parseLong(value);
        if (timeoutMillis < 0) {
            throw new IllegalArgumentException("Duration can't be negative");
        }

        return Duration.ofMillis(timeoutMillis);
    };

    private static final Function<String, Integer> INTEGER_CONVERTER = (value) -> Integer.valueOf(value);
    private static final Function<String, String> STRING_CONVERTER = (value) -> value;
    private static final String[] EMPTY_LIST = new String[0];

    private final String name;
    private final String[] aliases;
    private final String[] environmentVariables;
    private final Function<String, T> converter;
    private final T defaultValue;
    private final boolean isGlobal;
    private final boolean canLogValue;
    private final boolean isRequired;

    // todo (configuration)
    //  - required flag?
    //  - range

    ConfigurationProperty(String name, T defaultValue, boolean isRequired, Function<String, T> converter, boolean isGlobal,
                          String[] environmentVariables, String[] aliases, boolean canLogValue) {
        this.name = name;
        this.environmentVariables = environmentVariables;
        this.aliases = aliases == null ? EMPTY_LIST : aliases;
        this.converter = converter;
        this.defaultValue = defaultValue;
        this.isRequired = isRequired;
        this.isGlobal = isGlobal;
        this.canLogValue = canLogValue;
    }

    public boolean isGlobal() {
        return isGlobal;
    }
    public boolean canLogValue() {
        return canLogValue;
    }
    public boolean isRequired() {
        return isRequired;
    }

    public String getName() {
        return name;
    }

    public Function<String, T> getConverter() {
        return converter;
    }

    public T getDefaultValue() {
        return defaultValue;
    }

    public String[] getAliases() {
        return aliases;
    }

    public String[] getEnvironmentVariables() {
        return environmentVariables;
    }

    public static ConfigurationPropertyBuilder<String> stringPropertyBuilder(String name) {
        return new ConfigurationPropertyBuilder<String>(name, STRING_CONVERTER);
    }

    public static ConfigurationPropertyBuilder<Integer> integerPropertyBuilder(String name) {
        return new ConfigurationPropertyBuilder<Integer>(name, INTEGER_CONVERTER).canLogValue(true);
    }

    public static ConfigurationPropertyBuilder<Duration> durationPropertyBuilder(String name) {
        return new ConfigurationPropertyBuilder<Duration>(name, DURATION_CONVERTER).canLogValue(true);
    }

    public static ConfigurationPropertyBuilder<Boolean> booleanPropertyBuilder(String name) {
        return new ConfigurationPropertyBuilder<Boolean>(name, BOOLEAN_CONVERTER).canLogValue(true);
    }

}
