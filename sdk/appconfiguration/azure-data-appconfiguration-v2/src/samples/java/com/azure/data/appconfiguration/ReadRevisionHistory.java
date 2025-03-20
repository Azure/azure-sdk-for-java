// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.appconfiguration;

import com.azure.v2.data.appconfiguration.AzureAppConfigurationClient;
import com.azure.v2.data.appconfiguration.AzureAppConfigurationClientBuilder;
import com.azure.v2.data.appconfiguration.models.KeyValue;
import io.clientcore.core.http.models.PagedIterable;
import io.clientcore.core.utils.configuration.Configuration;

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

        final AzureAppConfigurationClient client = new AzureAppConfigurationClientBuilder()
            .connectionString(connectionString)
//            .credential(new DefaultAzureCredentialBuilder().build())
            .buildClient();

        final String key = "hello";

        // Adding a couple of settings and then fetching all the settings in our repository.
        KeyValue setting = client.putKeyValue(key, null, null, null, null, null, new KeyValue().setValue("world"));
        System.out.printf("[SetConfigurationSetting] Key: %s, Value: %s", setting.getKey(), setting.getValue());
        KeyValue setting2 = client.putKeyValue(key, null, null, null, null, null, new KeyValue().setValue("newValue"));
        System.out.printf("[Override ConfigurationSetting] Key: %s, Value: %s", setting2.getKey(), setting2.getValue());

        PagedIterable<KeyValue> revisions = client.getRevisions(null, key, null, null, null, null, null, null);
        revisions.stream().forEach(
            revision -> {
                System.out.printf("[GetConfigurationSetting] Key: %s, Value: %s%n", revision.getKey(), revision.getValue());
            }
        );
    }
}
