// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.appconfiguration;


import com.azure.v2.data.appconfiguration.AzureAppConfigurationClient;
import com.azure.v2.data.appconfiguration.AzureAppConfigurationClientBuilder;
import com.azure.v2.data.appconfiguration.models.KeyValue;
import io.clientcore.core.utils.configuration.Configuration;

import java.util.Arrays;

import static com.azure.v2.data.appconfiguration.models.KeyValueFields.KEY;
import static com.azure.v2.data.appconfiguration.models.KeyValueFields.LABEL;
import static com.azure.v2.data.appconfiguration.models.KeyValueFields.VALUE;

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

        final AzureAppConfigurationClient client = new AzureAppConfigurationClientBuilder()
            .connectionString(connectionString)
            .buildClient();

        // Name of the key to add to the configuration service.
        final String key = "hello";
        final String value = "world";

        System.out.println("Beginning of synchronous sample...");

        KeyValue setting = client.putKeyValue(key, null, "l1", null, null, null, new KeyValue().setLabel("label").setValue(value));

        System.out.printf("[SetConfigurationSetting] Key: %s, Value: %s", setting.getKey(), setting.getValue());

        setting = client.getKeyValueWithResponse(key, null, "l1", Arrays.asList(KEY, LABEL, VALUE), null, null, null, null, null).getValue();
        System.out.printf("[GetConfigurationSetting] Key: %s, Value: %s%n", setting.getKey(), setting.getValue());

        setting = client.deleteKeyValueWithResponse(key, null, "l1", null, null, null).getValue();
        System.out.printf("[DeleteConfigurationSetting] Key: %s, Value: %s%n", setting.getKey(), setting.getValue());

        System.out.println("End of synchronous sample.");
    }
}
