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
public class ListSettingsByTagFilter {
    public static void main(String[] args) {
        // The connection string value can be obtained by going to your App Configuration instance in the Azure portal
        // and navigating to the "Access Keys" page under the "Settings" section.
        String connectionString = Configuration.getGlobalConfiguration().get("AZURE_APPCONFIG_CONNECTION_STRING");

        // Instantiate a client that will be used to call the service.
        ConfigurationClient client = new ConfigurationClientBuilder()
                .connectionString(connectionString)
                .buildClient();

        Map<String, String> tags = new HashMap<>();
        tags.put("release", "{link/id}");
        ConfigurationSetting setting1 = client.setConfigurationSetting(new ConfigurationSetting().setKey("keyForTag1")
                .setValue("value1").setTags(tags));

        // List settings by tag filter
        PagedIterable<ConfigurationSetting> configurationSettings = client.listConfigurationSettings(new SettingSelector().setTagsFilter("release"));



        configurationSettings.forEach(setting -> {
            System.out.printf("Key: %s, Value: %s", setting.getKey(), setting.getValue());
        });
    }
}
