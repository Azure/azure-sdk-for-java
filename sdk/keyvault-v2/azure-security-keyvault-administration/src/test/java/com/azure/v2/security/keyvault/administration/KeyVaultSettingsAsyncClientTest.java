// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.v2.security.keyvault.administration;

import io.clientcore.core.http.client.HttpClient;
import com.azure.v2.security.keyvault.administration.models.KeyVaultSetting;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class KeyVaultSettingsAsyncClientTest extends KeyVaultSettingsClientTestBase {
    private KeyVaultSettingsAsyncClient asyncClient;

    private void getClient(HttpClient httpClient, boolean forCleanup) {
        asyncClient = getClientBuilder(httpClient, forCleanup).buildAsyncClient();

        if (!interceptorManager.isLiveMode()) {
            // Remove `id` and `name` sanitizers from the list of common sanitizers.
            interceptorManager.removeSanitizers("AZSDK3430", "AZSDK3493");
        }
    }

    @ParameterizedTest(name = DISPLAY_NAME)
    @MethodSource("com.azure.v2.security.keyvault.administration.KeyVaultAdministrationClientTestBase#createHttpClients")
    public void getSettings(HttpClient httpClient) {
        getClient(httpClient, false);

        CompletableFuture<Void> testFuture = asyncClient.getSettings().thenAccept(getSettingsResult -> {
            assertNotNull(getSettingsResult);
            assertTrue(getSettingsResult.getSettings().size() > 0);

            for (KeyVaultSetting setting : getSettingsResult.getSettings()) {
                assertNotNull(setting);
                assertNotNull(setting.getName());
                assertNotNull(setting.getType());
            }
        });

        testFuture.join();
    }

    @ParameterizedTest(name = DISPLAY_NAME)
    @MethodSource("com.azure.v2.security.keyvault.administration.KeyVaultAdministrationClientTestBase#createHttpClients")
    public void getSetting(HttpClient httpClient) {
        getClient(httpClient, false);

        String settingName = "AllowKeyManagementOperationsThroughARM";

        CompletableFuture<Void> testFuture = asyncClient.getSetting(settingName).thenAccept(setting -> {
            assertNotNull(setting);
            assertNotNull(setting.getName());
            assertNotNull(setting.getType());
        });

        testFuture.join();
    }

    @ParameterizedTest(name = DISPLAY_NAME)
    @MethodSource("com.azure.v2.security.keyvault.administration.KeyVaultAdministrationClientTestBase#createHttpClients")
    public void updateSetting(HttpClient httpClient) {
        getClient(httpClient, false);

        String settingName = "AllowKeyManagementOperationsThroughARM";
        KeyVaultSetting settingToUpdate = new KeyVaultSetting(settingName, true);

        CompletableFuture<Void> testFuture = asyncClient.getSetting(settingName).thenCompose(setting -> {
            assertNotNull(setting);

            @SuppressWarnings("ConstantConditions")
            boolean originalSettingValue = setting.asBoolean();

            return asyncClient.updateSetting(settingToUpdate)
                .thenAccept(updatedSetting -> assertSettingEquals(settingToUpdate, updatedSetting))
                .thenCompose(v -> asyncClient.updateSetting(new KeyVaultSetting(settingName, originalSettingValue)))
                .thenAccept(Assertions::assertNotNull);
        });

        testFuture.join();
    }
}
