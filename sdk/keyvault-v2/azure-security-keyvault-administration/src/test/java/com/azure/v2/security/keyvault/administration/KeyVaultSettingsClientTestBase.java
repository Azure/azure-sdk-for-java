// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.v2.security.keyvault.administration;

import com.azure.v2.core.credentials.TokenCredential;
import com.azure.v2.core.test.models.CustomMatcher;
import com.azure.v2.core.test.models.TestProxyRequestMatcher;
import com.azure.v2.core.test.models.TestProxySanitizer;
import com.azure.v2.core.test.models.TestProxySanitizerType;
import com.azure.v2.identity.AzurePowerShellCredentialBuilder;
import com.azure.v2.identity.DefaultAzureCredentialBuilder;
import com.azure.v2.security.keyvault.administration.models.KeyVaultSetting;
import com.azure.v2.security.keyvault.administration.models.KeyVaultSettingType;
import io.clientcore.core.credentials.oauth.AccessToken;
import io.clientcore.core.http.client.HttpClient;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public abstract class KeyVaultSettingsClientTestBase extends KeyVaultAdministrationClientTestBase {
    KeyVaultSettingsClientBuilder getClientBuilder(HttpClient httpClient) throws IOException {
        TokenCredential credential;

        if (interceptorManager.isLiveMode()) {
            credential = new AzurePowerShellCredentialBuilder().additionallyAllowedTenants("*").build();
        } else if (interceptorManager.isRecordMode()) {
            credential = new DefaultAzureCredentialBuilder().additionallyAllowedTenants("*").build();
            List<TestProxySanitizer> customSanitizers = new ArrayList<>();

            customSanitizers.add(new TestProxySanitizer("token", null, "REDACTED", TestProxySanitizerType.BODY_KEY));
            interceptorManager.addSanitizers(customSanitizers);
        } else {
            credential = request -> new AccessToken("mockToken", OffsetDateTime.now().plusHours(2));

            List<TestProxyRequestMatcher> customMatchers = new ArrayList<>();

            customMatchers.add(new CustomMatcher().setComparingBodies(false)
                .setHeadersKeyOnlyMatch(Collections.singletonList("Accept"))
                .setExcludedHeaders(Arrays.asList("Authorization", "Accept-Language")));
            interceptorManager.addMatchers(customMatchers);
        }

        KeyVaultSettingsClientBuilder builder = new KeyVaultSettingsClientBuilder()
            .endpoint(getEndpoint())
            .credential(credential)
            .httpClient(interceptorManager.isPlaybackMode() ? interceptorManager.getPlaybackClient() : httpClient);

        if (interceptorManager.isRecordMode()) {
            return builder.addHttpPipelinePolicy(interceptorManager.getRecordPolicy());
        }

        return builder;
    }

    @Test
    public abstract void getSettings(HttpClient httpClient) throws IOException;

    @Test
    public abstract void getSetting(HttpClient httpClient) throws IOException;

    @Test
    public abstract void updateSetting(HttpClient httpClient) throws IOException;

    static void assertSettingEquals(KeyVaultSetting keyVaultSetting1, KeyVaultSetting keyVaultSetting2) {
        assertEquals(keyVaultSetting1.getName(), keyVaultSetting2.getName());
        assertEquals(keyVaultSetting1.getType(), keyVaultSetting2.getType());

        if (keyVaultSetting1.getType() == KeyVaultSettingType.BOOLEAN) {
            assertEquals(keyVaultSetting1.asBoolean(), keyVaultSetting2.asBoolean());
        }
    }
}
