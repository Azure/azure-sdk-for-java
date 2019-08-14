// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.appconfiguration;

import com.azure.data.appconfiguration.models.ConfigurationSetting;
import com.azure.data.appconfiguration.models.SettingSelector;
import reactor.util.context.Context;

/**
 * Code snippets for {@link ConfigurationAsyncClient}
 */
public class ConfigurationAsyncClientJavaDocCodeSnippets {

    private String key1 = "key1";
    private String key2 = "key2";
    private String value1 = "val1";
    private String value2 = "val2";

    /**
     * Code snippets for {@link ConfigurationAsyncClient#addSetting(String, String)}
     */
    public void addSettingsCodeSnippet() {
        ConfigurationAsyncClient client = getAsyncClient();
        // BEGIN: com.azure.data.appconfiguration.configurationasyncclient.addSetting#string-string
        client.addSetting("prodDBConnection", "db_connection")
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(response -> {
                System.out.printf("Key: %s, Value: %s", response.key(), response.value());
            });
        // END: com.azure.data.appconfiguration.configurationasyncclient.addSetting#string-string

        /**
         * Code snippets for {@link ConfigurationAsyncClient#addSettins(ConfigurationSetting)}
         */
        // BEGIN: com.azure.data.appconfiguration.configurationasyncclient.addSetting#ConfigurationSetting
        client.addSetting(new ConfigurationSetting().key("prodDBConnection").label("westUS").value("db_connection"))
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(response -> {
                System.out.printf("Key: %s, Value: %s", response.key(), response.value());
            });
        // END: com.azure.data.appconfiguration.configurationasyncclient.addSetting#ConfigurationSetting

        /**
         * Code snippets for {@link ConfigurationAsyncClient#addSettingWithResponse(String, String)}
         */
        // BEGIN: com.azure.data.appconfiguration.configurationasyncclient.addSettingWithResponse#ConfigurationSetting
        client.addSettingWithResponse(new ConfigurationSetting().key("prodDBConnection").label("westUS")
            .value("db_connection"))
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(response -> {
                ConfigurationSetting responseSetting = response.value();
                System.out.printf("Key: %s, Value: %s", responseSetting.key(), responseSetting.value());
            });
        // END: com.azure.data.appconfiguration.configurationasyncclient.addSettingWithResponse#ConfigurationSetting
    }

    /**
     * Code snippets for {@link ConfigurationAsyncClient#setSetting(String, String)}
     */
    public void setSettingsCodeSnippet() {
        ConfigurationAsyncClient client = getAsyncClient();
        // BEGIN: com.azure.data.appconfiguration.configurationasyncclient.setSetting#string-string
        client.setSetting("prodDBConnection", "db_connection")
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(response -> {
                System.out.printf("Key: %s, Value: %s", response.key(), response.value());
            });
        // Update the value of the setting to "updated_db_connection"
        client.setSetting("prodDBConnection", "updated_db_connection")
             .subscribe(response -> {
                 System.out.printf("Key: %s, Value: %s", response.key(), response.value());
             });
        // END: com.azure.data.appconfiguration.configurationasyncclient.setSetting#string-string

        /**
         * Code snippets for {@link ConfigurationAsyncClient#setSetting(ConfigurationSetting)}
         */
        // BEGIN: com.azure.data.appconfiguration.configurationasyncclient.setSetting#ConfigurationSetting
        client.setSetting(new ConfigurationSetting().key("prodDBConnection").label("westUS").value("db_connection"))
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(response -> {
                System.out.printf("Key: %s, Value: %s", response.key(), response.value());
            });
        // Update the value of the setting to "updated_db_connection"
        client.setSetting(new ConfigurationSetting().key("prodDBConnection").label("westUS")
            .value("updated_db_connection"))
            .subscribe(response -> {
                System.out.printf("Key: %s, Value: %s", response.key(), response.value());
            });
        // END: com.azure.data.appconfiguration.configurationasyncclient.setSetting#ConfigurationSetting

        /**
         * Code snippets for {@link ConfigurationAsyncClient#setSettingWithResponse(ConfigurationSetting)}
         */
        // BEGIN: com.azure.data.appconfiguration.configurationasyncclient.setSettingWithResponse#ConfigurationSetting
        client.setSettingWithResponse(new ConfigurationSetting().key("prodDBConnection").label("westUS")
            .value("db_connection"))
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(response -> {
                ConfigurationSetting result = response.value();
                System.out.printf("Key: %s, Value: %s", result.key(), result.value());
            });
        // Update the value of the setting to "updated_db_connection"
        client.setSettingWithResponse(new ConfigurationSetting().key("prodDBConnection").label("westUS")
            .value("updated_db_connection"))
            .subscribe(response -> {
                ConfigurationSetting responseSetting = response.value();
                System.out.printf("Key: %s, Value: %s", responseSetting.key(), responseSetting.value());
            });
        // END: com.azure.data.appconfiguration.configurationasyncclient.setSettingWithResponse#ConfigurationSetting
    }

