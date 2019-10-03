// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.appconfiguration;

import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.data.appconfiguration.credentials.ConfigurationClientCredentials;
import com.azure.data.appconfiguration.models.ConfigurationSetting;
import com.azure.data.appconfiguration.models.SettingSelector;

import java.security.GeneralSecurityException;
import java.time.OffsetDateTime;

/**
 * This class contains code samples for generating javadocs through doclets for {@link ConfigurationClient}
 */
public final class ConfigurationClientJavaDocCodeSnippets {

    private String key1 = "key1";
    private String key2 = "key2";
    private String value1 = "val1";
    private String value2 = "val2";

    /**
     * Generates code sample for creating a {@link ConfigurationClient}
     *
     * @return An instance of {@link ConfigurationClient}
     * @throws IllegalStateException If configuration credentials cannot be created.
     */
    public ConfigurationClient createAsyncConfigurationClientWithPipeline() {
        try {
            String connectionString = getConnectionString();
            // BEGIN: com.azure.data.applicationconfig.configurationclient.pipeline.instantiation
            HttpPipeline pipeline = new HttpPipelineBuilder()
                .policies(/* add policies */)
                .build();

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
     *
     * @return An instance of {@link ConfigurationClient}
     * @throws IllegalStateException If configuration credentials cannot be created
     */
    public ConfigurationAsyncClient createAsyncConfigurationClient() {
        try {
            String connectionString = getConnectionString();
            // BEGIN: com.azure.data.applicationconfig.async.configurationclient.instantiation
            ConfigurationAsyncClient configurationAsyncClient = new ConfigurationClientBuilder()
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
     *
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
     * Generates code sample for using {@link ConfigurationClient#addSetting(String, String, String)}
     */
    public void addSetting() {
        ConfigurationClient configurationClient = createSyncConfigurationClient();
        // BEGIN: com.azure.data.appconfiguration.ConfigurationClient.addSetting#String-String-String
        ConfigurationSetting result = configurationClient
            .addSetting("prodDBConnection", "db_connection", null);
        System.out.printf("Key: %s, Value: %s", result.getKey(), result.getValue());
        // END: com.azure.data.appconfiguration.ConfigurationClient.addSetting#String-String-String

    /*
      Generates code sample for using {@link ConfigurationClient#addSettingWithResponse(ConfigurationSetting, Context)}
     */
        // BEGIN: com.azure.data.appconfiguration.ConfigurationClient.addSettingWithResponse#ConfigurationSetting-Context
        Response<ConfigurationSetting> responseResultSetting = configurationClient
            .addSettingWithResponse(
                new ConfigurationSetting()
                    .setKey("prodDBConnection").setLabel("westUS").setValue("db_connection"),
                new Context(key1, value1));
        System.out.printf("Key: %s, Value: %s", responseResultSetting.getValue().getKey(),
            responseResultSetting.getValue().getValue());
        // END: com.azure.data.appconfiguration.ConfigurationClient.addSettingWithResponse#ConfigurationSetting-Context
    }

    /**
     * Generates code sample for using {@link ConfigurationClient#setSetting(String, String, String)}
     */
    public void setSetting() {
        ConfigurationClient configurationClient = createSyncConfigurationClient();
        // BEGIN: com.azure.data.appconfiguration.ConfigurationClient.setSetting#String-String-String
        ConfigurationSetting result = configurationClient
            .setSetting("prodDBConnection", null, "db_connection");
        System.out.printf("Key: %s, Value: %s", result.getKey(), result.getValue());

        // Update the value of the setting to "updated_db_connection".
        result = configurationClient.setSetting("prodDBConnection", null, "updated_db_connection");
        System.out.printf("Key: %s, Value: %s", result.getKey(), result.getValue());
        // END: com.azure.data.appconfiguration.ConfigurationClient.setSetting#String-String-String

    /**
      Generates code sample for using {@link ConfigurationClient#setSettingWithResponse(ConfigurationSetting, boolean, Context)}
     */
        // BEGIN: com.azure.data.appconfiguration.ConfigurationClient.setSettingWithResponse#ConfigurationSetting-boolean-Context
        // Add a setting with the key "prodDBConnection", label "westUS", and value "db_connection"
        Response<ConfigurationSetting> responseSetting = configurationClient.setSettingWithResponse(
            new ConfigurationSetting().setKey("prodDBConnection").setLabel("westUS").setValue("db_connection"),
            false,
            new Context(key2, value2));
        final ConfigurationSetting initSetting = responseSetting.getValue();
        System.out.printf("Key: %s, Value: %s", initSetting.getKey(), initSetting.getValue());

        // Update the value of the setting to "updated_db_connection".
        responseSetting = configurationClient.setSettingWithResponse(
            new ConfigurationSetting().setKey("prodDBConnection").setLabel("westUS").setValue("updated_db_connection"),
            false,
            new Context(key2, value2));
        final ConfigurationSetting updatedSetting = responseSetting.getValue();
        System.out.printf("Key: %s, Value: %s", updatedSetting.getKey(), updatedSetting.getValue());
        // END: com.azure.data.appconfiguration.ConfigurationClient.setSettingWithResponse#ConfigurationSetting-boolean-Context
    }

    /**
     * Generates code sample for using {@link ConfigurationClient#getSetting(String, String)}
     */
    public void getSetting() {
        ConfigurationClient configurationClient = createSyncConfigurationClient();

        // BEGIN: com.azure.data.applicationconfig.configurationclient.getSetting#string-string
        ConfigurationSetting resultNoDateTime = configurationClient.getSetting("prodDBConnection", null);
        System.out.printf("Key: %s, Value: %s", resultNoDateTime.getKey(), resultNoDateTime.getValue());
        // END: com.azure.data.applicationconfig.configurationclient.getSetting#string-string

    /**
     * Generates code sample for using {@link ConfigurationClient#getSetting(String, String, OffsetDateTime)}
     */
        // BEGIN: com.azure.data.applicationconfig.configurationclient.getSetting#string-string-OffsetDateTime
        ConfigurationSetting result = configurationClient.getSetting("prodDBConnection", null, null);
        System.out.printf("Key: %s, Value: %s", result.getKey(), result.getValue());
        // END: com.azure.data.applicationconfig.configurationclient.getSetting#string-string-OffsetDateTime

    /**
      Generates code sample for using {@link ConfigurationClient#getSettingWithResponse(ConfigurationSetting, boolean, Context)}
     */
        // BEGIN: com.azure.data.applicationconfig.configurationclient.getSettingWithResponse#ConfigurationSetting-boolean-Context
        // Retrieve the setting with the key-label "prodDBConnection"-"westUS".
        Response<ConfigurationSetting> responseResultSetting = configurationClient
            .getSettingWithResponse(new ConfigurationSetting().setKey("prodDBConnection").setLabel("westUS"), false,
                new Context(key1, value1));
        System.out.printf("Key: %s, Value: %s", responseResultSetting.getValue().getKey(),
            responseResultSetting.getValue().getValue());
        // END: com.azure.data.applicationconfig.configurationclient.getSettingWithResponse#ConfigurationSetting-boolean-Context
    }

    /**
     * Generates code sample for using {@link ConfigurationClient#deleteSetting(String, String)}
     */
    public void deleteSetting() {
        ConfigurationClient configurationClient = createSyncConfigurationClient();
        // BEGIN: com.azure.data.applicationconfig.configurationclient.deleteSetting#string-string
        ConfigurationSetting result = configurationClient
            .deleteSetting("prodDBConnection", null);
        System.out.printf("Key: %s, Value: %s", result.getKey(), result.getValue());

        // END: com.azure.data.applicationconfig.configurationclient.deleteSetting#string-string

        /**
         * Generates code sample for using {@link ConfigurationClient#deleteSettingWithResponse(ConfigurationSetting, Context)}
         */
        // BEGIN: com.azure.data.applicationconfig.configurationclient.deleteSettingWithResponse#ConfigurationSetting-boolean-Context
        Response<ConfigurationSetting> responseSetting = configurationClient
            .deleteSettingWithResponse(new ConfigurationSetting().setKey("prodDBConnection").setLabel("westUS"),
                false, new Context(key2, value2));
        System.out.printf(
            "Key: %s, Value: %s", responseSetting.getValue().getKey(), responseSetting.getValue().getValue());
        // END: com.azure.data.applicationconfig.configurationclient.deleteSettingWithResponse#ConfigurationSetting-boolean-Context
    }

    /**
     * Generates code sample for using {@link ConfigurationClient#listSettings(SettingSelector)}
     */
    public void listSettings() {
        ConfigurationClient configurationClient = createSyncConfigurationClient();
        // BEGIN: com.azure.data.applicationconfig.configurationclient.listSettings#settingSelector
        SettingSelector settingSelector = new SettingSelector().setKeys("prodDBConnection");
        configurationClient.listSettings(settingSelector).forEach(setting -> {
            System.out.printf("Key: %s, Value: %s", setting.getKey(), setting.getValue());
        });
        // END: com.azure.data.applicationconfig.configurationclient.listSettings#settingSelector
    }

    /**
     * Generates code sample for using {@link ConfigurationClient#listSettings(SettingSelector, Context)}
     */
    public void listSettingsContext() {
        ConfigurationClient configurationClient = createSyncConfigurationClient();
        // BEGIN: com.azure.data.applicationconfig.configurationclient.listSettings#settingSelector-context
        SettingSelector settingSelector = new SettingSelector().setKeys("prodDBConnection");
        Context ctx = new Context(key2, value2);
        configurationClient.listSettings(settingSelector, ctx).forEach(setting -> {
            System.out.printf("Key: %s, Value: %s", setting.getKey(), setting.getValue());
        });
        // END: com.azure.data.applicationconfig.configurationclient.listSettings#settingSelector-context
    }

    /**
     * Generates code sample for using {@link ConfigurationClient#listSettingRevisions(SettingSelector)}
     */
    public void listSettingRevisions() {
        ConfigurationClient client = createSyncConfigurationClient();
        // BEGIN: com.azure.data.applicationconfig.configurationclient.listSettingRevisions#settingSelector
        SettingSelector settingSelector = new SettingSelector().setKeys("prodDBConnection");
        client.listSettingRevisions(settingSelector).streamByPage().forEach(resp -> {
            System.out.printf("Response headers are %s. Url %s  and status code %d %n", resp.getHeaders(),
                resp.getRequest().getUrl(), resp.getStatusCode());
            resp.getItems().forEach(value -> {
                System.out.printf("Response value is %d %n", value);
            });
        });
        // END: com.azure.data.applicationconfig.configurationclient.listSettingRevisions#settingSelector
    }

    /**
     * Generates code sample for using {@link ConfigurationClient#listSettingRevisions(SettingSelector, Context)}
     */
    public void listSettingRevisionsContext() {
        ConfigurationClient configurationClient = createSyncConfigurationClient();
        // BEGIN: com.azure.data.applicationconfig.configurationclient.listSettingRevisions#settingSelector-context
        SettingSelector settingSelector = new SettingSelector().setKeys("prodDBConnection");
        Context ctx = new Context(key2, value2);
        configurationClient.listSettingRevisions(settingSelector, ctx).forEach(setting -> {
            System.out.printf("Key: %s, Value: %s", setting.getKey(), setting.getValue());
        });
        // END: com.azure.data.applicationconfig.configurationclient.listSettingRevisions#settingSelector-context
    }

    /**
     * Implementation not provided for this method
     *
     * @return {@code null}
     */
    private String getConnectionString() {
        return null;
    }
}
