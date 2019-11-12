// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.appconfiguration;

import com.azure.core.credential.TokenCredential;
import com.azure.data.appconfiguration.models.ConfigurationSetting;

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
        // The endpoint value can be obtained from connection string. Connection String has string format
        // "endpoint={endpoint_value};id={id_value};secret={secret_value}" where the endpoint is what it is.
        // The connection string can be obtained by going to your App Configuration instance in the Azure portal
        // and navigating to "Access Keys" page under the "Settings" section.
        String endpoint = "{endpoint_value}";

        // Token Credential could be an AAD token which you can get from Identity
        // or other service authentication service.
        TokenCredential tokenCredential = null;


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
