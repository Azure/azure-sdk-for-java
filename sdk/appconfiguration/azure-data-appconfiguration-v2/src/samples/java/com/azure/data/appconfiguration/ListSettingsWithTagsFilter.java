// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.appconfiguration;

import com.azure.v2.data.appconfiguration.AzureAppConfigurationClient;
import com.azure.v2.data.appconfiguration.AzureAppConfigurationClientBuilder;
import com.azure.v2.data.appconfiguration.models.KeyValue;
import io.clientcore.core.http.paging.PagedIterable;
import io.clientcore.core.utils.CoreUtils;
import io.clientcore.core.utils.configuration.Configuration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Sample demonstrates how to list settings with tags filter.
 */
public class ListSettingsWithTagsFilter {
    /**
     * Runs the sample algorithm and demonstrates how to list settings with tags filter.
     *
     * @param args Unused. Arguments to the program.
     */
    public static void main(String[] args) {
        String connectionString = Configuration.getGlobalConfiguration().get("AZURE_APPCONFIG_CONNECTION_STRING");

        final AzureAppConfigurationClient client = new AzureAppConfigurationClientBuilder()
            .connectionString(connectionString)
            .buildClient();

        Map<String, String> tags = new HashMap<>();
        tags.put("release", "first");
        Map<String, String> tags2 = new HashMap<>();
        tags2.put("release", "first");
        tags2.put("release2", "second");


        KeyValue setting1 = client.putKeyValue("keyForTag1", null, "prod1",  null, null, null, new KeyValue().setValue("value1").setTags(tags));
        System.out.printf("Key: %s, Value: %s, Tags: %s%n", setting1.getKey(), setting1.getValue(), setting1.getTags());
        KeyValue setting2 = client.putKeyValue("keyForTag2", null, "prod1",  null, null, null, new KeyValue().setValue("value2").setTags(tags2));
        System.out.printf("Key: %s, Value: %s, Tags: %s%n", setting2.getKey(), setting2.getValue(), setting2.getTags());
        KeyValue setting3 = client.putKeyValue("key3WithoutTag", null, "prod1",  null, null, null, new KeyValue().setValue("value3"));
        System.out.printf("Key: %s, Value: %s, Tags: %s%n", setting3.getKey(), setting3.getValue(), setting3.getTags());

        List<String> tagsFilterInString = getTagsFilterInString(tags2);
        System.out.println("List settings with tags filter = " + tagsFilterInString);
        PagedIterable<KeyValue> configurationSettings = client.getKeyValues(
            null, "key*", null, null, null,
            null, null, null, null, null,
            tagsFilterInString, null);
        configurationSettings.forEach(setting -> System.out.printf(
                "\tKey: %s, Value: %s, Tags: %s%n", setting.getKey(), setting.getValue(), setting.getTags()));
    }


    // Convert the Map<String, String> to a filter string
    public static List<String> getTagsFilterInString(Map<String, String> tagsFilter) {
        List<String> tagsFilters;

        if (tagsFilter != null) {
            tagsFilters = new ArrayList<>();
            tagsFilter.forEach((key, value) -> {
                if (!CoreUtils.isNullOrEmpty(key) && !CoreUtils.isNullOrEmpty(value)) {
                    tagsFilters.add(key + "=" + value);
                }
            });
        } else {
            tagsFilters = null;
        }
        return tagsFilters;
    }
}
