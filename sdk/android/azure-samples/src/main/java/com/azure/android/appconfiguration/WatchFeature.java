// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.appconfiguration;

import android.util.Log;

import com.azure.data.appconfiguration.ConfigurationClient;
import com.azure.data.appconfiguration.ConfigurationClientBuilder;
import com.azure.data.appconfiguration.models.ConfigurationSetting;
import com.azure.identity.ClientSecretCredential;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class WatchFeature {
    /**
     * Runs the sample algorithm and demonstrates how to read configuration setting revision history.
     */

    private static final String TAG = "WatchFeatureOutput";
    public static void main(String endpoint, ClientSecretCredential credential) {
        // The connection string value can be obtained by going to your App Configuration instance in the Azure portal
        // and navigating to "Access Keys" page under the "Settings" section.

        // Instantiate a client that will be used to call the service.
        ConfigurationClient client = new ConfigurationClientBuilder()
                .credential(credential)
                .endpoint(endpoint)
                .buildClient();

        // Prepare a list of watching settings and update one same setting value to the service.
        String prodDBConnectionKey = "prodDBConnection";
        String prodDBConnectionLabel = "prodLabel";

        // Assume we have a list of watching setting that stored somewhere.
        List<ConfigurationSetting> watchingSettings = Arrays.asList(
            client.addConfigurationSetting(prodDBConnectionKey, prodDBConnectionLabel, "prodValue"),
            client.addConfigurationSetting("stageDBConnection", "stageLabel", "stageValue")
        );

        Log.i(TAG, "Watching settings:");
        for (ConfigurationSetting setting : watchingSettings) {
            Log.i(TAG, String.format("\tkey=%s, label=%s, value=%s, ETag=%s.%n",
                setting.getKey(), setting.getLabel(), setting.getValue(), setting.getETag()));
        }

        // One of the watching settings is been updated by someone in other place.
        ConfigurationSetting updatedSetting = client.setConfigurationSetting(
            prodDBConnectionKey, prodDBConnectionLabel, "updatedProdValue");
        Log.i(TAG, "Updated settings:");
        Log.i(TAG, String.format("\tkey=%s, label=%s, value=%s, ETag=%s.%n",
            updatedSetting.getKey(), updatedSetting.getLabel(), updatedSetting.getValue(), updatedSetting.getETag()));

        // Updates the watching settings if needed, and only returns a list of updated settings.
        List<ConfigurationSetting> refreshedSettings = refresh(client, watchingSettings);

        Log.i(TAG, "Refreshed settings:");
        for (ConfigurationSetting setting : refreshedSettings) {
            Log.i(TAG, String.format("\tkey=%s, label=%s, value=%s, ETag=%s.%n",
                setting.getKey(), setting.getLabel(), setting.getValue(), setting.getETag()));
        }

        // Cleaning up after ourselves by deleting the values.
        Log.i(TAG, "Deleting settings:");
        watchingSettings.forEach(setting -> {
            client.deleteConfigurationSetting(setting.getKey(), setting.getLabel());
            Log.i(TAG, String.format("\tkey: %s, value: %s.%n", setting.getKey(), setting.getValue()));
        });
    }

    /**
     * A refresh method that runs every day to update settings and returns a updated settings.
     *
     * @param client a configuration client.
     * @param watchSettings a list of settings in the watching store.
     *
     * @return a list of updated settings that doesn't match previous ETag value.
     */
    private static List<ConfigurationSetting> refresh(ConfigurationClient client,
        List<ConfigurationSetting> watchSettings) {
        return watchSettings
                   .stream()
                   .filter(setting -> {
                       ConfigurationSetting retrievedSetting = client.getConfigurationSetting(setting.getKey(),
                           setting.getLabel());
                       String latestETag = retrievedSetting.getETag();
                       String watchingETag = setting.getETag();
                       if (!latestETag.equals(watchingETag)) {
                           Log.i(TAG, String.format(
                               "Some keys in watching key store matching the key [%s] and label [%s] is updated, "
                                   + "preview ETag value [%s] not equals to current value [%s].%n",
                               retrievedSetting.getKey(), retrievedSetting.getLabel(), watchingETag, latestETag));
                           setting.setETag(latestETag).setValue(retrievedSetting.getValue());
                           return true;
                       }
                       return false;
                   })
                   .collect(Collectors.toList());
    }
}
