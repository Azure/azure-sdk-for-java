// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.appconfiguration;

import com.azure.data.appconfiguration.models.ConfigurationSetting;
import com.azure.data.appconfiguration.models.SettingSelector;
import reactor.util.context.Context;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/**
 * Code snippets for {@link ConfigurationAsyncClient}
 */
public class ConfigurationAsyncClientJavaDocCodeSnippets {

    private static final String NO_LABEL = null;
    private String key1 = "key1";
    private String key2 = "key2";
    private String value1 = "val1";
    private String value2 = "val2";

    /**
     * Code snippets for {@link ConfigurationAsyncClient#addConfigurationSetting(String, String, String)}
     */
    public void addConfigurationSettingsCodeSnippet() {
        ConfigurationAsyncClient client = getAsyncClient();
        // BEGIN: com.azure.data.appconfiguration.configurationasyncclient.addConfigurationSetting#string-string-string
        client.addConfigurationSetting("prodDBConnection", "westUS", "db_connection")
            .subscribe(response -> System.out.printf("Key: %s, Label: %s, Value: %s",
                response.getKey(), response.getLabel(), response.getValue()));
        // END: com.azure.data.appconfiguration.configurationasyncclient.addConfigurationSetting#string-string-string

        // BEGIN: com.azure.data.appconfiguration.configurationasyncclient.addConfigurationSetting#ConfigurationSetting
        client.addConfigurationSetting(
            new ConfigurationSetting().setKey("prodDBConnection").setLabel("westUS").setValue("db_connection"))
            .subscribe(response -> System.out.printf("Key: %s, Label: %s, Value: %s",
                response.getKey(), response.getLabel(), response.getValue()));
        // END: com.azure.data.appconfiguration.configurationasyncclient.addConfigurationSetting#ConfigurationSetting

        /**
         * Code snippets for {@link ConfigurationAsyncClient#addConfigurationSettingWithResponse(ConfigurationSetting)}
         */
        // BEGIN: com.azure.data.appconfiguration.configurationasyncclient.addConfigurationSettingWithResponse#ConfigurationSetting
        client.addConfigurationSettingWithResponse(
            new ConfigurationSetting().setKey("prodDBConnection").setLabel("westUS").setValue("db_connection"))
            .subscribe(response -> {
                ConfigurationSetting responseSetting = response.getValue();
                System.out.printf("Key: %s, Label: %s, Value: %s",
                    responseSetting.getKey(), responseSetting.getLabel(), responseSetting.getValue());
            });
        // END: com.azure.data.appconfiguration.configurationasyncclient.addConfigurationSettingWithResponse#ConfigurationSetting
    }

