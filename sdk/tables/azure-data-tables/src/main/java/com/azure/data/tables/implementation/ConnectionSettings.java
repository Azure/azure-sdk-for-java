// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.tables.implementation;

import com.azure.core.util.logging.ClientLogger;

import java.util.HashMap;
import java.util.Map;

/**
 * A dictionary representation of all settings in a connection string.
 */
final class ConnectionSettings implements Cloneable {
    private final Map<String, String> settings;

    /**
     * Checks if a given setting exists.
     *
     * @param name The setting name.
     *
     * @return {@code true} if the setting exists, {@code false} otherwise.
     */
    public boolean hasSetting(String name) {
        return this.settings.containsKey(name);
    }

    /**
     * Remove a setting with the given name if it exists.
     *
     * @param name The setting name.
     */
    public void removeSetting(String name) {
        this.settings.remove(name);
    }

    /**
     * Get value of the setting with the given name.
     *
     * @param name The setting name.
     *
     * @return The setting value if it exists.
     */
    public String getSettingValue(String name) {
        return this.settings.get(name);
    }

    /**
     * @return {@code true} If there are no settings, {@code false} otherwise.
     */
    public boolean isEmpty() {
        return this.settings.isEmpty();
    }

    /**
     * Set a setting.
     *
     * @param name The setting name.
     * @param value The setting value.
     */
    public void setSetting(String name, String value) {
        this.settings.put(name, value);
    }

    /**
     * Creates {@link ConnectionSettings} from the given connection string.
     *
     * @param connectionString The connection string.
     * @param logger The {@link ClientLogger}.
     *
     * @return The {@link ConnectionSettings}.
     */
    public static ConnectionSettings fromConnectionString(final String connectionString, final ClientLogger logger) {
        HashMap<String, String> map = new HashMap<>();
        final String[] settings = connectionString.split(";");

        for (String s : settings) {
            String setting = s.trim();

            if (setting.length() > 0) {
                final int idx = setting.indexOf("=");

                if (idx == -1 || idx == 0 || idx == s.length() - 1) {
                    // handle no_equal_symbol, "=Bar", "Foo="
                    throw logger.logExceptionAsError(new IllegalArgumentException("Invalid connection string."));
                }

                map.put(setting.substring(0, idx), setting.substring(idx + 1));
            }
        }

        return new ConnectionSettings(map);
    }

    @Override
    public ConnectionSettings clone() {
        return new ConnectionSettings(new HashMap<>(this.settings));
    }

    /**
     * Creates an instance of {@link ConnectionSettings}.
     *
     * @param settings The {@link ConnectionSettings}.
     */
    private ConnectionSettings(Map<String, String> settings) {
        this.settings = settings;
    }
}
