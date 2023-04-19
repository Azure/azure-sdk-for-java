// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.keyvault.administration;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.test.TestMode;
import com.azure.core.util.logging.ClientLogger;
import com.azure.security.keyvault.administration.models.KeyVaultSetting;
import com.azure.security.keyvault.administration.models.KeyVaultSettingType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public abstract class KeyVaultSettingsClientTestBase extends KeyVaultAdministrationClientTestBase {
    private static final ClientLogger LOGGER = new ClientLogger(KeyVaultSettingsClientTestBase.class);

    protected KeyVaultSettingsClientBuilder getClientBuilder(HttpClient httpClient, boolean forCleanup) {
        List<HttpPipelinePolicy> policies = getPolicies();

        if (getTestMode() == TestMode.RECORD && !forCleanup) {
            policies.add(interceptorManager.getRecordPolicy());
        }

        HttpPipeline httpPipeline = new HttpPipelineBuilder()
            .policies(policies.toArray(new HttpPipelinePolicy[0]))
            .httpClient(httpClient == null ? interceptorManager.getPlaybackClient() : httpClient)
            .build();

        return new KeyVaultSettingsClientBuilder()
            .vaultUrl(getEndpoint())
            .pipeline(httpPipeline);
    }

    @Test
    public abstract void getSettings(HttpClient httpClient);

    @Test
    public abstract void getSetting(HttpClient httpClient);

    @Test
    public abstract void updateSetting(HttpClient httpClient);

    static void assertSettingEquals(KeyVaultSetting keyVaultSetting1,
                                    KeyVaultSetting keyVaultSetting2) {
        assertEquals(keyVaultSetting1.getName(), keyVaultSetting2.getName());
        assertEquals(keyVaultSetting1.getType(), keyVaultSetting2.getType());

        if (keyVaultSetting1.getType() == KeyVaultSettingType.BOOLEAN) {
            assertEquals(keyVaultSetting1.asBoolean(), keyVaultSetting2.asBoolean());
        }
    }
}
