// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys;

import com.azure.core.exception.ResourceModifiedException;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.security.keyvault.keys.models.DeletedKey;
import com.azure.security.keyvault.keys.models.Key;
import com.azure.security.keyvault.keys.models.KeyProperties;
import com.azure.security.keyvault.keys.models.KeyCreateOptions;
import com.azure.security.keyvault.keys.models.webkey.KeyType;
import org.junit.Assert;
import reactor.test.StepVerifier;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class KeyAsyncClientTest extends KeyClientTestBase {

    private KeyAsyncClient client;

    @Override
    protected void beforeTest() {
        beforeTestSetup();

        if (interceptorManager.isPlaybackMode()) {
            client = clientSetup(pipeline -> new KeyClientBuilder()
                .endpoint(getEndpoint())
                .pipeline(pipeline)
                .buildAsyncClient());
        } else {
            client = clientSetup(pipeline -> new KeyClientBuilder()
                .pipeline(pipeline)
                .endpoint(getEndpoint())
                .buildAsyncClient());
        }
    }

    /**
     * Tests that a key can be created in the key vault.
     */
    public void setKey() {
        setKeyRunner((expected) -> StepVerifier.create(client.createKey(expected))
            .assertNext(response -> assertKeyEquals(expected, response))
            .verifyComplete());
    }

    /**
     * Tests that we cannot create a key when the key is an empty string.
     */
    public void setKeyEmptyName() {
        StepVerifier.create(client.createKey("", KeyType.RSA))
                .verifyErrorSatisfies(ex -> assertRestException(ex, ResourceModifiedException.class, HttpURLConnection.HTTP_BAD_REQUEST));
    }

    /**
     * Tests that we can create keys when value is not null or an empty string.
     */
    public void setKeyNullType() {
        setKeyEmptyValueRunner((key) -> {

            StepVerifier.create(client.createKey(key))
                    .verifyErrorSatisfies(ex -> assertRestException(ex, ResourceModifiedException.class, HttpURLConnection.HTTP_BAD_REQUEST));

        });
    }

    /**
     * Verifies that an exception is thrown when null key object is passed for creation.
     */
    public void setKeyNull() {
        StepVerifier.create(client.createKey(null))
            .verifyError(NullPointerException.class);
    }

    /**
     * Tests that a key is able to be updated when it exists.
     */
    public void updateKey() {
        updateKeyRunner((original, updated) -> {
            StepVerifier.create(client.createKey(original))
                .assertNext(response -> assertKeyEquals(original, response))
                .verifyComplete();
            Key keyToUpdate = client.getKey(original.getName()).block();

            StepVerifier.create(client.updateKeyProperties(keyToUpdate.getProperties().setExpires(updated.getExpires())))
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
    public void updateDisabledKey() {
        updateDisabledKeyRunner((original, updated) -> {
            StepVerifier.create(client.createKey(original))
                .assertNext(response -> assertKeyEquals(original, response))
                .verifyComplete();
            Key keyToUpdate = client.getKey(original.getName()).block();

            StepVerifier.create(client.updateKeyProperties(keyToUpdate.getProperties().setExpires(updated.getExpires())))
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
    public void getKeySpecificVersion() {
        getKeySpecificVersionRunner((key, keyWithNewVal) -> {
            final Key keyVersionOne = client.createKey(key).block();
            final Key keyVersionTwo = client.createKey(keyWithNewVal).block();

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
    public void getKeyNotFound() {
        StepVerifier.create(client.getKey("non-existing"))
                .verifyErrorSatisfies(ex -> assertRestException(ex, ResourceNotFoundException.class, HttpURLConnection.HTTP_NOT_FOUND));
    }


    /**
     * Tests that an existing key can be deleted.
     */
    public void deleteKey() {
        deleteKeyRunner((keyToDelete) -> {
            StepVerifier.create(client.createKey(keyToDelete))
                .assertNext(keyResponse -> assertKeyEquals(keyToDelete, keyResponse)).verifyComplete();

            StepVerifier.create(client.deleteKey(keyToDelete.getName()))
                .assertNext(deletedKeyResponse -> {
                    assertNotNull(deletedKeyResponse.getDeletedDate());
                    assertNotNull(deletedKeyResponse.getRecoveryId());
                    assertNotNull(deletedKeyResponse.getScheduledPurgeDate());
                    assertEquals(keyToDelete.getName(), deletedKeyResponse.getName());
                }).verifyComplete();
            sleepInRecordMode(30000);

            StepVerifier.create(client.purgeDeletedKeyWithResponse(keyToDelete.getName()))
                    .assertNext(voidResponse -> {
                        assertEquals(HttpURLConnection.HTTP_NO_CONTENT, voidResponse.getStatusCode());
                    }).verifyComplete();
            sleepInRecordMode(15000);
        });
    }

    public void deleteKeyNotFound() {
        StepVerifier.create(client.deleteKey("non-existing"))
                .verifyErrorSatisfies(ex -> assertRestException(ex, ResourceNotFoundException.class, HttpURLConnection.HTTP_NOT_FOUND));
    }

    /**
     * Tests that an attempt to retrieve a non existing deleted key throws an error on a soft-delete enabled vault.
     */
    public void getDeletedKeyNotFound() {
        StepVerifier.create(client.getDeletedKey("non-existing"))
                .verifyErrorSatisfies(ex -> assertRestException(ex, ResourceNotFoundException.class, HttpURLConnection.HTTP_NOT_FOUND));
    }

    /**
     * Tests that a deleted key can be recovered on a soft-delete enabled vault.
     */
    public void recoverDeletedKey() {
        recoverDeletedKeyRunner((keyToDeleteAndRecover) -> {
            StepVerifier.create(client.createKey(keyToDeleteAndRecover))
                .assertNext(keyResponse -> assertKeyEquals(keyToDeleteAndRecover, keyResponse)).verifyComplete();

            StepVerifier.create(client.deleteKey(keyToDeleteAndRecover.getName()))
                .assertNext(Assert::assertNotNull).verifyComplete();
            sleepInRecordMode(30000);

            StepVerifier.create(client.recoverDeletedKey(keyToDeleteAndRecover.getName()))
                .assertNext(keyResponse -> {
                    assertEquals(keyToDeleteAndRecover.getName(), keyResponse.getName());
                    assertEquals(keyToDeleteAndRecover.getNotBefore(), keyResponse.getProperties().getNotBefore());
                    assertEquals(keyToDeleteAndRecover.getExpires(), keyResponse.getProperties().getExpires());
                }).verifyComplete();
        });
    }

    /**
     * Tests that an attempt to recover a non existing deleted key throws an error on a soft-delete enabled vault.
     */
    public void recoverDeletedKeyNotFound() {
        StepVerifier.create(client.recoverDeletedKey("non-existing"))
                .verifyErrorSatisfies(ex -> assertRestException(ex, ResourceNotFoundException.class, HttpURLConnection.HTTP_NOT_FOUND));
    }

    /**
     * Tests that a key can be backed up in the key vault.
     */
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
    public void backupKeyNotFound() {
        StepVerifier.create(client.backupKey("non-existing"))
                .verifyErrorSatisfies(ex -> assertRestException(ex, ResourceNotFoundException.class, HttpURLConnection.HTTP_NOT_FOUND));
    }

    /**
     * Tests that a key can be backed up in the key vault.
     */
    public void restoreKey() {
        restoreKeyRunner((keyToBackupAndRestore) -> {
            StepVerifier.create(client.createKey(keyToBackupAndRestore))
                .assertNext(keyResponse -> assertKeyEquals(keyToBackupAndRestore, keyResponse)).verifyComplete();
            byte[] backup = client.backupKey(keyToBackupAndRestore.getName()).block();

            StepVerifier.create(client.deleteKey(keyToBackupAndRestore.getName()))
                .assertNext(Assert::assertNotNull).verifyComplete();
            pollOnKeyDeletion(keyToBackupAndRestore.getName());

            StepVerifier.create(client.purgeDeletedKeyWithResponse(keyToBackupAndRestore.getName()))
                    .assertNext(voidResponse -> {
                        assertEquals(HttpURLConnection.HTTP_NO_CONTENT, voidResponse.getStatusCode());
                    }).verifyComplete();
            pollOnKeyPurge(keyToBackupAndRestore.getName());

            sleepInRecordMode(60000);

            StepVerifier.create(client.restoreKey(backup))
                .assertNext(response -> {
                    assertEquals(keyToBackupAndRestore.getName(), response.getName());
                    assertEquals(keyToBackupAndRestore.getNotBefore(), response.getProperties().getNotBefore());
                    assertEquals(keyToBackupAndRestore.getExpires(), response.getProperties().getExpires());
                }).verifyComplete();
        });
    }

    /**
     * Tests that an attempt to restore a key from malformed backup bytes throws an error.
     */
    public void restoreKeyFromMalformedBackup() {
        byte[] keyBackupBytes = "non-existing".getBytes();
        StepVerifier.create(client.restoreKey(keyBackupBytes))
                .verifyErrorSatisfies(ex -> assertRestException(ex, ResourceModifiedException.class, HttpURLConnection.HTTP_BAD_REQUEST));
    }

    /**
     * Tests that a deleted key can be retrieved on a soft-delete enabled vault.
     */
    public void getDeletedKey() {
        getDeletedKeyRunner((keyToDeleteAndGet) -> {

            StepVerifier.create(client.createKey(keyToDeleteAndGet))
                .assertNext(keyResponse -> assertKeyEquals(keyToDeleteAndGet, keyResponse)).verifyComplete();

            StepVerifier.create(client.deleteKey(keyToDeleteAndGet.getName()))
                .assertNext(Assert::assertNotNull).verifyComplete();
            pollOnKeyDeletion(keyToDeleteAndGet.getName());
            sleepInRecordMode(30000);

            StepVerifier.create(client.getDeletedKey(keyToDeleteAndGet.getName()))
                .assertNext(deletedKeyResponse -> {
                    assertNotNull(deletedKeyResponse.getDeletedDate());
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
    @Override
    public void listDeletedKeys() {
        listDeletedKeysRunner((keys) -> {

            List<DeletedKey> deletedKeys = new ArrayList<>();
            for (KeyCreateOptions key : keys.values()) {
                StepVerifier.create(client.createKey(key))
                    .assertNext(keyResponse -> assertKeyEquals(key, keyResponse)).verifyComplete();
            }
            sleepInRecordMode(10000);

            for (KeyCreateOptions key : keys.values()) {
                StepVerifier.create(client.deleteKey(key.getName()))
                    .assertNext(Assert::assertNotNull).verifyComplete();
                pollOnKeyDeletion(key.getName());
            }

            sleepInRecordMode(60000);
            client.listDeletedKeys().subscribe(deletedKeys::add);
            sleepInRecordMode(30000);

            for (DeletedKey actualKey : deletedKeys) {
                if (keys.containsKey(actualKey.getName())) {
                    assertNotNull(actualKey.getDeletedDate());
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
    @Override
    public void listKeyVersions() {
        listKeyVersionsRunner((keys) -> {
            List<KeyProperties> output = new ArrayList<>();
            String keyName = null;
            for (KeyCreateOptions key : keys) {
                keyName = key.getName();
                client.createKey(key).subscribe(keyResponse -> assertKeyEquals(key, keyResponse));
                sleepInRecordMode(1000);
            }
            sleepInRecordMode(30000);
            client.listKeyVersions(keyName).subscribe(output::add);
            sleepInRecordMode(30000);

            assertEquals(keys.size(), output.size());

            StepVerifier.create(client.deleteKey(keyName))
                .assertNext(Assert::assertNotNull).verifyComplete();
            pollOnKeyDeletion(keyName);


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
    public void listKeys() {
        listKeysRunner((keys) -> {
            List<KeyProperties> output = new ArrayList<>();
            for (KeyCreateOptions key : keys.values()) {
                client.createKey(key).subscribe(keyResponse -> assertKeyEquals(key, keyResponse));
                sleepInRecordMode(1000);
            }
            sleepInRecordMode(30000);
            client.listKeys().subscribe(output::add);
            sleepInRecordMode(30000);

            for (KeyProperties actualKey : output) {
                if (keys.containsKey(actualKey.getName())) {
                    KeyCreateOptions expectedKey = keys.get(actualKey.getName());
                    assertEquals(expectedKey.getExpires(), actualKey.getExpires());
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

