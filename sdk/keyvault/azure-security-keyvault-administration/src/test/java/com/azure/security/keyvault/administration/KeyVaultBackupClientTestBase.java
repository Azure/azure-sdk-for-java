// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.keyvault.administration;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.test.TestMode;
import com.azure.core.util.Configuration;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.azure.storage.common.sas.AccountSasPermission;
import com.azure.storage.common.sas.AccountSasResourceType;
import com.azure.storage.common.sas.AccountSasService;
import com.azure.storage.common.sas.AccountSasSignatureValues;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

public abstract class KeyVaultBackupClientTestBase extends KeyVaultAdministrationClientTestBase {
    protected final String blobStorageUrl = IS_MANAGED_HSM_DEPLOYED
        ? getStorageEndpoint() + Configuration.getGlobalConfiguration().get("BLOB_CONTAINER_NAME")
        : "https://testaccount.blob.core.windows.net/backup";
    protected final String sasToken = IS_MANAGED_HSM_DEPLOYED ? generateSasToken() : "someSasToken";

    protected HttpPipeline getPipeline(HttpClient httpClient, boolean forCleanup) {
        List<HttpPipelinePolicy> policies = getPolicies();

        if (getTestMode() == TestMode.RECORD && !forCleanup) {
            policies.add(interceptorManager.getRecordPolicy());
        }

        return new HttpPipelineBuilder()
            .policies(policies.toArray(new HttpPipelinePolicy[0]))
            .httpClient(httpClient == null ? interceptorManager.getPlaybackClient() : httpClient)
            .build();
    }

    protected KeyVaultBackupClientBuilder getClientBuilder(HttpClient httpClient, boolean forCleanup) {
        return new KeyVaultBackupClientBuilder()
            .vaultUrl(getEndpoint())
            .pipeline(getPipeline(httpClient, forCleanup));
    }

    @Test
    public abstract void beginBackup(HttpClient httpClient);

    @Test
    public abstract void beginRestore(HttpClient httpClient);

    @Test
    public abstract void beginSelectiveKeyRestore(HttpClient httpClient);

    private static String getStorageEndpoint() {
        String accountName = Configuration.getGlobalConfiguration().get("BLOB_STORAGE_ACCOUNT_NAME");

        return "https://" + accountName + ".blob."
            + Configuration.getGlobalConfiguration().get("KEYVAULT_STORAGE_ENDPOINT_SUFFIX") + "/";
    }

    private static String generateSasToken() {
        String accountName = Configuration.getGlobalConfiguration().get("BLOB_STORAGE_ACCOUNT_NAME");
        String accountKey = Configuration.getGlobalConfiguration().get("BLOB_PRIMARY_STORAGE_ACCOUNT_KEY");

        BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
            .credential(new StorageSharedKeyCredential(accountName, accountKey))
            .endpoint(getStorageEndpoint())
            .buildClient();

        AccountSasSignatureValues accountSasSignatureValues = new AccountSasSignatureValues(
            OffsetDateTime.of(2050, 12, 12, 0, 0, 0, 0, ZoneOffset.UTC),
            AccountSasPermission.parse("rwdlacuptfx"),
            AccountSasService.parse("b"),
            AccountSasResourceType.parse("sco")
        );

        return blobServiceClient.generateAccountSas(accountSasSignatureValues);
    }
}
