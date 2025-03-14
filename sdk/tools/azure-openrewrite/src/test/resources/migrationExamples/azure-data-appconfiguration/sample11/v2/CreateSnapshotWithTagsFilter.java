// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.appconfiguration;

import com.azure.core.util.Configuration;
import com.azure.core.util.Context;
import com.azure.core.util.polling.PollOperationDetails;
import com.azure.core.util.polling.SyncPoller;
import com.azure.data.appconfiguration.models.ConfigurationSetting;
import com.azure.data.appconfiguration.models.ConfigurationSettingsFilter;
import com.azure.data.appconfiguration.models.ConfigurationSnapshot;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.azure.data.appconfiguration.implementation.Utility.getTagsFilterInString;

/**
 * Sample demonstrates how to create configuration setting snapshot with tags filter, and list settings by snapshot name.
 */
public class CreateSnapshotWithTagsFilter {
    /**
     * Runs the sample demonstrates how to create, retrieve, archive, recover a configuration setting snapshot, and
     * list settings by snapshot name.
     *
     * @param args Unused. Arguments to the program.
     */
    public static void main(String[] args) {
        // The connection string value can be obtained by going to your App Configuration instance in the Azure portal
        // and navigating to "Access Keys" page under the "Settings" section.
        String connectionString = Configuration.getGlobalConfiguration().get("AZURE_APPCONFIG_CONNECTION_STRING");

        // Instantiate a client that will be used to call the service.
        final ConfigurationClient client = new ConfigurationClientBuilder()
                .connectionString(connectionString)
                .buildClient();

        // Prepare settings with tags
        Map<String, String> tags = new HashMap<>();
        tags.put("release", "first");
        Map<String, String> tags2 = new HashMap<>();
        tags2.put("release", "first");
        tags2.put("release2", "second");
        ConfigurationSetting setting1 = client.setConfigurationSetting(new ConfigurationSetting().setKey("keyForTag1").setValue("value1").setTags(tags));
        System.out.printf("Key: %s, Value: %s, Tags: %s%n", setting1.getKey(), setting1.getValue(), setting1.getTags());
        ConfigurationSetting setting2 = client.setConfigurationSetting(new ConfigurationSetting().setKey("keyForTag2").setValue("value2").setTags(tags2));
        System.out.printf("Key: %s, Value: %s, Tags: %s%n", setting2.getKey(), setting2.getValue(), setting2.getTags());
        ConfigurationSetting setting3 = client.setConfigurationSetting(new ConfigurationSetting().setKey("key3WithoutTag").setValue("value3"));
        System.out.printf("Key: %s, Value: %s, Tags: %s%n", setting3.getKey(), setting3.getValue(), setting3.getTags());

        // Prepare the snapshot filters with key filter and tags filter
        List<String> tagsFilterInString = getTagsFilterInString(tags2);
        List<ConfigurationSettingsFilter> filters = new ArrayList<>();
        // Key Name also supports RegExp but only support prefix end with "*", such as "k*" and is case-sensitive.
        System.out.println("Prepare tags filter = " + tagsFilterInString);
        filters.add(new ConfigurationSettingsFilter("key*").setTags(tagsFilterInString));

        // Create a snapshot
        String snapshotName = "{snapshotName}";
        SyncPoller<PollOperationDetails, ConfigurationSnapshot> poller =
                client.beginCreateSnapshot(snapshotName, new ConfigurationSnapshot(filters), Context.NONE);
        poller.setPollInterval(Duration.ofSeconds(10));
        poller.waitForCompletion();
        ConfigurationSnapshot snapshot = poller.getFinalResult();
        System.out.printf("Snapshot name=%s is created at %s, snapshot status is %s.%n",
                snapshot.getName(), snapshot.getCreatedAt(), snapshot.getStatus());

        // List the configuration settings in the snapshot
        client.listConfigurationSettingsForSnapshot(snapshotName).forEach(
                settingInSnapshot -> {
                    System.out.printf("[ConfigurationSetting In Snapshot] Key: %s, Value: %s, Tags:%s.%n",
                            settingInSnapshot.getKey(), settingInSnapshot.getValue(), settingInSnapshot.getTags());
                }
        );

        System.out.println("End of synchronous sample.");
    }
}
