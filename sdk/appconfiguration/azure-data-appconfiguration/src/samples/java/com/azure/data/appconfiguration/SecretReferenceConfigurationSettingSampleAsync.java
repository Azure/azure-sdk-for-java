// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.appconfiguration;

import com.azure.data.appconfiguration.models.SecretReferenceConfigurationSetting;
import com.azure.data.appconfiguration.models.SettingSelector;

import java.util.concurrent.TimeUnit;

/**
 * Sample demonstrates how to add, get, list, and delete a secret reference configuration setting asynchronous.
 */
public class SecretReferenceConfigurationSettingSampleAsync {
    /**
     * Runs the sample algorithm and demonstrates how to add, get, list, and delete a secret reference configuration
     * setting asynchronous.
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
        final String secretIdValue = "{the-keyVault-secret-id-uri}";

        System.out.println("Beginning of asynchronous sample...");

        SecretReferenceConfigurationSetting referenceConfigurationSetting =
            new SecretReferenceConfigurationSetting(key, secretIdValue);

        // setConfigurationSetting adds or updates a setting to Azure App Configuration store. Alternatively, you can
        // call addConfigurationSetting which only succeeds if the setting does not exist in the store. Or,
        // you can call setConfigurationSetting to update a setting that is already present in the store.

        // We subscribe and wait for the service call to complete then print out the contents of our newly added setting.
        // If an error occurs, we print out that error.
        System.out.println("[Set-SecretReferenceConfigurationSetting]");
        client.setConfigurationSetting(referenceConfigurationSetting).subscribe(
            result -> printSecretReferenceConfigurationSetting((SecretReferenceConfigurationSetting) result),
            error -> System.err.println("There was an error adding the setting: " + error),
            () -> System.out.println("Set setting finished"));
        TimeUnit.MILLISECONDS.sleep(1000);

        System.out.println("[Get-SecretReferenceConfigurationSetting]");
        client.getConfigurationSetting(referenceConfigurationSetting).subscribe(
            result -> printSecretReferenceConfigurationSetting((SecretReferenceConfigurationSetting) result),
            error -> System.err.println("There was an error getting the setting: " + error),
            () -> System.out.println("Get setting finished"));
        TimeUnit.MILLISECONDS.sleep(1000);

        System.out.println("[List-SecretReferenceConfigurationSetting]");
        client.listConfigurationSettings(new SettingSelector()).subscribe(
            result -> {
                if (result instanceof SecretReferenceConfigurationSetting) {
                    System.out.println("-Listing-SecretReferenceConfigurationSetting");
                    printSecretReferenceConfigurationSetting((SecretReferenceConfigurationSetting) result);
                } else {
                    System.out.println("-Listing-non-SecretReferenceConfigurationSetting");
                    System.out.printf("Key: %s, Value: %s%n", result.getKey(), result.getValue());
                }
            },
            error -> System.err.println("There was an error adding the setting: " + error),
            () -> System.out.println("List settings finished"));
        TimeUnit.MILLISECONDS.sleep(1000);

        System.out.println("[Delete-SecretReferenceConfigurationSetting");
        client.getConfigurationSetting(referenceConfigurationSetting).subscribe(
            result -> printSecretReferenceConfigurationSetting((SecretReferenceConfigurationSetting) result),
            error -> System.err.println("There was an error getting the setting: " + error),
            () -> System.out.println("Delete setting finished"));
        TimeUnit.MILLISECONDS.sleep(1000);

        System.out.println("End of asynchronous sample.");
    }

    private static void printSecretReferenceConfigurationSetting(SecretReferenceConfigurationSetting setting) {
        System.out.printf("Key: %s, Secret ID: %s, Content Type: %s, Value: %s%n", setting.getKey(),
            setting.getSecretId(), setting.getContentType(), setting.getValue());
    }
}
