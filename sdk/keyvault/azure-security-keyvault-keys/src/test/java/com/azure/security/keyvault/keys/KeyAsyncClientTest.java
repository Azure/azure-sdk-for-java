// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.azure.core.exception.ResourceModifiedException;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.util.polling.AsyncPollResponse;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.PollerFlux;
import com.azure.security.keyvault.keys.models.CreateKeyOptions;
import com.azure.security.keyvault.keys.models.DeletedKey;
import com.azure.security.keyvault.keys.models.KeyVaultKey;
import com.azure.security.keyvault.keys.models.KeyProperties;
import com.azure.security.keyvault.keys.models.KeyType;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

public class KeyAsyncClientTest extends KeyClientTestBase {

    private KeyAsyncClient client;

    @Override
    protected void beforeTest() {
        beforeTestSetup();

        if (interceptorManager.isPlaybackMode()) {
            client = clientSetup(pipeline -> new KeyClientBuilder()
                .vaultUrl(getEndpoint())
                .pipeline(pipeline)
                .buildAsyncClient());
        } else {
            client = clientSetup(pipeline -> new KeyClientBuilder()
                .pipeline(pipeline)
                .vaultUrl(getEndpoint())
                .buildAsyncClient());
        }
    }

    /**
     * Tests that a key can be created in the key vault.
     */
    @Test
    public void setKey() {
        setKeyRunner((expected) -> StepVerifier.create(client.createKey(expected))
            .assertNext(response -> assertKeyEquals(expected, response))
            .verifyComplete());
    }

    /**
     * Tests that we cannot create a key when the key is an empty string.
     */
    @Test
    public void setKeyEmptyName() {
        StepVerifier.create(client.createKey("", KeyType.RSA))
                .verifyErrorSatisfies(ex -> assertRestException(ex, ResourceModifiedException.class, HttpURLConnection.HTTP_BAD_REQUEST));
    }

    /**
     * Tests that we can create keys when value is not null or an empty string.
     */
    @Test
    public void setKeyNullType() {
        setKeyEmptyValueRunner((key) -> {

            StepVerifier.create(client.createKey(key))
                    .verifyErrorSatisfies(ex -> assertRestException(ex, ResourceModifiedException.class, HttpURLConnection.HTTP_BAD_REQUEST));

        });
    }

    /**
     * Verifies that an exception is thrown when null key object is passed for creation.
     */
    @Test
    public void setKeyNull() {
        StepVerifier.create(client.createKey(null))
            .verifyError(NullPointerException.class);
    }

    /**
     * Tests that a key is able to be updated when it exists.
     */
    @Test
    public void updateKey() {
        updateKeyRunner((original, updated) -> {
            StepVerifier.create(client.createKey(original))
                .assertNext(response -> assertKeyEquals(original, response))
                .verifyComplete();
            KeyVaultKey keyToUpdate = client.getKey(original.getName()).block();

            StepVerifier.create(client.updateKeyProperties(keyToUpdate.getProperties().setExpiresOn(updated.getExpiresOn())))
                .assertNext(response -> {
                    assertNotNull(response);
                    assertEquals(original.getName(), response.getName());
                }).verifyComplete();

            StepVerifier.create(client.getKey(original.getName()))
                .assertNext(updatedKeyResponse -> assertKeyEquals(updated, updatedKeyResponse))
                .verifyComplete();
        });
    }

    /**
     * Tests that a key is not able to be updated when it is disabled. 403 error is expected.
     */
    @Test
    public void updateDisabledKey() {
        updateDisabledKeyRunner((original, updated) -> {
            StepVerifier.create(client.createKey(original))
                .assertNext(response -> assertKeyEquals(original, response))
                .verifyComplete();
            KeyVaultKey keyToUpdate = client.getKey(original.getName()).block();

            StepVerifier.create(client.updateKeyProperties(keyToUpdate.getProperties().setExpiresOn(updated.getExpiresOn())))
                .assertNext(response -> {
                    assertNotNull(response);
                    assertEquals(original.getName(), response.getName());
                }).verifyComplete();

            StepVerifier.create(client.getKey(original.getName()))
                .assertNext(updatedKeyResponse -> assertKeyEquals(updated, updatedKeyResponse))
                .verifyComplete();
        });
    }


