// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.appconfiguration;

import com.azure.data.appconfiguration.models.ConfigurationSetting;

import java.util.Arrays;
import java.util.List;

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
        ConfigurationClient client = new ConfigurationClientBuilder().connectionString(connectionString).buildClient();

        // Prepare a list of watching settings and update one same setting value to the service.
        String prodDBConnectionKey = "prodDBConnection";
        String prodDBConnectionLabel = "prodLabel";

        List<ConfigurationSetting> watchingSettings = Arrays.asList(
            client.addConfigurationSetting(prodDBConnectionKey, prodDBConnectionLabel, "prodValue"),
            client.addConfigurationSetting("stageDBConnection", "stageLabel", "stageValue")
        );

        ConfigurationSetting updateSetting = client.setConfigurationSetting(prodDBConnectionKey, prodDBConnectionLabel, "updateProdValue");
        System.out.printf("Updated setting's key: %s, value: %s, ETag: %s.%n",
            updateSetting.getKey(), updateSetting.getValue(), updateSetting.getETag());

        // Now, check to see if we need to update the list of existing watching settings. Update it if
        // refresh of existing watching setting is needed,
        refresh(client, watchingSettings, Arrays.asList(updateSetting));

        // Cleaning up after ourselves by deleting the values.
        watchingSettings.forEach(setting -> {
            System.out.printf("Deleting Setting's key: %s, value: %s.%n", setting.getKey(), setting.getValue());
            client.deleteConfigurationSetting(setting.getKey(), setting.getLabel());
        });
    }

    private static boolean refresh(ConfigurationClient client, List<ConfigurationSetting> watchSettings, List<ConfigurationSetting> latestSettings) {
        for (ConfigurationSetting watchSetting : watchSettings) {
            ConfigurationSetting latestSetting = client.getConfigurationSetting(watchSetting.getKey(), watchSetting.getLabel());
            String latestETag = latestSetting.getETag();
            String previousETag = watchSetting.getETag();
            if (!latestETag.equals(previousETag)) {
                System.out.printf(
                    "Some keys in watching key store matching the key [%s] and label [%s] is updated, preview ETag value [%s] not " +
                        "equals to current value [%s], will send refresh event.%n",
                    watchSetting.getKey(), watchSetting.getLabel(), previousETag, latestETag);
                // A refresh will trigger once the
                return true;
            }
        }
        // Don't need to refresh
        return false;
    }
}
