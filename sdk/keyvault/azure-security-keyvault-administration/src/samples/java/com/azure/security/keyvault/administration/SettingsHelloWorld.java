// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.administration;

import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.administration.models.KeyVaultGetSettingsResult;
import com.azure.security.keyvault.administration.models.KeyVaultSetting;

import java.util.ArrayList;
import java.util.List;

/**
 * This sample demonstrates how to update, get, and lists settings synchronously for a Key Vault account.
 */
public class SettingsHelloWorld {
    /**
     * Authenticates with the key vault and shows how to update, get, and lists settings synchronously for a Key Vault
     * account synchronously.
     *
     * @param args Unused. Arguments to the program.
     * @throws IllegalArgumentException when an invalid key vault URL is passed.
     */
    public static void main(String[] args) {
        /* Instantiate a KeyVaultSettingsClient that will be used to call the service. Notice that the client is
        using default Azure credentials. For more information on this and other types of credentials, see this document:
        https://docs.microsoft.com/java/api/overview/azure/identity-readme?view=azure-java-stable.

        To get started, you'll need a URL to an Azure Key Vault Managed HSM. See the README
        (https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/keyvault/azure-security-keyvault-administration/README.md)
        for links and instructions. */
        KeyVaultSettingsClient keyVaultSettingsClient = new KeyVaultSettingsClientBuilder()
            .vaultUrl("<your-managed-hsm-url>")
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildClient();

        /* In order to update a setting, we'll have to know which ones are available for the account. Let's get all of
        them. */
        List<KeyVaultSetting> settings = new ArrayList<>();
        KeyVaultGetSettingsResult getSettingsResult = keyVaultSettingsClient.getSettings();

        for (KeyVaultSetting setting : getSettingsResult.getSettings()) {
            settings.add(setting);

            System.out.printf("Retrieved setting with name '%s' and value '%s'.%n", setting.getName(),
                setting.asBoolean());
        }

        /* If you want to get only a specific setting and its value instead, you can do the following. */
        String settingName = settings.get(0).getName();
        KeyVaultSetting retrievedSetting = keyVaultSettingsClient.getSetting(settingName);

        System.out.printf("Retrieved setting with name '%s' and value '%s'.%n", retrievedSetting.getName(),
            retrievedSetting.asBoolean());

        /* Now let's update the settings to hold a new value. Currently, only boolean values are supported. */
        KeyVaultSetting settingToUpdate = new KeyVaultSetting(settingName, true);
        KeyVaultSetting updatedSetting = keyVaultSettingsClient.updateSetting(settingToUpdate);

        System.out.printf("Updated setting with name '%s' to '%s'.%n", updatedSetting.getName(),
            updatedSetting.asBoolean());
    }
}
