// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.appconfiguration;

import com.azure.data.appconfiguration.models.ConfigurationSetting;
import com.azure.data.appconfiguration.models.SettingSelector;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.stream.Collectors;
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
        String connectionString = "endpoint={endpoint_value};id={id_value};secret={secret_value}";

        // Instantiate a client that will be used to call the service.
        final ConfigurationAsyncClient client = new ConfigurationClientBuilder()
            .connectionString(connectionString)
            .buildAsyncClient();

        final String key = "hello";

        // Adding a couple of settings and then fetching all the settings in our repository.
        final List<ConfigurationSetting> settings = Flux.concat(
            client.addConfigurationSetting(key, null, "world"),
            client.setConfigurationSetting(key, null, "newValue"))
            .then(client.listRevisions(new SettingSelector().setKeys(key)).collectList())
            .block();

        // Cleaning up after ourselves by deleting the values.
        final Stream<ConfigurationSetting> stream = settings == null ? Stream.empty() : settings.stream();
        Flux.merge(stream.map(setting -> client.deleteConfigurationSettingWithResponse(setting, false))
            .collect(Collectors.toList())).blockLast();
    }
}
