// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.appconfiguration;

import com.azure.core.http.HttpPipeline;
import com.azure.core.test.models.RecordedData;
import com.azure.core.test.policy.RecordNetworkCallPolicy;
import com.azure.data.appconfiguration.credentials.ConfigurationClientCredentials;
import com.azure.data.appconfiguration.models.ConfigurationSetting;
import com.azure.data.appconfiguration.models.SettingSelector;

import java.net.MalformedURLException;
import java.security.GeneralSecurityException;
import java.util.stream.Stream;

/**
 * This class contains code samples for generating javadocs through doclets for {@link ConfigurationClient}
 */
public final class ConfigurationClientJavaDocCodeSnippets {

    /**
     * Generates code sample for creating a {@link ConfigurationClient}
     * @return An instance of {@link ConfigurationClient}
     * @throws IllegalStateException If configuration credentials cannot be created.
     * @throws MalformedURLException if service end point is malformed.
     */
    public ConfigurationClient createAsyncConfigurationClientWithPipeline() throws MalformedURLException {
        try {
            String connectionString = getConnectionString();
            // BEGIN: com.azure.data.applicationconfig.configurationclient.pipeline.instantiation
            RecordedData networkData = new RecordedData();
            HttpPipeline pipeline = HttpPipeline.builder().policies(new RecordNetworkCallPolicy(networkData)).build();

            ConfigurationClient configurationClient = new ConfigurationClientBuilder()
                .pipeline(pipeline)
                .endpoint("https://myconfig.azure.net/")
                .credential(new ConfigurationClientCredentials(connectionString))
                .buildClient();
            // END: com.azure.data.applicationconfig.configurationclient.pipeline.instantiation
            return configurationClient;
        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException("Failed to create configuration client credentials", ex);
        }
    }

    /**
     * Generates code sample for creating a {@link ConfigurationClient}
     * @return An instance of {@link ConfigurationClient}
     * @throws IllegalStateException If configuration credentials cannot be created
     */
    public ConfigurationAsyncClient createAsyncConfigurationClient() {
        try {
            String connectionString = getConnectionString();
            // BEGIN: com.azure.data.applicationconfig.async.configurationclient.instantiation
            ConfigurationAsyncClient  configurationAsyncClient = new ConfigurationClientBuilder()
                .credential(new ConfigurationClientCredentials(connectionString))
                .buildAsyncClient();
            // END: com.azure.data.applicationconfig.async.configurationclient.instantiation
            return configurationAsyncClient;
        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException("Failed to create configuration client credentials", ex);
        }
    }

    /**
     * Generates code sample for creating a {@link ConfigurationClient}
     * @return An instance of {@link ConfigurationClient}
     * @throws IllegalStateException If configuration credentials cannot be created
     */
    public ConfigurationClient createSyncConfigurationClient() {
        try {
            String connectionString = getConnectionString();
            // BEGIN: com.azure.data.applicationconfig.configurationclient.instantiation
            ConfigurationClient configurationClient = new ConfigurationClientBuilder()
                .credential(new ConfigurationClientCredentials(connectionString))
                .buildClient();
            // END: com.azure.data.applicationconfig.configurationclient.instantiation
            return configurationClient;
        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException("Failed to create configuration client credentials", ex);
        }
    }

    /**
     * Generates code sample for using {@link ConfigurationClient#addSetting(String, String)}
     */
    public void addSetting() {
        ConfigurationClient configurationClient = createSyncConfigurationClient();
        // BEGIN: com.azure.data.applicationconfig.configurationclient.addSetting#string-string
        ConfigurationSetting configurationSetting = configurationClient
            .addSetting("prodDBConnection", "db_connection").value();
        System.out.printf("Key: %s, Value: %s %n", configurationSetting.key(), configurationSetting.value());
        // END: com.azure.data.applicationconfig.configurationclient.addSetting#string-string
    }

    /**
     * Generates code sample for using {@link ConfigurationClient#listSettings(SettingSelector)}  }
     */
    public void listSettings() {
        ConfigurationClient configurationClient = createSyncConfigurationClient();
        // BEGIN: com.azure.data.applicationconfig.configurationclient.listSettings#settingSelector
        SettingSelector settingSelector = new SettingSelector().keys("prodDBConnection");
        Stream<ConfigurationSetting> csStream =  configurationClient.listSettings(settingSelector);
        csStream.forEach(setting -> {
            System.out.printf("Key: %s, Value: %s", setting.key(), setting.value());
        });
        // END: com.azure.data.applicationconfig.configurationclient.listSettings#settingSelector
    }

    /**
     * Generates code sample for using {@link ConfigurationClient#listSettingRevisions(SettingSelector)}  }
     */
    public void listSettingRevisions() {
        ConfigurationClient configurationClient = createSyncConfigurationClient();
        // BEGIN: com.azure.data.applicationconfig.configurationclient.listSettingRevisions#settingSelector
        SettingSelector settingSelector = new SettingSelector().keys("prodDBConnection");
        Stream<ConfigurationSetting> csStream =  configurationClient.listSettingRevisions(settingSelector);
        csStream.forEach(setting -> {
            System.out.printf("Key: %s, Value: %s", setting.key(), setting.value());
        });
        // END: com.azure.data.applicationconfig.configurationclient.listSettingRevisions#settingSelector
    }
    /**
     * Implementation not provided for this method
     * @return {@code null}
     */
    private String getConnectionString() {
        return null;
    }
}