    /**
     * Tests that an existing key can be retrieved.
     */
    @Test
    public void getKey() {
        getKeyRunner((original) -> {
            client.createKey(original);
            StepVerifier.create(client.getKey(original.getName()))
                .assertNext(response -> assertKeyEquals(original, response))
                .verifyComplete();
        });
    }

    /**
     * Tests that a specific version of the key can be retrieved.
     */
    @Test
    public void getKeySpecificVersion() {
        getKeySpecificVersionRunner((key, keyWithNewVal) -> {
            final KeyVaultKey keyVersionOne = client.createKey(key).block();
            final KeyVaultKey keyVersionTwo = client.createKey(keyWithNewVal).block();

            StepVerifier.create(client.getKey(key.getName(), keyVersionOne.getProperties().getVersion()))
                .assertNext(response -> assertKeyEquals(key, response))
                .verifyComplete();

            StepVerifier.create(client.getKey(keyWithNewVal.getName(), keyVersionTwo.getProperties().getVersion()))
                .assertNext(response -> assertKeyEquals(keyWithNewVal, response))
                .verifyComplete();
        });
    }

    /**
     * Tests that an attempt to get a non-existing key throws an error.
     */
    @Test
    public void getKeyNotFound() {
        StepVerifier.create(client.getKey("non-existing"))
                .verifyErrorSatisfies(ex -> assertRestException(ex, ResourceNotFoundException.class, HttpURLConnection.HTTP_NOT_FOUND));
    }


    /**
     * Tests that an existing key can be deleted.
     */
    @Test
    public void deleteKey() {
        deleteKeyRunner((keyToDelete) -> {
            StepVerifier.create(client.createKey(keyToDelete))
                .assertNext(keyResponse -> assertKeyEquals(keyToDelete, keyResponse)).verifyComplete();

            PollerFlux<DeletedKey, Void> poller = client.beginDeleteKey(keyToDelete.getName());
            AsyncPollResponse<DeletedKey, Void> deletedKeyPollResponse = poller
                    .takeUntil(apr -> apr.getStatus() == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED)
                    .blockLast();
            DeletedKey deletedKeyResponse = deletedKeyPollResponse.getValue();
            assertNotNull(deletedKeyResponse.getDeletedOn());
            assertNotNull(deletedKeyResponse.getRecoveryId());
            assertNotNull(deletedKeyResponse.getScheduledPurgeDate());
            assertEquals(keyToDelete.getName(), deletedKeyResponse.getName());

            StepVerifier.create(client.purgeDeletedKeyWithResponse(keyToDelete.getName()))
                    .assertNext(voidResponse -> {
                        assertEquals(HttpURLConnection.HTTP_NO_CONTENT, voidResponse.getStatusCode());
                    }).verifyComplete();
            sleepInRecordMode(15000);
        });
    }

    @Test
    public void deleteKeyNotFound() {
        StepVerifier.create(client.beginDeleteKey("non-existing"))
            .verifyErrorSatisfies(ex -> assertRestException(ex, ResourceNotFoundException.class, HttpURLConnection.HTTP_NOT_FOUND));
    }

    /**
     * Tests that an attempt to retrieve a non existing deleted key throws an error on a soft-delete enabled vault.
     */
    @Test
    public void getDeletedKeyNotFound() {
        StepVerifier.create(client.getDeletedKey("non-existing"))
                .verifyErrorSatisfies(ex -> assertRestException(ex, ResourceNotFoundException.class, HttpURLConnection.HTTP_NOT_FOUND));
    }

