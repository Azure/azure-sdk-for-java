// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.data.appconfiguration;


import com.azure.v2.data.appconfiguration.models.ConfigurationSetting;
import com.azure.v2.data.appconfiguration.models.SettingSelector;
import io.clientcore.core.http.models.RequestContext;
import io.clientcore.core.http.paging.PagedIterable;
import io.clientcore.core.utils.configuration.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Sample demonstrates how to read configuration setting revision history.
 */
public class ReadRevisionHistory {
    /**
     * Runs the sample algorithm and demonstrates how to read configuration setting revision history.
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

        final String key = "hello";

        // Adding a couple of settings and then fetching all the settings in our repository.
        final List<ConfigurationSetting> settings = new ArrayList<>();
        settings.add(client.addConfigurationSetting(key, null, "world"));
        settings.add(client.setConfigurationSetting(key, null, "newValue"));


        PagedIterable<ConfigurationSetting> configurationSettings = client.listRevisions(new SettingSelector().setKeyFilter(key));
        configurationSettings.forEach(configurationSetting -> {
            System.out.printf("Key: %s, Value: %s%n", configurationSetting.getKey(), configurationSetting.getValue());
        });

        // Cleaning up after ourselves by deleting the values.
        final Stream<ConfigurationSetting> stream = settings == null ? Stream.empty() : settings.stream();
        stream.forEach(item -> client.deleteConfigurationSettingWithResponse(item, false, RequestContext.none()));
    }
}
