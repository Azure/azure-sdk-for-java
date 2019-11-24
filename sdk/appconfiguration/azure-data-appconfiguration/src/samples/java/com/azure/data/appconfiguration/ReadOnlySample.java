// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.appconfiguration;

import com.azure.data.appconfiguration.models.ConfigurationSetting;

/**
 * Sample demonstrates how to set and clear read-only a configuration setting.
 */
public class ReadOnlySample {
    /**
     * Runs the sample algorithm and demonstrates how to set and clear read-only a configuration setting.
     *
     * @param args Unused. Arguments to the program.
     */
    public static void main(String[] args) {
        // The connection string value can be obtained by going to your App Configuration instance in the Azure portal
        // and navigating to "Access Keys" page under the "Settings" section.
        String connectionString = "endpoint={endpoint_value};id={id_value};secret={secret_value}";

        // Instantiate a client that will be used to call the service.
        final ConfigurationClient client = new ConfigurationClientBuilder()
            .connectionString(connectionString)
            .buildClient();

        // Name of the key to add to the configuration service.
        final String key = "hello";
        final String value = "world";

        final ConfigurationSetting setting = client.setConfigurationSetting(key, null, value);
        // Read-Only
        final ConfigurationSetting readOnlySetting = client.setReadOnly(setting.getKey(), setting.getLabel(), true);
        System.out.printf(String.format("Setting is read-only now, Key: %s, Value: %s",
            readOnlySetting.getKey(), readOnlySetting.getValue()));
        // Clear Read-Only
        final ConfigurationSetting clearedReadOnlySetting = client.setReadOnly(setting.getKey(), setting.getLabel(), false);
        System.out.printf(String.format("Setting is no longer read-only, Key: %s, Value: %s",
            clearedReadOnlySetting.getKey(), clearedReadOnlySetting.getValue()));
    }
}
