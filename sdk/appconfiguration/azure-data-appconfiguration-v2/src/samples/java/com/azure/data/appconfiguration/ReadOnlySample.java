// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.appconfiguration;

import com.azure.v2.data.appconfiguration.AzureAppConfigurationClient;
import com.azure.v2.data.appconfiguration.AzureAppConfigurationClientBuilder;
import com.azure.v2.data.appconfiguration.models.KeyValue;
import io.clientcore.core.utils.configuration.Configuration;

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
        String connectionString = Configuration.getGlobalConfiguration().get("AZURE_APPCONFIG_CONNECTION_STRING");

        final AzureAppConfigurationClient client = new AzureAppConfigurationClientBuilder()
            .connectionString(connectionString)
//            .credential(new DefaultAzureCredentialBuilder().build())
            .buildClient();

        // Name of the key to add to the configuration service.
        final String key = "hello";
        final String value = "world";

        KeyValue setting = client.putKeyValue(key, null, null, null, null, null, new KeyValue().setValue(value));
        System.out.printf("[SetConfigurationSetting] Key: %s, Value: %s", setting.getKey(), setting.getValue());


        // Read-Only
        final KeyValue readOnlySetting = client.putLock(setting.getKey(), null);
        System.out.printf("Setting is read-only now, Key: %s, Value: %s",
            readOnlySetting.getKey(), readOnlySetting.getValue());
        // Clear Read-Only
        final KeyValue clearedReadOnlySetting = client.deleteLock(setting.getKey(), null);
        System.out.printf("Setting is no longer read-only, Key: %s, Value: %s",
            clearedReadOnlySetting.getKey(), clearedReadOnlySetting.getValue());
    }
}
