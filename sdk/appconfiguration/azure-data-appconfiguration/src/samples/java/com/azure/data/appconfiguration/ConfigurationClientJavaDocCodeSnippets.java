// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.appconfiguration;

import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.data.appconfiguration.models.ConfigurationSetting;
import com.azure.data.appconfiguration.models.SettingSelector;

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

        String connectionString = getConnectionString();
        // BEGIN: com.azure.data.applicationconfig.configurationclient.pipeline.instantiation
        HttpPipeline pipeline = new HttpPipelineBuilder()
            .policies(/* add policies */)
            .build();

        ConfigurationClient configurationClient = new ConfigurationClientBuilder()
            .pipeline(pipeline)
            .endpoint("https://myconfig.azure.net/")
            .connectionString(connectionString)
            .buildClient();
        // END: com.azure.data.applicationconfig.configurationclient.pipeline.instantiation
        return configurationClient;
    }

    /**
     * Generates code sample for creating a {@link ConfigurationClient}
     *
     * @return An instance of {@link ConfigurationClient}
     * @throws IllegalStateException If configuration credentials cannot be created
     */
    public ConfigurationAsyncClient createAsyncConfigurationClient() {
        String connectionString = getConnectionString();
        // BEGIN: com.azure.data.applicationconfig.async.configurationclient.instantiation
        ConfigurationAsyncClient configurationAsyncClient = new ConfigurationClientBuilder()
            .connectionString(connectionString)
            .buildAsyncClient();
        // END: com.azure.data.applicationconfig.async.configurationclient.instantiation
        return configurationAsyncClient;
    }

    /**
     * Generates code sample for creating a {@link ConfigurationClient}
     *
     * @return An instance of {@link ConfigurationClient}
     * @throws IllegalStateException If configuration credentials cannot be created
     */
    public ConfigurationClient createSyncConfigurationClient() {
        String connectionString = getConnectionString();
        // BEGIN: com.azure.data.applicationconfig.configurationclient.instantiation
        ConfigurationClient configurationClient = new ConfigurationClientBuilder()
            .connectionString(connectionString)
            .buildClient();
        // END: com.azure.data.applicationconfig.configurationclient.instantiation
        return configurationClient;
    }

    /**
     * Generates code sample for using {@link ConfigurationClient#addConfigurationSetting(String, String, String)}
     */
    public void addConfigurationSetting() {
        ConfigurationClient configurationClient = createSyncConfigurationClient();
        // BEGIN: com.azure.data.appconfiguration.ConfigurationClient.addConfigurationSetting#String-String-String
        ConfigurationSetting result = configurationClient
            .addConfigurationSetting("prodDBConnection", "westUS", "db_connection");
        System.out.printf("Key: %s, Label: %s, Value: %s", result.getKey(), result.getLabel(), result.getValue());
        // END: com.azure.data.appconfiguration.ConfigurationClient.addConfigurationSetting#String-String-String

        /**
         Generates code sample for using
         {@link ConfigurationClient#addConfigurationSetting(ConfigurationSetting)}
         */
        // BEGIN: com.azure.data.appconfiguration.ConfigurationClient.addConfigurationSetting#ConfigurationSetting
        ConfigurationSetting setting = configurationClient.addConfigurationSetting(
            new ConfigurationSetting().setKey("prodDBConnection").setLabel("westUS").setValue("db_connection"));
        System.out.printf("Key: %s, Label: %s, Value: %s", setting.getKey(), setting.getLabel(), setting.getValue());
        // END: com.azure.data.appconfiguration.ConfigurationClient.addConfigurationSetting#ConfigurationSetting

        /**
          Generates code sample for using
         {@link ConfigurationClient#addConfigurationSettingWithResponse(ConfigurationSetting, Context)}
         */
        // BEGIN: com.azure.data.appconfiguration.ConfigurationClient.addConfigurationSettingWithResponse#ConfigurationSetting-Context
        Response<ConfigurationSetting> responseResultSetting = configurationClient
            .addConfigurationSettingWithResponse(
                new ConfigurationSetting()
                    .setKey("prodDBConnection").setLabel("westUS").setValue("db_connection"),
                new Context(key1, value1));
        final ConfigurationSetting resultSetting = responseResultSetting.getValue();
        System.out.printf("Key: %s, Label: %s, Value: %s", resultSetting.getKey(), resultSetting.getLabel(),
            resultSetting.getValue());
        // END: com.azure.data.appconfiguration.ConfigurationClient.addConfigurationSettingWithResponse#ConfigurationSetting-Context
    }

    /**
     * Generates code sample for using {@link ConfigurationClient#setConfigurationSetting(String, String, String)}
     */
    public void setConfigurationSetting() {
        ConfigurationClient configurationClient = createSyncConfigurationClient();
        // BEGIN: com.azure.data.appconfiguration.ConfigurationClient.setConfigurationSetting#String-String-String
        ConfigurationSetting result = configurationClient
            .setConfigurationSetting("prodDBConnection", "westUS", "db_connection");
        System.out.printf("Key: %s, Label: %s, Value: %s", result.getKey(), result.getLabel(), result.getValue());

        // Update the value of the setting to "updated_db_connection".
        result = configurationClient.setConfigurationSetting(
            "prodDBConnection", "westUS", "updated_db_connection");
        System.out.printf("Key: %s, Label: %s, Value: %s", result.getKey(), result.getLabel(), result.getValue());
        // END: com.azure.data.appconfiguration.ConfigurationClient.setConfigurationSetting#String-String-String

        // BEGIN: com.azure.data.appconfiguration.ConfigurationClient.setConfigurationSetting#ConfigurationSetting
        ConfigurationSetting setting = configurationClient.setConfigurationSetting(
            new ConfigurationSetting().setKey("prodDBConnection").setLabel("westUS").setValue("db_connection"));
        System.out.printf("Key: %s, Label: %s, Value: %s", setting.getKey(), setting.getLabel(), setting.getValue());

        // Update the value of the setting to "updated_db_connection".
        setting = configurationClient.setConfigurationSetting(
            new ConfigurationSetting().setKey("prodDBConnection").setLabel("westUS").setValue("updated_db_connection"));
        System.out.printf("Key: %s, Label: %s, Value: %s", setting.getKey(), setting.getLabel(), setting.getValue());
        // END: com.azure.data.appconfiguration.ConfigurationClient.setConfigurationSetting#ConfigurationSetting

        /**
         * Generates code sample for using
         * {@link ConfigurationClient#setConfigurationSettingWithResponse(ConfigurationSetting, boolean, Context)}
         */
        // BEGIN: com.azure.data.appconfiguration.ConfigurationClient.setConfigurationSettingWithResponse#ConfigurationSetting-boolean-Context
        // Add a setting with the key "prodDBConnection", label "westUS", and value "db_connection"
        Response<ConfigurationSetting> responseSetting = configurationClient.setConfigurationSettingWithResponse(
            new ConfigurationSetting().setKey("prodDBConnection").setLabel("westUS").setValue("db_connection"),
            false,
            new Context(key2, value2));
        final ConfigurationSetting initSetting = responseSetting.getValue();
        System.out.printf("Key: %s, Value: %s", initSetting.getKey(), initSetting.getValue());

        // Update the value of the setting to "updated_db_connection".
        responseSetting = configurationClient.setConfigurationSettingWithResponse(
            new ConfigurationSetting().setKey("prodDBConnection").setLabel("westUS").setValue("updated_db_connection"),
            false,
            new Context(key2, value2));
        final ConfigurationSetting updatedSetting = responseSetting.getValue();
        System.out.printf("Key: %s, Value: %s", updatedSetting.getKey(), updatedSetting.getValue());
        // END: com.azure.data.appconfiguration.ConfigurationClient.setConfigurationSettingWithResponse#ConfigurationSetting-boolean-Context
    }

    /**
     * Generates code sample for using {@link ConfigurationClient#getConfigurationSetting(String, String)}
     */
    public void getConfigurationSetting() {
        ConfigurationClient configurationClient = createSyncConfigurationClient();

        // BEGIN: com.azure.data.applicationconfig.configurationclient.getConfigurationSetting#string-string
        ConfigurationSetting resultNoDateTime =
            configurationClient.getConfigurationSetting("prodDBConnection", null);
        System.out.printf("Key: %s, Value: %s", resultNoDateTime.getKey(), resultNoDateTime.getValue());
        // END: com.azure.data.applicationconfig.configurationclient.getConfigurationSetting#string-string

    /**
     * Generates code sample for using {@link ConfigurationClient#getConfigurationSetting(String, String, OffsetDateTime)}
     */
        // BEGIN: com.azure.data.applicationconfig.configurationclient.getConfigurationSetting#string-string-OffsetDateTime
        ConfigurationSetting result =
            configurationClient.getConfigurationSetting("prodDBConnection", null, null);
        System.out.printf("Key: %s, Value: %s", result.getKey(), result.getValue());
        // END: com.azure.data.applicationconfig.configurationclient.getConfigurationSetting#string-string-OffsetDateTime

        // BEGIN: com.azure.data.applicationconfig.configurationclient.getConfigurationSetting#ConfigurationSetting
        ConfigurationSetting setting = configurationClient.getConfigurationSetting(
            new ConfigurationSetting().setKey("prodDBConnection"));
        System.out.printf("Key: %s, Value: %s", setting.getKey(), setting.getValue());
        // END: com.azure.data.applicationconfig.configurationclient.getConfigurationSetting#ConfigurationSetting

    /**
      * Generates code sample for using {@link ConfigurationClient#getConfigurationSettingWithResponse(
      * ConfigurationSetting, OffsetDateTime, boolean, Context)}
     */
        // BEGIN: com.azure.data.applicationconfig.configurationclient.getConfigurationSettingWithResponse#ConfigurationSetting-OffsetDateTime-boolean-Context
        // Retrieve the setting with the key-label "prodDBConnection"-"westUS".
        Response<ConfigurationSetting> responseResultSetting = configurationClient.getConfigurationSettingWithResponse(
            new ConfigurationSetting().setKey("prodDBConnection").setLabel("westUS"),
            null, false, new Context(key1, value1));
        System.out.printf("Key: %s, Value: %s", responseResultSetting.getValue().getKey(),
            responseResultSetting.getValue().getValue());
        // END: com.azure.data.applicationconfig.configurationclient.getConfigurationSettingWithResponse#ConfigurationSetting-OffsetDateTime-boolean-Context
    }

    /**
     * Generates code sample for using {@link ConfigurationClient#deleteConfigurationSetting(String, String)}
     */
    public void deleteConfigurationSetting() {
        ConfigurationClient configurationClient = createSyncConfigurationClient();
        // BEGIN: com.azure.data.applicationconfig.configurationclient.deleteConfigurationSetting#string-string
        ConfigurationSetting result = configurationClient.deleteConfigurationSetting("prodDBConnection", null);
        System.out.printf("Key: %s, Value: %s", result.getKey(), result.getValue());
        // END: com.azure.data.applicationconfig.configurationclient.deleteConfigurationSetting#string-string

        // BEGIN: com.azure.data.applicationconfig.configurationclient.deleteConfigurationSetting#ConfigurationSetting
        ConfigurationSetting setting = configurationClient.deleteConfigurationSetting(
            new ConfigurationSetting().setKey("prodDBConnection"));
        System.out.printf("Key: %s, Value: %s", setting.getKey(), setting.getValue());
        // END: com.azure.data.applicationconfig.configurationclient.deleteConfigurationSetting#ConfigurationSetting

        /**
         * Generates code sample for using
         * {@link ConfigurationClient#deleteConfigurationSettingWithResponse(ConfigurationSetting, Context)}
         */
        // BEGIN: com.azure.data.applicationconfig.configurationclient.deleteConfigurationSettingWithResponse#ConfigurationSetting-boolean-Context
        Response<ConfigurationSetting> responseSetting = configurationClient.deleteConfigurationSettingWithResponse(
            new ConfigurationSetting().setKey("prodDBConnection").setLabel("westUS"),
            false, new Context(key2, value2));
        System.out.printf(
            "Key: %s, Value: %s", responseSetting.getValue().getKey(), responseSetting.getValue().getValue());
        // END: com.azure.data.applicationconfig.configurationclient.deleteConfigurationSettingWithResponse#ConfigurationSetting-boolean-Context
    }

    /**
     * Generates code sample for using {@link ConfigurationClient#setReadOnly(String, String, boolean)}
     */
    public void lockSettingsCodeSnippet() {
        ConfigurationClient configurationClient = createSyncConfigurationClient();
        // BEGIN: com.azure.data.applicationconfig.configurationclient.setReadOnly#string-string-boolean
        ConfigurationSetting result = configurationClient.setReadOnly("prodDBConnection", "westUS", true);
        System.out.printf("Key: %s, Value: %s", result.getKey(), result.getValue());
        // END: com.azure.data.applicationconfig.configurationclient.setReadOnly#string-string-boolean

        // BEGIN: com.azure.data.applicationconfig.configurationclient.setReadOnly#ConfigurationSetting-boolean
        ConfigurationSetting setting = configurationClient.setReadOnly(
            new ConfigurationSetting().setKey("prodDBConnection").setLabel("westUS"), true);
        System.out.printf("Key: %s, Value: %s", setting.getKey(), setting.getValue());
        // END: com.azure.data.applicationconfig.configurationclient.setReadOnly#ConfigurationSetting-boolean

        /**
         * Generates code sample for using {@link ConfigurationClient#setReadOnlyWithResponse(ConfigurationSetting, Boolean, Context)}
         */
        // BEGIN: com.azure.data.applicationconfig.configurationclient.setReadOnlyWithResponse#ConfigurationSetting-Boolean-Context
        ConfigurationSetting resultSetting = configurationClient.setReadOnlyWithResponse(
            new ConfigurationSetting().setKey("prodDBConnection").setLabel("westUS"), true, Context.NONE)
            .getValue();
        System.out.printf("Key: %s, Value: %s", resultSetting.getKey(), resultSetting.getValue());
        // END: com.azure.data.applicationconfig.configurationclient.setReadOnlyWithResponse#ConfigurationSetting-Boolean-Context
    }

    /**
     * Generates code sample for using {@link ConfigurationClient#setReadOnly(String, String, boolean)}
     */
    public void unlockSettingsCodeSnippet() {
        ConfigurationClient configurationClient = createSyncConfigurationClient();
        // BEGIN: com.azure.data.applicationconfig.configurationclient.setReadOnly#string-string-boolean-clearReadOnly
        ConfigurationSetting result = configurationClient.setReadOnly("prodDBConnection", "westUS", false);
        System.out.printf("Key: %s, Value: %s", result.getKey(), result.getValue());
        // END: com.azure.data.applicationconfig.configurationclient.setReadOnly#string-string-boolean-clearReadOnly

        // BEGIN: com.azure.data.applicationconfig.configurationclient.setReadOnly#ConfigurationSetting-boolean-clearReadOnly
        ConfigurationSetting setting = configurationClient.setReadOnly(
            new ConfigurationSetting().setKey("prodDBConnection").setLabel("westUS"), false);
        System.out.printf("Key: %s, Value: %s", setting.getKey(), setting.getValue());
        // END: com.azure.data.applicationconfig.configurationclient.setReadOnly#ConfigurationSetting-boolean-clearReadOnly

        /**
         * Generates code sample for using
         * {@link ConfigurationClient#setReadOnlyWithResponse(ConfigurationSetting, Boolean, Context)}
         */
        // BEGIN: com.azure.data.applicationconfig.configurationclient.setReadOnlyWithResponse#ConfigurationSetting-boolean-Context-ClearReadOnly
        Response<ConfigurationSetting> responseSetting = configurationClient
            .setConfigurationSettingWithResponse(
                new ConfigurationSetting().setKey("prodDBConnection").setLabel("westUS"), false,
                new Context(key2, value2));
        System.out.printf("Key: %s, Value: %s", responseSetting.getValue().getKey(),
            responseSetting.getValue().getValue());
        // END: com.azure.data.applicationconfig.configurationclient.setReadOnlyWithResponse#ConfigurationSetting-boolean-Context-ClearReadOnly
    }

    /**
     * Generates code sample for using {@link ConfigurationClient#listConfigurationSettings(SettingSelector)}
     */
    public void listConfigurationSettings() {
        ConfigurationClient configurationClient = createSyncConfigurationClient();
        // BEGIN: com.azure.data.applicationconfig.configurationclient.listConfigurationSettings#settingSelector
        SettingSelector settingSelector = new SettingSelector().setKeyFilter("prodDBConnection");
        configurationClient.listConfigurationSettings(settingSelector).forEach(setting -> {
            System.out.printf("Key: %s, Value: %s", setting.getKey(), setting.getValue());
        });
        // END: com.azure.data.applicationconfig.configurationclient.listConfigurationSettings#settingSelector
    }

    /**
     * Generates code sample for using {@link ConfigurationClient#listConfigurationSettings(SettingSelector, Context)}
     */
    public void listConfigurationSettingsContext() {
        ConfigurationClient configurationClient = createSyncConfigurationClient();
        // BEGIN: com.azure.data.applicationconfig.configurationclient.listConfigurationSettings#settingSelector-context
        SettingSelector settingSelector = new SettingSelector().setKeyFilter("prodDBConnection");
        Context ctx = new Context(key2, value2);
        configurationClient.listConfigurationSettings(settingSelector, ctx).forEach(setting -> {
            System.out.printf("Key: %s, Value: %s", setting.getKey(), setting.getValue());
        });
        // END: com.azure.data.applicationconfig.configurationclient.listConfigurationSettings#settingSelector-context
    }

    /**
     * Generates code sample for using {@link ConfigurationClient#listRevisions(SettingSelector)}
     */
    public void listRevisions() {
        ConfigurationClient client = createSyncConfigurationClient();
        // BEGIN: com.azure.data.applicationconfig.configurationclient.listRevisions#settingSelector
        SettingSelector settingSelector = new SettingSelector().setKeyFilter("prodDBConnection");
        client.listRevisions(settingSelector).streamByPage().forEach(resp -> {
            System.out.printf("Response headers are %s. Url %s  and status code %d %n", resp.getHeaders(),
                resp.getRequest().getUrl(), resp.getStatusCode());
            resp.getItems().forEach(value -> {
                System.out.printf("Response value is %d %n", value);
            });
        });
        // END: com.azure.data.applicationconfig.configurationclient.listRevisions#settingSelector
    }

    /**
     * Generates code sample for using {@link ConfigurationClient#listRevisions(SettingSelector, Context)}
     */
    public void listRevisionsContext() {
        ConfigurationClient configurationClient = createSyncConfigurationClient();
        // BEGIN: com.azure.data.applicationconfig.configurationclient.listRevisions#settingSelector-context
        SettingSelector settingSelector = new SettingSelector().setKeyFilter("prodDBConnection");
        Context ctx = new Context(key2, value2);
        configurationClient.listRevisions(settingSelector, ctx).forEach(setting -> {
            System.out.printf("Key: %s, Value: %s", setting.getKey(), setting.getValue());
        });
        // END: com.azure.data.applicationconfig.configurationclient.listRevisions#settingSelector-context
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
