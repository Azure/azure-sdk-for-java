package com.azure.core.util;

public class ConfigurationProperty {
    private static final String[] EMPTY = new String[0];
    private final String clientName;
    private final String name;

    boolean isLocal;

    public ConfigurationProperty(String name) {
        this(null, name, true);
    }

    //TODO(configuration) aliases
    public ConfigurationProperty(String clientName, String name, boolean isLocal) {
        this.clientName = clientName;
        this.name = name;
        this.isLocal = isLocal;
    }

    public boolean isLocal() {
        return isLocal;
    }

    public String getFullName() {
        return clientName != null ? clientName + "." + name : name;
    }

    public ConfigurationProperty getBase() {
        if (isLocal) {
            return null;
        }

        // TODO lazy
        return new ConfigurationProperty(null, name, false);
    }
}
