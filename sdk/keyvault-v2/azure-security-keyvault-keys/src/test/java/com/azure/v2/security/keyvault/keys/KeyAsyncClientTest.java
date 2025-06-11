// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.security.keyvault.keys;

import io.clientcore.core.http.models.HttpResponseException;
import io.clientcore.core.http.client.HttpClient;
import io.clientcore.core.instrumentation.logging.ClientLogger;
import com.azure.v2.security.keyvault.keys.implementation.KeyVaultCredentialPolicy;
import com.azure.v2.security.keyvault.keys.models.CreateKeyOptions;
import com.azure.v2.security.keyvault.keys.models.CreateRsaKeyOptions;
import com.azure.v2.security.keyvault.keys.models.DeletedKey;
import com.azure.v2.security.keyvault.keys.models.KeyProperties;
import com.azure.v2.security.keyvault.keys.models.KeyType;
import com.azure.v2.security.keyvault.keys.models.KeyVaultKey;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.net.HttpURLConnection;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class KeyAsyncClientTest extends KeyClientTestBase {
    private static final ClientLogger LOGGER = new ClientLogger(KeyAsyncClientTest.class);

    protected KeyAsyncClient keyAsyncClient;

    @Override
    protected void beforeTest() {
        beforeTestSetup();
    }

    protected void createKeyAsyncClient(HttpClient httpClient, KeyServiceVersion serviceVersion) {
        createKeyAsyncClient(httpClient, serviceVersion, null);
    }

    protected void createKeyAsyncClient(HttpClient httpClient, KeyServiceVersion serviceVersion, String testTenantId) {
        keyAsyncClient = getKeyClientBuilder(httpClient, testTenantId, getEndpoint(), serviceVersion).buildAsyncClient();
    }

    /**
     * Tests that a key can be created in the key vault.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void createKey(HttpClient httpClient, KeyServiceVersion serviceVersion) {
        createKeyAsyncClient(httpClient, serviceVersion);

        createKeyRunner((keyToCreate) -> {
            CompletableFuture<KeyVaultKey> createdKeyFuture = keyAsyncClient.createKey(keyToCreate);
            KeyVaultKey createdKey = createdKeyFuture.join();

            assertKeyEquals(keyToCreate, createdKey);

            if (!isHsmEnabled) {
                assertEquals("0", createdKey.getProperties().getHsmPlatform());
            }
        });
    }

    /**
     * Tests that an RSA key can be created in the key vault.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void createRsaKey(HttpClient httpClient, KeyServiceVersion serviceVersion) {
        createKeyAsyncClient(httpClient, serviceVersion);

        createRsaKeyRunner((keyToCreate) -> {
            CompletableFuture<KeyVaultKey> createdKeyFuture = keyAsyncClient.createRsaKey(keyToCreate);
            KeyVaultKey createdKey = createdKeyFuture.join();

            assertRsaKeyEquals(keyToCreate, createdKey);
        });
    }

    /**
     * Tests that a key can be updated in the key vault.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void updateKey(HttpClient httpClient, KeyServiceVersion serviceVersion) {
        createKeyAsyncClient(httpClient, serviceVersion);

        updateKeyRunner((originalKey, updatedKey) -> {
            CompletableFuture<KeyVaultKey> originalKeyFuture = keyAsyncClient.createKey(originalKey);
            KeyVaultKey originalKeyResponse = originalKeyFuture.join();

            CompletableFuture<KeyVaultKey> updatedKeyFuture = keyAsyncClient.updateKeyProperties(originalKeyResponse.getProperties());
            KeyVaultKey updatedKeyResponse = updatedKeyFuture.join();

            assertKeyEquals(updatedKey, updatedKeyResponse);
        });
    }

    /**
     * Tests that a key can be retrieved from the key vault.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void getKey(HttpClient httpClient, KeyServiceVersion serviceVersion) {
        createKeyAsyncClient(httpClient, serviceVersion);

        getKeyRunner((keyToGet) -> {
            CompletableFuture<KeyVaultKey> createdKeyFuture = keyAsyncClient.createKey(keyToGet);
            KeyVaultKey createdKey = createdKeyFuture.join();

            CompletableFuture<KeyVaultKey> retrievedKeyFuture = keyAsyncClient.getKey(keyToGet.getName());
            KeyVaultKey retrievedKey = retrievedKeyFuture.join();

            assertKeyEquals(keyToGet, retrievedKey);
        });
    }

    /**
     * Tests that a key can be deleted from the key vault.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void deleteKey(HttpClient httpClient, KeyServiceVersion serviceVersion) {
        createKeyAsyncClient(httpClient, serviceVersion);

        deleteKeyRunner((keyToDelete) -> {
            CompletableFuture<KeyVaultKey> createdKeyFuture = keyAsyncClient.createKey(keyToDelete);
            KeyVaultKey createdKey = createdKeyFuture.join();

            CompletableFuture<DeletedKey> deletedKeyFuture = keyAsyncClient.beginDeleteKey(keyToDelete.getName())
                .thenCompose(poller -> poller.getFinalResult());
            DeletedKey deletedKey = deletedKeyFuture.join();

            assertNotNull(deletedKey.getDeletedOn());
            assertNotNull(deletedKey.getRecoveryId());
            assertNotNull(deletedKey.getScheduledPurgeDate());
            assertEquals(keyToDelete.getName(), deletedKey.getName());
        });
    }

    /**
     * Tests that a deleted key can be retrieved from the key vault.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void getDeletedKey(HttpClient httpClient, KeyServiceVersion serviceVersion) {
        createKeyAsyncClient(httpClient, serviceVersion);

        getDeletedKeyRunner((keyToDeleteAndGet) -> {
            CompletableFuture<KeyVaultKey> createdKeyFuture = keyAsyncClient.createKey(keyToDeleteAndGet);
            KeyVaultKey createdKey = createdKeyFuture.join();

            CompletableFuture<DeletedKey> deletedKeyFuture = keyAsyncClient.beginDeleteKey(keyToDeleteAndGet.getName())
                .thenCompose(poller -> poller.getFinalResult());
            DeletedKey deletedKey = deletedKeyFuture.join();

            CompletableFuture<DeletedKey> retrievedDeletedKeyFuture = keyAsyncClient.getDeletedKey(keyToDeleteAndGet.getName());
            DeletedKey retrievedDeletedKey = retrievedDeletedKeyFuture.join();

            assertNotNull(retrievedDeletedKey.getDeletedOn());
            assertNotNull(retrievedDeletedKey.getRecoveryId());
            assertNotNull(retrievedDeletedKey.getScheduledPurgeDate());
            assertEquals(keyToDeleteAndGet.getName(), retrievedDeletedKey.getName());
        });
    }

    /**
     * Tests that a deleted key can be recovered from the key vault.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void recoverDeletedKey(HttpClient httpClient, KeyServiceVersion serviceVersion) {
        createKeyAsyncClient(httpClient, serviceVersion);

        recoverDeletedKeyRunner((keyToDeleteAndRecover) -> {
            CompletableFuture<KeyVaultKey> createdKeyFuture = keyAsyncClient.createKey(keyToDeleteAndRecover);
            KeyVaultKey createdKey = createdKeyFuture.join();

            CompletableFuture<DeletedKey> deletedKeyFuture = keyAsyncClient.beginDeleteKey(keyToDeleteAndRecover.getName())
                .thenCompose(poller -> poller.getFinalResult());
            DeletedKey deletedKey = deletedKeyFuture.join();

            CompletableFuture<KeyVaultKey> recoveredKeyFuture = keyAsyncClient.beginRecoverDeletedKey(keyToDeleteAndRecover.getName())
                .thenCompose(poller -> poller.getFinalResult());
            KeyVaultKey recoveredKey = recoveredKeyFuture.join();

            assertEquals(keyToDeleteAndRecover.getName(), recoveredKey.getName());
            assertEquals(keyToDeleteAndRecover.getKeyType(), recoveredKey.getKey().getKeyType());
        });
    }

    /**
     * Tests that a deleted key can be permanently purged from the key vault.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void purgeDeletedKey(HttpClient httpClient, KeyServiceVersion serviceVersion) {
        createKeyAsyncClient(httpClient, serviceVersion);

        purgeDeletedKeyRunner((keyToDeleteAndPurge) -> {
            CompletableFuture<KeyVaultKey> createdKeyFuture = keyAsyncClient.createKey(keyToDeleteAndPurge);
            KeyVaultKey createdKey = createdKeyFuture.join();

            CompletableFuture<DeletedKey> deletedKeyFuture = keyAsyncClient.beginDeleteKey(keyToDeleteAndPurge.getName())
                .thenCompose(poller -> poller.getFinalResult());
            DeletedKey deletedKey = deletedKeyFuture.join();

            CompletableFuture<Void> purgeFuture = keyAsyncClient.purgeDeletedKey(keyToDeleteAndPurge.getName());
            purgeFuture.join();

            sleepIfRunningAgainstService(10000);

            CompletableFuture<DeletedKey> getDeletedKeyFuture = keyAsyncClient.getDeletedKey(keyToDeleteAndPurge.getName());
            HttpResponseException exception = assertThrows(HttpResponseException.class, getDeletedKeyFuture::join);
            assertEquals(HttpURLConnection.HTTP_NOT_FOUND, exception.getResponse().getStatusCode());
        });
    }

    /**
     * Tests that keys can be listed from the key vault.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void listKeys(HttpClient httpClient, KeyServiceVersion serviceVersion) {
        createKeyAsyncClient(httpClient, serviceVersion);

        listKeysRunner((keys) -> {
            List<KeyProperties> output = new ArrayList<>();

            CompletableFuture<List<KeyProperties>> listFuture = keyAsyncClient.listPropertiesOfKeys()
                .thenApply(stream -> {
                    List<KeyProperties> result = new ArrayList<>();
                    stream.forEach(actualKey -> {
                        if (keys.containsKey(actualKey.getName())) {
                            result.add(actualKey);
                        }
                    });
                    return result;
                });

            List<KeyProperties> result = listFuture.join();
            assertEquals(keys.size(), result.size());
        });
    }

    /**
     * Tests that key versions can be listed from the key vault.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void listKeyVersions(HttpClient httpClient, KeyServiceVersion serviceVersion) {
        createKeyAsyncClient(httpClient, serviceVersion);

        listKeyVersionsRunner((keyVersions) -> {
            CompletableFuture<List<KeyProperties>> listFuture = keyAsyncClient.listPropertiesOfKeyVersions(keyVersions.get(0).getName())
                .thenApply(stream -> {
                    List<KeyProperties> result = new ArrayList<>();
                    stream.forEach(result::add);
                    return result;
                });

            List<KeyProperties> result = listFuture.join();
            assertEquals(keyVersions.size(), result.size());
        });
    }

    /**
     * Tests that deleted keys can be listed from the key vault.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void listDeletedKeys(HttpClient httpClient, KeyServiceVersion serviceVersion) {
        createKeyAsyncClient(httpClient, serviceVersion);

        listDeletedKeysRunner((deletedKeys) -> {
            CompletableFuture<List<DeletedKey>> listFuture = keyAsyncClient.listDeletedKeys()
                .thenApply(stream -> {
                    List<DeletedKey> result = new ArrayList<>();
                    stream.forEach(actualKey -> {
                        if (deletedKeys.containsKey(actualKey.getName())) {
                            result.add(actualKey);
                        }
                    });
                    return result;
                });

            List<DeletedKey> result = listFuture.join();
            assertEquals(deletedKeys.size(), result.size());
        });
    }

    private void assertKeyEquals(CreateKeyOptions expected, KeyVaultKey actual) {
        assertEquals(expected.getName(), actual.getName());
        assertEquals(expected.getKeyType(), actual.getKey().getKeyType());
        assertEquals(expected.getExpiresOn(), actual.getProperties().getExpiresOn());
        assertEquals(expected.getNotBefore(), actual.getProperties().getNotBefore());
        assertEquals(expected.getTags(), actual.getProperties().getTags());
    }

    private void assertRsaKeyEquals(CreateRsaKeyOptions expected, KeyVaultKey actual) {
        assertEquals(expected.getName(), actual.getName());
        assertEquals(expected.getExpiresOn(), actual.getProperties().getExpiresOn());
        assertEquals(expected.getNotBefore(), actual.getProperties().getNotBefore());
        assertEquals(expected.getTags(), actual.getProperties().getTags());
    }
}
