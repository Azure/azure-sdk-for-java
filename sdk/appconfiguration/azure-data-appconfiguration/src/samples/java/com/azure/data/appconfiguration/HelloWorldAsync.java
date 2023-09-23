// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.appconfiguration;

import com.azure.data.appconfiguration.models.ConfigurationSetting;

import java.util.concurrent.TimeUnit;

/**
 * Sample demonstrates how to add, get, and delete a configuration setting asynchronous.
 */
public class HelloWorldAsync {
    /**
     * Runs the sample algorithm and demonstrates how to add, get, and delete a configuration setting asynchronous.
     *
     * @param args Unused. Arguments to the program.
     * @throws InterruptedException when a thread is waiting, sleeping, or otherwise occupied,
     * and the thread is interrupted, either before or during the activity.
     */
    public static void main(String[] args) throws InterruptedException {
        // The connection string value can be obtained by going to your App Configuration instance in the Azure portal
        // and navigating to "Access Keys" page under the "Settings" section.
        String connectionString = "endpoint={endpoint_value};id={id_value};secret={secret_value}";

        // Asynchronous sample
        // Instantiate a client that will be used to call the service.
        final ConfigurationAsyncClient client = new ConfigurationClientBuilder()
            .connectionString(connectionString)
            .buildAsyncClient();

        // Name of the key to add to the configuration service.
        final String key = "hello";
        final String value = "world";

        // setConfigurationSetting adds or updates a setting to Azure App Configuration store. Alternatively, you can call
        // addConfigurationSetting which only succeeds if the setting does not exist in the store. Or, you can call setConfigurationSetting to
        // update a setting that is already present in the store.
        // We subscribe and wait for the service call to complete then print out the contents of our newly added setting.
        // If an error occurs, we print out that error.
        client.setConfigurationSetting(key, null, value).subscribe(
            result -> {
                final ConfigurationSetting setting = result;
                System.out.printf(String.format("[SetConfigurationSetting] Key: %s, Value: %s", setting.getKey(), setting.getValue()));
            },
            error -> System.err.println("There was an error adding the setting: " + error),
            () -> System.out.printf(String.format("Set setting with key=%s and value=%s added or updated.", key, value)));

        TimeUnit.MILLISECONDS.sleep(1000);

        client.getConfigurationSetting(key, null, null).subscribe(
            result -> {
                final ConfigurationSetting setting = result;
                System.out.printf(String.format("[GetConfigurationSetting] Key: %s, Value: %s", setting.getKey(), setting.getValue()));
            },
            error -> System.err.println("There was an error getting the setting: " + error),
            () -> {
                // On completion of the subscription, we delete the setting.
                // .block() exists there so the program does not end before the deletion has completed.
                System.out.println("Completed. Deleting setting...");
                client.deleteConfigurationSetting(key, null).block();
            });

        TimeUnit.MILLISECONDS.sleep(1000);
    }
}
