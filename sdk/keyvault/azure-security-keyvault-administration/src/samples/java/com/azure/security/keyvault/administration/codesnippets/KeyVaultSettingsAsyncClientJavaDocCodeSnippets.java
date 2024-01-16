// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.administration.codesnippets;

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
        // BEGIN: com.azure.security.keyvault.administration.keyVaultSettingsAsyncClient.instantiation
        KeyVaultSettingsAsyncClient keyVaultSettingsAsyncClient = new KeyVaultSettingsClientBuilder()
            .vaultUrl("<your-managed-hsm-url>")
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildAsyncClient();
        // END: com.azure.security.keyvault.administration.keyVaultSettingsAsyncClient.instantiation

        return keyVaultSettingsAsyncClient;
    }

    /**
     * Generates code samples for using {@link KeyVaultSettingsAsyncClient#updateSetting(KeyVaultSetting)} and
     * {@link KeyVaultSettingsAsyncClient#updateSettingWithResponse(KeyVaultSetting)} .
     */
    public void updateSetting() {
        KeyVaultSettingsAsyncClient keyVaultSettingsAsyncClient = createClient();
        String settingName = "<setting-to-update>";

        // BEGIN: com.azure.security.keyvault.administration.keyVaultSettingsAsyncClient.updateSetting#KeyVaultSetting
        KeyVaultSetting settingToUpdate = new KeyVaultSetting(settingName, true);

        keyVaultSettingsAsyncClient.updateSetting(settingToUpdate)
            .subscribe(updatedSetting ->
                System.out.printf("Updated setting '%s' to '%s'.%n", updatedSetting.getName(),
                    updatedSetting.asBoolean()));
        // END: com.azure.security.keyvault.administration.keyVaultSettingsAsyncClient.updateSetting#KeyVaultSetting

        // BEGIN: com.azure.security.keyvault.administration.keyVaultSettingsAsyncClient.updateSettingWithResponse#KeyVaultSetting
        KeyVaultSetting mySettingToUpdate = new KeyVaultSetting(settingName, true);

        keyVaultSettingsAsyncClient.updateSettingWithResponse(mySettingToUpdate)
            .subscribe(response ->
                System.out.printf("Response successful with status code: %d. Updated setting '%s' to '%s'.%n",
                    response.getStatusCode(), response.getValue().getName(), response.getValue().asBoolean()));
        // END: com.azure.security.keyvault.administration.keyVaultSettingsAsyncClient.updateSettingWithResponse#KeyVaultSetting
    }

    /**
     * Generates code samples for using {@link KeyVaultSettingsAsyncClient#getSetting(String)}
     * and {@link KeyVaultSettingsAsyncClient#getSettingWithResponse(String)}.
     */
    public void getSetting() {
        KeyVaultSettingsAsyncClient keyVaultSettingsAsyncClient = createClient();
        String settingName = "<setting-to-update>";

        // BEGIN: com.azure.security.keyvault.administration.keyVaultSettingsAsyncClient.getSetting#String
        keyVaultSettingsAsyncClient.getSetting(settingName)
            .subscribe(setting ->
                System.out.printf("Retrieved setting '%s' with value '%s'.%n", setting.getName(), setting.asBoolean()));
        // END: com.azure.security.keyvault.administration.keyVaultSettingsAsyncClient.getSetting#String

        // BEGIN: com.azure.security.keyvault.administration.keyVaultSettingsAsyncClient.getSettingWithResponse#String
        keyVaultSettingsAsyncClient.getSettingWithResponse(settingName)
            .subscribe(response ->
                System.out.printf("Response successful with status code: %d. Retrieved setting '%s' with value '%s'.%n",
                    response.getStatusCode(), response.getValue().getName(), response.getValue().asBoolean()));
        // END: com.azure.security.keyvault.administration.keyVaultSettingsAsyncClient.getSettingWithResponse#String
    }

    /**
     * Generates code samples for using {@link KeyVaultSettingsAsyncClient#getSettings()} and
     * {@link KeyVaultSettingsAsyncClient#getSettingsWithResponse()}.
     */
    public void getSettings() {
        KeyVaultSettingsAsyncClient keyVaultSettingsAsyncClient = createClient();

        // BEGIN: com.azure.security.keyvault.administration.keyVaultSettingsAsyncClient.getSettings
        keyVaultSettingsAsyncClient.getSettings().subscribe(getSettingsResult ->
            getSettingsResult.getSettings().forEach(setting ->
                System.out.printf("Retrieved setting with name '%s' and value %s'.%n", setting.getName(),
                    setting.asBoolean())));
        // END: com.azure.security.keyvault.administration.keyVaultSettingsAsyncClient.getSettings

        // BEGIN: com.azure.security.keyvault.administration.keyVaultSettingsAsyncClient.getSettingsWithResponse
        keyVaultSettingsAsyncClient.getSettingsWithResponse()
            .subscribe(response -> {
                System.out.printf("Response successful with status code: %d.", response.getStatusCode());

                KeyVaultGetSettingsResult getSettingsResult = response.getValue();
                List<KeyVaultSetting> settings = getSettingsResult.getSettings();

                settings.forEach(setting ->
                    System.out.printf("Retrieved setting with name '%s' and value %s'.%n", setting.getName(),
                        setting.asBoolean()));
            });
        // END: com.azure.security.keyvault.administration.keyVaultSettingsAsyncClient.getSettingsWithResponse
    }
}
