package com.azure.core.util;

import com.azure.core.util.logging.ClientLogger;

public class ConfigurationHelpers {

    private static final ClientLogger LOGGER = new ClientLogger(ConfigurationHelpers.class);

    public static boolean containsAny(Configuration configuration, ConfigurationProperty<?> property1, ConfigurationProperty<?> property2) {
        return configuration.contains(property1) || configuration.contains(property2);
    }

    public static boolean containsAny(Configuration configuration, ConfigurationProperty<?> property1, ConfigurationProperty<?> property2, ConfigurationProperty<?> property3) {
        return configuration.contains(property1) || configuration.contains(property2) || configuration.contains(property3);
    }

    public static boolean containsAny(Configuration configuration, ConfigurationProperty<?>... properties) {
        for (ConfigurationProperty<?> prop : properties) {
            if (configuration.contains(prop)) {
                return true;
            }
        }

        return false;
    }

    public static boolean containsAll(Configuration configuration, ConfigurationProperty<?> property1, ConfigurationProperty<?> property2) {
        return configuration.contains(property1) && configuration.contains(property2);
    }

    public static boolean containsAll(Configuration configuration, ConfigurationProperty<?> property1, ConfigurationProperty<?> property2, ConfigurationProperty<?> property3) {
        return configuration.contains(property1) && configuration.contains(property2) && configuration.contains(property3);
    }

    public static boolean containsAll(Configuration configuration, ConfigurationProperty<?>... properties) {
        for (ConfigurationProperty<?> prop : properties) {
            if (!configuration.contains(prop)) {
                return false;
            }
        }

        return true;
    }
}
