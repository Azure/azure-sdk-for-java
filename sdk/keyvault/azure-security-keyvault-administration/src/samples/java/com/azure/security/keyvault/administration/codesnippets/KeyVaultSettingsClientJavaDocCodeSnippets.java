// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.administration.codesnippets;

import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.administration.KeyVaultSettingsClient;
import com.azure.security.keyvault.administration.KeyVaultSettingsClientBuilder;
import com.azure.security.keyvault.administration.models.KeyVaultGetSettingsResult;
import com.azure.security.keyvault.administration.models.KeyVaultSetting;

import java.util.List;

/**
 * This class contains code samples for generating javadocs through doclets for {@link KeyVaultSettingsClient}.
 */
public class KeyVaultSettingsClientJavaDocCodeSnippets {
    /**
     * Generates a code sample for creating a {@link KeyVaultSettingsClient}.
     *
     * @return An instance of {@link KeyVaultSettingsClient}.
     */
    public KeyVaultSettingsClient createClient() {
        // BEGIN: com.azure.security.keyvault.administration.keyVaultSettingsClient.instantiation
        KeyVaultSettingsClient keyVaultSettingsClient = new KeyVaultSettingsClientBuilder()
            .vaultUrl("<your-managed-hsm-url>")
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildClient();
        // END: com.azure.security.keyvault.administration.keyVaultSettingsClient.instantiation

        return keyVaultSettingsClient;
    }

    /**
     * Generates code samples for using {@link KeyVaultSettingsClient#updateSetting(KeyVaultSetting)} and
     * {@link KeyVaultSettingsClient#updateSettingWithResponse(KeyVaultSetting, Context)} .
     */
    public void updateSetting() {
        KeyVaultSettingsClient keyVaultSettingsClient = createClient();
        String settingName = "<setting-to-update>";

        // BEGIN: com.azure.security.keyvault.administration.keyVaultSettingsClient.updateSetting#KeyVaultSetting
        KeyVaultSetting settingToUpdate = new KeyVaultSetting(settingName, true);
        KeyVaultSetting updatedSetting = keyVaultSettingsClient.updateSetting(settingToUpdate);

        System.out.printf("Updated setting '%s' to '%s'.%n", updatedSetting.getName(), updatedSetting.asBoolean());
        // END: com.azure.security.keyvault.administration.keyVaultSettingsClient.updateSetting#KeyVaultSetting

        // BEGIN: com.azure.security.keyvault.administration.keyVaultSettingsClient.updateSettingWithResponse#KeyVaultSetting-Context
        KeyVaultSetting mySettingToUpdate = new KeyVaultSetting(settingName, true);
        Response<KeyVaultSetting> response =
            keyVaultSettingsClient.updateSettingWithResponse(mySettingToUpdate, new Context("key1", "value1"));

        System.out.printf("Response successful with status code: %d. Updated setting '%s' to '%s'.%n",
            response.getStatusCode(), response.getValue().getName(), response.getValue().asBoolean());
        // END: com.azure.security.keyvault.administration.keyVaultSettingsClient.updateSettingWithResponse#KeyVaultSetting-Context
    }

    /**
     * Generates code samples for using {@link KeyVaultSettingsClient#getSetting(String)}
     * and {@link KeyVaultSettingsClient#getSettingWithResponse(String, Context)}.
     */
    public void getSetting() {
        KeyVaultSettingsClient keyVaultSettingsClient = createClient();
        String settingName = "<setting-to-update>";

        // BEGIN: com.azure.security.keyvault.administration.keyVaultSettingsClient.getSetting#String
        KeyVaultSetting setting = keyVaultSettingsClient.getSetting(settingName);

        System.out.printf("Retrieved setting '%s' with value '%s'.%n", setting.getName(), setting.asBoolean());
        // END: com.azure.security.keyvault.administration.keyVaultSettingsClient.getSetting#String

        // BEGIN: com.azure.security.keyvault.administration.keyVaultSettingsClient.getSettingWithResponse#String-Context
        Response<KeyVaultSetting> response =
            keyVaultSettingsClient.getSettingWithResponse(settingName, new Context("key1", "value1"));

        System.out.printf("Response successful with status code: %d. Retrieved setting '%s' with value '%s'.%n",
            response.getStatusCode(), response.getValue().getName(), response.getValue().asBoolean());
        // END: com.azure.security.keyvault.administration.keyVaultSettingsClient.getSettingWithResponse#String-Context
    }

    /**
     * Generates code samples for using {@link KeyVaultSettingsClient#getSettings()} and
     * {@link KeyVaultSettingsClient#getSettingsWithResponse(Context)}.
     */
    public void getSettings() {
        KeyVaultSettingsClient keyVaultSettingsClient = createClient();

        // BEGIN: com.azure.security.keyvault.administration.keyVaultSettingsClient.getSettings
        KeyVaultGetSettingsResult getSettingsResult = keyVaultSettingsClient.getSettings();
        List<KeyVaultSetting> settings = getSettingsResult.getSettings();

        settings.forEach(setting ->
            System.out.printf("Retrieved setting with name '%s' and value %s'.%n", setting.getName(),
                setting.asBoolean()));
        // END: com.azure.security.keyvault.administration.keyVaultSettingsClient.getSettings

        // BEGIN: com.azure.security.keyvault.administration.keyVaultSettingsClient.getSettingsWithResponse#Context
        Response<KeyVaultGetSettingsResult> response =
            keyVaultSettingsClient.getSettingsWithResponse(new Context("key1", "value1"));

        System.out.printf("Response successful with status code: %d.", response.getStatusCode());

        KeyVaultGetSettingsResult myGetSettingsResult = response.getValue();
        List<KeyVaultSetting> mySettings = myGetSettingsResult.getSettings();

        mySettings.forEach(setting ->
            System.out.printf("Retrieved setting with name '%s' and value %s'.%n", setting.getName(),
                setting.asBoolean()));
        // END: com.azure.security.keyvault.administration.keyVaultSettingsClient.getSettingsWithResponse#Context
    }
}
