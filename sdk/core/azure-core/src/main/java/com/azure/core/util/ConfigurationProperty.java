package com.azure.core.util;

import java.time.Duration;
import java.util.function.Function;

public class ConfigurationProperty<T> {
    private static final Function<String, String> STRING_CONVERTER = (value) -> value;
    private static final Function<String, Integer> INTEGER_CONVERTER = (value) -> Integer.valueOf(value);
    private static final Function<String, Duration> DURATION_CONVERTER = (value) -> Duration.parse(value);
    private static final String[] EMPTY_LIST = new String[0];

    private final String prefix;
    private final String name;
    private final String fullName;
    private final String[] aliases;
    private final String environmentVariableName;
    private final Function<String, T> converter;
    private final T defaultValue;
    private final boolean isLocal;

    // todo (configuration)
    //  - required flag?
    //  - range

    public ConfigurationProperty(String prefix, String name, Function<String, T> converter, boolean isLocal, String environmentVariableName, T defaultValue, String... aliases) {
        this.prefix = prefix;
        this.name = name;
        this.fullName = prefix == null ? name : prefix + "." + name;
        this.environmentVariableName = environmentVariableName;
        this.aliases = aliases == null ? EMPTY_LIST : aliases;
        this.converter = converter;
        this.defaultValue = defaultValue;
        this.isLocal = isLocal;
    }

    public boolean isLocal() {
        return isLocal;
    }

    public String getName() {
        return name;
    }

    public String getFullName() {
        return fullName;
    }

    public String getPrefix() {
        return prefix;
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

    public String getEnvironmentVariableName() {
        return environmentVariableName;
    }

    public static ConfigurationProperty<String> stringLocalProperty(String name, String defaultValue) {
        return new ConfigurationProperty<>(null, name, STRING_CONVERTER, true, null, defaultValue);
    }

    public static ConfigurationProperty<String> stringGlobalProperty(String name, String defaultValue) {
        return new ConfigurationProperty<>(null, name, STRING_CONVERTER, false, null, defaultValue);
    }

    public static ConfigurationProperty<String> stringProperty(String prefix, String name, String environmentVariableName, String defaultValue) {
        return new ConfigurationProperty<>(prefix, name, STRING_CONVERTER, false, environmentVariableName, defaultValue);
    }

    public static ConfigurationProperty<Integer> integerProperty(String prefix, String name, String environmentVariableName, Integer defaultValue) {
        return new ConfigurationProperty<>(prefix, name, INTEGER_CONVERTER, false, environmentVariableName, defaultValue);
    }

    public static ConfigurationProperty<Duration> durationProperty(String prefix, String name, String environmentVariableName, Duration defaultValue) {
        return new ConfigurationProperty<>(prefix, name, DURATION_CONVERTER, false, environmentVariableName, defaultValue);
    }
}
