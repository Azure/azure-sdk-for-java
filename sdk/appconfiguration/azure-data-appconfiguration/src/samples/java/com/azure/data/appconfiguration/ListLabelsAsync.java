// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.appconfiguration;

import com.azure.core.util.Configuration;
import com.azure.data.appconfiguration.models.LabelSelector;

import java.util.concurrent.TimeUnit;

/**
 * A sample demonstrate how to list labels asynchronously.
 */
public class ListLabelsAsync {
    /**
     * Runs the sample algorithm and demonstrates how to list labels asynchronously.
     *
     * @param args Unused. Arguments to the program.
     * @throws InterruptedException when a thread is waiting, sleeping, or otherwise occupied,
     * and the thread is interrupted, either before or during the activity.
     */
    public static void main(String[] args) throws InterruptedException {
        // The connection string value can be obtained by going to your App Configuration instance in the Azure portal
        // and navigating to "Access Keys" page under the "Settings" section.
        String connectionString = Configuration.getGlobalConfiguration().get("AZURE_APPCONFIG_CONNECTION_STRING");

        // Asynchronous sample
        // Instantiate a client that will be used to call the service.
        final ConfigurationAsyncClient client = new ConfigurationClientBuilder()
                .connectionString(connectionString)
                .buildAsyncClient();
        // Prepare three settings with different labels, prod1, prod2, prod3
        client.setConfigurationSetting("prod:prod1", "prod1", "prod1").subscribe(
                setting -> System.out.printf("Key: %s, Label: %s, Value: %s%n", setting.getKey(), setting.getLabel(), setting.getValue()));
        TimeUnit.MILLISECONDS.sleep(1000);

        client.setConfigurationSetting("prod:prod2", "prod2", "prod2").subscribe(
                setting -> System.out.printf("Key: %s, Label: %s, Value: %s%n", setting.getKey(), setting.getLabel(), setting.getValue()));
        TimeUnit.MILLISECONDS.sleep(1000);

        client.setConfigurationSetting("prod:prod3", "prod3", "prod3").subscribe(
                setting -> System.out.printf("Key: %s, Label: %s, Value: %s%n", setting.getKey(), setting.getLabel(), setting.getValue()));
        TimeUnit.MILLISECONDS.sleep(1000);

        // If you want to list all labels in the sources, simply pass selector=null in the request;
        // If you want to list labels by exact match, use the exact label name as the filter.
        // If you want to list all labels by wildcard, pass wildcard where AppConfig supports, such as "prod*",
        System.out.println("List all labels:");
        client.listLabels(null).subscribe(label -> System.out.println("\tLabel name = " + label.getName()));
        TimeUnit.MILLISECONDS.sleep(1000);

        System.out.println("List label by exact match:");
        client.listLabels(new LabelSelector().setLabelFilter("prod2")).subscribe(
                label -> System.out.println("\tLabel name = " + label.getName()));
        TimeUnit.MILLISECONDS.sleep(1000);

        System.out.println("List labels by wildcard:");
        client.listLabels(new LabelSelector().setLabelFilter("prod*")).subscribe(
                label -> System.out.println("\tLabel name = " + label.getName()));
        TimeUnit.MILLISECONDS.sleep(1000);
    }
}
