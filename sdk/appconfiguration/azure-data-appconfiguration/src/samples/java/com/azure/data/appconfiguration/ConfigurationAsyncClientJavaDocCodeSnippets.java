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
        // BEGIN: com.azure.data.appconfiguration.configurationasyncclient.addsettingWithResponse#String-String
        client.addSettingWithResponse("prodDBConnection", "db_connection")
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(response -> {
                ConfigurationSetting result = response.value();
                System.out.printf("Key: %s, Value: %s", result.key(), result.value());
            });
        // END: com.azure.data.appconfiguration.configurationasyncclient.addsettingWithResponse#String-String
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
