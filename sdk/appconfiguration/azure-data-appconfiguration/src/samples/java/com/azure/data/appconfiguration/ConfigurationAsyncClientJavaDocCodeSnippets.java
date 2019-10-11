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
     * Code snippets for {@link ConfigurationAsyncClient#addSetting(String, String, String)}
     */
    public void addSettingsCodeSnippet() {
        ConfigurationAsyncClient client = getAsyncClient();
        // BEGIN: com.azure.data.appconfiguration.configurationasyncclient.addSetting#string-string-string
        client.addSetting("prodDBConnection", "westUS", "db_connection")
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(response -> System.out.printf("Key: %s, Label: %s, Value: %s",
                response.getKey(), response.getLabel(), response.getValue()));
        // END: com.azure.data.appconfiguration.configurationasyncclient.addSetting#string-string-string

        /**
         * Code snippets for {@link ConfigurationAsyncClient#addSetting(ConfigurationSetting)}
         */
        // BEGIN: com.azure.data.appconfiguration.configurationasyncclient.addSetting#ConfigurationSetting
        client.addSetting(new ConfigurationSetting().setKey("prodDBConnection").setLabel("westUS").setValue("db_connection"))
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(response -> System.out.printf("Key: %s, Label: %s, Value: %s",
                response.getKey(), response.getLabel(), response.getValue()));
        // END: com.azure.data.appconfiguration.configurationasyncclient.addSetting#ConfigurationSetting

        /**
         * Code snippets for {@link ConfigurationAsyncClient#addSettingWithResponse(ConfigurationSetting)}
         */
        // BEGIN: com.azure.data.appconfiguration.configurationasyncclient.addSettingWithResponse#ConfigurationSetting
        client.addSettingWithResponse(new ConfigurationSetting().setKey("prodDBConnection").setLabel("westUS")
            .setValue("db_connection"))
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(response -> {
                ConfigurationSetting responseSetting = response.getValue();
                System.out.printf("Key: %s, Label: %s, Value: %s",
                    responseSetting.getKey(), responseSetting.getLabel(), responseSetting.getValue());
            });
        // END: com.azure.data.appconfiguration.configurationasyncclient.addSettingWithResponse#ConfigurationSetting
    }

    /**
     * Code snippets for {@link ConfigurationAsyncClient#setSetting(String, String, String)}
     */
    public void setSettingsCodeSnippet() {
        ConfigurationAsyncClient client = getAsyncClient();
        // BEGIN: com.azure.data.appconfiguration.configurationasyncclient.setSetting#string-string-string
        client.setSetting("prodDBConnection", "westUS", "db_connection")
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(response -> System.out.printf("Key: %s, Label: %s, Value: %s",
                response.getKey(), response.getLabel(), response.getValue()));
        // Update the value of the setting to "updated_db_connection"
        client.setSetting("prodDBConnection", "westUS", "updated_db_connection")
            .subscribe(response -> System.out.printf("Key: %s, Label: %s, Value: %s",
                response.getKey(), response.getLabel(), response.getValue()));
        // END: com.azure.data.appconfiguration.configurationasyncclient.setSetting#string-string-string

        /**
         * Code snippets for {@link ConfigurationAsyncClient#setSettingWithResponse(ConfigurationSetting, boolean)}
         */
        // BEGIN: com.azure.data.appconfiguration.configurationasyncclient.setSettingWithResponse#ConfigurationSetting-boolean
        client.setSettingWithResponse(new ConfigurationSetting().setKey("prodDBConnection").setLabel("westUS")
            .setValue("db_connection"), false)
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(response -> {
                final ConfigurationSetting result = response.getValue();
                System.out.printf("Key: %s, Label: %s, Value: %s",
                    result.getKey(), result.getLabel(), result.getValue());
            });
        // Update the value of the setting to "updated_db_connection"
        client.setSettingWithResponse(new ConfigurationSetting().setKey("prodDBConnection").setLabel("westUS")
            .setValue("updated_db_connection"), false)
            .subscribe(response -> {
                final ConfigurationSetting responseSetting = response.getValue();
                System.out.printf("Key: %s, Label: %s, Value: %s",
                    responseSetting.getKey(), responseSetting.getLabel(), responseSetting.getValue());
            });
        // END: com.azure.data.appconfiguration.configurationasyncclient.setSettingWithResponse#ConfigurationSetting-boolean
    }

    /**
     * Code snippets for {@link ConfigurationAsyncClient#getSetting(String, String)}
     */
    public void getSettingsCodeSnippet() {
        ConfigurationAsyncClient client = getAsyncClient();

        // BEGIN: com.azure.data.appconfiguration.configurationasyncclient.getSetting#string-string
        client.getSetting("prodDBConnection", null)
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(response -> System.out.printf("Key: %s, Label: %s, Value: %s",
                response.getKey(), response.getLabel(), response.getValue()));
        // END: com.azure.data.appconfiguration.configurationasyncclient.getSetting#string-string

        /**
         * Code snippets for {@link ConfigurationAsyncClient#getSetting(String, String, OffsetDateTime)}
         */
        // BEGIN: com.azure.data.appconfiguration.configurationasyncclient.getSetting#string-string-OffsetDateTime
        client.getSetting("prodDBConnection", null, OffsetDateTime.now(ZoneOffset.UTC).minusMinutes(1))
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(response -> System.out.printf("Key: %s, Label: %s, Value: %s",
                response.getKey(), response.getLabel(), response.getValue()));
        // END: com.azure.data.appconfiguration.configurationasyncclient.getSetting#string-string-OffsetDateTime

        /**
         * Code snippets for {@link ConfigurationAsyncClient#getSettingWithResponse(ConfigurationSetting, OffsetDateTime, boolean)}
         */
        // BEGIN: com.azure.data.appconfiguration.configurationasyncclient.getSettingWithResponse#ConfigurationSetting-OffsetDateTime-boolean
        client.getSettingWithResponse(new ConfigurationSetting().setKey("prodDBConnection").setLabel("westUS"), null,
            false)
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(response -> {
                final ConfigurationSetting result = response.getValue();
                System.out.printf("Key: %s, Label: %s, Value: %s",
                    result.getKey(), result.getLabel(), result.getValue());
            });
        // END: com.azure.data.appconfiguration.configurationasyncclient.getSettingWithResponse#ConfigurationSetting-OffsetDateTime-boolean
    }

    /**
     * Code snippets for {@link ConfigurationAsyncClient#deleteSetting(String, String)}
     */
    public void deleteSettingsCodeSnippet() {
        ConfigurationAsyncClient client = getAsyncClient();
        // BEGIN: com.azure.data.appconfiguration.configurationasyncclient.deleteSetting#string-string
        client.deleteSetting("prodDBConnection", null)
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(response -> System.out.printf("Key: %s, Label: %s, Value: %s",
                response.getKey(), response.getLabel(), response.getValue()));
        // END: com.azure.data.appconfiguration.configurationasyncclient.deleteSetting#string-string

        /**
         * Code snippets for {@link ConfigurationAsyncClient#deleteSettingWithResponse(ConfigurationSetting, boolean)}
         */
        // BEGIN: com.azure.data.appconfiguration.configurationasyncclient.deleteSettingWithResponse#ConfigurationSetting-boolean
        client.deleteSettingWithResponse(new ConfigurationSetting().setKey("prodDBConnection").setLabel("westUS"), false)
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(response -> {
                final ConfigurationSetting responseSetting = response.getValue();
                System.out.printf("Key: %s, Label: %s, Value: %s",
                    responseSetting.getKey(), responseSetting.getLabel(), responseSetting.getValue());
            });
        // END: com.azure.data.appconfiguration.configurationasyncclient.deleteSettingWithResponse#ConfigurationSetting-boolean
    }

    /**
     * Code snippets for {@link ConfigurationAsyncClient#setReadOnly(String, String)}
     */
    public void lockSettingsCodeSnippet() {
        ConfigurationAsyncClient client = getAsyncClient();
        // BEGIN: com.azure.data.appconfiguration.configurationasyncclient.setReadOnly#string-string
        client.setReadOnly("prodDBConnection", "westUS")
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(response -> System.out.printf("Key: %s, Label: %s, Value: %s",
                response.getKey(), response.getLabel(), response.getValue()));
        // END: com.azure.data.appconfiguration.configurationasyncclient.setReadOnly#string-string

        /**
         * Code snippets for {@link ConfigurationAsyncClient#setReadOnlyWithResponse(ConfigurationSetting)}
         */
        // BEGIN: com.azure.data.appconfiguration.configurationasyncclient.setReadOnlyWithResponse#ConfigurationSetting
        client.setReadOnlyWithResponse(new ConfigurationSetting().setKey("prodDBConnection").setLabel("westUS"))
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(response -> {
                final ConfigurationSetting result = response.getValue();
                System.out.printf("Key: %s, Label: %s, Value: %s",
                    result.getKey(), result.getLabel(), result.getValue());
            });
        // END: com.azure.data.appconfiguration.configurationasyncclient.setReadOnlyWithResponse#ConfigurationSetting
    }

    /**
     * Code snippets for {@link ConfigurationAsyncClient#clearReadOnly(String, String)}
     */
    public void unlockSettingsCodeSnippet() {
        ConfigurationAsyncClient client = getAsyncClient();
        // BEGIN: com.azure.data.appconfiguration.configurationasyncclient.clearReadOnly#string-string
        client.clearReadOnly("prodDBConnection", "westUS")
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(response -> System.out.printf("Key: %s, Value: %s", response.getKey(), response.getValue()));
        // END: com.azure.data.appconfiguration.configurationasyncclient.clearReadOnly#string-string

        /**
         * Code snippets for {@link ConfigurationAsyncClient#clearReadOnlyWithResponse(ConfigurationSetting)}
         */
        // BEGIN: com.azure.data.appconfiguration.configurationasyncclient.clearReadOnlyWithResponse#ConfigurationSetting
        client.clearReadOnlyWithResponse(new ConfigurationSetting().setKey("prodDBConnection").setLabel("westUS"))
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(response -> {
                ConfigurationSetting result = response.getValue();
                System.out.printf("Key: %s, Value: %s", result.getKey(), result.getValue());
            });
        // END: com.azure.data.appconfiguration.configurationasyncclient.clearReadOnlyWithResponse#ConfigurationSetting
    }


    /**
     * Code snippets for {@link ConfigurationAsyncClient#listSettings(SettingSelector)}
     */
    public void listSettingCodeSnippet() {
        ConfigurationAsyncClient client = getAsyncClient();
        // BEGIN: com.azure.data.appconfiguration.configurationasyncclient.listsettings
        client.listSettings(new SettingSelector().setKeys("prodDBConnection"))
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(setting ->
                System.out.printf("Key: %s, Value: %s", setting.getKey(), setting.getValue()));
        // END: com.azure.data.appconfiguration.configurationasyncclient.listsettings
    }

    /**
     * Code snippets for {@link ConfigurationAsyncClient#listSettingRevisions(SettingSelector)}
     */
    public void listSettingRevisionsCodeSnippet() {
        ConfigurationAsyncClient client = getAsyncClient();
        // BEGIN: com.azure.data.appconfiguration.configurationasyncclient.listsettingrevisions
        client.listSettingRevisions(new SettingSelector().setKeys("prodDBConnection"))
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
        return new ConfigurationClientBuilder().credential("connectionString").buildAsyncClient();
    }
}
