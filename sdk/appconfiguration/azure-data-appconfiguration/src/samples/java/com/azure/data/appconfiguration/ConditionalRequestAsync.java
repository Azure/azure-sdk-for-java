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

        // If you want to conditionally update the setting, set `ifUnchanged` to true. If the ETag of the
        // given setting matches the one in the service, then the setting is updated. Otherwise, it is
        // not updated.
        // If the given setting is not exist in the service, the setting will be added to the service.
        client.setConfigurationSettingWithResponse(setting, true).subscribe(
            result -> {
                final ConfigurationSetting output = result.getValue();
                final int statusCode = result.getStatusCode();
                System.out.printf(String.format("Status code: %s, Key: %s, Value: %s", statusCode, output.getKey(),
                    output.getValue()));
            },
            error -> System.err.println("There was an error while setting the setting: " + error));

        TimeUnit.MILLISECONDS.sleep(1000);

        // If you want to conditionally retrieve the setting, set `ifChanged` to true. If the ETag of the
        // given setting matches the one in the service, then 304 status code with null value returned in the response.
        // Otherwise, a setting with new ETag returned, which is the latest setting retrieved from the service.
        client.getConfigurationSettingWithResponse(setting, null, true).subscribe(
            result -> {
                final ConfigurationSetting output = result.getValue();
                final int statusCode = result.getStatusCode();
                System.out.printf(String.format("Status code: %s, Key: %s, Value: %s", statusCode, output.getKey(),
                    output.getValue()));
            },
            error -> System.err.println("There was an error while getting the setting: " + error));

        TimeUnit.MILLISECONDS.sleep(1000);

        // If you want to conditionally delete the setting, set `ifUnchanged` to true. If the ETag of the
        // given setting matches the one in the service, then the setting is deleted. Otherwise, it is
        // not deleted.
        client.deleteConfigurationSettingWithResponse(setting, true).subscribe(
            result -> {
                final ConfigurationSetting output = result.getValue();
                final int statusCode = result.getStatusCode();
                System.out.printf(String.format("Status code: %s, Key: %s, Value: %s", statusCode, output.getKey(),
                    output.getValue()));
            },
            error -> System.err.println("There was an error while deleting the setting: " + error));
    }


}
