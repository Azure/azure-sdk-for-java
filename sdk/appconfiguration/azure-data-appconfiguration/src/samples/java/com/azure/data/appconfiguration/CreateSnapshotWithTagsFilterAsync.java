// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.appconfiguration;

import com.azure.core.util.Configuration;
import com.azure.data.appconfiguration.models.ConfigurationSetting;
import com.azure.data.appconfiguration.models.ConfigurationSettingsFilter;
import com.azure.data.appconfiguration.models.ConfigurationSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.azure.data.appconfiguration.implementation.Utility.getTagsFilterInString;

/**
 * Sample demonstrates how to create configuration setting snapshot with tags filter, and list settings by snapshot name
 * asynchronously.
 */
public class CreateSnapshotWithTagsFilterAsync {
    /**
     * Runs the sample demonstrates how to create configuration setting snapshot with tags filter, and list settings by snapshot name
     * asynchronously.
     *
     * @param args Unused. Arguments to the program.
     * @throws InterruptedException when a thread is waiting, sleeping, or otherwise occupied,
     * and the thread is interrupted, either before or during the activity.
     */
    public static void main(String[] args) throws InterruptedException {
        // The connection string value can be obtained by going to your App Configuration instance in the Azure portal
        // and navigating to "Access Keys" page under the "Settings" section.
        String connectionString = Configuration.getGlobalConfiguration().get("AZURE_APPCONFIG_CONNECTION_STRING");

        // Asynchronous sample
        // Instantiate a client that will be used to call the service.
        final ConfigurationAsyncClient client = new ConfigurationClientBuilder()
                .connectionString(connectionString)
                .buildAsyncClient();

        // Prepare settings with tags
        Map<String, String> tags = new HashMap<>();
        tags.put("release", "first");
        Map<String, String> tags2 = new HashMap<>();
        tags2.put("release", "first");
        tags2.put("release2", "second");
        client.setConfigurationSetting(new ConfigurationSetting().setKey("keyForTag1").setValue("value1").setTags(tags))
                .subscribe(setting -> System.out.printf("Key: %s, Value: %s, Tags: %s%n", setting.getKey(), setting.getValue(), setting.getTags()));

        client.setConfigurationSetting(new ConfigurationSetting().setKey("keyForTag2").setValue("value2").setTags(tags2))
                .subscribe(setting -> System.out.printf("Key: %s, Value: %s, Tags: %s%n", setting.getKey(), setting.getValue(), setting.getTags()));

        client.setConfigurationSetting(new ConfigurationSetting().setKey("key3WithoutTag").setValue("value3"))
                .subscribe(setting -> System.out.printf("Key: %s, Value: %s, Tags: %s%n", setting.getKey(), setting.getValue(), setting.getTags()));
        TimeUnit.MILLISECONDS.sleep(1000);

        // Prepare the snapshot filters with key filter and tags filter
        List<String> tagsFilterInString = getTagsFilterInString(tags2);
        List<ConfigurationSettingsFilter> filters = new ArrayList<>();
        // Key Name also supports RegExp but only support prefix end with "*", such as "k*" and is case-sensitive.
        System.out.println("Prepare tags filter = " + tagsFilterInString);
        filters.add(new ConfigurationSettingsFilter("key*").setTags(tagsFilterInString));
        String snapshotName = "{snapshotName}6";

        client.beginCreateSnapshot(snapshotName, new ConfigurationSnapshot(filters))
                .flatMap(result -> result.getFinalResult())
                .subscribe(
                        snapshot -> {
                            System.out.printf("Snapshot name=%s is created at %s, snapshot status is %s.%n",
                                    snapshot.getName(), snapshot.getCreatedAt(), snapshot.getStatus());
                        },
                        ex -> System.out.printf("Error on creating a snapshot=%s, with error=%s.%n", snapshotName, ex.getMessage()),
                        () -> System.out.println("Successfully created a snapshot."));

        TimeUnit.MINUTES.sleep(1);

        // List the configuration settings in the snapshot
        client.listConfigurationSettingsForSnapshot(snapshotName).subscribe(
                settingInSnapshot -> {
                    System.out.printf("[ConfigurationSetting In Snapshot] Key: %s, Value: %s, Tags:%s.%n",
                            settingInSnapshot.getKey(), settingInSnapshot.getValue(), settingInSnapshot.getTags());
                },
                ex -> System.out.printf("Error on listing settings in snapshot=%s, with error=%s.%n", snapshotName, ex.getMessage()),
                () -> System.out.println("Successfully listed settings."));

        TimeUnit.MILLISECONDS.sleep(1000);
    }
}