    /**
     * Code snippets for {@link ConfigurationAsyncClient#setConfigurationSetting(String, String, String)}
     */
    public void setConfigurationSettingsCodeSnippet() {
        ConfigurationAsyncClient client = getAsyncClient();
        // BEGIN: com.azure.data.appconfiguration.configurationasyncclient.setConfigurationSetting#string-string-string
        client.setConfigurationSetting("prodDBConnection", "westUS", "db_connection")
            .subscribe(response -> System.out.printf("Key: %s, Label: %s, Value: %s",
                response.getKey(), response.getLabel(), response.getValue()));
        // Update the value of the setting to "updated_db_connection"
        client.setConfigurationSetting("prodDBConnection", "westUS", "updated_db_connection")
            .subscribe(response -> System.out.printf("Key: %s, Label: %s, Value: %s",
                response.getKey(), response.getLabel(), response.getValue()));
        // END: com.azure.data.appconfiguration.configurationasyncclient.setConfigurationSetting#string-string-string

        // BEGIN: com.azure.data.appconfiguration.configurationasyncclient.setConfigurationSetting#ConfigurationSetting
        client.setConfigurationSetting(new ConfigurationSetting().setKey("prodDBConnection").setLabel("westUS"))
            .subscribe(response -> System.out.printf("Key: %s, Label: %s, Value: %s",
                response.getKey(), response.getLabel(), response.getValue()));
        // Update the value of the setting to "updated_db_connection"
        client.setConfigurationSetting(
            new ConfigurationSetting().setKey("prodDBConnection").setLabel("westUS").setValue("updated_db_connection"))
            .subscribe(response -> System.out.printf("Key: %s, Label: %s, Value: %s",
                response.getKey(), response.getLabel(), response.getValue()));
        // END: com.azure.data.appconfiguration.configurationasyncclient.setConfigurationSetting#ConfigurationSetting

        /**
         * Code snippets for
         * {@link ConfigurationAsyncClient#setConfigurationSettingWithResponse(ConfigurationSetting, boolean)}
         */
        // BEGIN: com.azure.data.appconfiguration.configurationasyncclient.setConfigurationSettingWithResponse#ConfigurationSetting-boolean
        client.setConfigurationSettingWithResponse(new ConfigurationSetting().setKey("prodDBConnection").setLabel("westUS")
            .setValue("db_connection"), false)
            .subscribe(response -> {
                final ConfigurationSetting result = response.getValue();
                System.out.printf("Key: %s, Label: %s, Value: %s",
                    result.getKey(), result.getLabel(), result.getValue());
            });
        // Update the value of the setting to "updated_db_connection"
        client.setConfigurationSettingWithResponse(new ConfigurationSetting().setKey("prodDBConnection").setLabel("westUS")
            .setValue("updated_db_connection"), false)
            .subscribe(response -> {
                final ConfigurationSetting responseSetting = response.getValue();
                System.out.printf("Key: %s, Label: %s, Value: %s",
                    responseSetting.getKey(), responseSetting.getLabel(), responseSetting.getValue());
            });
        // END: com.azure.data.appconfiguration.configurationasyncclient.setConfigurationSettingWithResponse#ConfigurationSetting-boolean
    }

    /**
     * Code snippets for {@link ConfigurationAsyncClient#getConfigurationSetting(String, String)}
     */
    public void getConfigurationSettingsCodeSnippet() {
        ConfigurationAsyncClient client = getAsyncClient();

        // BEGIN: com.azure.data.appconfiguration.configurationasyncclient.getConfigurationSetting#string-string
        client.getConfigurationSetting("prodDBConnection", null)
            .subscribe(response -> System.out.printf("Key: %s, Label: %s, Value: %s",
                response.getKey(), response.getLabel(), response.getValue()));
        // END: com.azure.data.appconfiguration.configurationasyncclient.getConfigurationSetting#string-string

        /**
         * Code snippets for {@link ConfigurationAsyncClient#getConfigurationSetting(String, String, OffsetDateTime)}
         */
        // BEGIN: com.azure.data.appconfiguration.configurationasyncclient.getConfigurationSetting#string-string-OffsetDateTime
        client.getConfigurationSetting(
            "prodDBConnection", null, OffsetDateTime.now(ZoneOffset.UTC).minusMinutes(1))
            .subscribe(response -> System.out.printf("Key: %s, Label: %s, Value: %s",
                response.getKey(), response.getLabel(), response.getValue()));
        // END: com.azure.data.appconfiguration.configurationasyncclient.getConfigurationSetting#string-string-OffsetDateTime

        // BEGIN: com.azure.data.appconfiguration.configurationasyncclient.getConfigurationSetting#ConfigurationSetting
        client.getConfigurationSetting(new ConfigurationSetting().setKey("prodDBConnection").setLabel("westUS"))
            .subscribe(response -> System.out.printf("Key: %s, Label: %s, Value: %s",
                response.getKey(), response.getLabel(), response.getValue()));
        // END: com.azure.data.appconfiguration.configurationasyncclient.getConfigurationSetting#ConfigurationSetting

        /**
         * Code snippets for {@link ConfigurationAsyncClient#getConfigurationSettingWithResponse(
         * ConfigurationSetting, OffsetDateTime, boolean)}
         */
        // BEGIN: com.azure.data.appconfiguration.configurationasyncclient.getConfigurationSettingWithResponse#ConfigurationSetting-OffsetDateTime-boolean
        client.getConfigurationSettingWithResponse(
            new ConfigurationSetting().setKey("prodDBConnection").setLabel("westUS"), null, false)
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(response -> {
                final ConfigurationSetting result = response.getValue();
                System.out.printf("Key: %s, Label: %s, Value: %s",
                    result.getKey(), result.getLabel(), result.getValue());
            });
        // END: com.azure.data.appconfiguration.configurationasyncclient.getConfigurationSettingWithResponse#ConfigurationSetting-OffsetDateTime-boolean
    }

