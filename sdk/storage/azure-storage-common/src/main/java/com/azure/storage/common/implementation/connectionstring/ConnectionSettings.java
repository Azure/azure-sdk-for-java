// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.implementation.connectionstring;

import com.azure.core.util.logging.ClientLogger;
import java.util.HashMap;
import java.util.Map;

/**
 * A dictionary representation of all settings in a connection string.
 */
final class ConnectionSettings implements Cloneable {
    private final Map<String, String> settings;

    /**
     * Checks a given settings exists.
     *
     * @param name the setting name
     * @return true if setting exists, false otherwise
     */
    public boolean hasSetting(String name) {
        return this.settings.containsKey(name);
    }

    /**
     * Remove a setting with given name if exists.
     *
     * @param name the setting name
     */
    public void removeSetting(String name) {
        this.settings.remove(name);
    }

    /**
     * Get value of the setting with given name.
     *
     * @param name the setting name
     * @return the setting value if exists
     */
    public String getSettingValue(String name) {
        return this.settings.get(name);
    }

    /**
     * @return true if there no settings exists, false otherwise.
     */
    public boolean isEmpty() {
        return this.settings.isEmpty();
    }

    /**
     * Set a setting.
     *
     * @param name the setting name
     * @param value the setting value
     */
    public void setSetting(String name, String value) {
        this.settings.put(name, value);
    }

    /**
     * Creates {@link ConnectionSettings} from the given connection string.
     *
     * @param connString the connection string
     * @param logger the logger
     * @return the ConnectionSettings
     */
    public static ConnectionSettings fromConnectionString(final String connString,
                                                          final ClientLogger logger) {
        HashMap<String, String> map = new HashMap<>();
        final String[] settings = connString.split(";");
        for (int i = 0; i < settings.length; i++) {
            String setting = settings[i].trim();
            if (setting.length() > 0) {
                final int idx = setting.indexOf("=");
                if (idx == -1
                        || idx == 0
                        || idx == settings[i].length() - 1) {
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
     * Creates ConnectionSettings.
     *
     * @param settings the settings as a map
     */
    private ConnectionSettings(Map<String, String> settings) {
        this.settings = settings;
    }
}
