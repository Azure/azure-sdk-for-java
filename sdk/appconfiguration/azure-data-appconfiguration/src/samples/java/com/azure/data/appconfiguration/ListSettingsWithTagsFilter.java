// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.appconfiguration;

import com.azure.core.http.rest.PagedIterable;
import com.azure.core.util.Configuration;
import com.azure.data.appconfiguration.models.ConfigurationSetting;
import com.azure.data.appconfiguration.models.SettingSelector;

import javax.net.ssl.SSLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.azure.data.appconfiguration.implementation.Utility.getTagsFilterInString;

/**
 * Sample demonstrates how to list settings with tags filter.
 */
public class ListSettingsWithTagsFilter {
    /**
     * Runs the sample algorithm and demonstrates how to list settings with tags filter.
     *
     * @param args Unused. Arguments to the program.
     */
    public static void main(String[] args) throws SSLException {
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
        ConfigurationSetting setting1 = client.setConfigurationSetting(new ConfigurationSetting().setKey("keyForTag1").setValue("value1").setTags(tags));
        System.out.printf("Key: %s, Value: %s, Tags: %s%n", setting1.getKey(), setting1.getValue(), setting1.getTags());
        ConfigurationSetting setting2 = client.setConfigurationSetting(new ConfigurationSetting().setKey("keyForTag2").setValue("value2").setTags(tags2));
        System.out.printf("Key: %s, Value: %s, Tags: %s%n", setting2.getKey(), setting2.getValue(), setting2.getTags());
        ConfigurationSetting setting3 = client.setConfigurationSetting(new ConfigurationSetting().setKey("key3WithoutTag").setValue("value3"));
        System.out.printf("Key: %s, Value: %s, Tags: %s%n", setting3.getKey(), setting3.getValue(), setting3.getTags());

        List<String> tagsFilterInString = getTagsFilterInString(tags2);
        System.out.println("List settings with tags filter = " + tagsFilterInString);
        PagedIterable<ConfigurationSetting> configurationSettings = client.listConfigurationSettings(new SettingSelector().setKeyFilter("key*")
                .setTagsFilter(tagsFilterInString));
        configurationSettings.forEach(setting -> System.out.printf(
                "\tKey: %s, Value: %s, Tags: %s%n", setting.getKey(), setting.getValue(), setting.getTags()));
    }
}
