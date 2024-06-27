// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.keyvault.administration;

import com.azure.core.http.HttpClient;
import com.azure.core.test.http.AssertingHttpClientBuilder;
import com.azure.security.keyvault.administration.models.KeyVaultSetting;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class KeyVaultSettingsAsyncClientTest extends KeyVaultSettingsClientTestBase {
    private KeyVaultSettingsAsyncClient asyncClient;

    private HttpClient buildAsyncAssertingClient(HttpClient httpClient) {
        return new AssertingHttpClientBuilder(httpClient)
            .assertAsync()
            .build();
    }

    private void getClient(HttpClient httpClient, boolean forCleanup) {
        asyncClient = getClientBuilder(buildAsyncAssertingClient(
            interceptorManager.isPlaybackMode() ? interceptorManager.getPlaybackClient() : httpClient), forCleanup)
            .buildAsyncClient();
    }

    @ParameterizedTest(name = DISPLAY_NAME)
    @MethodSource("com.azure.security.keyvault.administration.KeyVaultAdministrationClientTestBase#createHttpClients")
    public void getSettings(HttpClient httpClient) {
        getClient(httpClient, false);

        StepVerifier.create(asyncClient.getSettings())
            .assertNext(getSettingsResult -> {
                assertNotNull(getSettingsResult);
                assertTrue(getSettingsResult.getSettings().size() > 0);

                for (KeyVaultSetting setting : getSettingsResult.getSettings()) {
                    assertNotNull(setting);
                    assertNotNull(setting.getName());
                    assertNotNull(setting.getType());
                }
            }).verifyComplete();
    }

    @ParameterizedTest(name = DISPLAY_NAME)
    @MethodSource("com.azure.security.keyvault.administration.KeyVaultAdministrationClientTestBase#createHttpClients")
    public void getSetting(HttpClient httpClient) {
        getClient(httpClient, false);

        String settingName = "AllowKeyManagementOperationsThroughARM";

        StepVerifier.create(asyncClient.getSetting(settingName))
            .assertNext(setting -> {
                assertNotNull(setting);
                assertNotNull(setting.getName());
                assertNotNull(setting.getType());
            }).verifyComplete();
    }

    @ParameterizedTest(name = DISPLAY_NAME)
    @MethodSource("com.azure.security.keyvault.administration.KeyVaultAdministrationClientTestBase#createHttpClients")
    public void updateSetting(HttpClient httpClient) {
        getClient(httpClient, false);

        String settingName = "AllowKeyManagementOperationsThroughARM";
        KeyVaultSetting settingToUpdate = new KeyVaultSetting(settingName, true);

        StepVerifier.create(asyncClient.getSetting(settingName)
                .flatMap(setting -> {
                    assertNotNull(setting);

                    @SuppressWarnings("ConstantConditions")
                    boolean originalSettingValue = setting.asBoolean();

                    return asyncClient.updateSetting(settingToUpdate)
                        .doOnSuccess(updatedSetting -> assertSettingEquals(settingToUpdate, updatedSetting))
                        .then(asyncClient.updateSetting(new KeyVaultSetting(settingName, originalSettingValue)));
                }))
            .assertNext(Assertions::assertNotNull)
            .verifyComplete();
    }
}
