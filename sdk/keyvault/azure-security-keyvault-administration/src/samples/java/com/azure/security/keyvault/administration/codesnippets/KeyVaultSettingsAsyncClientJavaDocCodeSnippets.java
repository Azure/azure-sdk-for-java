// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.administration.codesnippets;

import com.azure.core.http.HttpClient;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.administration.KeyVaultSettingsAsyncClient;
import com.azure.security.keyvault.administration.KeyVaultSettingsClientBuilder;
import com.azure.security.keyvault.administration.models.KeyVaultGetSettingsResult;
import com.azure.security.keyvault.administration.models.KeyVaultSetting;

import java.util.List;

/**
 * This class contains code samples for generating javadocs through doclets for {@link KeyVaultSettingsAsyncClient}.
 */
public class KeyVaultSettingsAsyncClientJavaDocCodeSnippets {
    /**
     * Generates a code sample for creating a {@link KeyVaultSettingsAsyncClient}.
     *
     * @return An instance of {@link KeyVaultSettingsAsyncClient}.
     */
    public KeyVaultSettingsAsyncClient createClient() {
        // BEGIN: com.azure.security.keyvault.administration.KeyVaultSettingsAsyncClient.instantiation
        KeyVaultSettingsAsyncClient keyVaultSettingsAsyncClient = new KeyVaultSettingsClientBuilder()
            .vaultUrl("<your-managed-hsm-url>")
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildAsyncClient();
        // END: com.azure.security.keyvault.administration.KeyVaultSettingsAsyncClient.instantiation

        return keyVaultSettingsAsyncClient;
    }

    /**
     * Generates code sample for creating a {@link KeyVaultSettingsAsyncClient} using a custom {@link HttpClient}.
     *
     * @return An instance of {@link KeyVaultSettingsAsyncClient}.
     */
    public KeyVaultSettingsAsyncClient createAsyncClientWithHttpClient() {
        // BEGIN: com.azure.security.keyvault.administration.KeyVaultSettingsAsyncClient.instantiation.withHttpClient
        KeyVaultSettingsAsyncClient keyVaultSettingsAsyncClient = new KeyVaultSettingsClientBuilder()
            .vaultUrl("<your-key-vault-url>")
            .credential(new DefaultAzureCredentialBuilder().build())
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
            .httpClient(HttpClient.createDefault())
            .buildAsyncClient();
        // END: com.azure.security.keyvault.administration.KeyVaultSettingsAsyncClient.instantiation.withHttpClient
        return keyVaultSettingsAsyncClient;
    }

    /**
     * Generates code samples for using {@link KeyVaultSettingsAsyncClient#updateSetting(KeyVaultSetting)} and
     * {@link KeyVaultSettingsAsyncClient#updateSettingWithResponse(KeyVaultSetting)} .
     */
    public void updateSetting() {
        KeyVaultSettingsAsyncClient keyVaultSettingsAsyncClient = createClient();
        String settingName = "<setting-to-update>";

        // BEGIN: com.azure.security.keyvault.administration.KeyVaultSettingsAsyncClient.updateSetting#KeyVaultSetting
        KeyVaultSetting settingToUpdate = new KeyVaultSetting(settingName, true);

        keyVaultSettingsAsyncClient.updateSetting(settingToUpdate)
            .subscribe(updatedSetting ->
                System.out.printf("Updated setting '%s' to '%s'.%n", updatedSetting.getName(),
                    updatedSetting.asBoolean()));
        // END: com.azure.security.keyvault.administration.KeyVaultSettingsAsyncClient.updateSetting#KeyVaultSetting

        // BEGIN: com.azure.security.keyvault.administration.KeyVaultSettingsAsyncClient.updateSettingWithResponse#KeyVaultSetting
        KeyVaultSetting mySettingToUpdate = new KeyVaultSetting(settingName, true);

        keyVaultSettingsAsyncClient.updateSettingWithResponse(mySettingToUpdate)
            .subscribe(response ->
                System.out.printf("Response successful with status code: %d. Updated setting '%s' to '%s'.%n",
                    response.getStatusCode(), response.getValue().getName(), response.getValue().asBoolean()));
        // END: com.azure.security.keyvault.administration.KeyVaultSettingsAsyncClient.updateSettingWithResponse#KeyVaultSetting
    }

    /**
     * Generates code samples for using {@link KeyVaultSettingsAsyncClient#getSetting(String)}
     * and {@link KeyVaultSettingsAsyncClient#getSettingWithResponse(String)}.
     */
    public void getSetting() {
        KeyVaultSettingsAsyncClient keyVaultSettingsAsyncClient = createClient();
        String settingName = "<setting-to-update>";

        // BEGIN: com.azure.security.keyvault.administration.KeyVaultSettingsAsyncClient.getSetting#String
        keyVaultSettingsAsyncClient.getSetting(settingName)
            .subscribe(setting ->
                System.out.printf("Retrieved setting '%s' with value '%s'.%n", setting.getName(), setting.asBoolean()));
        // END: com.azure.security.keyvault.administration.KeyVaultSettingsAsyncClient.getSetting#String

        // BEGIN: com.azure.security.keyvault.administration.KeyVaultSettingsAsyncClient.getSettingWithResponse#String
        keyVaultSettingsAsyncClient.getSettingWithResponse(settingName)
            .subscribe(response ->
                System.out.printf("Response successful with status code: %d. Retrieved setting '%s' with value '%s'.%n",
                    response.getStatusCode(), response.getValue().getName(), response.getValue().asBoolean()));
        // END: com.azure.security.keyvault.administration.KeyVaultSettingsAsyncClient.getSettingWithResponse#String
    }

    /**
     * Generates code samples for using {@link KeyVaultSettingsAsyncClient#getSettings()} and
     * {@link KeyVaultSettingsAsyncClient#getSettingsWithResponse()}.
     */
    public void getSettings() {
        KeyVaultSettingsAsyncClient keyVaultSettingsAsyncClient = createClient();

        // BEGIN: com.azure.security.keyvault.administration.KeyVaultSettingsAsyncClient.getSettings
        keyVaultSettingsAsyncClient.getSettings().subscribe(getSettingsResult ->
            getSettingsResult.getSettings().forEach(setting ->
                System.out.printf("Retrieved setting with name '%s' and value %s'.%n", setting.getName(),
                    setting.asBoolean())));
        // END: com.azure.security.keyvault.administration.KeyVaultSettingsAsyncClient.getSettings

        // BEGIN: com.azure.security.keyvault.administration.KeyVaultSettingsAsyncClient.getSettingsWithResponse
        keyVaultSettingsAsyncClient.getSettingsWithResponse()
            .subscribe(response -> {
                System.out.printf("Response successful with status code: %d.", response.getStatusCode());

                KeyVaultGetSettingsResult getSettingsResult = response.getValue();
                List<KeyVaultSetting> settings = getSettingsResult.getSettings();

                settings.forEach(setting ->
                    System.out.printf("Retrieved setting with name '%s' and value %s'.%n", setting.getName(),
                        setting.asBoolean()));
            });
        // END: com.azure.security.keyvault.administration.KeyVaultSettingsAsyncClient.getSettingsWithResponse
    }
}
