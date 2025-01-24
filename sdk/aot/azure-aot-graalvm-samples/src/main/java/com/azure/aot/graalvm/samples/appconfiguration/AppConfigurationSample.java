// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.aot.graalvm.samples.appconfiguration;

import com.azure.data.appconfiguration.ConfigurationClient;
import com.azure.data.appconfiguration.ConfigurationClientBuilder;
import com.azure.data.appconfiguration.models.ConfigurationSetting;
import com.azure.identity.DefaultAzureCredentialBuilder;

/**
 * A sample to demonstrate setting and getting a config from Azure Application Configuration operations using GraalVM.
 */
public final class AppConfigurationSample {
    private static final String AZURE_APP_CONFIGURATION_CONNECTION_STRING
        = System.getenv("AZURE_APP_CONFIGURATION_CONNECTION_STRING");

    /**
     * The method to run the app configuration sample.
     */
    public static void runSample() {
        System.out.println("\n================================================================");
        System.out.println(" Starting App Configuration Sample");
        System.out.println("================================================================");

        final ConfigurationClientBuilder configurationClientBuilder = new ConfigurationClientBuilder();

        if (AZURE_APP_CONFIGURATION_CONNECTION_STRING != null && !AZURE_APP_CONFIGURATION_CONNECTION_STRING.isEmpty()) {
            configurationClientBuilder.connectionString(AZURE_APP_CONFIGURATION_CONNECTION_STRING);
        } else {
            configurationClientBuilder.credential(new DefaultAzureCredentialBuilder().build());
        }

        final ConfigurationClient configurationClient = configurationClientBuilder.buildClient();

        System.out.println("Setting configuration");
        ConfigurationSetting setting = configurationClient.setConfigurationSetting("key", "label", "value");
        System.out.println("Done: " + setting.getLastModified());

        setting = configurationClient.getConfigurationSetting("key", "label");
        System.out.println("Retrieved setting again, value is " + setting.getValue());

        System.out.println("\n================================================================");
        System.out.println(" App Configuration Sample Complete");
        System.out.println("================================================================");
    }

    private AppConfigurationSample() {
    }
}
