// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.v2.security.keyvault.administration;

import com.azure.v2.security.keyvault.administration.models.KeyVaultGetSettingsResult;
import com.azure.v2.security.keyvault.administration.models.KeyVaultSetting;
import io.clientcore.core.http.client.HttpClient;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class KeyVaultSettingsClientTest extends KeyVaultSettingsClientTestBase {
    private KeyVaultSettingsClient client;

    private void getClient(HttpClient httpClient) throws IOException {
        client = getClientBuilder(buildSyncAssertingClient(
            interceptorManager.isPlaybackMode() ? interceptorManager.getPlaybackClient() : httpClient)).buildClient();

        if (!interceptorManager.isLiveMode()) {
            // Remove `id` and `name` sanitizers from the list of common sanitizers.
            interceptorManager.removeSanitizers("AZSDK3430", "AZSDK3493");
        }
    }

    private HttpClient buildSyncAssertingClient(HttpClient httpClient) {
        return new TestUtils.AssertingHttpClientBuilder(httpClient).assertSync().build();
    }

    @ParameterizedTest(name = DISPLAY_NAME)
    @MethodSource("com.azure.v2.security.keyvault.administration.KeyVaultAdministrationClientTestBase#createHttpClients")
    public void getSettings(HttpClient httpClient) throws IOException {
        getClient(httpClient);

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
    @MethodSource("com.azure.v2.security.keyvault.administration.KeyVaultAdministrationClientTestBase#createHttpClients")
    public void getSetting(HttpClient httpClient) throws IOException {
        getClient(httpClient);

        String settingName = "AllowKeyManagementOperationsThroughARM";
        KeyVaultSetting setting = client.getSetting(settingName);

        assertNotNull(setting);
        assertNotNull(setting.getName());
        assertNotNull(setting.getType());
    }

    @ParameterizedTest(name = DISPLAY_NAME)
    @MethodSource("com.azure.v2.security.keyvault.administration.KeyVaultAdministrationClientTestBase#createHttpClients")
    public void updateSetting(HttpClient httpClient) throws IOException {
        getClient(httpClient);

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