    /**
     * Code snippets for {@link ConfigurationAsyncClient#updateSetting(String, String)}
     */
    public void updateSettingsCodeSnippet() {
        ConfigurationAsyncClient client = getAsyncClient();
        // BEGIN: com.azure.data.appconfiguration.configurationasyncclient.updateSetting#string-string
        client.updateSetting("prodDBConnection", "updated_db_connection")
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(response -> {
                System.out.printf("Key: %s, Value: %s", response.key(), response.value());
            });
        // END: com.azure.data.appconfiguration.configurationasyncclient.updateSetting#string-string

        /**
         * Code snippets for {@link ConfigurationAsyncClient#updateSetting(ConfigurationSetting)}
         */
        // BEGIN: com.azure.data.appconfiguration.configurationasyncclient.updateSetting#ConfigurationSetting
        client.updateSetting(new ConfigurationSetting().key("prodDBConnection").label("westUS")
            .value("updated_db_connection"))
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(response -> {
                System.out.printf("Key: %s, Value: %s", response.key(), response.value());
            });
        // END: com.azure.data.appconfiguration.configurationasyncclient.updateSetting#ConfigurationSetting

        /**
         * Code snippets for {@link ConfigurationAsyncClient#updateSettingWithResponse(ConfigurationSetting)}
         */
        // BEGIN: com.azure.data.appconfiguration.configurationasyncclient.updateSettingWithResponse#ConfigurationSetting
        client.updateSettingWithResponse(new ConfigurationSetting().key("prodDBConnection").label("westUS")
            .value("updated_db_connection"))
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(response -> {
                ConfigurationSetting responseSetting = response.value();
                System.out.printf("Key: %s, Value: %s", responseSetting.key(), responseSetting.value());
            });
        // END: com.azure.data.appconfiguration.configurationasyncclient.updateSettingWithResponse#ConfigurationSetting
    }

    /**
     * Code snippets for {@link ConfigurationAsyncClient#getSetting(String)}
     */
    public void getSettingsCodeSnippet() {
        ConfigurationAsyncClient client = getAsyncClient();
        // BEGIN: com.azure.data.appconfiguration.configurationasyncclient.getSetting#string
        client.getSetting("prodDBConnection")
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(response -> {
                System.out.printf("Key: %s, Value: %s", response.key(), response.value());
            });
        // END: com.azure.data.appconfiguration.configurationasyncclient.getSetting#string

        /**
         * Code snippets for {@link ConfigurationAsyncClient#getSetting(ConfigurationSetting)}
         */
        // BEGIN: com.azure.data.appconfiguration.configurationasyncclient.getSetting#ConfigurationSetting
        client.getSetting(new ConfigurationSetting().key("prodDBConnection").label("westUS"))
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(response -> {
                System.out.printf("Key: %s, Value: %s", response.key(), response.value());
            });
        // END: com.azure.data.appconfiguration.configurationasyncclient.getSetting#ConfigurationSetting

        /**
         * Code snippets for {@link ConfigurationAsyncClient#getSettingWithResponse(String, String)}
         */
        // BEGIN: com.azure.data.appconfiguration.configurationasyncclient.getSettingWithResponse#ConfigurationSetting
        client.getSettingWithResponse(new ConfigurationSetting().key("prodDBConnection").label("westUS"))
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(response -> {
                ConfigurationSetting result = response.value();
                System.out.printf("Key: %s, Value: %s", result.key(), result.value());
            });
        // END: com.azure.data.appconfiguration.configurationasyncclient.getSettingWithResponse#ConfigurationSetting
    }