    /**
     * Tests that a deleted key can be recovered on a soft-delete enabled vault.
     */
    @Test
    public void recoverDeletedKey() {
        recoverDeletedKeyRunner((keyToDeleteAndRecover) -> {
            StepVerifier.create(client.createKey(keyToDeleteAndRecover))
                .assertNext(keyResponse -> assertKeyEquals(keyToDeleteAndRecover, keyResponse)).verifyComplete();

            PollerFlux<DeletedKey, Void> poller = client.beginDeleteKey(keyToDeleteAndRecover.getName());
            AsyncPollResponse<DeletedKey, Void> deleteKeyPollResponse
                    = poller.takeUntil(apr -> apr.getStatus() == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED)
                            .blockLast();

            assertNotNull(deleteKeyPollResponse.getValue());

            PollerFlux<KeyVaultKey, Void> recoverPoller = client.beginRecoverDeletedKey(keyToDeleteAndRecover.getName());

            AsyncPollResponse<KeyVaultKey, Void> recoverKeyPollResponse
                    = recoverPoller.takeUntil(apr -> apr.getStatus() == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED)
                .blockLast();

            KeyVaultKey keyResponse = recoverKeyPollResponse.getValue();
            assertEquals(keyToDeleteAndRecover.getName(), keyResponse.getName());
            assertEquals(keyToDeleteAndRecover.getNotBefore(), keyResponse.getProperties().getNotBefore());
            assertEquals(keyToDeleteAndRecover.getExpiresOn(), keyResponse.getProperties().getExpiresOn());
        });
    }

    /**
     * Tests that an attempt to recover a non existing deleted key throws an error on a soft-delete enabled vault.
     */
    @Test
    public void recoverDeletedKeyNotFound() {
        StepVerifier.create(client.beginRecoverDeletedKey("non-existing"))
            .verifyErrorSatisfies(ex -> assertRestException(ex, ResourceNotFoundException.class, HttpURLConnection.HTTP_NOT_FOUND));
    }

    /**
     * Tests that a key can be backed up in the key vault.
     */
    @Test
    public void backupKey() {
        backupKeyRunner((keyToBackup) -> {
            StepVerifier.create(client.createKey(keyToBackup))
                .assertNext(keyResponse -> assertKeyEquals(keyToBackup, keyResponse)).verifyComplete();

            StepVerifier.create(client.backupKey(keyToBackup.getName()))
                .assertNext(response -> {
                    assertNotNull(response);
                    assertTrue(response.length > 0);
                }).verifyComplete();
        });
    }

    /**
     * Tests that an attempt to backup a non existing key throws an error.
     */
    @Test
    public void backupKeyNotFound() {
        StepVerifier.create(client.backupKey("non-existing"))
                .verifyErrorSatisfies(ex -> assertRestException(ex, ResourceNotFoundException.class, HttpURLConnection.HTTP_NOT_FOUND));
    }

    /**
     * Tests that a key can be backed up in the key vault.
     */
    @Test
    public void restoreKey() {
        restoreKeyRunner((keyToBackupAndRestore) -> {
            StepVerifier.create(client.createKey(keyToBackupAndRestore))
                .assertNext(keyResponse -> assertKeyEquals(keyToBackupAndRestore, keyResponse)).verifyComplete();
            byte[] backup = client.backupKey(keyToBackupAndRestore.getName()).block();

            PollerFlux<DeletedKey, Void> poller = client.beginDeleteKey(keyToBackupAndRestore.getName());
            AsyncPollResponse<DeletedKey, Void> pollResponse = poller
                    .takeUntil(apr -> apr.getStatus() == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED)
                    .blockLast();
            assertNotNull(pollResponse.getValue());

            StepVerifier.create(client.purgeDeletedKeyWithResponse(keyToBackupAndRestore.getName()))
                    .assertNext(voidResponse -> {
                        assertEquals(HttpURLConnection.HTTP_NO_CONTENT, voidResponse.getStatusCode());
                    }).verifyComplete();
            pollOnKeyPurge(keyToBackupAndRestore.getName());

            sleepInRecordMode(60000);

            StepVerifier.create(client.restoreKeyBackup(backup))
                .assertNext(response -> {
                    assertEquals(keyToBackupAndRestore.getName(), response.getName());
                    assertEquals(keyToBackupAndRestore.getNotBefore(), response.getProperties().getNotBefore());
                    assertEquals(keyToBackupAndRestore.getExpiresOn(), response.getProperties().getExpiresOn());
                }).verifyComplete();
        });
    }

