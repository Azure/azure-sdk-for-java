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
import io.clientcore.core.credentials.oauth.AccessToken;
import io.clientcore.core.http.client.HttpClient;
import io.clientcore.core.utils.configuration.Configuration;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public abstract class KeyVaultBackupClientTestBase extends KeyVaultAdministrationClientTestBase {
    protected final String blobStorageUrl = IS_MANAGED_HSM_DEPLOYED
        ? getStorageEndpoint() + "/" + Configuration.getGlobalConfiguration().get("BLOB_CONTAINER_NAME")
        : "https://testaccountprim.blob.core.windows.net/backup";
    protected final String sasToken = IS_MANAGED_HSM_DEPLOYED ? "REDACTED" : "REDACTED";

    KeyVaultBackupClientBuilder getClientBuilder(HttpClient httpClient) throws IOException {
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

        KeyVaultBackupClientBuilder builder = new KeyVaultBackupClientBuilder()
            .endpoint(getEndpoint())
            .credential(credential)
            .httpClient(interceptorManager.isPlaybackMode() ? interceptorManager.getPlaybackClient() : httpClient);

        if (interceptorManager.isRecordMode()) {
            return builder.addHttpPipelinePolicy(interceptorManager.getRecordPolicy());
        }

        return builder;
    }

    @Test
    public abstract void beginBackup(HttpClient httpClient) throws IOException;

    @Test
    public abstract void beginRestore(HttpClient httpClient) throws IOException;

    @Test
    public abstract void beginSelectiveKeyRestore(HttpClient httpClient) throws IOException;

    private static String getStorageEndpoint() {
        String accountName = Configuration.getGlobalConfiguration().get("BLOB_STORAGE_ACCOUNT_NAME");
        String suffix = Configuration.getGlobalConfiguration().get("KEYVAULT_STORAGE_ENDPOINT_SUFFIX");

        return "https://" + accountName + ".blob." + suffix;
    }
}
