// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.keyvault.administration;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.util.Configuration;
import com.azure.identity.AzurePowerShellCredentialBuilder;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.UserDelegationKey;
import com.azure.storage.blob.sas.BlobContainerSasPermission;
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public abstract class KeyVaultBackupClientTestBase extends KeyVaultAdministrationClientTestBase {
    protected final String blobStorageUrl = IS_MANAGED_HSM_DEPLOYED
        ? getStorageEndpoint() + Configuration.getGlobalConfiguration().get("BLOB_CONTAINER_NAME")
        : "https://externalkeysdkprim.blob.core.windows.net/backup";
    protected String sasToken;

    @Override
    protected void beforeTest() {
        super.beforeTest();

        sasToken = interceptorManager.isPlaybackMode() ? "REDACTED" : generateSasToken();
    }

    KeyVaultBackupClientBuilder getClientBuilder(HttpClient httpClient, boolean forCleanup) {
        return new KeyVaultBackupClientBuilder().vaultUrl(getEndpoint()).pipeline(getPipeline(httpClient, forCleanup));
    }

    @Test
    public abstract void beginBackup(HttpClient httpClient);

    @Test
    public abstract void beginPreBackup(HttpClient httpClient);

    @Test
    public abstract void beginRestore(HttpClient httpClient);

    @Test
    public abstract void beginPreRestore(HttpClient httpClient);

    @Test
    public abstract void beginSelectiveKeyRestore(HttpClient httpClient);

    private static String getStorageEndpoint() {
        String accountName = Configuration.getGlobalConfiguration().get("BLOB_STORAGE_ACCOUNT_NAME");

        return "https://" + accountName + ".blob."
            + Configuration.getGlobalConfiguration().get("KEYVAULT_STORAGE_ENDPOINT_SUFFIX") + "/";
    }

    // The storage account disallows shared key access, so sign the SAS with an Azure AD user delegation key.
    private String generateSasToken() {
        TokenCredential credential = interceptorManager.isLiveMode()
            ? new AzurePowerShellCredentialBuilder().additionallyAllowedTenants("*").build()
            : new DefaultAzureCredentialBuilder().additionallyAllowedTenants("*").build();

        BlobServiceClient blobServiceClient
            = new BlobServiceClientBuilder().credential(credential).endpoint(getStorageEndpoint()).buildClient();

        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        OffsetDateTime expiresOn = now.plusHours(1);
        UserDelegationKey userDelegationKey = blobServiceClient.getUserDelegationKey(now.minusMinutes(5), expiresOn);

        return blobServiceClient
            .getBlobContainerClient(Configuration.getGlobalConfiguration().get("BLOB_CONTAINER_NAME"))
            .generateUserDelegationSas(
                new BlobServiceSasSignatureValues(expiresOn, BlobContainerSasPermission.parse("racwdl")),
                userDelegationKey);
    }
}
