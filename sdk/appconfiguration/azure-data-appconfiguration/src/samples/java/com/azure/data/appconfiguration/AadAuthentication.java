// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.appconfiguration;

import com.azure.core.credential.TokenCredential;
import com.azure.data.appconfiguration.models.ConfigurationSetting;
import com.azure.identity.DefaultAzureCredentialBuilder;

/**
 * Sample demonstrates how to use AAD token to build a configuration client.
 */
public class AadAuthentication {
    /**
     * Sample for how to use AAD token Authentication.
     *
     * @param args Unused. Arguments to the program.
     */
    public static void main(String[] args) {
        // The endpoint can be obtained by going to your App Configuration instance in the Azure portal
        // and navigating to "Overview" page. Looking for the "Endpoint" keyword.
        String endpoint = "{endpoint_value}";

        // Token Credential could be an AAD token which you can get from Identity
        // or other service authentication service.
        TokenCredential tokenCredential = new DefaultAzureCredentialBuilder().build();

        final ConfigurationClient client = new ConfigurationClientBuilder()
            .credential(tokenCredential) // AAD authentication
            .endpoint(endpoint)
            .buildClient();

        // Name of the key to add to the configuration service.
        final String key = "hello";
        final String value = "world";

        System.out.println("Beginning of synchronous sample...");

        ConfigurationSetting setting = client.setConfigurationSetting(key, null, value);
        System.out.printf(String.format("[SetConfigurationSetting] Key: %s, Value: %s", setting.getKey(), setting.getValue()));

        setting = client.getConfigurationSetting(key, null, null);
        System.out.printf(String.format("[GetConfigurationSetting] Key: %s, Value: %s", setting.getKey(), setting.getValue()));

        setting = client.deleteConfigurationSetting(key, null);
        System.out.printf(String.format("[DeleteConfigurationSetting] Key: %s, Value: %s", setting.getKey(), setting.getValue()));

        System.out.println("End of synchronous sample.");
    }
}
