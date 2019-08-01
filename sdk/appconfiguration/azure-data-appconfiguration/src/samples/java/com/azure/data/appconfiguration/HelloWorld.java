// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.appconfiguration;

import com.azure.data.appconfiguration.credentials.ConfigurationClientCredentials;
import com.azure.data.appconfiguration.models.ConfigurationSetting;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * Sample demonstrates how to add, get, and delete a configuration setting.
 */
public class HelloWorld {
    /**
     * Runs the sample algorithm and demonstrates how to add, get, and delete a configuration setting.
     *
     * @param args Unused. Arguments to the program.
     * @throws NoSuchAlgorithmException when credentials cannot be created because the service cannot resolve the
     * HMAC-SHA256 algorithm.
     * @throws InvalidKeyException when credentials cannot be created because the connection string is invalid.
     */
    public static void main(String[] args) throws NoSuchAlgorithmException, InvalidKeyException {
        // The connection string value can be obtained by going to your App Configuration instance in the Azure portal
        // and navigating to "Access Keys" page under the "Settings" section.
        String connectionString = "endpoint={endpoint_value};id={id_value};name={secret_value}";

        // Instantiate a client that will be used to call the service.
        ConfigurationAsyncClient client = new ConfigurationClientBuilder()
            .credential(new ConfigurationClientCredentials(connectionString))
            .buildAsyncClient();

        // Name of the key to add to the configuration service.
        String key = "hello";

        // setSetting adds or updates a setting to Azure App Configuration store. Alternatively, you can call
        // addSetting which only succeeds if the setting does not exist in the store. Or, you can call updateSetting to
        // update a setting that is already present in the store.
        // We subscribe and wait for the service call to complete then print out the contents of our newly added setting.
        // If an error occurs, we print out that error. On completion of the subscription, we delete the setting.
        // .block() exists there so the program does not end before the deletion has completed.
        client.setSetting(key, "world").subscribe(
            result -> {
                ConfigurationSetting setting = result;
                System.out.println(String.format("Key: %s, Value: %s", setting.key(), setting.value()));
            },
            error -> System.err.println("There was an error adding the setting: " + error.toString()),
            () -> {
                System.out.println("Completed. Deleting setting...");
                client.deleteSetting(key).block();
            });
    }
}