    /**
     * Code snippets for {@link ConfigurationAsyncClient#deleteConfigurationSetting(String, String)}
     */
    public void deleteConfigurationSettingsCodeSnippet() {
        ConfigurationAsyncClient client = getAsyncClient();
        // BEGIN: com.azure.data.appconfiguration.configurationasyncclient.deleteConfigurationSetting#string-string
        client.deleteConfigurationSetting("prodDBConnection", null)
            .subscribe(response -> System.out.printf("Key: %s, Label: %s, Value: %s",
                response.getKey(), response.getLabel(), response.getValue()));
        // END: com.azure.data.appconfiguration.configurationasyncclient.deleteConfigurationSetting#string-string

        // BEGIN: com.azure.data.appconfiguration.configurationasyncclient.deleteConfigurationSetting#ConfigurationSetting
        client.deleteConfigurationSetting(new ConfigurationSetting().setKey("prodDBConnection"))
            .subscribe(response -> System.out.printf("Key: %s, Label: %s, Value: %s",
                response.getKey(), response.getValue()));
        // END: com.azure.data.appconfiguration.configurationasyncclient.deleteConfigurationSetting#ConfigurationSetting

        /**
         * Code snippets for
         * {@link ConfigurationAsyncClient#deleteConfigurationSettingWithResponse(ConfigurationSetting, boolean)}
         */
        // BEGIN: com.azure.data.appconfiguration.configurationasyncclient.deleteConfigurationSettingWithResponse#ConfigurationSetting-boolean
        client.deleteConfigurationSettingWithResponse(
            new ConfigurationSetting().setKey("prodDBConnection").setLabel("westUS"), false)
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(response -> {
                final ConfigurationSetting responseSetting = response.getValue();
                System.out.printf("Key: %s, Label: %s, Value: %s",
                    responseSetting.getKey(), responseSetting.getLabel(), responseSetting.getValue());
            });
        // END: com.azure.data.appconfiguration.configurationasyncclient.deleteConfigurationSettingWithResponse#ConfigurationSetting-boolean
    }

    /**
     * Code snippets for {@link ConfigurationAsyncClient#setReadOnly(String, String, boolean)} set to read-only setting
     */
    public void lockSettingsCodeSnippet() {
        ConfigurationAsyncClient client = getAsyncClient();
        // BEGIN: com.azure.data.appconfiguration.configurationasyncclient.setReadOnly#string-string-boolean
        client.setReadOnly("prodDBConnection", "westUS", true)
            .subscribe(response -> System.out.printf("Key: %s, Label: %s, Value: %s",
                response.getKey(), response.getLabel(), response.getValue()));
        // END: com.azure.data.appconfiguration.configurationasyncclient.setReadOnly#string-string-boolean

        // BEGIN: com.azure.data.appconfiguration.configurationasyncclient.setReadOnly#ConfigurationSetting-boolean
        client.setReadOnly(new ConfigurationSetting().setKey("prodDBConnection").setLabel("westUS"), true)
            .subscribe(response -> System.out.printf("Key: %s, Label: %s, Value: %s",
                response.getKey(), response.getLabel(), response.getValue()));
        // END: com.azure.data.appconfiguration.configurationasyncclient.setReadOnly#ConfigurationSetting-boolean

        /**
         * Code snippets for {@link ConfigurationAsyncClient#setReadOnlyWithResponse(ConfigurationSetting, Boolean)}
         */
        // BEGIN: com.azure.data.appconfiguration.configurationasyncclient.setReadOnlyWithResponse#ConfigurationSetting-boolean
        client.setReadOnlyWithResponse(new ConfigurationSetting().setKey("prodDBConnection").setLabel("westUS"), true)
            .subscribe(response -> {
                final ConfigurationSetting result = response.getValue();
                System.out.printf("Key: %s, Label: %s, Value: %s",
                    result.getKey(), result.getLabel(), result.getValue());
            });
        // END: com.azure.data.appconfiguration.configurationasyncclient.setReadOnlyWithResponse#ConfigurationSetting-boolean
    }

