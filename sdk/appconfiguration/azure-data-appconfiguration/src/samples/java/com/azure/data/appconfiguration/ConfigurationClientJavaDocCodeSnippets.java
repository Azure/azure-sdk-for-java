// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.appconfiguration;

import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.rest.Response;
import com.azure.core.test.models.RecordedData;
import com.azure.core.test.policy.RecordNetworkCallPolicy;
import com.azure.core.util.Context;
import com.azure.data.appconfiguration.credentials.ConfigurationClientCredentials;
import com.azure.data.appconfiguration.models.ConfigurationSetting;
import com.azure.data.appconfiguration.models.SettingSelector;

import java.security.GeneralSecurityException;

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
     * @return An instance of {@link ConfigurationClient}
     * @throws IllegalStateException If configuration credentials cannot be created.
     */
    public ConfigurationClient createAsyncConfigurationClientWithPipeline() {
        try {
            String connectionString = getConnectionString();
            // BEGIN: com.azure.data.applicationconfig.configurationclient.pipeline.instantiation
            RecordedData networkData = new RecordedData();
            HttpPipeline pipeline = new HttpPipelineBuilder().policies(new RecordNetworkCallPolicy(networkData)).build();

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
        ConfigurationSetting result = configurationClient
            .addSetting("prodDBConnection", "db_connection");
        System.out.printf("Key: %s, Value: %s", result.key(), result.value());
        // END: com.azure.data.applicationconfig.configurationclient.addSetting#string-string

    /*
      Generates code sample for using {@link ConfigurationClient#addSetting(ConfigurationSetting)}
     */
        // BEGIN: com.azure.data.applicationconfig.configurationclient.addSetting#ConfigurationSetting
        ConfigurationSetting resultSetting = configurationClient
            .addSetting(new ConfigurationSetting().key("prodDBConnection").label("westUS").value("db_connection"));
        System.out.printf("Key: %s, Value: %s", resultSetting.key(), resultSetting.value());
        // END: com.azure.data.applicationconfig.configurationclient.addSetting#ConfigurationSetting

    /*
      Generates code sample for using {@link ConfigurationClient#addSettingWithResponse(ConfigurationSetting, Context)}
     */
        // BEGIN: com.azure.data.applicationconfig.configurationclient.addSettingWithResponse#ConfigurationSetting-Context
        Response<ConfigurationSetting> responseResultSetting = configurationClient
            .addSettingWithResponse(
                new ConfigurationSetting()
                    .key("prodDBConnection").label("westUS").value("db_connection"), new Context(key1, value1));
        System.out.printf("Key: %s, Value: %s", responseResultSetting.value().key(), responseResultSetting.value().value());
        // END: com.azure.data.applicationconfig.configurationclient.addSettingWithResponse#ConfigurationSetting-Context
    }

    /**
     * Generates code sample for using {@link ConfigurationClient#setSetting(String, String)}
     */
    public void setSetting() {
        ConfigurationClient configurationClient = createSyncConfigurationClient();
        // BEGIN: com.azure.data.applicationconfig.configurationclient.setSetting#string-string
        ConfigurationSetting result = configurationClient
            .setSetting("prodDBConnection", "db_connection");
        System.out.printf("Key: %s, Value: %s", result.key(), result.value());

        // Update the value of the setting to "updated_db_connection".
        result = configurationClient.setSetting("prodDBConnection", "updated_db_connection");
        System.out.printf("Key: %s, Value: %s", result.key(), result.value());
        // END: com.azure.data.applicationconfig.configurationclient.setSetting#string-string

    /*
      Generates code sample for using {@link ConfigurationClient#setSetting(ConfigurationSetting)}
     */
        // BEGIN: com.azure.data.applicationconfig.configurationclient.setSetting#ConfigurationSetting
        // Add a setting with the key "prodDBConnection", label "westUS", and value "db_connection"
        ConfigurationSetting resultSetting = configurationClient
            .setSetting(new ConfigurationSetting().key("prodDBConnection").label("westUS").value("db_connection"));
        System.out.printf("Key: %s, Value: %s", resultSetting.key(), resultSetting.value());

        // Update the value of the setting to "updated_db_connection".
        resultSetting = configurationClient
             .setSetting(new ConfigurationSetting()
                 .key("prodDBConnection").label("westUS").value("updated_db_connection"));
        System.out.printf("Key: %s, Value: %s", resultSetting.key(), resultSetting.value());
        // END: com.azure.data.applicationconfig.configurationclient.setSetting#ConfigurationSetting

    /*
      Generates code sample for using {@link ConfigurationClient#setSettingWithResponse(ConfigurationSetting, Context)}
     */
        // BEGIN: com.azure.data.applicationconfig.configurationclient.setSettingWithResponse#ConfigurationSetting-Context
        // Add a setting with the key "prodDBConnection", label "westUS", and value "db_connection"
        Response<ConfigurationSetting> responseSetting = configurationClient
            .setSettingWithResponse(new ConfigurationSetting().key("prodDBConnection").label("westUS")
                    .value("db_connection"), new Context(key2, value2));
        System.out.printf("Key: %s, Value: %s", responseSetting.value().key(), responseSetting.value().value());

        // Update the value of the setting to "updated_db_connection".
        responseSetting = configurationClient
            .setSettingWithResponse(new ConfigurationSetting().key("prodDBConnection").label("westUS")
                .value("updated_db_connection"), new Context(key2, value2));
        System.out.printf("Key: %s, Value: %s", responseSetting.value().key(), responseSetting.value().value());
        // END: com.azure.data.applicationconfig.configurationclient.setSettingWithResponse#ConfigurationSetting-Context
    }

    /**
     * Generates code sample for using {@link ConfigurationClient#getSetting(String)}
     */
    public void getSetting() {
        ConfigurationClient configurationClient = createSyncConfigurationClient();
        // BEGIN: com.azure.data.applicationconfig.configurationclient.getSetting#string
        ConfigurationSetting result = configurationClient.getSetting("prodDBConnection");
        System.out.printf("Key: %s, Value: %s", result.key(), result.value());
        // END: com.azure.data.applicationconfig.configurationclient.getSetting#string

    /*
      Generates code sample for using {@link ConfigurationClient#getSetting(ConfigurationSetting)}
     */
        // BEGIN: com.azure.data.applicationconfig.configurationclient.getSetting#ConfigurationSetting
        // Retrieve the setting with the key-label "prodDBConnection"-"westUS".
        ConfigurationSetting resultSetting = configurationClient
            .getSetting(new ConfigurationSetting().key("prodDBConnection").label("westUS"));
        System.out.printf("Key: %s, Value: %s", resultSetting.key(), resultSetting.value());
        // END: com.azure.data.applicationconfig.configurationclient.getSetting#ConfigurationSetting

    /*
      Generates code sample for using {@link ConfigurationClient#getSettingWithResponse(ConfigurationSetting, Context)}
     */
        // BEGIN: com.azure.data.applicationconfig.configurationclient.getSettingWithResponse#ConfigurationSetting-Context
        // Retrieve the setting with the key-label "prodDBConnection"-"westUS".
        Response<ConfigurationSetting> responseResultSetting = configurationClient
            .getSettingWithResponse(new ConfigurationSetting().key("prodDBConnection").label("westUS"),
                new Context(key1, value1));
        System.out.printf("Key: %s, Value: %s", responseResultSetting.value().key(),
            responseResultSetting.value().value());
        // END: com.azure.data.applicationconfig.configurationclient.getSettingWithResponse#ConfigurationSetting-Context
    }

    /**
     * Generates code sample for using {@link ConfigurationClient#updateSetting(String, String)}
     */
    public void updateSetting() {
        ConfigurationClient configurationClient = createSyncConfigurationClient();
        // BEGIN: com.azure.data.applicationconfig.configurationclient.updateSetting#string-string

        // Update a setting with the key "prodDBConnection" to have the value "updated_db_connection".

        ConfigurationSetting result = configurationClient.updateSetting("prodDBConnection", "updated_db_connection");
        System.out.printf("Key: %s, Value: %s", result.key(), result.value());
        // END: com.azure.data.applicationconfig.configurationclient.updateSetting#string-string

    /*
      Generates code sample for using {@link ConfigurationClient#updateSetting(ConfigurationSetting)}
     */
        // BEGIN: com.azure.data.applicationconfig.configurationclient.updateSetting#ConfigurationSetting
        // Update the setting with the key-label pair "prodDBConnection"-"westUS" to have the value "updated_db_connection".
        ConfigurationSetting resultSetting = configurationClient
            .updateSetting(
                new ConfigurationSetting().key("prodDBConnection").label("westUS").value("updated_db_connection"));
        System.out.printf("Key: %s, Value: %s", resultSetting.key(), resultSetting.value());
        // END: com.azure.data.applicationconfig.configurationclient.updateSetting#ConfigurationSetting

    /*
      Generates code sample for using {@link ConfigurationClient#updateSettingWithResponse(ConfigurationSetting, Context)}
     */
        // BEGIN: com.azure.data.applicationconfig.configurationclient.updateSettingWithResponse#ConfigurationSetting-Context
        // Update the setting with the key-label pair "prodDBConnection"-"westUS" to have the value "updated_db_connection".
        Response<ConfigurationSetting> responseResultSetting = configurationClient
            .updateSettingWithResponse(new ConfigurationSetting().key("prodDBConnection").label("westUS")
                .value("updated_db_connection"), new Context(key1, value1));
        System.out.printf("Key: %s, Value: %s", responseResultSetting.value().key(),
            responseResultSetting.value().value());
        // END: com.azure.data.applicationconfig.configurationclient.updateSettingWithResponse#ConfigurationSetting-Context
    }

    /**
     * Generates code sample for using {@link ConfigurationClient#deleteSetting(String)}
     */
    public void deleteSetting() {
        ConfigurationClient configurationClient = createSyncConfigurationClient();
        // BEGIN: com.azure.data.applicationconfig.configurationclient.deleteSetting#string
        ConfigurationSetting result = configurationClient
            .deleteSetting("prodDBConnection");
        System.out.printf("Key: %s, Value: %s", result.key(), result.value());

        // END: com.azure.data.applicationconfig.configurationclient.deleteSetting#string

        /**
         * Generates code sample for using {@link ConfigurationClient#addSetting(ConfigurationSetting)}
         */
        // BEGIN: com.azure.data.applicationconfig.configurationclient.deleteSetting#ConfigurationSetting
        ConfigurationSetting resultSetting = configurationClient
            .deleteSetting(new ConfigurationSetting().key("prodDBConnection").label("westUS"));
        System.out.printf("Key: %s, Value: %s", resultSetting.key(), resultSetting.value());
        // END: com.azure.data.applicationconfig.configurationclient.deleteSetting#ConfigurationSetting

        /**
         * Generates code sample for using {@link ConfigurationClient#deleteSettingWithResponse(ConfigurationSetting, Context)}
         */
        // BEGIN: com.azure.data.applicationconfig.configurationclient.deleteSettingWithResponse#ConfigurationSetting-Context
        Response<ConfigurationSetting> responseSetting = configurationClient
            .deleteSettingWithResponse(new ConfigurationSetting().key("prodDBConnection").label("westUS"),
                new Context(key2, value2));
        System.out.printf("Key: %s, Value: %s", responseSetting.value().key(), responseSetting.value().value());
        // END: com.azure.data.applicationconfig.configurationclient.deleteSettingWithResponse#ConfigurationSetting-Context
    }

    /**
     * Generates code sample for using {@link ConfigurationClient#listSettings(SettingSelector)}
     */
    public void listSettings() {
        ConfigurationClient configurationClient = createSyncConfigurationClient();
        // BEGIN: com.azure.data.applicationconfig.configurationclient.listSettings#settingSelector
        SettingSelector settingSelector = new SettingSelector().keys("prodDBConnection");
        configurationClient.listSettings(settingSelector).forEach(setting -> {
            System.out.printf("Key: %s, Value: %s", setting.key(), setting.value());
        });
        // END: com.azure.data.applicationconfig.configurationclient.listSettings#settingSelector
    }

    /**
     * Generates code sample for using {@link ConfigurationClient#listSettings(SettingSelector, Context)}
     */
    public void listSettingsContext() {
        ConfigurationClient configurationClient = createSyncConfigurationClient();
        // BEGIN: com.azure.data.applicationconfig.configurationclient.listSettings#settingSelector-context
        SettingSelector settingSelector = new SettingSelector().keys("prodDBConnection");
        Context ctx = new Context(key2, value2);
        configurationClient.listSettings(settingSelector, ctx).forEach(setting -> {
            System.out.printf("Key: %s, Value: %s", setting.key(), setting.value());
        });
        // END: com.azure.data.applicationconfig.configurationclient.listSettings#settingSelector-context
    }

    /**
     * Generates code sample for using {@link ConfigurationClient#listSettingRevisions(SettingSelector)}
     */
    public void listSettingRevisions() {
        ConfigurationClient client = createSyncConfigurationClient();
        // BEGIN: com.azure.data.applicationconfig.configurationclient.listSettingRevisions#settingSelector
        SettingSelector settingSelector = new SettingSelector().keys("prodDBConnection");
        client.listSettingRevisions(settingSelector).streamByPage().forEach(resp -> {
            System.out.printf("Response headers are %s. Url %s  and status code %d %n", resp.headers(),
                resp.request().url(), resp.statusCode());
            resp.items().forEach(value -> {
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
        SettingSelector settingSelector = new SettingSelector().keys("prodDBConnection");
        Context ctx = new Context(key2, value2);
        configurationClient.listSettingRevisions(settingSelector, ctx).forEach(setting -> {
            System.out.printf("Key: %s, Value: %s", setting.key(), setting.value());
        });
        // END: com.azure.data.applicationconfig.configurationclient.listSettingRevisions#settingSelector-context
    }

    /**
     * Implementation not provided for this method
     * @return {@code null}
     */
    private String getConnectionString() {
        return null;
    }
}
