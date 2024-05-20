// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.keyvault.administration;

import com.azure.core.http.HttpClient;
import com.azure.core.test.http.AssertingHttpClientBuilder;
import com.azure.core.util.polling.AsyncPollResponse;
import com.azure.security.keyvault.keys.KeyClient;
import com.azure.security.keyvault.keys.KeyClientBuilder;
import com.azure.security.keyvault.keys.models.CreateRsaKeyOptions;
import com.azure.security.keyvault.keys.models.KeyVaultKey;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.test.StepVerifier;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class KeyVaultBackupAsyncClientTest extends KeyVaultBackupClientTestBase {
    private KeyVaultBackupAsyncClient asyncClient;

    private void getAsyncClient(HttpClient httpClient, boolean forCleanup) {
        asyncClient = getClientBuilder(buildAsyncAssertingClient(
            interceptorManager.isPlaybackMode() ? interceptorManager.getPlaybackClient() : httpClient), forCleanup)
            .buildAsyncClient();
    }

    private HttpClient buildAsyncAssertingClient(HttpClient httpClient) {
        return new AssertingHttpClientBuilder(httpClient)
            .assertAsync()
            .build();
    }

    /**
     * Tests that a Key Vault or MHSM can be backed up.
     */
    @SuppressWarnings("ConstantConditions")
    @ParameterizedTest(name = DISPLAY_NAME)
    @MethodSource("com.azure.security.keyvault.administration.KeyVaultAdministrationClientTestBase#createHttpClients")
    public void beginBackup(HttpClient httpClient) {
        getAsyncClient(httpClient, false);

        StepVerifier.create(setPlaybackPollerFluxPollInterval(asyncClient.beginBackup(blobStorageUrl, sasToken))
                .last()
                .flatMap(AsyncPollResponse::getFinalResult))
            .assertNext(backupBlobUri -> {
                assertNotNull(backupBlobUri);
                assertTrue(backupBlobUri.startsWith(blobStorageUrl));
            })
            .verifyComplete();
    }

    /**
     * Tests that a Key Vault or MHSM can be pre-backed up.
     */
    @SuppressWarnings("ConstantConditions")
    @ParameterizedTest(name = DISPLAY_NAME)
    @MethodSource("com.azure.security.keyvault.administration.KeyVaultAdministrationClientTestBase#createHttpClients")
    public void beginPreBackup(HttpClient httpClient) {
        getAsyncClient(httpClient, false);

        StepVerifier.create(setPlaybackPollerFluxPollInterval(asyncClient.beginPreBackup(blobStorageUrl, sasToken))
                .last()
                .flatMap(AsyncPollResponse::getFinalResult)
                .mapNotNull(backupBlobUri -> {
                    assertNull(backupBlobUri);

                    return backupBlobUri;
                }))
            .verifyComplete();
    }

    /**
     * Tests that a Key Vault can be restored from a backup.
     */
    @SuppressWarnings("ConstantConditions")
    @ParameterizedTest(name = DISPLAY_NAME)
    @MethodSource("com.azure.security.keyvault.administration.KeyVaultAdministrationClientTestBase#createHttpClients")
    public void beginRestore(HttpClient httpClient) {
        getAsyncClient(httpClient, false);

        StepVerifier.create(setPlaybackPollerFluxPollInterval(asyncClient.beginBackup(blobStorageUrl, sasToken))
                .last()
                .flatMap(AsyncPollResponse::getFinalResult)
                .map(backupBlobUri -> {
                    assertNotNull(backupBlobUri);
                    assertTrue(backupBlobUri.startsWith(blobStorageUrl));

                    return backupBlobUri;
                })
                .map(backupBlobUri -> asyncClient.beginRestore(backupBlobUri, sasToken)
                    .last()
                    .map(AsyncPollResponse::getValue)))
            .assertNext(Assertions::assertNotNull)
            .verifyComplete();

        // For some reason, the service might still think a restore operation is running even after returning a success
        // signal. This gives it some time to "clear" the operation.
        sleepIfRunningAgainstService(30000);
    }

    /**
     * Tests that a Key Vault can be pre-restored from a backup.
     */
    @SuppressWarnings("ConstantConditions")
    @ParameterizedTest(name = DISPLAY_NAME)
    @MethodSource("com.azure.security.keyvault.administration.KeyVaultAdministrationClientTestBase#createHttpClients")
    public void beginPreRestore(HttpClient httpClient) {
        getAsyncClient(httpClient, false);

        StepVerifier.create(setPlaybackPollerFluxPollInterval(asyncClient.beginBackup(blobStorageUrl, sasToken))
                .last()
                .flatMap(AsyncPollResponse::getFinalResult)
                .map(backupBlobUri -> {
                    assertNotNull(backupBlobUri);
                    assertTrue(backupBlobUri.startsWith(blobStorageUrl));

                    return backupBlobUri;
                })
                .map(backupBlobUri -> asyncClient.beginPreRestore(backupBlobUri, sasToken)
                    .last()
                    .map(AsyncPollResponse::getValue)))
            .assertNext(Assertions::assertNotNull)
            .verifyComplete();

        // For some reason, the service might still think a restore operation is running even after returning a success
        // signal. This gives it some time to "clear" the operation.
        sleepIfRunningAgainstService(30000);
    }

    /**
     * Tests that a key can be restored from a backup.
     */
    @SuppressWarnings("ConstantConditions")
    @ParameterizedTest(name = DISPLAY_NAME)
    @MethodSource("com.azure.security.keyvault.administration.KeyVaultAdministrationClientTestBase#createHttpClients")
    public void beginSelectiveKeyRestore(HttpClient httpClient) {
        KeyClient keyClient = new KeyClientBuilder()
            .vaultUrl(getEndpoint())
            .pipeline(getPipeline(httpClient, false))
            .buildClient();

        String keyName = testResourceNamer.randomName("backupKey", 20);
        CreateRsaKeyOptions rsaKeyOptions = new CreateRsaKeyOptions(keyName)
            .setExpiresOn(OffsetDateTime.of(2050, 1, 30, 0, 0, 0, 0, ZoneOffset.UTC))
            .setNotBefore(OffsetDateTime.of(2000, 1, 30, 12, 59, 59, 0, ZoneOffset.UTC));

        KeyVaultKey createdKey = keyClient.createRsaKey(rsaKeyOptions);

        getAsyncClient(httpClient, false);

        StepVerifier.create(setPlaybackPollerFluxPollInterval(asyncClient.beginBackup(blobStorageUrl, sasToken))
                .last()
                .flatMap(AsyncPollResponse::getFinalResult)
                .map(backupBlobUri -> {
                    assertNotNull(backupBlobUri);
                    assertTrue(backupBlobUri.startsWith(blobStorageUrl));

                    return backupBlobUri;
                })
                .map(backupBlobUri ->
                    asyncClient.beginSelectiveKeyRestore(createdKey.getName(), backupBlobUri, sasToken)
                        .last()
                        .map(AsyncPollResponse::getValue)))
            .assertNext(Assertions::assertNotNull)
            .verifyComplete();

        // For some reason, the service might still think a restore operation is running even after returning a success
        // signal. This gives it some time to "clear" the operation.
        sleepIfRunningAgainstService(30000);
    }
}
