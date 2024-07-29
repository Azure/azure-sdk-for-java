// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.appconfiguration;

import com.azure.core.util.Configuration;
import com.azure.core.util.Context;
import com.azure.data.appconfiguration.models.ConfigurationSetting;
import com.azure.data.appconfiguration.models.LabelSelector;

/**
 * A sample demonstrate how to list labels.
 */
public class ListLabels {
    /**
     * Runs the sample algorithm and demonstrates how to list labels.
     *
     * @param args Unused. Arguments to the program.
     */
    public static void main(String[] args) {
        // The connection string value can be obtained by going to your App Configuration instance in the Azure portal
        // and navigating to "Access Keys" page under the "Settings" section.
        String connectionString = Configuration.getGlobalConfiguration().get("AZURE_APPCONFIG_CONNECTION_STRING");

        final ConfigurationClient client = new ConfigurationClientBuilder()
                .connectionString(connectionString)
                .buildClient();

        // Prepare three settings with different labels, prod1, prod2, prod3
        ConfigurationSetting setting = client.setConfigurationSetting("prod:prod1", "prod1", "prod1");
        System.out.printf("Key: %s, Label: %s, Value: %s%n", setting.getKey(), setting.getLabel(), setting.getValue());
        ConfigurationSetting setting1 = client.setConfigurationSetting("prod:prod2", "prod2", "prod2");
        System.out.printf("Key: %s, Label: %s, Value: %s%n", setting1.getKey(), setting1.getLabel(), setting1.getValue());
        ConfigurationSetting setting2 = client.setConfigurationSetting("prod:prod3", "prod3", "prod3");
        System.out.printf("Key: %s, Label: %s, Value: %s%n", setting2.getKey(), setting2.getLabel(), setting2.getValue());

        // If you want to list all labels in the sources, simply pass selector=null in the request;
        // If you want to list labels by exact match, use the exact label name as the filter.
        // If you want to list all labels by wildcard, pass wildcard where AppConfig supports, such as "prod*",
        System.out.println("List all labels:");
        client.listLabels(null, Context.NONE)
                .forEach(label -> System.out.println("\tLabel name = " + label.getName()));

        System.out.println("List label by exact match:");
        client.listLabels(new LabelSelector().setLabelFilter("prod2"), Context.NONE)
                .forEach(label -> System.out.println("\tLabel name = " + label.getName()));

        System.out.println("List labels by wildcard:");
        client.listLabels(new LabelSelector().setLabelFilter("prod*"), Context.NONE)
                .forEach(label -> System.out.println("\tLabel name = " + label.getName()));
    }
}
