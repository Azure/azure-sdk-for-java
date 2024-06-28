// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.keyvault.administration;

import com.azure.core.http.HttpClient;
import com.azure.core.test.http.AssertingHttpClientBuilder;
import com.azure.security.keyvault.administration.models.KeyVaultGetSettingsResult;
import com.azure.security.keyvault.administration.models.KeyVaultSetting;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class KeyVaultSettingsClientTest extends KeyVaultSettingsClientTestBase {
    private KeyVaultSettingsClient client;

    private HttpClient buildSyncAssertingClient(HttpClient httpClient) {
        return new AssertingHttpClientBuilder(httpClient)
            .assertSync()
            .build();
    }

    private void getClient(HttpClient httpClient, boolean forCleanup) {
        client = getClientBuilder(buildSyncAssertingClient(
            interceptorManager.isPlaybackMode() ? interceptorManager.getPlaybackClient() : httpClient), forCleanup)
            .buildClient();
    }

    @ParameterizedTest(name = DISPLAY_NAME)
    @MethodSource("com.azure.security.keyvault.administration.KeyVaultAdministrationClientTestBase#createHttpClients")
    public void getSettings(HttpClient httpClient) {
        getClient(httpClient, false);

        KeyVaultGetSettingsResult getSettingsResult = client.getSettings();

        assertNotNull(getSettingsResult);
        assertTrue(getSettingsResult.getSettings().size() > 0);

        for (KeyVaultSetting setting : getSettingsResult.getSettings()) {
            assertNotNull(setting);
            assertNotNull(setting.getName());
            assertNotNull(setting.getType());
        }
    }

    @ParameterizedTest(name = DISPLAY_NAME)
    @MethodSource("com.azure.security.keyvault.administration.KeyVaultAdministrationClientTestBase#createHttpClients")
    public void getSetting(HttpClient httpClient) {
        getClient(httpClient, false);

        String settingName = "AllowKeyManagementOperationsThroughARM";
        KeyVaultSetting setting = client.getSetting(settingName);

        assertNotNull(setting);
        assertNotNull(setting.getName());
        assertNotNull(setting.getType());
    }

    @ParameterizedTest(name = DISPLAY_NAME)
    @MethodSource("com.azure.security.keyvault.administration.KeyVaultAdministrationClientTestBase#createHttpClients")
    public void updateSetting(HttpClient httpClient) {
        getClient(httpClient, false);

        String settingName = "AllowKeyManagementOperationsThroughARM";
        KeyVaultSetting setting = client.getSetting(settingName);

        assertNotNull(setting);

        @SuppressWarnings("ConstantConditions")
        boolean originalSettingValue = setting.asBoolean();

        KeyVaultSetting settingToUpdate = new KeyVaultSetting(settingName, true);
        KeyVaultSetting updatedSetting = client.updateSetting(settingToUpdate);

        assertSettingEquals(settingToUpdate, updatedSetting);

        // Cleanup
        client.updateSetting(new KeyVaultSetting(settingName, originalSettingValue));
    }
}
