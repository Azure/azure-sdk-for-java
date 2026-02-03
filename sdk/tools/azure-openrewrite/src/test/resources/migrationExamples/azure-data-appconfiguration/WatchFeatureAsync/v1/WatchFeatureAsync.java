// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.appconfiguration;

import com.azure.data.appconfiguration.models.ConfigurationSetting;
import com.azure.data.appconfiguration.models.SettingSelector;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class WatchFeatureAsync {
    /**
     * Runs the sample algorithm and demonstrates how to read configuration setting revision history.
     *
     * @param args Unused. Arguments to the program.
     */
    public static void main(String[] args) throws InterruptedException {
        // The connection string value can be obtained by going to your App Configuration instance in the Azure portal
        // and navigating to "Access Keys" page under the "Settings" section.
        String connectionString = "endpoint={endpoint_value};id={id_value};secret={secret_value}";

        // Instantiate a client that will be used to call the service.
        ConfigurationAsyncClient client = new ConfigurationClientBuilder()
                                              .connectionString(connectionString)
                                              .buildAsyncClient();

        // Prepare a list of watching settings and update one same setting value to the service.
        String prodDBConnectionKey = "prodDBConnection";
        String prodDBConnectionLabel = "prodLabel";
        String updatedProdDBConnectionValue = "updateProdValue";

        // Assume we have a list of watching setting that stored somewhere.
        List<ConfigurationSetting> watchingSettings = new ArrayList<>();
        Flux.concat(
            client.addConfigurationSetting(prodDBConnectionKey, prodDBConnectionLabel, "prodValue"),
            client.addConfigurationSetting("stageDBConnection", "stageLabel", "stageValue"))
            .then(client.listConfigurationSettings(new SettingSelector().setKeyFilter("*")).collectList())
            .subscribe(
                settings -> watchingSettings.addAll(settings),
                error -> System.err.printf("There was an error while adding the settings: %s.%n", error),
                () -> System.out.println("Add settings completed.")
            );

        // The .subscribe() creation and assignment is not a blocking call. For the purpose of this example, we sleep
        // the thread so the program does not end before the send operation is complete. Using .block() instead of
        // .subscribe() will turn this into a synchronous call.
        TimeUnit.MILLISECONDS.sleep(1000);

        System.out.println("Watching settings:");
        for (ConfigurationSetting setting : watchingSettings) {
            System.out.printf("\tWatching key=%s, label=%s, value=%s, ETag=%s.%n",
                setting.getKey(), setting.getLabel(), setting.getValue(), setting.getETag());
        }
        TimeUnit.MILLISECONDS.sleep(1000);

        // One of the watching settings is been updated by someone in other place.
        client.setConfigurationSetting(prodDBConnectionKey, prodDBConnectionLabel, updatedProdDBConnectionValue)
            .subscribe(
                updatedSetting -> {
                    System.out.println("Updated settings:");
                    System.out.printf("\tUpdated key=%s, label=%s, value=%s, ETag=%s.%n",
                        updatedSetting.getKey(), updatedSetting.getLabel(), updatedSetting.getValue(),
                        updatedSetting.getETag());
                },
                error -> System.err.printf("There was an error while updating the setting: %s.%n", error),
                () -> System.out.printf("Update setting completed, key=%s, label=%s, value=%s.%n",
                    prodDBConnectionKey, prodDBConnectionLabel, updatedProdDBConnectionValue));
        TimeUnit.MILLISECONDS.sleep(1000);

        // Updates the watching settings if needed, and only returns a list of updated settings.
        List<ConfigurationSetting> refreshedSettings = refresh(client, watchingSettings);
        System.out.println("Refreshed settings:");
        for (ConfigurationSetting setting : refreshedSettings) {
            System.out.printf("\tRefreshed key=%s, label=%s, value=%s, ETag=%s.%n",
                setting.getKey(), setting.getLabel(), setting.getValue(), setting.getETag());
        }
        TimeUnit.MILLISECONDS.sleep(1000);

        // Cleaning up after ourselves by deleting the values.
        System.out.println("Deleting settings:");
        Stream<ConfigurationSetting> stream = watchingSettings == null ? Stream.empty() : watchingSettings.stream();
        Flux.merge(stream.map(setting -> {
            System.out.printf("\tDeleting key: %s, value: %s.%n", setting.getKey(), setting.getValue());
            return client.deleteConfigurationSettingWithResponse(setting, false);
        }).collect(Collectors.toList())).blockLast();
    }

    /**
     * A refresh method that runs every day to update settings and returns a updated settings.
     *
     * @param client a configuration client.
     * @param watchSettings a list of settings in the watching store.
     *
     * @return a list of updated settings that doesn't match previous ETag value.
     */
    private static List<ConfigurationSetting> refresh(ConfigurationAsyncClient client,
        List<ConfigurationSetting> watchSettings) {
        return watchSettings
                   .stream()
                   .filter(setting -> {
                       final boolean[] isUpdated = new boolean[1];
                       String key = setting.getKey();
                       String label = setting.getLabel();
                       client.getConfigurationSetting(key, label)
                           .subscribe(
                               retrievedSetting -> {
                                   String latestETag = retrievedSetting.getETag();
                                   String watchingETag = setting.getETag();
                                   if (!latestETag.equals(watchingETag)) {
                                       System.out.printf(
                                           "Some keys in watching key store matching the key [%s] and label [%s] is "
                                               + "updated, preview ETag value [%s] not equals to current value [%s].%n",
                                           retrievedSetting.getKey(), retrievedSetting.getLabel(), watchingETag,
                                           latestETag);
                                       setting.setETag(latestETag).setValue(retrievedSetting.getValue());
                                       isUpdated[0] = true;
                                   }
                               },
                               error -> System.err.printf("There was an error while retrieving the setting: %s.%n",
                                   error),
                               () -> System.out.printf("Retrieve setting completed, key=%s, label=%s.%n", key, label));

                       // The .subscribe() creation and assignment is not a blocking call. For the purpose of this
                       // example, we sleep the thread so the program does not end before the send operation is
                       // complete. Using .block() instead of .subscribe() will turn this into a synchronous call.
                       try {
                           TimeUnit.MILLISECONDS.sleep(1000);
                       } catch (InterruptedException e) {
                           e.printStackTrace();
                       }

                       return isUpdated[0];
                   })
                   .collect(Collectors.toList());
    }
}
