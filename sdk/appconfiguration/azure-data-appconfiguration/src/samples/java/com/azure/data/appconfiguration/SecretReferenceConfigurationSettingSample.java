// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.appconfiguration;

import com.azure.core.http.rest.PagedIterable;
import com.azure.data.appconfiguration.models.ConfigurationSetting;
import com.azure.data.appconfiguration.models.SecretReferenceConfigurationSetting;
import com.azure.data.appconfiguration.models.SettingSelector;

/**
 * Sample demonstrates how to add, get, list, and delete a secret reference configuration setting.
 */
public class SecretReferenceConfigurationSettingSample {
    /**
     * Runs the sample algorithm and demonstrates how to add, get, list, and delete a secret reference configuration
     * setting.
     *
     * @param args Unused. Arguments to the program.
     */
    public static void main(String[] args) {
        // The connection string value can be obtained by going to your App Configuration instance in the Azure portal
        // and navigating to "Access Keys" page under the "Settings" section.
        String connectionString = "endpoint={endpoint_value};id={id_value};secret={secret_value}";
        final ConfigurationClient client = new ConfigurationClientBuilder()
                                               .connectionString(connectionString)
                                               .buildClient();

        // Name of the key to add to the configuration service.
        final String key = "hello";
        final String secretIdValue = "{the-keyVault-secret-id-uri}";

        System.out.println("Beginning of synchronous sample...");

        SecretReferenceConfigurationSetting referenceConfigurationSetting =
            new SecretReferenceConfigurationSetting(key, secretIdValue);

        // setConfigurationSetting adds or updates a setting to Azure App Configuration store. Alternatively, you can
        // call addConfigurationSetting which only succeeds if the setting does not exist in the store. Or,
        // you can call setConfigurationSetting to update a setting that is already present in the store.
        System.out.println("[Set-SecretReferenceConfigurationSetting]");
        SecretReferenceConfigurationSetting setting = (SecretReferenceConfigurationSetting) client.setConfigurationSetting(referenceConfigurationSetting);
        printSecretReferenceConfigurationSetting(setting);

        System.out.println("[Get-SecretReferenceConfigurationSetting]");
        setting = (SecretReferenceConfigurationSetting) client.getConfigurationSetting(setting);
        printSecretReferenceConfigurationSetting(setting);

        System.out.println("[List-SecretReferenceConfigurationSetting]");
        final PagedIterable<ConfigurationSetting> configurationSettings = client.listConfigurationSettings(new SettingSelector());
        for (ConfigurationSetting configurationSetting : configurationSettings) {
            if (configurationSetting instanceof SecretReferenceConfigurationSetting) {
                System.out.println("-Listing-SecretReferenceConfigurationSetting");
                printSecretReferenceConfigurationSetting((SecretReferenceConfigurationSetting) configurationSetting);
            } else {
                System.out.println("-Listing-non-SecretReferenceConfigurationSetting");
                System.out.printf("Key: %s, Value: %s%n", configurationSetting.getKey(), configurationSetting.getValue());
            }
        }

        System.out.println("[Delete-SecretReferenceConfigurationSetting");
        setting = (SecretReferenceConfigurationSetting) client.deleteConfigurationSetting(setting);
        printSecretReferenceConfigurationSetting(setting);

        System.out.println("End of synchronous sample.");
    }

    private static void printSecretReferenceConfigurationSetting(SecretReferenceConfigurationSetting setting) {
        System.out.printf("Key: %s, Secret ID: %s, Content Type: %s, Value: %s%n", setting.getKey(),
            setting.getSecretId(), setting.getContentType(), setting.getValue());
    }
}
