package com.azure.core.util;

import java.util.function.Function;

public class ConfigurationPropertyBuilder<T> {
    private static final String[] EMPTY_LIST = new String[0];

    private final String name;
    private String[] aliases = EMPTY_LIST;
    private String[] environmentVariableNames = EMPTY_LIST;
    private Function<String, T> converter;
    private T defaultValue;
    private boolean isGlobal;
    private boolean canLogValue;
    private boolean isRequired;

    public ConfigurationPropertyBuilder(String name, Function<String, T> converter) {
        this.name = name;
        this.converter = converter;
    }

    public ConfigurationPropertyBuilder<T> defaultValue(T defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }

    public ConfigurationPropertyBuilder<T> global(boolean global) {
        this.isGlobal = global;
        return this;
    }

    public ConfigurationPropertyBuilder<T> canLogValue(boolean canLogValue) {
        this.canLogValue = canLogValue;
        return this;
    }

    public ConfigurationPropertyBuilder<T> required(boolean isRequired) {
        this.isRequired = isRequired;
        return this;
    }

    public ConfigurationPropertyBuilder<T> aliases(String... aliases) {
        aliases = aliases;
        return this;
    }

    public ConfigurationPropertyBuilder<T> environmentVariables(String... names) {
        environmentVariableNames = names;
        return this;
    }

    public ConfigurationProperty<T> build() {
        return new ConfigurationProperty<T>(name, defaultValue, isRequired, converter, isGlobal, environmentVariableNames, aliases, canLogValue);
    }
}