    /**
     * Tests that an attempt to restore a key from malformed backup bytes throws an error.
     */
    @Test
    public void restoreKeyFromMalformedBackup() {
        byte[] keyBackupBytes = "non-existing".getBytes();
        StepVerifier.create(client.restoreKeyBackup(keyBackupBytes))
                .verifyErrorSatisfies(ex -> assertRestException(ex, ResourceModifiedException.class, HttpURLConnection.HTTP_BAD_REQUEST));
    }

    /**
     * Tests that a deleted key can be retrieved on a soft-delete enabled vault.
     */
    @Test
    public void getDeletedKey() {
        getDeletedKeyRunner((keyToDeleteAndGet) -> {

            StepVerifier.create(client.createKey(keyToDeleteAndGet))
                .assertNext(keyResponse -> assertKeyEquals(keyToDeleteAndGet, keyResponse)).verifyComplete();

            PollerFlux<DeletedKey, Void> poller = client.beginDeleteKey(keyToDeleteAndGet.getName());
            AsyncPollResponse<DeletedKey, Void> pollResponse = poller
                    .takeUntil(apr -> apr.getStatus() == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED)
                    .blockLast();
            assertNotNull(pollResponse.getValue());

            StepVerifier.create(client.getDeletedKey(keyToDeleteAndGet.getName()))
                .assertNext(deletedKeyResponse -> {
                    assertNotNull(deletedKeyResponse.getDeletedOn());
                    assertNotNull(deletedKeyResponse.getRecoveryId());
                    assertNotNull(deletedKeyResponse.getScheduledPurgeDate());
                    assertEquals(keyToDeleteAndGet.getName(), deletedKeyResponse.getName());
                }).verifyComplete();

            StepVerifier.create(client.purgeDeletedKeyWithResponse(keyToDeleteAndGet.getName()))
                    .assertNext(voidResponse -> {
                        assertEquals(HttpURLConnection.HTTP_NO_CONTENT, voidResponse.getStatusCode());
                    }).verifyComplete();
            pollOnKeyPurge(keyToDeleteAndGet.getName());
            sleepInRecordMode(15000);
        });
    }
//
    /**
     * Tests that deleted keys can be listed in the key vault.
     */
    @Test
    public void listDeletedKeys() {
        listDeletedKeysRunner((keys) -> {

            List<DeletedKey> deletedKeys = new ArrayList<>();
            for (CreateKeyOptions key : keys.values()) {
                StepVerifier.create(client.createKey(key))
                    .assertNext(keyResponse -> assertKeyEquals(key, keyResponse)).verifyComplete();
            }
            sleepInRecordMode(10000);

            for (CreateKeyOptions key : keys.values()) {
                PollerFlux<DeletedKey, Void> poller = client.beginDeleteKey(key.getName());

                AsyncPollResponse<DeletedKey, Void> response = poller.blockLast();
                assertNotNull(response.getValue());
            }

            sleepInRecordMode(60000);
            client.listDeletedKeys().subscribe(deletedKeys::add);
            sleepInRecordMode(30000);

            for (DeletedKey actualKey : deletedKeys) {
                if (keys.containsKey(actualKey.getName())) {
                    assertNotNull(actualKey.getDeletedOn());
                    assertNotNull(actualKey.getRecoveryId());
                    keys.remove(actualKey.getName());
                }
            }

            assertEquals(0, keys.size());

            for (DeletedKey deletedKey : deletedKeys) {
                StepVerifier.create(client.purgeDeletedKeyWithResponse(deletedKey.getName()))
                        .assertNext(voidResponse -> {
                            assertEquals(HttpURLConnection.HTTP_NO_CONTENT, voidResponse.getStatusCode());
                        }).verifyComplete();
                pollOnKeyPurge(deletedKey.getName());
            }
        });
    }