    /**
     * Code snippets for {@link ConfigurationAsyncClient#setReadOnly(String, String, boolean)} set to not read-only setting
     */
    public void unlockSettingsCodeSnippet() {
        ConfigurationAsyncClient client = getAsyncClient();
        // BEGIN: com.azure.data.appconfiguration.configurationasyncclient.setReadOnly#string-string-boolean-clearReadOnly
        client.setReadOnly("prodDBConnection", "westUS", false)
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(response -> System.out.printf("Key: %s, Value: %s", response.getKey(), response.getValue()));
        // END: com.azure.data.appconfiguration.configurationasyncclient.setReadOnly#string-string-boolean-clearReadOnly

        // BEGIN: com.azure.data.appconfiguration.configurationasyncclient.setReadOnly#ConfigurationSetting-boolean-clearReadOnly
        client.setReadOnly(new ConfigurationSetting().setKey("prodDBConnection").setLabel("westUS"), false)
            .subscribe(response -> System.out.printf("Key: %s, Value: %s", response.getKey(), response.getValue()));
        // END: com.azure.data.appconfiguration.configurationasyncclient.setReadOnly#ConfigurationSetting-boolean-clearReadOnly

        /**
         * Code snippets for {@link ConfigurationAsyncClient#setReadOnlyWithResponse(ConfigurationSetting, Boolean)}
         */
        // BEGIN: com.azure.data.appconfiguration.configurationasyncclient.setReadOnlyWithResponse#ConfigurationSetting-boolean-clearReadOnly
        client.setReadOnlyWithResponse(new ConfigurationSetting().setKey("prodDBConnection").setLabel("westUS"), false)
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(response -> {
                ConfigurationSetting result = response.getValue();
                System.out.printf("Key: %s, Value: %s", result.getKey(), result.getValue());
            });
        // END: com.azure.data.appconfiguration.configurationasyncclient.setReadOnlyWithResponse#ConfigurationSetting-boolean-clearReadOnly
    }


    /**
     * Code snippets for {@link ConfigurationAsyncClient#listConfigurationSettings(SettingSelector)}
     */
    public void listSettingCodeSnippet() {
        ConfigurationAsyncClient client = getAsyncClient();
        // BEGIN: com.azure.data.appconfiguration.configurationasyncclient.listsettings
        client.listConfigurationSettings(new SettingSelector().setKeyFilter("prodDBConnection"))
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(setting ->
                System.out.printf("Key: %s, Value: %s", setting.getKey(), setting.getValue()));
        // END: com.azure.data.appconfiguration.configurationasyncclient.listsettings
    }

    /**
     * Code snippets for {@link ConfigurationAsyncClient#listRevisions(SettingSelector)}
     */
    public void listRevisionsCodeSnippet() {
        ConfigurationAsyncClient client = getAsyncClient();
        // BEGIN: com.azure.data.appconfiguration.configurationasyncclient.listsettingrevisions
        client.listRevisions(new SettingSelector().setKeyFilter("prodDBConnection"))
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(setting ->
                System.out.printf("Key: %s, Value: %s", setting.getKey(), setting.getValue()));
        // END: com.azure.data.appconfiguration.configurationasyncclient.listsettingrevisions
    }

    /**
     * Implementation not provided
     *
     * @return {@code null}
     */
    private ConfigurationAsyncClient getAsyncClient() {
        return new ConfigurationClientBuilder().connectionString("connectionString").buildAsyncClient();
    }
}
