// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.data.appconfiguration;

import com.azure.v2.data.appconfiguration.models.ConfigurationSetting;
import io.clientcore.core.utils.configuration.Configuration;

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
        String connectionString = Configuration.getGlobalConfiguration().get("AZURE_APPCONFIG_CONNECTION_STRING");

        final ConfigurationClient client = new ConfigurationClientBuilder()
            .connectionString(connectionString)
            .buildClient();

        // Name of the key to add to the configuration service.
        final String key = "hello";
        final String value = "world";

        System.out.println("Beginning of synchronous sample...");

        ConfigurationSetting setting = client.setConfigurationSetting(key, null, value);
        System.out.printf("[SetConfigurationSetting] Key: %s, Value: %s", setting.getKey(), setting.getValue());

        setting = client.getConfigurationSetting(key, null, null);
        System.out.printf("[GetConfigurationSetting] Key: %s, Value: %s", setting.getKey(), setting.getValue());

        setting = client.deleteConfigurationSetting(key, null);
        System.out.printf("[DeleteConfigurationSetting] Key: %s, Value: %s%n", setting.getKey(), setting.getValue());

        System.out.println("End of synchronous sample.");
    }
}