    /**
     * Tests that key versions can be listed in the key vault.
     */
    @Test
    public void listKeyVersions() {
        listKeyVersionsRunner((keys) -> {
            List<KeyProperties> output = new ArrayList<>();
            String keyName = null;
            for (CreateKeyOptions key : keys) {
                keyName = key.getName();
                client.createKey(key).subscribe(keyResponse -> assertKeyEquals(key, keyResponse));
                sleepInRecordMode(1000);
            }
            sleepInRecordMode(30000);
            client.listPropertiesOfKeyVersions(keyName).subscribe(output::add);
            sleepInRecordMode(30000);

            assertEquals(keys.size(), output.size());

            PollerFlux<DeletedKey, Void> poller = client.beginDeleteKey(keyName);
            AsyncPollResponse<DeletedKey, Void> pollResponse = poller.blockLast();
            assertNotNull(pollResponse.getValue());

            StepVerifier.create(client.purgeDeletedKeyWithResponse(keyName))
                    .assertNext(voidResponse -> {
                        assertEquals(HttpURLConnection.HTTP_NO_CONTENT, voidResponse.getStatusCode());
                    }).verifyComplete();
            pollOnKeyPurge(keyName);
        });

    }

    /**
     * Tests that keys can be listed in the key vault.
     */
    @Test
    public void listKeys() {
        listKeysRunner((keys) -> {
            List<KeyProperties> output = new ArrayList<>();
            for (CreateKeyOptions key : keys.values()) {
                client.createKey(key).subscribe(keyResponse -> assertKeyEquals(key, keyResponse));
                sleepInRecordMode(1000);
            }
            sleepInRecordMode(30000);
            client.listPropertiesOfKeys().subscribe(output::add);
            sleepInRecordMode(30000);

            for (KeyProperties actualKey : output) {
                if (keys.containsKey(actualKey.getName())) {
                    CreateKeyOptions expectedKey = keys.get(actualKey.getName());
                    assertEquals(expectedKey.getExpiresOn(), actualKey.getExpiresOn());
                    assertEquals(expectedKey.getNotBefore(), actualKey.getNotBefore());
                    keys.remove(actualKey.getName());
                }
            }
            assertEquals(0, keys.size());
        });
    }

    private void pollOnKeyDeletion(String keyName) {
        int pendingPollCount = 0;
        while (pendingPollCount < 30) {
            DeletedKey deletedKey = null;
            try {
                deletedKey = client.getDeletedKeyWithResponse(keyName).block().getValue();
            } catch (ResourceNotFoundException e) {
            }
            if (deletedKey == null) {
                sleepInRecordMode(2000);
                pendingPollCount += 1;
            } else {
                return;
            }
        }
        System.err.printf("Deleted Key %s not found \n", keyName);
    }

    private void pollOnKeyPurge(String keyName) {
        int pendingPollCount = 0;
        while (pendingPollCount < 10) {
            DeletedKey deletedKey = null;
            try {
                deletedKey = client.getDeletedKeyWithResponse(keyName).block().getValue();
            } catch (ResourceNotFoundException e) {
            }
            if (deletedKey != null) {
                sleepInRecordMode(2000);
                pendingPollCount += 1;
            } else {
                return;
            }
        }
        System.err.printf("Deleted Key %s was not purged \n", keyName);
    }
}

