// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.appconfiguration;

import android.util.Log;

import com.azure.core.http.rest.PagedIterable;
import com.azure.data.appconfiguration.ConfigurationClient;
import com.azure.data.appconfiguration.ConfigurationClientBuilder;
import com.azure.data.appconfiguration.models.ConfigurationSetting;
import com.azure.data.appconfiguration.models.SecretReferenceConfigurationSetting;
import com.azure.data.appconfiguration.models.SettingSelector;
import com.azure.identity.ClientSecretCredential;


/**
 * Sample demonstrates how to add, get, list, and delete a secret reference configuration setting.
 */
public class SecretReferenceConfigurationSettingSample {
    /**
     * Runs the sample algorithm and demonstrates how to add, get, list, and delete a secret reference configuration
     * setting.
     *
     * @param args Unused. Arguments to the program.
     */

    private static final String TAG = "SecretReferenceConfigurationSettingOutput";
    public static void main(String endpoint, ClientSecretCredential credential) {
        // The connection string value can be obtained by going to your App Configuration instance in the Azure portal
        // and navigating to "Access Keys" page under the "Settings" section.

        final ConfigurationClient client = new ConfigurationClientBuilder()
                .credential(credential)
                .endpoint(endpoint)
                .buildClient();

        // Name of the key to add to the configuration service.
        final String key = "hello";
        final String secretIdValue = "{the-keyVault-secret-id-uri}";

        Log.i(TAG, "Beginning of synchronous sample...");

        SecretReferenceConfigurationSetting referenceConfigurationSetting =
            new SecretReferenceConfigurationSetting(key, secretIdValue);

        // setConfigurationSetting adds or updates a setting to Azure App Configuration store. Alternatively, you can
        // call addConfigurationSetting which only succeeds if the setting does not exist in the store. Or,
        // you can call setConfigurationSetting to update a setting that is already present in the store.
        Log.i(TAG, "[Set-SecretReferenceConfigurationSetting]");
        SecretReferenceConfigurationSetting setting = (SecretReferenceConfigurationSetting) client.setConfigurationSetting(referenceConfigurationSetting);
        printSecretReferenceConfigurationSetting(setting);

        Log.i(TAG, "[Get-SecretReferenceConfigurationSetting]");
        setting = (SecretReferenceConfigurationSetting) client.getConfigurationSetting(setting);
        printSecretReferenceConfigurationSetting(setting);

        Log.i(TAG, "[List-SecretReferenceConfigurationSetting]");
        final PagedIterable<ConfigurationSetting> configurationSettings = client.listConfigurationSettings(new SettingSelector());
        for (ConfigurationSetting configurationSetting : configurationSettings) {
            if (configurationSetting instanceof SecretReferenceConfigurationSetting) {
                Log.i(TAG, "-Listing-SecretReferenceConfigurationSetting");
                printSecretReferenceConfigurationSetting((SecretReferenceConfigurationSetting) configurationSetting);
            } else {
                Log.i(TAG, "-Listing-non-SecretReferenceConfigurationSetting");
                Log.i(TAG, String.format("Key: %s, Value: %s%n", configurationSetting.getKey(), configurationSetting.getValue()));
            }
        }

        Log.i(TAG, "[Delete-SecretReferenceConfigurationSetting");
        setting = (SecretReferenceConfigurationSetting) client.deleteConfigurationSetting(setting);
        printSecretReferenceConfigurationSetting(setting);

        Log.i(TAG, "End of synchronous sample.");
    }

    private static void printSecretReferenceConfigurationSetting(SecretReferenceConfigurationSetting setting) {
        Log.i(TAG, String.format("Key: %s, Secret ID: %s, Content Type: %s, Value: %s%n", setting.getKey(),
            setting.getSecretId(), setting.getContentType(), setting.getValue()));
    }
}
