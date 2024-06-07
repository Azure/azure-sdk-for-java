// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.appconfiguration;

import com.azure.core.http.rest.PagedIterable;
import com.azure.core.util.Configuration;
import com.azure.data.appconfiguration.models.ConfigurationSetting;
import com.azure.data.appconfiguration.models.SettingSelector;

import java.util.HashMap;
import java.util.Map;

/**
 * Sample demonstrates how to list settings by tag filter.
 */
public class ListSettingsByTagsFilter {
    /**
     * Runs the sample algorithm and demonstrates how to list settings by tag filter.
     *
     * @param args Unused. Arguments to the program.
     */
    public static void main(String[] args) {

        // The connection string value can be obtained by going to your App Configuration instance in the Azure portal
        // and navigating to the "Access Keys" page under the "Settings" section.
        String connectionString = Configuration.getGlobalConfiguration().get("AZURE_APPCONFIG_CONNECTION_STRING");

        // Instantiate a client that will be used to call the service.
        ConfigurationClient client = new ConfigurationClientBuilder()
                .connectionString(connectionString)
                .buildClient();

        Map<String, String> tags = new HashMap<>();
        tags.put("release", "first");

        Map<String, String> tags2 = new HashMap<>();
        tags2.put("release", "first");
        tags2.put("release2", "second");
        client.setConfigurationSetting(new ConfigurationSetting().setKey("keyForTag1").setValue("value1").setTags(tags));
        client.setConfigurationSetting(new ConfigurationSetting().setKey("keyForTag2").setValue("value2"));
        client.setConfigurationSetting(new ConfigurationSetting().setKey("keyForTag3").setValue("value3"));
        client.setConfigurationSetting(new ConfigurationSetting().setKey("keyForTag4").setValue("value4").setTags(tags2));

        PagedIterable<ConfigurationSetting> configurationSettingsAll = client.listConfigurationSettings(null);
        configurationSettingsAll.forEach(setting -> {
            System.out.printf("All Key: %s, Labels: %s, Value: %s%n", setting.getKey(), setting.getLabel(), setting.getValue());
            Map<String, String> tags1 = setting.getTags();
            tags1.forEach((key, value) -> System.out.printf("\tTag: %s, Value: %s%n", key, value));
        });

        // List settings by tag filter
        PagedIterable<ConfigurationSetting> configurationSettings = client.listConfigurationSettings(new SettingSelector().setTagsFilter(tags));

        configurationSettings.forEach(setting -> {
            System.out.printf("Key: %s, Labels: %s, Value: %s%n", setting.getKey(), setting.getLabel(), setting.getValue());
            Map<String, String> tags1 = setting.getTags();
            tags1.forEach((key, value) -> System.out.printf("\tTag: %s, Value: %s%n", key, value));
        });
    }
}
