// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.appconfiguration;

import com.azure.core.util.Configuration;
import com.azure.data.appconfiguration.models.ConfigurationSetting;
import com.azure.data.appconfiguration.models.SettingSelector;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.azure.data.appconfiguration.implementation.Utility.getTagsFilterInString;

/**
 * Sample demonstrates how to list revisions with tags filter asynchronously.
 */
public class ReadRevisionHistoryWithTagsFilterAsync {
    /**
     * Runs the sample algorithm and demonstrates how to list revisions with tags filter asynchronously.
     *
     * @param args Unused. Arguments to the program.
     * @throws InterruptedException when a thread is waiting, sleeping, or otherwise occupied,
     * and the thread is interrupted, either before or during the activity.
     */
    public static void main(String[] args) throws InterruptedException {
        // The connection string value can be obtained by going to your App Configuration instance in the Azure portal
        // and navigating to the "Access Keys" page under the "Settings" section.
        String connectionString = Configuration.getGlobalConfiguration().get("AZURE_APPCONFIG_CONNECTION_STRING");

        // Instantiate a client that will be used to call the service.
        ConfigurationAsyncClient client = new ConfigurationClientBuilder()
                .connectionString(connectionString)
                .buildAsyncClient();

        Map<String, String> tags = new HashMap<>();
        tags.put("release", "first");
        Map<String, String> tags2 = new HashMap<>();
        tags2.put("release", "first");
        tags2.put("release2", "second");

        String sameKey = "sameKey";
        client.setConfigurationSetting(new ConfigurationSetting().setKey(sameKey).setValue("value1").setTags(tags))
                .subscribe(setting -> System.out.printf("Key: %s, Value: %s, Tags: %s%n", setting.getKey(),
                        setting.getValue(), setting.getTags()));
        TimeUnit.MILLISECONDS.sleep(1000);

        client.setConfigurationSetting(new ConfigurationSetting().setKey(sameKey).setValue("value2").setTags(tags2))
                .subscribe(setting -> System.out.printf("Key: %s, Value: %s, Tags: %s%n", setting.getKey(),
                        setting.getValue(), setting.getTags()));
        TimeUnit.MILLISECONDS.sleep(1000);

        client.setConfigurationSetting(new ConfigurationSetting().setKey(sameKey).setValue("value3"))
                .subscribe(setting -> System.out.printf("Key: %s, Value: %s, Tags: %s%n", setting.getKey(),
                        setting.getValue(), setting.getTags()));
        TimeUnit.MILLISECONDS.sleep(1000);

        List<String> tagsFilterInString = getTagsFilterInString(tags2);
        System.out.println("List revisions with tags filter = " + tagsFilterInString);
        client.listRevisions(new SettingSelector().setTagsFilter(tagsFilterInString)).subscribe(
                setting -> System.out.printf("\tKey: %s, Value: %s, Tags: %s%n",
                        setting.getKey(), setting.getValue(), setting.getTags()));
        TimeUnit.MILLISECONDS.sleep(4000);
    }
}
