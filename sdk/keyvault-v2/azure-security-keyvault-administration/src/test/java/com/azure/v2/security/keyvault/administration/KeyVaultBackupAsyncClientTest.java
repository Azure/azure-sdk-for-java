// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.v2.security.keyvault.administration;

import io.clientcore.core.http.client.HttpClient;
import com.azure.v2.security.keyvault.keys.KeyClient;
import com.azure.v2.security.keyvault.keys.KeyClientBuilder;
import com.azure.v2.security.keyvault.keys.models.CreateRsaKeyOptions;
import com.azure.v2.security.keyvault.keys.models.KeyVaultKey;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class KeyVaultBackupAsyncClientTest extends KeyVaultBackupClientTestBase {
    private KeyVaultBackupAsyncClient asyncClient;

    private void getAsyncClient(HttpClient httpClient, boolean forCleanup) {
        asyncClient = getClientBuilder(httpClient, forCleanup).buildAsyncClient();
    }

    /**
     * Tests that a Key Vault or MHSM can be backed up.
     */
    @SuppressWarnings("ConstantConditions")
    @ParameterizedTest(name = DISPLAY_NAME)
    @MethodSource("com.azure.v2.security.keyvault.administration.KeyVaultAdministrationClientTestBase#createHttpClients")
    public void beginBackup(HttpClient httpClient) {
        getAsyncClient(httpClient, false);

        CompletableFuture<String> backupResult = asyncClient.beginBackup(blobStorageUrl, sasToken)
            .thenCompose(poller -> poller.getFinalResult());

        String backupBlobUri = backupResult.join();
        assertNotNull(backupBlobUri);
        assertTrue(backupBlobUri.startsWith(blobStorageUrl));
    }

    /**
     * Tests that a Key Vault or MHSM can be pre-backed up.
     */
    @SuppressWarnings("ConstantConditions")
    @ParameterizedTest(name = DISPLAY_NAME)
    @MethodSource("com.azure.v2.security.keyvault.administration.KeyVaultAdministrationClientTestBase#createHttpClients")
    public void beginPreBackup(HttpClient httpClient) {
        getAsyncClient(httpClient, false);

        CompletableFuture<String> backupResult = asyncClient.beginPreBackup(blobStorageUrl, sasToken)
            .thenCompose(poller -> poller.getFinalResult());

        String backupBlobUri = backupResult.join();
        assertNull(backupBlobUri);
    }

    /**
     * Tests that a Key Vault can be restored from a backup.
     */
    @SuppressWarnings("ConstantConditions")
    @ParameterizedTest(name = DISPLAY_NAME)
    @MethodSource("com.azure.v2.security.keyvault.administration.KeyVaultAdministrationClientTestBase#createHttpClients")
    public void beginRestore(HttpClient httpClient) {
        getAsyncClient(httpClient, false);

        CompletableFuture<String> backupResult = asyncClient.beginBackup(blobStorageUrl, sasToken)
            .thenCompose(poller -> poller.getFinalResult());

        String backupBlobUri = backupResult.join();
        assertNotNull(backupBlobUri);
        assertTrue(backupBlobUri.startsWith(blobStorageUrl));

        CompletableFuture<Void> restoreResult = asyncClient.beginRestore(backupBlobUri, sasToken)
            .thenCompose(poller -> poller.getFinalResult());

        restoreResult.join();

        // For some reason, the service might still think a restore operation is running even after returning a success
        // signal. This gives it some time to "clear" the operation.
        sleepIfRunningAgainstService(30000);
    }

    /**
     * Tests that a Key Vault can be pre-restored from a backup.
     */
    @SuppressWarnings("ConstantConditions")
    @ParameterizedTest(name = DISPLAY_NAME)
    @MethodSource("com.azure.v2.security.keyvault.administration.KeyVaultAdministrationClientTestBase#createHttpClients")
    public void beginPreRestore(HttpClient httpClient) {
        getAsyncClient(httpClient, false);

        CompletableFuture<String> backupResult = asyncClient.beginBackup(blobStorageUrl, sasToken)
            .thenCompose(poller -> poller.getFinalResult());

        String backupBlobUri = backupResult.join();
        assertNotNull(backupBlobUri);
        assertTrue(backupBlobUri.startsWith(blobStorageUrl));

        CompletableFuture<Void> restoreResult = asyncClient.beginPreRestore(backupBlobUri, sasToken)
            .thenCompose(poller -> poller.getFinalResult());

        restoreResult.join();

        // For some reason, the service might still think a restore operation is running even after returning a success
        // signal. This gives it some time to "clear" the operation.
        sleepIfRunningAgainstService(30000);
    }

    /**
     * Tests that a key can be restored from a backup.
     */
    @SuppressWarnings("ConstantConditions")
    @ParameterizedTest(name = DISPLAY_NAME)
    @MethodSource("com.azure.v2.security.keyvault.administration.KeyVaultAdministrationClientTestBase#createHttpClients")
    public void beginSelectiveKeyRestore(HttpClient httpClient) {
        KeyClient keyClient
            = new KeyClientBuilder().vaultUrl(getEndpoint()).pipeline(getPipeline(httpClient, false)).buildClient();

        String keyName = testResourceNamer.randomName("backupKey", 20);
        CreateRsaKeyOptions rsaKeyOptions
            = new CreateRsaKeyOptions(keyName).setExpiresOn(OffsetDateTime.of(2050, 1, 30, 0, 0, 0, 0, ZoneOffset.UTC))
                .setNotBefore(OffsetDateTime.of(2000, 1, 30, 12, 59, 59, 0, ZoneOffset.UTC));

        KeyVaultKey createdKey = keyClient.createRsaKey(rsaKeyOptions);

        getAsyncClient(httpClient, false);

        CompletableFuture<String> backupResult = asyncClient.beginBackup(blobStorageUrl, sasToken)
            .thenCompose(poller -> poller.getFinalResult());

        String backupBlobUri = backupResult.join();
        assertNotNull(backupBlobUri);
        assertTrue(backupBlobUri.startsWith(blobStorageUrl));

        CompletableFuture<Void> restoreResult = asyncClient.beginSelectiveKeyRestore(createdKey.getName(), backupBlobUri, sasToken)
            .thenCompose(poller -> poller.getFinalResult());

        restoreResult.join();

        // For some reason, the service might still think a restore operation is running even after returning a success
        // signal. This gives it some time to "clear" the operation.
        sleepIfRunningAgainstService(30000);
    }
}
