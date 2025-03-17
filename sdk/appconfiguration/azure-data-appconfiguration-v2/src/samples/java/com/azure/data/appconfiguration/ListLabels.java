// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.appconfiguration;


import com.azure.v2.data.appconfiguration.AzureAppConfigurationClient;
import com.azure.v2.data.appconfiguration.AzureAppConfigurationClientBuilder;
import com.azure.v2.data.appconfiguration.models.KeyValue;
import io.clientcore.core.utils.configuration.Configuration;

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

        final AzureAppConfigurationClient client = new AzureAppConfigurationClientBuilder()
            .connectionString(connectionString)
            .buildClient();

        // Prepare three settings with different labels, prod1, prod2, prod3
        KeyValue setting = client.putKeyValue("prod:prod1", null, "prod1",  null, null, null, new KeyValue().setValue("prod1"));
        System.out.printf("Key: %s, Label: %s, Value: %s%n", setting.getKey(), setting.getLabel(), setting.getValue());
        KeyValue setting1 = client.putKeyValue("prod:prod2", null, "prod2", null, null, null, new KeyValue().setValue("prod2"));
        System.out.printf("Key: %s, Label: %s, Value: %s%n", setting1.getKey(), setting1.getLabel(), setting1.getValue());
        KeyValue setting2 = client.putKeyValue("prod:prod3", null, "prod3", null, null, null, new KeyValue().setValue("prod3"));
        System.out.printf("Key: %s, Label: %s, Value: %s%n", setting2.getKey(), setting2.getLabel(), setting2.getValue());

        // If you want to list all labels in the sources, simply pass selector=null in the request;
        // If you want to list labels by exact match, use the exact label name as the filter.
        // If you want to list all labels by wildcard, pass wildcard where AppConfig supports, such as "prod*",
        System.out.println("List all labels:");
        client.getLabels(null)
                .forEach(label -> System.out.println("\tLabel name = " + label.getName()));

//        System.out.println("List label by exact match:");
//        client.getLabels(null, "prod2", null, null, null, null)
//                .forEach(label -> System.out.println("\tLabel name = " + label.getName()));
//
//        System.out.println("List labels by wildcard:");
//        client.getLabels(null, "prod*", null, null, null, null)
//                .forEach(label -> System.out.println("\tLabel name = " + label.getName()));
    }
}
