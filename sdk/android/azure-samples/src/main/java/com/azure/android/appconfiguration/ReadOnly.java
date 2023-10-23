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
public class ReadOnly {
    /**
     * Runs the sample algorithm and demonstrates how to add, get, and delete a configuration setting.
     */

    private static final String TAG = "AppconfigReadOnlyOutput";

    public static void main(String endpoint, ClientSecretCredential credential) {

        final ConfigurationClient client = new ConfigurationClientBuilder()
            .credential(credential)
            .endpoint(endpoint)
            .buildClient();

        // Name of the key to add to the configuration service.
        final String key = "hello";
        final String value = "world";

        final ConfigurationSetting setting = client.setConfigurationSetting(key, null, value);
        // Read-Only
        final ConfigurationSetting readOnlySetting = client.setReadOnly(setting.getKey(), setting.getLabel(), true);
        Log.i(TAG, String.format("Setting is read-only now, Key: %s, Value: %s",
            readOnlySetting.getKey(), readOnlySetting.getValue()));

        // Clear Read-Only
        final ConfigurationSetting clearedReadOnlySetting = client.setReadOnly(setting.getKey(), setting.getLabel(), false);
        Log.i(TAG, String.format("Setting is no longer read-only, Key: %s, Value: %s",
            clearedReadOnlySetting.getKey(), clearedReadOnlySetting.getValue()));

    }
}
