// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.appconfiguration;

import com.azure.data.appconfiguration.models.ConfigurationSetting;

public class ReadOnlySample {
    /**
     * Runs the sample algorithm and demonstrates how to add a custom policy to the HTTP pipeline.
     *
     * @param args Unused. Arguments to the program.
     */
    public static void main(String[] args) {
        // The connection string value can be obtained by going to your App Configuration instance in the Azure portal
        // and navigating to "Access Keys" page under the "Settings" section.
        String connectionString = "endpoint={endpoint_value};id={id_value};secret={secret_value}";

        // Instantiate a client that will be used to call the service.
        final ConfigurationAsyncClient client = new ConfigurationClientBuilder()
            .connectionString(connectionString)
            .buildAsyncClient();

        // Name of the key to add to the configuration service.
        final String key = "hello";
        final String value = "world";

        client.setConfigurationSetting(key, null, value).subscribe(
            result -> {
                final ConfigurationSetting setting = result;
                System.out.println(String.format("[setConfigurationSetting] Key: %s, Value: %s", setting.getKey(), setting.getValue()));
            },
            error -> System.err.println("There was an error while adding the setting: " + error.toString()),
            () -> System.out.println(String.format("Set setting with key=%s and value=%s added or updated.", key, value)));

        // Read-Only
        client.setReadOnly(key, null).subscribe(
            result -> {
                final ConfigurationSetting setting = result;
                System.out.println(String.format("[Locked Setting] Key: %s, Value: %s", setting.getKey(), setting.getValue()));
            },
            error -> System.err.println("There was an error while making the setting to read-only: " + error.toString()),
            null
        );

        // Clear Read-Only
        client.clearReadOnly(key, null).subscribe(
            result -> {
                final ConfigurationSetting setting = result;
                System.out.println(String.format("[Locked Setting] Key: %s, Value: %s", setting.getKey(), setting.getValue()));
            },
            error -> System.err.println("There was an error while making the setting to read-only: " + error.toString()),
            null
        );
    }
}
