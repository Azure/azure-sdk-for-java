// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.appconfiguration;

import android.util.Log;

import com.azure.data.appconfiguration.ConfigurationClient;
import com.azure.data.appconfiguration.ConfigurationClientBuilder;
import com.azure.data.appconfiguration.models.ConfigurationSetting;
import com.azure.identity.ClientSecretCredential;

/**
 * Sample demonstrates how to add, get, and delete a configuration setting.
 */
public class HelloWorld {
    /**
     * Runs the sample algorithm and demonstrates how to add, get, and delete a configuration setting.
     */

    private static final String TAG = "AppconfigHelloWorldOutput";
    public static void main(String endpoint, ClientSecretCredential credential) {
        // The connection string value can be obtained by going to your App Configuration instance in the Azure portal
        // and navigating to "Access Keys" page under the "Settings" section.

        final ConfigurationClient client = new ConfigurationClientBuilder()
                .credential(credential)
                .endpoint(endpoint)
                .buildClient();

        // Name of the key to add to the configuration service.
        final String key = "hello";
        final String value = "world";

        Log.i(TAG, "Beginning of synchronous sample...");

        ConfigurationSetting setting = client.setConfigurationSetting(key, null, value);
        Log.i(TAG, String.format("[SetConfigurationSetting] Key: %s, Value: %s", setting.getKey(), setting.getValue()));

        setting = client.getConfigurationSetting(key, null, null);
        Log.i(TAG, String.format("[GetConfigurationSetting] Key: %s, Value: %s", setting.getKey(), setting.getValue()));

        setting = client.deleteConfigurationSetting(key, null);
        Log.i(TAG, String.format("[DeleteConfigurationSetting] Key: %s, Value: %s", setting.getKey(), setting.getValue()));

        Log.i(TAG, "End of synchronous sample.");
    }
}
