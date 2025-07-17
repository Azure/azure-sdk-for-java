// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.appconfiguration;

import com.azure.data.appconfiguration.models.ConfigurationSetting;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class WatchFeature {
    /**
     * Runs the sample algorithm and demonstrates how to read configuration setting revision history.
     *
     * @param args Unused. Arguments to the program.
     */
    public static void main(String[] args) {
        // The connection string value can be obtained by going to your App Configuration instance in the Azure portal
        // and navigating to "Access Keys" page under the "Settings" section.
        String connectionString = "endpoint={endpoint_value};id={id_value};secret={secret_value}";

        // Instantiate a client that will be used to call the service.
        ConfigurationClient client = new ConfigurationClientBuilder()
                                         .connectionString(connectionString)
                                         .buildClient();

        // Prepare a list of watching settings and update one same setting value to the service.
        String prodDBConnectionKey = "prodDBConnection";
        String prodDBConnectionLabel = "prodLabel";

        // Assume we have a list of watching setting that stored somewhere.
        List<ConfigurationSetting> watchingSettings = Arrays.asList(
            client.addConfigurationSetting(prodDBConnectionKey, prodDBConnectionLabel, "prodValue"),
            client.addConfigurationSetting("stageDBConnection", "stageLabel", "stageValue")
        );

        System.out.println("Watching settings:");
        for (ConfigurationSetting setting : watchingSettings) {
            System.out.printf("\tkey=%s, label=%s, value=%s, ETag=%s.%n",
                setting.getKey(), setting.getLabel(), setting.getValue(), setting.getETag());
        }

        // One of the watching settings is been updated by someone in other place.
        ConfigurationSetting updatedSetting = client.setConfigurationSetting(
            prodDBConnectionKey, prodDBConnectionLabel, "updatedProdValue");
        System.out.println("Updated settings:");
        System.out.printf("\tkey=%s, label=%s, value=%s, ETag=%s.%n",
            updatedSetting.getKey(), updatedSetting.getLabel(), updatedSetting.getValue(), updatedSetting.getETag());

        // Updates the watching settings if needed, and only returns a list of updated settings.
        List<ConfigurationSetting> refreshedSettings = refresh(client, watchingSettings);

        System.out.println("Refreshed settings:");
        for (ConfigurationSetting setting : refreshedSettings) {
            System.out.printf("\tkey=%s, label=%s, value=%s, ETag=%s.%n",
                setting.getKey(), setting.getLabel(), setting.getValue(), setting.getETag());
        }

        // Cleaning up after ourselves by deleting the values.
        System.out.println("Deleting settings:");
        watchingSettings.forEach(setting -> {
            client.deleteConfigurationSetting(setting.getKey(), setting.getLabel());
            System.out.printf("\tkey: %s, value: %s.%n", setting.getKey(), setting.getValue());
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
                           System.out.printf(
                               "Some keys in watching key store matching the key [%s] and label [%s] is updated, "
                                   + "preview ETag value [%s] not equals to current value [%s].%n",
                               retrievedSetting.getKey(), retrievedSetting.getLabel(), watchingETag, latestETag);
                           setting.setETag(latestETag).setValue(retrievedSetting.getValue());
                           return true;
                       }
                       return false;
                   })
                   .collect(Collectors.toList());
    }
}