   /**
     * Code snippets for {@link ConfigurationAsyncClient#deleteSetting(String)}
     */
    public void deleteSettingsCodeSnippet() {
        ConfigurationAsyncClient client = getAsyncClient();
        // BEGIN: com.azure.data.appconfiguration.configurationasyncclient.deleteSetting#string
        client.deleteSetting("prodDBConnection")
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(response -> {
                System.out.printf("Key: %s, Value: %s", response.key(), response.value());
            });
        // END: com.azure.data.appconfiguration.configurationasyncclient.deleteSetting#string

        /**
         * Code snippets for {@link ConfigurationAsyncClient#deleteSetting(ConfigurationSetting)}
         */
        // BEGIN: com.azure.data.appconfiguration.configurationasyncclient.deleteSetting#ConfigurationSetting
        client.deleteSetting(new ConfigurationSetting().key("prodDBConnection").label("westUS"))
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(response -> {
                System.out.printf("Key: %s, Value: %s", response.key(), response.value());
            });
        // END: com.azure.data.appconfiguration.configurationasyncclient.deleteSetting#ConfigurationSetting

        /**
         * Code snippets for {@link ConfigurationAsyncClient#deleteSettingWithResponse(String, String)}
         */
        // BEGIN: com.azure.data.appconfiguration.configurationasyncclient.deleteSettingWithResponse#ConfigurationSetting
        client.deleteSettingWithResponse(new ConfigurationSetting().key("prodDBConnection").label("westUS"))
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(response -> {
                ConfigurationSetting result = response.value();
                System.out.printf("Key: %s, Value: %s", result.key(), result.value());
            });
        // END: com.azure.data.appconfiguration.configurationasyncclient.deleteSettingWithResponse#ConfigurationSetting
    }

    /**
     * Code snippets for {@link ConfigurationAsyncClient#listSettings(SettingSelector)}
     */
    public void listSettingCodeSnippet() {
        ConfigurationAsyncClient client = getAsyncClient();
        // BEGIN: com.azure.data.appconfiguration.configurationasyncclient.listsettings
        client.listSettings(new SettingSelector().keys("prodDBConnection"))
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(setting ->
                System.out.printf("Key: %s, Value: %s", setting.key(), setting.value()));
        // END: com.azure.data.appconfiguration.configurationasyncclient.listsettings
    }

    /**
     * Code snippets for {@link ConfigurationAsyncClient#listSettingRevisions(SettingSelector)}
     */
    public void listSettingRevisionsCodeSnippet() {
        ConfigurationAsyncClient client = getAsyncClient();
        // BEGIN: com.azure.data.appconfiguration.configurationasyncclient.listsettingrevisions
        client.listSettingRevisions(new SettingSelector().keys("prodDBConnection"))
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(setting ->
                System.out.printf("Key: %s, Value: %s", setting.key(), setting.value()));
        // END: com.azure.data.appconfiguration.configurationasyncclient.listsettingrevisions
    }

    /**
     * Implementation not provided
     * @return {@code null}
     */
    private ConfigurationAsyncClient getAsyncClient() {
        return new ConfigurationClientBuilder().buildAsyncClient();
    }
}
