// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.applicationmodel.configuration;

import com.azure.applicationmodel.configuration.credentials.ConfigurationClientCredentials;
import com.azure.applicationmodel.configuration.models.ConfigurationSetting;
import java.security.GeneralSecurityException;

/**
 * This class contains code samples for generating javadocs through doclets for {@link ConfigurationClient}
 */
public final class ConfigurationClientJavaDocCodeSnippets {

    /**
     * Generates code sample for creating a {@link ConfigurationClient}
     * @return An instance of {@link ConfigurationClient}
     * @throws IllegalStateException If configuration credentials cannot be created
     */
    public ConfigurationClient createConfigurationClient() {
        try {
            String connectionString = getConnectionString();
            // BEGIN: com.azure.applicationconfig.configurationclient.instantiation
            ConfigurationClient configurationClient = ConfigurationClient.builder()
                .credentials(new ConfigurationClientCredentials(connectionString))
                .build();
            // END: com.azure.applicationconfig.configurationclient.instantiation
            return configurationClient;
        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException("Failed to create configuration client credentials", ex);
        }
    }

    /**
     * Generates code sample for using {@link ConfigurationClient#addSetting(String, String)}
     */
    public void addSetting() {
        ConfigurationClient configurationClient = createConfigurationClient();
        // BEGIN: com.azure.applicationconfig.configurationclient.addSetting#string-string
        ConfigurationSetting configurationSetting = configurationClient
            .addSetting("prodDBConnection", "db_connection").value();
        System.out.printf("Key: %s, Value: %s %n", configurationSetting.key(), configurationSetting.value());
        // END: com.azure.applicationconfig.configurationclient.addSetting#string-string
    }

    /**
     * Implementation not provided for this method
     * @return {@code null}
     */
    private String getConnectionString() {
        return null;
    }
}
