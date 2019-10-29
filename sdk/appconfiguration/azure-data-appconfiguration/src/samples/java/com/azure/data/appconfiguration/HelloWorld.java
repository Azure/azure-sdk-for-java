// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.appconfiguration;

import com.azure.data.appconfiguration.models.ConfigurationSetting;

/**
 * Sample demonstrates how to add, get, and delete a configuration setting.
 */
public class HelloWorld {
    /**
     * Runs the sample algorithm and demonstrates how to add, get, and delete a configuration setting.
     *
     * @param args Unused. Arguments to the program.
     */
    public static void main(String[] args) {
        // The connection string value can be obtained by going to your App Configuration instance in the Azure portal
        // and navigating to "Access Keys" page under the "Settings" section.
        String connectionString = "endpoint={endpoint_value};id={id_value};secret={secret_value}";

        final ConfigurationClient client = new ConfigurationClientBuilder()
            .connectionString(connectionString)
            .buildClient();

        // Name of the key to add to the configuration service.
        final String key = "hello";
        final String value = "world";

        System.out.println("Beginning of synchronous sample...");

        ConfigurationSetting setting = client.setConfigurationSetting(key, null, value);
        System.out.printf(String.format("[SetConfigurationSetting] Key: %s, Value: %s", setting.getKey(), setting.getValue()));

        setting = client.getConfigurationSetting(key, null, null);
        System.out.printf(String.format("[GetConfigurationSetting] Key: %s, Value: %s", setting.getKey(), setting.getValue()));

        setting = client.deleteConfigurationSetting(key, null);
        System.out.printf(String.format("[DeleteConfigurationSetting] Key: %s, Value: %s", setting.getKey(), setting.getValue()));

        System.out.println("End of synchronous sample.");
    }
}
