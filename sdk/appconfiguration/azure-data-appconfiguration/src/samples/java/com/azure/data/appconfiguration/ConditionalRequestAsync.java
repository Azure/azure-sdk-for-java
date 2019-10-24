// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.appconfiguration;

import com.azure.data.appconfiguration.models.ConfigurationSetting;

import java.util.concurrent.TimeUnit;

/**
 * Sample demonstrates how to add, get, and delete a configuration setting by conditional request asynchronously.
 */
public class ConditionalRequestAsync {
    /**
     * Runs the sample algorithm and demonstrates how to add, get, and delete a configuration setting by conditional
     * request asynchronously
     * @param args Unused. Arguments to the program.
     * @throws InterruptedException when a thread is waiting, sleeping, or otherwise occupied,
     * and the thread is interrupted, either before or during the activity.
     */
    public static void main(String[] args) throws InterruptedException {
        // The connection string value can be obtained by going to your App Configuration instance in the Azure portal
        // and navigating to "Access Keys" page under the "Settings" section.
        String connectionString = "endpoint={endpoint_value};id={id_value};secret={secret_value}";

        // Instantiate a client that will be used to call the service.
        final ConfigurationAsyncClient client = new ConfigurationClientBuilder()
            .connectionString(connectionString)
            .buildAsyncClient();

        ConfigurationSetting setting = new ConfigurationSetting().setKey("key").setLabel("label").setValue("value");


        // If you want to use conditional request, then you should set the boolean value of 'ifUnchanged' to true, so
        // the API will use the ETag value from the given setting to make a conditional request.
        client.setConfigurationSettingWithResponse(setting, true).subscribe(
            result -> {
                final ConfigurationSetting output = result.getValue();
                final int statusCode = result.getStatusCode();
                System.out.println(String.format("Status code: %s, Key: %s, Value: %s", statusCode, output.getKey(),
                    output.getValue()));
            },
            error -> System.err.println("There was an error while setting the setting: " + error.toString()),
            null);

        TimeUnit.MILLISECONDS.sleep(1000);

        // If you want to use conditional request, then you should set the boolean value of ifChanged' to true, so the
        // API will use the ETag value from the given setting to make a conditional request.
        client.getConfigurationSettingWithResponse(setting, null, true).subscribe(
            result -> {
                final ConfigurationSetting output = result.getValue();
                final int statusCode = result.getStatusCode();
                System.out.println(String.format("Status code: %s, Key: %s, Value: %s", statusCode, output.getKey(),
                    output.getValue()));
            },
            error -> System.err.println("There was an error while getting the setting: " + error.toString()),
            null);

        TimeUnit.MILLISECONDS.sleep(1000);

        // If you want to use conditional request, then you should set the boolean value of 'ifUnchanged' to true, so
        // the API will use the ETag value from the given setting to make a conditional request.
        client.deleteConfigurationSettingWithResponse(setting, true).subscribe(
            result -> {
                final ConfigurationSetting output = result.getValue();
                final int statusCode = result.getStatusCode();
                System.out.println(String.format("Status code: %s, Key: %s, Value: %s", statusCode, output.getKey(),
                    output.getValue()));
            },
            error -> System.err.println("There was an error while deleting the setting: " + error.toString()),
            null);
    }


}
