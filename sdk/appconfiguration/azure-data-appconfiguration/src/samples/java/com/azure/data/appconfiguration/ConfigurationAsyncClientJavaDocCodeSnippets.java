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

    /**
     * Code snippets for {@link ConfigurationAsyncClient#addSetting(String, String)}
     */
    public void addSettingsCodeSnippet() {
        String key1 = null;
        String key2 = null;
        String value1 = null;
        String value2 = null;

        ConfigurationAsyncClient client = getAsyncClient();
        // BEGIN: com.azure.data.appconfiguration.configurationasyncclient.addsetting#String-String
        client.addSetting("prodDBConnection", "db_connection")
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(response -> {
                ConfigurationSetting result = response.value();
                System.out.printf("Key: %s, Value: %s", result.key(), result.value());
            });
        // END: com.azure.data.appconfiguration.configurationasyncclient.addsetting#String-String
    }

    /**
     * Code snippets for {@link ConfigurationAsyncClient#listSettingRevisions(SettingSelector)}
     */
    public void listSettingRevisionsCodeSnippet() {
        String key1 = null;
        String key2 = null;
        String value1 = null;
        String value2 = null;

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
        return null;
    }
}
